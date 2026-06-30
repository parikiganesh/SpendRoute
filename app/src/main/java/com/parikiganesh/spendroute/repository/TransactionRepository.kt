package com.parikiganesh.spendroute.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.parikiganesh.spendroute.data.model.Transaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cloudBackupService: CloudBackupService
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val transactionsState = MutableStateFlow<List<Transaction>>(emptyList())

    init {
        scope.launch {
            cloudBackupService.observeTransactions().collect { transactions ->
                transactionsState.value = transactions
            }
        }
    }

    // Get all transactions
    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionsState.asStateFlow()
    }

    // Insert transaction
    suspend fun insertTransaction(transaction: Transaction) {
        val previous = transactionsState.value
        ensureOnline()
        transactionsState.value = listOf(transaction) + previous.filterNot { it.id == transaction.id }
        try {
            val created = cloudBackupService.createTransactionWithSequentialId(transaction)
            transactionsState.value = listOf(created) +
                transactionsState.value.filterNot { it.id == transaction.id || it.id == created.id }
        } catch (e: Exception) {
            transactionsState.value = previous
            throw e
        }
    }

    // Update transaction
    suspend fun updateTransaction(transaction: Transaction) {
        val previous = transactionsState.value
        ensureOnline()
        transactionsState.value = previous.map { existing ->
            if (existing.id == transaction.id) transaction else existing
        }
        try {
            cloudBackupService.upsertTransaction(transaction)
        } catch (e: Exception) {
            transactionsState.value = previous
            throw e
        }
    }

    // Delete transaction by ID
    suspend fun deleteTransaction(transactionId: String) {
        ensureOnline()
        // Optimistic update for snappier UI; realtime listener will reconcile with cloud.
        transactionsState.value = transactionsState.value.filterNot { it.id == transactionId }
        cloudBackupService.deleteTransactionById(transactionId)
    }

    // Delete all transactions
    suspend fun deleteAllTransactions() {
        ensureOnline()
        cloudBackupService.clearAllCloudTransactions()
        transactionsState.value = emptyList()
    }

    suspend fun getAllTransactionsSnapshot(): List<Transaction> {
        return transactionsState.value
    }

    suspend fun replaceLocalTransactions(transactions: List<Transaction>) {
        transactionsState.value = transactions
    }


    private fun ensureOnline() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        if (!hasInternet) {
            throw IOException("No internet connection")
        }
    }
}

