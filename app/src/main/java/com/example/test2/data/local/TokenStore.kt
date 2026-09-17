package com.example.test2.data.local

interface TokenStore {

    suspend fun tokenOnce(): String?

    suspend fun refreshTokenOnce(): String?

    suspend fun updateTokens(token: String, refreshToken: String?)

    suspend fun clear()
}
