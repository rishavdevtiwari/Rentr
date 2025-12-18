package com.example.rentr

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val url = getString(R.string.cloudinary_url)
        if (url.isNotBlank() && url != "null") {
            val config = mapOf(
                "cloud_name" to url.substringAfterLast("@"),
                "api_key" to url.substringAfter("://").substringBefore(":"),
                "api_secret" to url.substringAfter("://").substringBefore("@").substringAfter(":")
            )
            MediaManager.init(this, config)
        }
    }
}
