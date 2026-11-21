package com.example.sgrms.viewmodel

import com.example.sgrms.data.model.UserModel
import org.junit.Assert.assertEquals
import org.junit.Test

class RecentAccountsTest {

    private fun user(id: String, role: String, createdAt: Long) =
        UserModel(userId = id, role = role, createdAt = createdAt)

    @Test
    fun newestFirstAndAdminsExcluded() {
        val users = listOf(
            user("a", "Student", 100),
            user("admin", "Admin", 999),
            user("b", "Teacher", 300),
            user("c", "Student", 200),
            user("d", "Student", 50)
        )

        assertEquals(listOf("b", "c", "a"), recentAccounts(users).map { it.userId })
    }

    @Test
    fun fewerThanLimit() {
        assertEquals(listOf("a"), recentAccounts(listOf(user("a", "Student", 1))).map { it.userId })
        assertEquals(emptyList<UserModel>(), recentAccounts(emptyList()))
    }
}
