package com.example.rentr.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 10.0.2.2 is the special alias to your host PC's localhost
    private const val BASE_URL = "https://khalti-delta.vercel.app/"

    val api: KhaltiBackendApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KhaltiBackendApi::class.java)
    }
}