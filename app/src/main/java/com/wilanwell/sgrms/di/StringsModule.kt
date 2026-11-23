package com.wilanwell.sgrms.di

import com.wilanwell.sgrms.utils.AndroidStrings
import com.wilanwell.sgrms.utils.Strings
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class StringsModule {

    @Binds
    abstract fun bindStrings(impl: AndroidStrings): Strings
}
