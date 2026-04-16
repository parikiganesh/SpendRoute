package com.example.spendroute.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.spendroute.data.local.dao.TransactionDao
import com.example.spendroute.data.local.entity.TransactionEntity

@Database(entities = [TransactionEntity::class], version = 2, exportSchema = false)
abstract class SpendRouteDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: SpendRouteDatabase? = null

        fun getDatabase(context: Context): SpendRouteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpendRouteDatabase::class.java,
                    "spend_route_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

