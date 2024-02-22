package com.you4me.you4me

import android.app.Application
import com.cloudinary.android.MediaManager

class You4MeApp : Application() {

    override fun onCreate() {
        super.onCreate()
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