package com.you4me.you4me.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.you4me.you4me.repository.AuthenticationRepository
import com.you4me.you4me.repository.BaseRepository
import com.you4me.you4me.repository.DbRepository
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.repository.ProfileRepository
import com.you4me.you4me.ui.authentication.AuthenticationViewModel
import com.you4me.you4me.ui.main.MainViewModel
import com.you4me.you4me.ui.profile.ProfileViewModel
import java.lang.IllegalArgumentException

class ViewModelFactory(
    private val repository: BaseRepository,
    private val dbRepository: DbRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthenticationViewModel::class.java) -> AuthenticationViewModel(
                repository as AuthenticationRepository,
                dbRepository
            ) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(
                repository as ProfileRepository,
                dbRepository
            ) as T
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(
                repository as MainRepository,
                dbRepository
            ) as T

            else -> throw IllegalArgumentException("ViewModel Class not found")
        }
    }
}