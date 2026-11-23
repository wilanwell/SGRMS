package com.wilanwell.sgrms.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ClassModel(
    val classId: String = "",
    val className: String = "",
    val academicYear: String = "",
    val classTeacherId: String = "",
    val classTeacherName: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Older versions saved this English text as classTeacherName when a class had no
 * teacher. New saves store "" instead; screens show their own localized label.
 */
const val LEGACY_NOT_ASSIGNED = "Not assigned"

/** The class teacher's name, or "" when there is none (including the legacy text). */
val ClassModel.assignedTeacherName: String
    get() = classTeacherName.takeUnless { it == LEGACY_NOT_ASSIGNED }.orEmpty()