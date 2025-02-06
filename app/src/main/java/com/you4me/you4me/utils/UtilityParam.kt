package com.you4me.you4me.utils

object UtilityParam {
    init {
        System.loadLibrary("api-keys")
    }

    private external fun getGooglePlacesSecretKey(): String

    private external fun getMixpanelSecretKey(): String

    val GOOGLE_PLACES_SECRET_KEY = getGooglePlacesSecretKey()

    val MIXPANEL_SECRET_KEY = getMixpanelSecretKey()
}
