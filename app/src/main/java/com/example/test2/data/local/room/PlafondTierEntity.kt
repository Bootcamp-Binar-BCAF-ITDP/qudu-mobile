package com.example.test2.data.local.room

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import com.example.test2.core.LoanTier

@Entity(tableName = "plafond_tiers")
data class PlafondTierEntity(

    @PrimaryKey
    val level: Int,

    val name: String,
    val minAmount: Long,
    val maxAmount: Long,
    val minTenor: Int,
    val maxTenor: Int,
    val annualRate: Double,
    val adminFee: Long,

    val fetchedAt: Long,
)

fun PlafondTierEntity.toTier(): LoanTier = LoanTier(
    level = level,
    name = name,
    minAmount = minAmount,
    maxAmount = maxAmount,
    minTenor = minTenor,
    maxTenor = maxTenor,
    annualRate = annualRate,
    adminFee = adminFee,
)

fun LoanTier.toEntity(fetchedAt: Long): PlafondTierEntity = PlafondTierEntity(
    level = level,
    name = name,
    minAmount = minAmount,
    maxAmount = maxAmount,
    minTenor = minTenor,
    maxTenor = maxTenor,
    annualRate = annualRate,
    adminFee = adminFee,
    fetchedAt = fetchedAt,
)

@Dao
interface PlafondTierDao {

    @Query("SELECT * FROM plafond_tiers ORDER BY level ASC")
    suspend fun loadAll(): List<PlafondTierEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PlafondTierEntity>)

    @Query("DELETE FROM plafond_tiers")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(items: List<PlafondTierEntity>) {
        deleteAll()
        insertAll(items)
    }
}
