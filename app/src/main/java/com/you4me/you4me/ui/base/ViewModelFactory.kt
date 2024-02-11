package com.you4me.you4me.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.you4me.you4me.repository.AuthenticationRepository
import com.you4me.you4me.repository.BaseRepository
import com.you4me.you4me.ui.authentication.AuthenticationViewModel
import java.lang.IllegalArgumentException

class ViewModelFactory(
    private val repository: BaseRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthenticationViewModel::class.java) -> AuthenticationViewModel(repository as AuthenticationRepository) as T
            else -> throw IllegalArgumentException("ViewModel Class not found")
        }
    }
}