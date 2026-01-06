package com.example.rentr.repository

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

// 1. What we send to YOUR Node.js backend
// MUST match: { amount, purchaseOrderId, purchaseOrderName }
data class BackendRequest(
    val amount: Long,
    val purchaseOrderId: String,
    val purchaseOrderName: String
)

// 2. What YOUR Node.js backend returns
// MUST match: { "pidx": "..." }
data class BackendResponse(
    val pidx: String
)

// 3. The Interface
interface KhaltiBackendApi {
    @POST("khalti/initiate") // This matches your app.post("/khalti/initiate")
    suspend fun getPidx(@Body request: BackendRequest): BackendResponse
}