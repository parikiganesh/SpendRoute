package com.parikiganesh.spendroute.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.parikiganesh.spendroute.data.model.Transaction
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudBackupService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    fun currentUserId(): String? = firebaseAuth.currentUser?.uid

    suspend fun backupTransactions(transactions: List<Transaction>) {
        val uid = currentUserId() ?: return
        val userTxCollection = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(TRANSACTIONS_COLLECTION)

        val snapshot = userTxCollection.get().await()
        val batch = firestore.batch()

        // Replace cloud state with local state for deterministic sync.
        snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
        transactions.forEach { txn ->
            val doc = userTxCollection.document(txn.id)
            batch.set(doc, transactionToMap(txn))
        }

        batch.commit().await()
    }

    suspend fun restoreTransactions(): List<Transaction> {
        val uid = currentUserId() ?: return emptyList()
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(TRANSACTIONS_COLLECTION)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            mapToTransaction(doc.id, doc.data ?: return@mapNotNull null)
        }
    }

    suspend fun backupUserName(name: String) {
         val uid = currentUserId() ?: return
         val trimmedName = name.trim()
         if (trimmedName.isEmpty()) return

         firestore.collection(USERS_COLLECTION)
             .document(uid)
             .set(
                 mapOf(
                     USER_NAME_FIELD to trimmedName,
                     USER_PROFILE_UPDATED_AT_FIELD to System.currentTimeMillis()
                 ),
                 SetOptions.merge()
             )
             .await()
     }

    suspend fun restoreUserName(): String {
        val uid = currentUserId() ?: return ""
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .await()
        return snapshot.getString(USER_NAME_FIELD)?.trim().orEmpty()
    }

    suspend fun backupAccountCreatedDate(accountCreatedDate: String) {
         val uid = currentUserId() ?: return
         val trimmedDate = accountCreatedDate.trim()
         if (trimmedDate.isEmpty()) return

         firestore.collection(USERS_COLLECTION)
             .document(uid)
             .set(
                 mapOf(
                     ACCOUNT_CREATED_DATE_FIELD to trimmedDate,
                     USER_PROFILE_UPDATED_AT_FIELD to System.currentTimeMillis()
                 ),
                 SetOptions.merge()
             )
             .await()
     }

    suspend fun restoreAccountCreatedDate(): String {
        val uid = currentUserId() ?: return ""
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .await()
        return snapshot.getString(ACCOUNT_CREATED_DATE_FIELD)?.trim().orEmpty()
    }

    private fun transactionToMap(transaction: Transaction): Map<String, Any?> {
        return mapOf(
            "title" to transaction.title,
            "category" to transaction.category,
            "amount" to transaction.amount,
            "date" to transaction.date,
            "time" to transaction.time,
            "isIncome" to transaction.isIncome,
            "note" to transaction.note,
            "timestamp" to System.currentTimeMillis()
        )
    }

    private fun mapToTransaction(id: String, data: Map<String, Any>): Transaction? {
        val title = data["title"] as? String ?: return null
        val category = data["category"] as? String ?: return null
        val amount = (data["amount"] as? Number)?.toDouble() ?: return null
        val date = data["date"] as? String ?: return null
        val time = data["time"] as? String ?: return null
        val isIncome = data["isIncome"] as? Boolean ?: return null
        val note = data["note"] as? String

        return Transaction(
            id = id,
            title = title,
            category = category,
            amount = amount,
            date = date,
            time = time,
            isIncome = isIncome,
            note = note
        )
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val TRANSACTIONS_COLLECTION = "transactions"
        const val USER_NAME_FIELD = "name"
        const val ACCOUNT_CREATED_DATE_FIELD = "accountCreatedDate"
        const val USER_PROFILE_UPDATED_AT_FIELD = "profileUpdatedAt"
    }
}

