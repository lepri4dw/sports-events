package com.example.sportevents.data.network

import android.util.Log
import com.example.sportevents.util.AuthManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val BASE_URL = "http://192.168.43.53:8000/api/"
    private const val TAG = "RetrofitClient"

    // Создаем настроенный Gson для правильной обработки типов
    private val gson by lazy {
        GsonConfig.createGson()
    }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val authInterceptor by lazy {
        AuthInterceptor()
    }

    private val okHttpClient by lazy {
        try {
            Log.d(TAG, "Creating OkHttpClient - AuthManager initialized: ${AuthManager.isInitialized()}")
            val builder = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                
            Log.d(TAG, "Using base URL: $BASE_URL")
            builder.build()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating OkHttpClient: ${e.message}", e)
            // Fallback client without auth interceptor
            OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }
    }

    val apiService: ApiService by lazy {
        Log.d(TAG, "Creating ApiService with URL: $BASE_URL")
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}