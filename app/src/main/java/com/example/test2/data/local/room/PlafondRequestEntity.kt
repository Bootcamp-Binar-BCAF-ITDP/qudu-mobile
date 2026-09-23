package com.example.test2.data.local.room

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import com.example.test2.data.dto.PlafondDto
import com.example.test2.data.dto.PlafondRequestDto
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Entity(tableName = "plafond_requests")
data class PlafondRequestEntity(

    @PrimaryKey
    val requestId: String,

    @ColumnInfo(index = true)
    val customerId: String?,

    val customerName: String?,
    val previousLevel: Int?,
    val requestedLevel: Int?,
    val requestedAmount: BigDecimal?,
    val approvedAmount: BigDecimal?,
    val status: String?,
    val requestDate: String?,
    val decisionDate: String?,
    val reviewedBy: String?,
    val notes: String?,

    val requestedPlafond: PlafondDto?,

    val fetchedAt: Long,
)

fun PlafondRequestEntity.toDto(): PlafondRequestDto = PlafondRequestDto(
    requestId = requestId,
    customerId = customerId,
    customerName = customerName,
    previousLevel = previousLevel,
    requestedLevel = requestedLevel,
    requestedAmount = requestedAmount,
    approvedAmount = approvedAmount,
    status = status,
    requestDate = requestDate,
    decisionDate = decisionDate,
    reviewedBy = reviewedBy,
    notes = notes,
    requestedPlafond = requestedPlafond,
)

fun PlafondRequestDto.toEntity(fetchedAt: Long): PlafondRequestEntity? {
    val id = requestId ?: return null

    return PlafondRequestEntity(
        requestId = id,
        customerId = customerId,
        customerName = customerName,
        previousLevel = previousLevel,
        requestedLevel = requestedLevel,
        requestedAmount = requestedAmount,
        approvedAmount = approvedAmount,
        status = status,
        requestDate = requestDate,
        decisionDate = decisionDate,
        reviewedBy = reviewedBy,
        notes = notes,
        requestedPlafond = requestedPlafond,
        fetchedAt = fetchedAt,
    )
}

@Dao
interface PlafondRequestDao {

    @Query("SELECT * FROM plafond_requests ORDER BY requestDate DESC")
    fun observeAll(): Flow<List<PlafondRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PlafondRequestEntity>)

    @Query("DELETE FROM plafond_requests")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(items: List<PlafondRequestEntity>) {
        deleteAll()
        insertAll(items)
    }
}
