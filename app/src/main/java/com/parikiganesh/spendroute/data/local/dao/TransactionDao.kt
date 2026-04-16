package com.parikiganesh.spendroute.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.parikiganesh.spendroute.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Int)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isIncome = 1 ORDER BY timestamp DESC")
    fun getIncomeTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isIncome = 0 ORDER BY timestamp DESC")
    fun getExpenseTransactions(): Flow<List<TransactionEntity>>

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}

