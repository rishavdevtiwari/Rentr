package com.example.rentr.utils



import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.auth.oauth2.GoogleCredentials
import com.example.rentr.R // Import your R file
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream

class FcmSenderV1(
    private val context: Context,
    private val userToken: String,
    private val title: String,
    private val body: String
) {


    private val projectId = "rentr-db9e6"

    suspend fun send() {
        withContext(Dispatchers.IO) {
            try {
                // 1. Get the Access Token from the JSON file
                val token = getAccessToken()

                // 2. Create the JSON Payload
                val notificationData = JSONObject()
                val message = JSONObject()
                val notification = JSONObject()

                notification.put("title", title)
                notification.put("body", body)

                message.put("token", userToken)
                message.put("notification", notification)

                notificationData.put("message", message)

                // 3. Send the Request using Volley
                sendNotificationV1(notificationData, token)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getAccessToken(): String {
        val inputStream: InputStream = context.resources.openRawResource(R.raw.service_account)
        val googleCredentials = GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

        googleCredentials.refreshIfExpired()
        return googleCredentials.accessToken.tokenValue
    }

    private fun sendNotificationV1(jsonBody: JSONObject, accessToken: String) {
        val url = "https://fcm.googleapis.com/v1/projects/$projectId/messages:send"

        val requestQueue = Volley.newRequestQueue(context)

        val request = object : JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                android.util.Log.d("FCM", "✅ Notification Sent: $response")
            },
            { error ->
                android.util.Log.e("FCM", "❌ Error Sending: $error")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $accessToken"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        requestQueue.add(request)
    }
}