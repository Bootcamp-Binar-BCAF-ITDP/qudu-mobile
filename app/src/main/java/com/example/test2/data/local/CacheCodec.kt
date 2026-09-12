package com.example.test2.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

internal class CacheCodec(private val gson: Gson = Gson()) {

    fun <T> encode(items: List<T>, elementType: Type, fetchedAt: Long): String {
        val entries = items.map { CachedEntry(it, fetchedAt) }
        return gson.toJson(entries, listTypeOf(elementType))
    }

    fun <T> decode(json: String?, elementType: Type): CachedList<T> {
        if (json.isNullOrBlank()) return CachedList.empty()

        return try {
            val entries: List<CachedEntry<T>> =
                gson.fromJson(json, listTypeOf(elementType)) ?: return CachedList.empty()

            CachedList(
                items = entries.map { it.value },
                fetchedAt = entries.minOfOrNull { it.fetchedAt },
            )
        } catch (e: Exception) {
            CachedList.empty()
        }
    }

    private fun listTypeOf(elementType: Type): Type =
        TypeToken.getParameterized(
            List::class.java,
            TypeToken.getParameterized(CachedEntry::class.java, elementType).type,
        ).type
}
