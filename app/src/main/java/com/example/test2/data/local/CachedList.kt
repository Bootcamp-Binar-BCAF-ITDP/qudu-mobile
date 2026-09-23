package com.example.test2.data.local

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
