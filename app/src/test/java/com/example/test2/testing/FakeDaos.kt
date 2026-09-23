package com.example.test2.testing

import com.example.test2.data.local.room.PlafondTierDao
import com.example.test2.data.local.room.PlafondTierEntity

class FakeTierDao : PlafondTierDao {

    private var rows: List<PlafondTierEntity> = emptyList()

    override suspend fun loadAll(): List<PlafondTierEntity> = rows.sortedBy { it.level }

    override suspend fun insertAll(items: List<PlafondTierEntity>) {
        rows = rows + items
    }

    override suspend fun deleteAll() {
        rows = emptyList()
    }
}
