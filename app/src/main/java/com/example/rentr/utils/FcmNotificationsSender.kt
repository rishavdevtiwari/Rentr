package com.example.rentr.utils

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class FcmNotificationsSender(
    val userFcmToken: String,
    val title: String,
    val body: String,
    val context: Context
) {
    // PASTE YOUR KEY HERE
    private val fcmServerKey = "AAAA... (Paste the key you just copied)"
    private val postUrl = "https://fcm.googleapis.com/fcm/send"

    fun send() {
        val requestQueue = Volley.newRequestQueue(context)
        val mainObj = JSONObject()
        try {
            mainObj.put("to", userFcmToken)
            val notiObj = JSONObject()
            notiObj.put("title", title)
            notiObj.put("body", body)

            mainObj.put("notification", notiObj)

            val request = object : JsonObjectRequest(Request.Method.POST, postUrl, mainObj,
                { response -> /* Success */ },
                { error -> /* Error */ }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    headers["Authorization"] = "key=$fcmServerKey"
                    return headers
                }
            }
            requestQueue.add(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}