package com.example.sgrms.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgrms.data.model.GradeModel
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.data.model.TeacherModel
import com.example.sgrms.data.model.UserModel
import com.example.sgrms.data.repository.AdminSummary
import com.example.sgrms.data.repository.DashboardRepository
import com.example.sgrms.data.repository.GradeFirebaseRepository
import com.example.sgrms.data.repository.ProfileImageRepository
import com.example.sgrms.data.repository.StudentFirebaseRepository
import com.example.sgrms.data.repository.StudentProfileUpdate
import com.example.sgrms.data.repository.TeacherAssignments
import com.example.sgrms.data.repository.TeacherFirebaseRepository
import com.example.sgrms.data.repository.TeacherProfileUpdate
import com.example.sgrms.data.repository.UserFirebaseRepository
import com.example.sgrms.data.repository.UserProfileUpdate
import com.example.sgrms.data.session.SessionManager
import com.example.sgrms.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State shared by the view / edit profile screens of every role.
 * [saved] becomes true once a profile save succeeds, so the screen can
 * navigate back; [message] is a one-off confirmation for a snackbar.
 */
data class ProfileUiState<T>(
    val profile: T? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isUploadingImage: Boolean = false,
    val errorMessage: String = "",
    val message: String = "",
    val saved: Boolean = false
)

/** Photo upload / removal and save plumbing shared by the three profiles. */
abstract class BaseProfileViewModel<T>(
    private val profileImageRepository: ProfileImageRepository,
    protected val sessionManager: SessionManager
) : ViewModel() {

    protected val profileState = MutableStateFlow(ProfileUiState<T>())
    val profileUiState: StateFlow<ProfileUiState<T>> = profileState.asStateFlow()

    /** Writes the new photo URL ("" = removed) to the profile document. */
    protected abstract suspend fun writeImageUrl(profile: T, imageUrl: String)

    /** The profile with its photo URL replaced, for the local state. */
    protected abstract fun T.withImageUrl(imageUrl: String): T

    fun onImageSelected(imageUri: Uri) {
        val profile = profileState.value.profile ?: return
        val userId = sessionManager.currentUserId

        runImageChange("Profile picture updated successfully", "Failed to save profile picture.") {
            val url = profileImageRepository.uploadProfileImage(userId = userId, imageUri = imageUri)
            writeImageUrl(profile, url)
            url
        }
    }

    fun removeImage() {
        val profile = profileState.value.profile ?: return
        val userId = sessionManager.currentUserId

        runImageChange("Profile picture removed successfully", "Failed to remove profile picture.") {
            writeImageUrl(profile, "")
            profileImageRepository.deleteProfileImage(userId)
            ""
        }
    }

    fun clearMessage() {
        profileState.update { it.copy(message = "", errorMessage = "") }
    }

    protected fun load(fallbackError: String, block: suspend () -> T?) {
        viewModelScope.launch {
            profileState.update { it.copy(isLoading = true, errorMessage = "") }

            try {
                val profile = block()
                profileState.update {
                    it.copy(
                        profile = profile,
                        isLoading = false,
                        errorMessage = if (profile == null) "Profile not found." else ""
                    )
                }
            } catch (e: Exception) {
                profileState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage(fallbackError)) }
            }
        }
    }

    protected fun save(transform: T.() -> T, write: suspend (T) -> Unit) {
        val profile = profileState.value.profile ?: return

        viewModelScope.launch {
            profileState.update { it.copy(isSaving = true, errorMessage = "") }

            try {
                write(profile)
                sessionManager.refresh()
                profileState.update {
                    it.copy(
                        profile = profile.transform(),
                        isSaving = false,
                        saved = true,
                        message = "Profile updated successfully"
                    )
                }
            } catch (e: Exception) {
                profileState.update { it.copy(isSaving = false, errorMessage = e.toUserMessage("Failed to update profile.")) }
            }
        }
    }

    private fun runImageChange(
        successMessage: String,
        fallbackError: String,
        block: suspend () -> String
    ) {
        viewModelScope.launch {
            profileState.update { it.copy(isUploadingImage = true, errorMessage = "") }

            try {
                val url = block()
                // Keep the session user (dashboard header avatar) in step with the new photo.
                runCatching { sessionManager.refresh() }
                profileState.update {
                    it.copy(
                        profile = it.profile?.withImageUrl(url),
                        isUploadingImage = false,
                        message = successMessage
                    )
                }
            } catch (e: Exception) {
                profileState.update { it.copy(isUploadingImage = false, errorMessage = e.toUserMessage(fallbackError)) }
            }
        }
    }
}

// ---------------------------------------------------------------- Admin

@HiltViewModel
class AdminProfileViewModel @Inject constructor(
    private val userRepository: UserFirebaseRepository,
    private val dashboardRepository: DashboardRepository,
    profileImageRepository: ProfileImageRepository,
    sessionManager: SessionManager
) : BaseProfileViewModel<UserModel>(profileImageRepository, sessionManager) {

    private val _summary = MutableStateFlow(AdminSummary())
    val summary: StateFlow<AdminSummary> = _summary.asStateFlow()

    init {
        refresh()
    }

    /** Loads the profile; also called when coming back from the edit screen. */
    fun refresh() {
        load("Failed to load admin profile.") {
            userRepository.getUser(sessionManager.currentUserId)
        }
        viewModelScope.launch {
            runCatching { dashboardRepository.getAdminSummary() }.onSuccess { _summary.value = it }
        }
    }

    fun saveProfile(update: UserProfileUpdate) = save(
        transform = {
            copy(
                fullName = update.fullName,
                username = update.username,
                phoneNumber = update.phoneNumber,
                address = update.address,
                profileNote = update.profileNote
            )
        },
        write = { userRepository.updateOwnProfile(it.userId, update) }
    )

    override suspend fun writeImageUrl(profile: UserModel, imageUrl: String) =
        userRepository.updateProfileImage(profile.userId, imageUrl)

    override fun UserModel.withImageUrl(imageUrl: String) =
        copy(profileImageUrl = imageUrl, profileImageBase64 = "")
}

// ---------------------------------------------------------------- Teacher

@HiltViewModel
class TeacherProfileViewModel @Inject constructor(
    private val teacherRepository: TeacherFirebaseRepository,
    private val userRepository: UserFirebaseRepository,
    private val dashboardRepository: DashboardRepository,
    profileImageRepository: ProfileImageRepository,
    sessionManager: SessionManager
) : BaseProfileViewModel<TeacherModel>(profileImageRepository, sessionManager) {

    private val _assignments = MutableStateFlow(TeacherAssignments())
    val assignments: StateFlow<TeacherAssignments> = _assignments.asStateFlow()

    init {
        refresh()
    }

    /** Loads the profile; also called when coming back from the edit screen. */
    fun refresh() {
        load("Failed to load teacher profile.") {
            val teacher = sessionManager.currentTeacherId
                .takeIf { it.isNotBlank() }
                ?.let { teacherRepository.getTeacherById(it) }
                ?: teacherRepository.getTeacherByUserId(sessionManager.currentUserId)
                ?: throw IllegalStateException("The teacher account is not linked to a teacher profile.")

            viewModelScope.launch {
                runCatching { dashboardRepository.getTeacherAssignments(teacher) }
                    .onSuccess { _assignments.value = it }
            }

            teacher
        }
    }

    fun saveProfile(update: TeacherProfileUpdate) = save(
        transform = {
            copy(
                phoneNumber = update.phoneNumber,
                department = update.department,
                position = update.position,
                officeLocation = update.officeLocation,
                qualification = update.qualification,
                specialization = update.specialization,
                address = update.address,
                profileNote = update.profileNote
            )
        },
        write = { teacherRepository.updateOwnProfile(it.teacherId, update) }
    )

    override suspend fun writeImageUrl(profile: TeacherModel, imageUrl: String) {
        teacherRepository.updateProfileImage(profile.teacherId, imageUrl)
        // users/{uid} carries the photo too: User management and the session read it.
        userRepository.updateProfileImage(sessionManager.currentUserId, imageUrl)
    }

    override fun TeacherModel.withImageUrl(imageUrl: String) =
        copy(profileImageUrl = imageUrl, profileImageBase64 = "")
}

// ---------------------------------------------------------------- Student

data class StudentProfileExtras(
    val account: UserModel? = null,
    val grades: List<GradeModel> = emptyList()
)

@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    private val studentRepository: StudentFirebaseRepository,
    private val userRepository: UserFirebaseRepository,
    private val gradeRepository: GradeFirebaseRepository,
    profileImageRepository: ProfileImageRepository,
    sessionManager: SessionManager
) : BaseProfileViewModel<StudentModel>(profileImageRepository, sessionManager) {

    private val _extras = MutableStateFlow(StudentProfileExtras(account = sessionManager.currentUser))
    val extras: StateFlow<StudentProfileExtras> = _extras.asStateFlow()

    init {
        refresh()
    }

    /** Loads the profile; also called when coming back from the edit screen. */
    fun refresh() {
        load("Failed to load student profile.") {
            val studentId = sessionManager.currentStudentId
            if (studentId.isBlank()) {
                throw IllegalStateException("The student account is not linked to a student profile.")
            }

            viewModelScope.launch {
                runCatching { gradeRepository.getGradesByStudentId(studentId) }
                    .onSuccess { grades -> _extras.update { it.copy(grades = grades) } }
            }

            studentRepository.getStudentById(studentId)
        }
    }

    fun saveProfile(update: StudentProfileUpdate) = save(
        transform = {
            copy(
                phoneNumber = update.phoneNumber,
                dateOfBirth = update.dateOfBirth,
                address = update.address,
                guardianName = update.guardianName,
                guardianPhone = update.guardianPhone,
                emergencyContact = update.emergencyContact,
                profileNote = update.profileNote
            )
        },
        write = { studentRepository.updateOwnProfile(it.studentId, update) }
    )

    override suspend fun writeImageUrl(profile: StudentModel, imageUrl: String) {
        studentRepository.updateProfileImage(profile.studentId, imageUrl)
        // users/{uid} carries the photo too: User management and the session read it.
        userRepository.updateProfileImage(sessionManager.currentUserId, imageUrl)
    }

    override fun StudentModel.withImageUrl(imageUrl: String) =
        copy(profileImageUrl = imageUrl, profileImageBase64 = "")
}
