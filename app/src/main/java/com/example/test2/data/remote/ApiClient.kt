package com.example.test2.data.remote

import android.content.Context
import com.example.test2.BuildConfig
import com.example.test2.data.local.SessionStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    /**
     * A second, deliberately bare client for the refresh call alone.
     *
     * It carries no auth interceptor and no authenticator. If the refresh went
     * through the main client, a 401 from refresh would trigger another
     * refresh, and so on.
     */
    private fun refreshApi(): RefreshApi =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RefreshApi::class.java)

    fun create(sessionStore: SessionStore, context: Context): ApiService {

        val authInterceptor = Interceptor { chain ->
            val token = runBlocking { sessionStore.tokenOnce() }
            val request = if (token.isNullOrBlank()) {
                chain.request()
            } else {
                // header(), not addHeader(). A request replayed by the
                // authenticator already carries an Authorization header, and
                // addHeader would append a second one rather than replace it.
                chain.request().newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            }
            chain.proceed(request)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .authenticator(TokenAuthenticator(sessionStore, refreshApi()))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)

        networkInspectors(context).forEach(builder::addInterceptor)

        val client = builder.build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
