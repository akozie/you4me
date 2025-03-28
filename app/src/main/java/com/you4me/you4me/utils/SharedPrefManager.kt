package com.you4me.you4me.utils

import android.content.Context

object SharedPrefManager {
    private lateinit var sharedPrefHelper: SharedPrefHelper

    fun init(context: Context) {
        sharedPrefHelper = SharedPrefHelper(context.applicationContext)
    }

    fun getInstance(): SharedPrefHelper {
        if (!::sharedPrefHelper.isInitialized) {
            throw IllegalStateException("SharedPrefManager is not initialized. Call init(context) in Application class.")
        }
        return sharedPrefHelper
    }
}
