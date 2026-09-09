package com.example.test2.messaging

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun currentFcmToken(): String = suspendCancellableCoroutine { continuation ->
    FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token -> continuation.resume(token) }
        .addOnFailureListener { error -> continuation.resumeWithException(error) }
}
