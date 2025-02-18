package com.you4me.you4me

import android.app.Application
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp
import com.you4me.you4me.utils.MixpanelManager

class You4MeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        configureCloudinary()
        MixpanelManager.getInstance(this) // Ensures Mixpanel is initialized
    }

    private fun configureCloudinary() {
        val config = HashMap<String, Any>()
        config.put("cloud_name", "mmuodev")
        config.put("api_key", "242463877447421")
        config.put("api_secret", "WSmcb4hg_yho18HyXrMgz9TAr9E")
        config.put("secure", true)
        MediaManager.init(this, config)
    }
}
