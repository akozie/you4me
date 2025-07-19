package com.you4me.you4me.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleHelper {

    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
    fun applySavedLocale(context: Context): Context {
        val sharedPref = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val lang = sharedPref.getString("AppLanguage", Locale.getDefault().language) ?: "en"
        return setLocale(context, lang)
    }

     fun saveLanguageToPreferences(context: Context, languageCode: String) {
        val sharedPref = context.getSharedPreferences("AppSettings", MODE_PRIVATE)
        sharedPref.edit().putString("AppLanguage", languageCode).apply()
    }

     fun getLanguageCode(language: String): String {
        // Simple map; expand as needed
        return when (language) {
            "English" -> "en"
            "Français" -> "fr"
            "Deutsch" -> "de"
            "Español" -> "es"
            "Português" -> "pt"
            "हिन्दी" -> "hi"
            "中文 (简体)" -> "zh"
            else -> "en" // fallback
        }
    }

}
