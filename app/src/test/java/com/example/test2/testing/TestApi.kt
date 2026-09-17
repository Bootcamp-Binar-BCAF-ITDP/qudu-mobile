package com.example.test2.testing

import com.example.test2.data.remote.ApiService
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun MockWebServer.apiService(): ApiService =
    Retrofit.Builder()
        .baseUrl(url("/"))
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

fun MockWebServer.respond(code: Int, body: String) {
    enqueue(
        MockResponse()
            .setResponseCode(code)
            .setHeader("Content-Type", "application/json")
            .setBody(body)
    )
}

fun waitUntil(timeoutMillis: Long = 5_000, condition: () -> Boolean) {
    val deadline = System.currentTimeMillis() + timeoutMillis
    while (!condition()) {
        check(System.currentTimeMillis() < deadline) { "Condition not met within $timeoutMillis ms" }
        Thread.sleep(10)
    }
}
