package com.example.test2.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "quickduit_session")

data class Session(
    val token: String,
    val customerId: String,
    val fullName: String,
    val email: String,
    val refreshToken: String = "",
)

class SessionStore(private val context: Context) : TokenStore {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val CUSTOMER_ID = stringPreferencesKey("customer_id")
        val FULL_NAME = stringPreferencesKey("full_name")
        val EMAIL = stringPreferencesKey("email")
    }

    val session: Flow<Session?> = context.dataStore.data.map { prefs ->
        val token = prefs[Keys.TOKEN]
        val customerId = prefs[Keys.CUSTOMER_ID]

        if (token.isNullOrBlank() || customerId.isNullOrBlank()) {
            null
        } else {
            Session(
                token = token,
                customerId = customerId,
                fullName = prefs[Keys.FULL_NAME].orEmpty(),
                email = prefs[Keys.EMAIL].orEmpty(),
                refreshToken = prefs[Keys.REFRESH_TOKEN].orEmpty(),
            )
        }
    }

    suspend fun save(session: Session) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = session.token
            prefs[Keys.REFRESH_TOKEN] = session.refreshToken
            prefs[Keys.CUSTOMER_ID] = session.customerId
            prefs[Keys.FULL_NAME] = session.fullName
            prefs[Keys.EMAIL] = session.email
        }
    }

    /**
     * Writes the pair a refresh returned, leaving the identity fields alone.
     *
     * Separate from [save] because the authenticator runs on an OkHttp thread
     * with only the two tokens in hand. Rewriting the whole session there would
     * mean reading it back first, and a blank name would quietly overwrite a
     * real one.
     */
    override suspend fun updateTokens(token: String, refreshToken: String?) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            if (!refreshToken.isNullOrBlank()) {
                prefs[Keys.REFRESH_TOKEN] = refreshToken
            }
        }
    }

    override suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun sessionOnce(): Session? = session.first()

    override suspend fun tokenOnce(): String? = context.dataStore.data.first()[Keys.TOKEN]

    override suspend fun refreshTokenOnce(): String? =
        context.dataStore.data.first()[Keys.REFRESH_TOKEN]
}
