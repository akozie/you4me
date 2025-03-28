package com.you4me.you4me.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefHelper(context: Context) {
    companion object {
        const val USER_ID = "user_id"
        const val USER_PROFILE = "user_profile"
        const val IS_LOGGED_IN = "is_logged_in"
        const val IS_ONBOARDED = "onboarding_finished"
        const val IS_SUBSCRIBED = "is_subscribed"
        const val IS_FREE_PLAN = "is_free_plan"
        const val APP_TOKEN = "app_token"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("You4me_Preferences", 0)

    fun getString(key: String): String {
        return sharedPreferences.getString(key, "").toString()
    }

    fun saveString(
        key: String,
        value: String,
    ) {
        sharedPreferences.edit().putString(key, value).apply()
    }

//    fun saveUser(key: String, value: User) {
//        sharedPreferences.edit().putString(key, value.toString()).apply()
//    }
//
//    fun getUser(key: String): String {
//        return sharedPreferences.getString(key, "").toString()
//    }

    fun saveInt(
        key: String,
        balance: Int,
    ) {
        sharedPreferences.edit().putInt(key, balance).apply()
    }

    fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    fun saveBoolean(
        key: String,
        value: Boolean,
    ) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun saveFloat(
        key: String,
        balance: Float,
    ) {
        sharedPreferences.edit().putFloat(key, balance).apply()
    }

    fun getFloat(key: String): Float {
        return sharedPreferences.getFloat(key, 0.0F)
    }

    fun delString(key: String) {
        sharedPreferences.edit().apply()
    }

    fun clearTempPreferences() {
        sharedPreferences.edit().clear().apply()
    }
}
