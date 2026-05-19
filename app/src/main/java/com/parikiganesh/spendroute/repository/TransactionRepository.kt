package com.parikiganesh.spendroute.repository

import com.parikiganesh.spendroute.data.local.dao.TransactionDao
import com.parikiganesh.spendroute.data.local.entity.TransactionEntity
import com.parikiganesh.spendroute.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepository @Inject constructor(private val transactionDao: TransactionDao) {

    // Get all transactions
    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    // Get income transactions only
    fun getIncomeTransactions(): Flow<List<Transaction>> {
        return transactionDao.getIncomeTransactions().map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    // Get expense transactions only
    fun getExpenseTransactions(): Flow<List<Transaction>> {
        return transactionDao.getExpenseTransactions().map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    // Insert transaction
    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    // Update transaction
    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    // Delete transaction by ID
    suspend fun deleteTransaction(transactionId: String) {
        transactionDao.deleteTransactionById(transactionId.toIntOrNull() ?: return)
    }

    // Delete transaction object
    suspend fun deleteTransactionObject(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
    }

    // Delete all transactions
    suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }

    // Helper: Entity to Model
    private fun TransactionEntity.toTransaction(): Transaction {
        return Transaction(
            id = this.id.toString(),
            title = this.title,
            category = this.category,
            amount = this.amount,
            date = this.date,
            time = this.time,
            isIncome = this.isIncome,
            note = this.note
        )
    }

    // Helper: Model to Entity
    private fun Transaction.toEntity(): TransactionEntity {
        return TransactionEntity(
            id = this.id.toIntOrNull() ?: 0,
            title = this.title,
            category = this.category,
            amount = this.amount,
            date = this.date,
            time = this.time,
            isIncome = this.isIncome,
            note = this.note
        )
    }
}

