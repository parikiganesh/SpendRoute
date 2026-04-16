package com.parikiganesh.spendroute.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val category: String,
    val amount: Double,
    val date: String,
    val time: String,
    val isIncome: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null  // Optional note field for transactions
)

