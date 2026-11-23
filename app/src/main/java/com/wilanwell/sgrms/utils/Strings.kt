package com.wilanwell.sgrms.utils

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads text from strings.xml for code that has no Compose context
 * (ViewModels, the notification worker). An interface, so tests can pass a fake.
 */
interface Strings {
    fun get(@StringRes id: Int, vararg args: Any): String
}

@Singleton
class AndroidStrings @Inject constructor(
    @param:ApplicationContext private val context: Context
) : Strings {
    override fun get(@StringRes id: Int, vararg args: Any): String = context.getString(id, *args)
}

/**
 * An error whose message is meant for the user. It carries a string resource,
 * so the data layer can report problems without needing a Context.
 */
class UserMessageException(@StringRes val messageRes: Int) : IllegalStateException()
