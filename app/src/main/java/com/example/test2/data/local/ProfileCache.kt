package com.example.test2.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.test2.data.dto.CustomerProfileDto
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.profileCacheStore by preferencesDataStore(name = "quickduit_profile_cache")

class ProfileCache(
    context: Context,
    gson: Gson = Gson(),
) {

    private val codec = CacheCodec(gson)
    private val store = context.applicationContext.profileCacheStore

    private val key = stringPreferencesKey("profile")

    val profile: Flow<CachedList<CustomerProfileDto>> = store.data
        .catch { cause -> if (cause is IOException) emit(emptyPreferences()) else throw cause }
        .map { prefs -> codec.decode(prefs[key], CustomerProfileDto::class.java) }

    suspend fun replace(profile: CustomerProfileDto) {
        val json = codec.encode(
            items = listOf(stripLimits(profile)),
            elementType = CustomerProfileDto::class.java,
            fetchedAt = System.currentTimeMillis(),
        )
        store.edit { prefs -> prefs[key] = json }
    }

    suspend fun clear() {
        store.edit { it.clear() }
    }

    private fun stripLimits(profile: CustomerProfileDto): CustomerProfileDto = profile.copy(
        approvedLimit = null,
        usedLimit = null,
        availableLimit = null,
        plafond = null,
    )
}
