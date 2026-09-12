package com.example.test2.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.test2.data.dto.LoanApplicationDto
import com.example.test2.data.dto.PlafondRequestDto
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.lang.reflect.Type

private val Context.loanCacheStore by preferencesDataStore(name = "quickduit_loan_cache")

data class CachedEntry<T>(
    val value: T,
    val fetchedAt: Long,
)

data class CachedList<T>(
    val items: List<T>,
    val fetchedAt: Long?,
) {
    val isEmpty: Boolean get() = items.isEmpty()
    val everCached: Boolean get() = fetchedAt != null

    companion object {
        fun <T> empty(): CachedList<T> = CachedList(emptyList(), null)
    }
}

class LoanCache(
    context: Context,
    gson: Gson = Gson(),
) {

    private val codec = CacheCodec(gson)
    private val store = context.applicationContext.loanCacheStore

    private object Keys {
        val APPLICATIONS = stringPreferencesKey("applications")
        val UPGRADE_REQUESTS = stringPreferencesKey("upgrade_requests")
    }

    val applications: Flow<CachedList<LoanApplicationDto>> =
        read(Keys.APPLICATIONS, LoanApplicationDto::class.java)

    val upgradeRequests: Flow<CachedList<PlafondRequestDto>> =
        read(Keys.UPGRADE_REQUESTS, PlafondRequestDto::class.java)

    suspend fun replaceApplications(items: List<LoanApplicationDto>) =
        write(Keys.APPLICATIONS, items, LoanApplicationDto::class.java)

    suspend fun replaceUpgradeRequests(items: List<PlafondRequestDto>) =
        write(Keys.UPGRADE_REQUESTS, items, PlafondRequestDto::class.java)

    suspend fun clear() {
        store.edit { it.clear() }
    }

    private fun <T> read(
        key: Preferences.Key<String>,
        elementType: Type,
    ): Flow<CachedList<T>> = store.data
        .catch { cause -> if (cause is IOException) emit(emptyPreferences()) else throw cause }
        .map { prefs -> codec.decode(prefs[key], elementType) }

    private suspend fun <T> write(
        key: Preferences.Key<String>,
        items: List<T>,
        elementType: Type,
    ) {
        val json = codec.encode(items, elementType, System.currentTimeMillis())
        store.edit { prefs -> prefs[key] = json }
    }
}
