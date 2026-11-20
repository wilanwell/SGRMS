package com.example.sgrms.data.model

/**
 * Account roles. [value] is the exact string stored in users/{uid}.role and
 * checked by firestore.rules and the Cloud Functions, so never rename it.
 */
enum class UserRole(val value: String) {
    ADMIN("Admin"),
    TEACHER("Teacher"),
    STUDENT("Student");

    companion object {
        fun fromValue(value: String?): UserRole? {
            return entries.firstOrNull { it.value.equals(value?.trim(), ignoreCase = true) }
        }
    }
}

/** Typed role of this user, or null when the stored value is unknown. */
val UserModel.userRole: UserRole?
    get() = UserRole.fromValue(role)
