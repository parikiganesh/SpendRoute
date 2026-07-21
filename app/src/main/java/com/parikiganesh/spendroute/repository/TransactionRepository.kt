package com.parikiganesh.spendroute.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import android.util.Log
import com.parikiganesh.spendroute.data.model.Transaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
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
        ensureOnline()
        val transactionWithReceipt = resolveReceiptUrlIfNeeded(transaction)
        val previous = transactionsState.value
        val optimistic = listOf(transactionWithReceipt) + previous.filterNot { it.id == transaction.id }
        transactionsState.value = optimistic

        try {
            val created = cloudBackupService.createTransactionWithSequentialId(transactionWithReceipt)
            val current = transactionsState.value
            transactionsState.value = listOf(created) +
                current.filterNot { it.id == transaction.id || it.id == created.id }
        } catch (e: Exception) {
            transactionsState.value = previous
            throw e
        }
    }

    // Update transaction
    suspend fun updateTransaction(transaction: Transaction) {
        val previous = transactionsState.value
        ensureOnline()
        val transactionWithReceipt = resolveReceiptUrlIfNeeded(transaction)
        transactionsState.value = previous.map { existing ->
            if (existing.id == transaction.id) transactionWithReceipt else existing
        }
        try {
            cloudBackupService.upsertTransaction(transactionWithReceipt)
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

    private suspend fun resolveReceiptUrlIfNeeded(transaction: Transaction): Transaction {
        val tag = "TransactionRepository"
        val rawReceipt = transaction.receiptImageUrl?.trim().orEmpty()
        if (rawReceipt.isEmpty()) {
            return transaction.copy(receiptImageUrl = null)
        }
        if (
            rawReceipt.startsWith("https://") ||
            rawReceipt.startsWith("http://") ||
            rawReceipt.startsWith("gs://") ||
            rawReceipt.startsWith("data:image")
        ) {
            return transaction
        }

        return try {
            Log.d(tag, "Attempting to upload receipt for transaction ${transaction.id}")
            val uri = Uri.parse(rawReceipt)
            val dataUri = encodeReceiptAsDataUri(uri)
                ?: return transaction.copy(receiptImageUrl = null)
            Log.d(tag, "Receipt encoded for Firestore (${dataUri.length} chars)")
            transaction.copy(receiptImageUrl = dataUri)
        } catch (e: Exception) {
            Log.e(tag, "Receipt upload failed: ${e.message}", e)
            transaction.copy(receiptImageUrl = null)
        }
    }

    private fun encodeReceiptAsDataUri(uri: Uri): String? {
        val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size) ?: return null

        val output = ByteArrayOutputStream()
        var quality = 85
        do {
            output.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
            quality -= 10
        } while (output.size() > MAX_RECEIPT_BYTES && quality >= 25)

        if (output.size() > MAX_RECEIPT_BYTES) {
            throw IOException("Receipt image is too large for Firestore. Please choose a smaller image.")
        }

        val base64 = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        return "data:image/jpeg;base64,$base64"
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

    private companion object {
        // Keep binary bytes small so encoded value safely fits Firestore document limits.
        // 200 KB compresses to ~265 KB Base64, manageable in Firestore console while maintaining receipt quality
        const val MAX_RECEIPT_BYTES = 200 * 1024
    }
}

