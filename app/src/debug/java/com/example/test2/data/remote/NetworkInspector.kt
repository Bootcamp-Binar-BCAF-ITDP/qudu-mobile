package com.example.test2.data.remote

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import okhttp3.Interceptor

fun networkInspectors(context: Context): List<Interceptor> = listOf(
    ChuckerInterceptor.Builder(context)
        .collector(
            ChuckerCollector(
                context = context,
                showNotification = true,
                retentionPeriod = RetentionManager.Period.ONE_HOUR,
            )
        )
        .redactHeaders("Authorization")
        .maxContentLength(250_000L)
        .alwaysReadResponseBody(true)
        .build()
)
