package com.you4me.you4me.utils

object UtilityParam {
    init {
        System.loadLibrary("api-keys")
    }

    private external fun getGooglePlacesSecretKey(): String

    private external fun getMixpanelSecretKey(): String
    private external fun getApiKey(): String
    private external fun getBaseUrl(): String

    val GOOGLE_PLACES_SECRET_KEY = getGooglePlacesSecretKey()

    val MIXPANEL_SECRET_KEY = getMixpanelSecretKey()
    val API_KEY = getApiKey()
    val BASE_URL = getBaseUrl()
}
