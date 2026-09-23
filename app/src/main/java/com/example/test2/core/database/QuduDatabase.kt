package com.example.test2.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.test2.data.local.room.CustomerProfileDao
import com.example.test2.data.local.room.CustomerProfileEntity
import com.example.test2.data.local.room.LoanApplicationDao
import com.example.test2.data.local.room.LoanApplicationEntity
import com.example.test2.data.local.room.PlafondRequestDao
import com.example.test2.data.local.room.PlafondRequestEntity
import com.example.test2.data.local.room.PlafondTierDao
import com.example.test2.data.local.room.PlafondTierEntity
import com.example.test2.data.local.room.RoomConverters

@Database(
    entities = [
        LoanApplicationEntity::class,
        PlafondRequestEntity::class,
        CustomerProfileEntity::class,
        PlafondTierEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
abstract class QuduDatabase : RoomDatabase() {

    abstract fun loanApplicationDao(): LoanApplicationDao

    abstract fun plafondRequestDao(): PlafondRequestDao

    abstract fun customerProfileDao(): CustomerProfileDao

    abstract fun plafondTierDao(): PlafondTierDao

    companion object {
        const val NAME = "qudu.db"
    }
}
