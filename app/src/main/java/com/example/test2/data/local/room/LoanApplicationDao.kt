package com.example.test2.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanApplicationDao {

    @Query("SELECT * FROM loan_applications ORDER BY submissionDate DESC")
    fun observeAll(): Flow<List<LoanApplicationEntity>>

    @Query("SELECT * FROM loan_applications WHERE customerId = :customerId ORDER BY submissionDate DESC")
    fun observeByCustomer(customerId: String): Flow<List<LoanApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<LoanApplicationEntity>)

    @Query("DELETE FROM loan_applications")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(items: List<LoanApplicationEntity>) {
        deleteAll()
        insertAll(items)
    }
}
