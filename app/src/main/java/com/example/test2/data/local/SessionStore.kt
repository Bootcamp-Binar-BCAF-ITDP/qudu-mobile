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
)

class SessionStore(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
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
            )
        }
    }

    suspend fun save(session: Session) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = session.token
            prefs[Keys.CUSTOMER_ID] = session.customerId
            prefs[Keys.FULL_NAME] = session.fullName
            prefs[Keys.EMAIL] = session.email
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun sessionOnce(): Session? = session.first()

    suspend fun tokenOnce(): String? = context.dataStore.data.first()[Keys.TOKEN]
}
