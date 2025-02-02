package com.you4me.you4me

import android.app.Application
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp

class You4MeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        configureCloudinary()
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
