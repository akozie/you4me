package com.you4me.you4me.utils

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.you4me.you4me.utils.UtilityParam.MIXPANEL_SECRET_KEY
import org.json.JSONObject

class MixpanelManager private constructor(context: Context) {
     val mixpanel: MixpanelAPI = MixpanelAPI.getInstance(context, MIXPANEL_SECRET_KEY)

    companion object {
        @Volatile private var instance: MixpanelManager? = null

        fun getInstance(context: Context): MixpanelManager {
            return instance ?: synchronized(this) {
                instance ?: MixpanelManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // General Event Tracking
    fun track(event: String, properties: Map<String, Any> = emptyMap()) {
        val jsonProps = JSONObject(properties)
        mixpanel.track(event, jsonProps)
    }

    // Identify Users
    fun identifyUser(userId: String) {
        mixpanel.identify(userId)
        mixpanel.people.identify(userId)
    }

    // Set User Properties (People)
    fun setUserProperties(userId: String, email: String, name: String) {
        identifyUser(userId)
        mixpanel.people.set(
            JSONObject(
                mapOf(
                    "\$name" to name,
                    "\$email" to email,
                    "user_id" to userId
                )
            )
        )
    }

    // Track Login
    fun trackLogin(userId: String) {
        identifyUser(userId)
    }

    // Track Signup
    fun trackSignup(userId: String, email: String, name: String) {
        setUserProperties(userId, email, name)
        track("User Signed Up", mapOf("user_id" to userId, "email" to email))
    }
}