package com.you4me.you4me.utils

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject

class MixpanelHelper(context: Context) {
    companion object {
        private const val MIXPANEL_TOKEN = "d1800cfe7956f69cf896a35dd8653f17"
    }

    private val mixpanel: MixpanelAPI = MixpanelAPI.getInstance(context, MIXPANEL_TOKEN)

    // Function to track events
    fun trackEvent(
        eventName: String,
        properties: JSONObject? = null,
    ) {
        mixpanel.track(eventName, properties)
    }

    // Function to identify and set user properties
    fun identifyUser(
        userId: String,
        properties: JSONObject,
    ) {
        mixpanel.identify(userId)
        mixpanel.people.identify(userId)

        properties.keys().forEach {
            mixpanel.people.set(it, properties.get(it))
        }
    }

    // Function to register super properties (applies to all events)
    fun registerSuperProperties(properties: JSONObject) {
        mixpanel.registerSuperProperties(properties)
    }

    // Function to flush events (send data immediately)
    fun flush() {
        mixpanel.flush()
    }

    // Function to opt-out tracking
    fun optOut() {
        mixpanel.optOutTracking()
    }

    // Function to opt-in tracking
    fun optIn() {
        mixpanel.optInTracking()
    }
}
