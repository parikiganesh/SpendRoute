package com.parikiganesh.spendroute.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import com.parikiganesh.spendroute.data.model.Transaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudBackupService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val cloudEncryptionService: CloudEncryptionService
) {

    data class CloudUserProfile(
        val name: String,
        val accountCreatedDate: String
    )

    fun currentUserId(): String? = firebaseAuth.currentUser?.uid

    /**
     * Uploads local transactions to cloud Firestore with selective encryption.
     *
     * **Encryption Behavior**:
     * - Amount and note fields → encrypted with AES-256-GCM, prefixed "enc::"
     * - Other fields (title, category, date, time, isIncome) → plaintext
     * - Profile fields (name, accountCreatedDate) → plaintext (see backupUserName/backupAccountCreatedDate)
     *
     * **Preservation Strategy**:
     * - Does NOT delete existing cloud documents
     * - Only uploads transaction IDs that don't already exist in cloud
     * - Existing plaintext records remain untouched (backward compatible)
     *
     * **Migration Path** (if encryption needs removal):
     * 1. Stop calling this during normal sync
     * 2. New transactions: Use alternative uploader that skips encryption
     * 3. Migrate existing encrypted records (optional):
     *    - Read all cloud txns via restoreTransactions() (auto-decrypts)
     *    - Write back as plaintext with new map function
     * 4. Once all plaintext, remove CloudEncryptionService dependency
     *
     * @param transactions List of local Transaction objects to backup
     */
    suspend fun backupTransactions(transactions: List<Transaction>) {
        val uid = currentUserId() ?: return
        val userTxCollection = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(TRANSACTIONS_COLLECTION)

        val snapshot = userTxCollection.get().await()
        val existingIds = snapshot.documents.map { it.id }.toHashSet()
        val newTransactions = transactions.filter { it.id !in existingIds }
        if (newTransactions.isEmpty()) return

        val batch = firestore.batch()

        // Preserve existing cloud docs as-is. Only write brand-new transaction IDs.
        newTransactions.forEach { txn ->
            val doc = userTxCollection.document(txn.id)
            batch.set(doc, transactionToMap(uid, txn))
        }

        batch.commit().await()
    }

    /**
     * Downloads all transactions from cloud Firestore with transparent decryption.
     *
     * **Decryption Behavior** (via mapToTransaction):
     * - Amount and note fields → Auto-decrypt if "enc::..." format; otherwise plaintext
     * - Other fields → Plaintext always
     * - Handles mixed plaintext/encrypted records safely
     *
     * **Used In Post-Login Sync**:
     * - BackupSyncManager.runPostLoginSync() calls this after auth
     * - Result replaces local Room DB to restore user's transactions
     *
     * **Migration Path** (removing encryption):
     * Once all encrypted records migrated to plaintext:
     * 1. Remove CloudEncryptionService injection from CloudBackupService
     * 2. Simplify mapToTransaction() to skip decryption logic
     * 3. Or keep decryption indefinitely (no performance cost if all plaintext)
     *
     * @return List of Transaction objects with decrypted amount/note fields
     */
    suspend fun restoreTransactions(): List<Transaction> {
        val uid = currentUserId() ?: return emptyList()
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(TRANSACTIONS_COLLECTION)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            mapToTransaction(uid, doc.id, doc.data ?: return@mapNotNull null)
        }
    }

    /**
     * Real-time transaction stream for the currently authenticated user.
     * Automatically switches listener when auth user changes.
     */
    fun observeTransactions(): Flow<List<Transaction>> = callbackFlow {
        var txRegistration: com.google.firebase.firestore.ListenerRegistration? = null

        fun attachTransactionsListener(uid: String) {
            txRegistration?.remove()
            txRegistration = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .collection(TRANSACTIONS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val docs = snapshot?.documents.orEmpty()
                    val transactions = docs.mapNotNull { doc ->
                        mapToTransaction(uid, doc.id, doc.data ?: return@mapNotNull null)
                    }
                    trySend(transactions)
                }
        }

        val authListener = FirebaseAuth.AuthStateListener { auth ->
            val uid = auth.currentUser?.uid
            if (uid == null) {
                txRegistration?.remove()
                txRegistration = null
                trySend(emptyList())
            } else {
                attachTransactionsListener(uid)
            }
        }

        firebaseAuth.addAuthStateListener(authListener)
        // Emit immediately for current auth state.
        authListener.onAuthStateChanged(firebaseAuth)

        awaitClose {
            txRegistration?.remove()
            firebaseAuth.removeAuthStateListener(authListener)
        }
    }

    /**
     * Real-time profile stream for current user.
     */
    fun observeUserProfile(): Flow<CloudUserProfile> = callbackFlow {
        var profileRegistration: com.google.firebase.firestore.ListenerRegistration? = null

        fun attachProfileListener(uid: String) {
            profileRegistration?.remove()
            profileRegistration = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val name = snapshot?.getString(USER_NAME_FIELD)?.trim().orEmpty()
                    val accountCreatedDate = snapshot?.getString(ACCOUNT_CREATED_DATE_FIELD)?.trim().orEmpty()
                    trySend(CloudUserProfile(name = name, accountCreatedDate = accountCreatedDate))
                }
        }

        val authListener = FirebaseAuth.AuthStateListener { auth ->
            val uid = auth.currentUser?.uid
            if (uid == null) {
                profileRegistration?.remove()
                profileRegistration = null
                trySend(CloudUserProfile(name = "", accountCreatedDate = ""))
            } else {
                attachProfileListener(uid)
            }
        }

        firebaseAuth.addAuthStateListener(authListener)
        authListener.onAuthStateChanged(firebaseAuth)

        awaitClose {
            profileRegistration?.remove()
            firebaseAuth.removeAuthStateListener(authListener)
        }
    }

    /**
     * Creates or updates a single transaction in cloud for current user.
     */
    suspend fun upsertTransaction(transaction: Transaction) {
        val uid = currentUserId() ?: return
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(TRANSACTIONS_COLLECTION)
            .document(transaction.id)
            .set(transactionToMap(uid, transaction))
            .await()
    }

    /**
     * Creates a new transaction with a per-user sequential numeric ID (1,2,3...).
     *
     * Uses a Firestore transaction so IDs remain unique across multiple devices.
     */
    suspend fun createTransactionWithSequentialId(transaction: Transaction): Transaction {
        val uid = currentUserId() ?: return transaction
        val userDoc = firestore.collection(USERS_COLLECTION).document(uid)
        val txCollection = userDoc.collection(TRANSACTIONS_COLLECTION)

        val assignedId = firestore.runTransaction { dbTxn ->
            val userSnapshot = dbTxn.get(userDoc)
            val lastId = userSnapshot.getLong(TRANSACTION_COUNTER_LAST_ID_FIELD) ?: 0L
            val nextId = lastId + 1L

            dbTxn.set(userDoc, mapOf(TRANSACTION_COUNTER_LAST_ID_FIELD to nextId), SetOptions.merge())

            val newTransaction = transaction.copy(id = nextId.toString())
            dbTxn.set(
                txCollection.document(nextId.toString()),
                transactionToMap(uid, newTransaction)
            )

            nextId.toString()
        }.await()

        return transaction.copy(id = assignedId)
    }

    /**
     * Deletes one transaction by id from cloud for current user.
     */
    suspend fun deleteTransactionById(transactionId: String) {
        val uid = currentUserId() ?: return
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(TRANSACTIONS_COLLECTION)
            .document(transactionId)
            .delete()
            .await()
    }

    /**
     * Deletes all transactions for the current authenticated user from Firestore.
     */
    suspend fun clearAllCloudTransactions() {
        val uid = currentUserId() ?: return
        val txCollection = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(TRANSACTIONS_COLLECTION)

        val snapshot = txCollection.get().await()
        if (snapshot.isEmpty) return

        val batch = firestore.batch()
        snapshot.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
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
                     USER_PROFILE_UPDATED_AT_FIELD to Timestamp.now()
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
                     USER_PROFILE_UPDATED_AT_FIELD to Timestamp.now()
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

    /**
     * Stores a Contact Us submission in Firestore under the authenticated user.
     *
     * Paths:
     * - users/{uid}/contactRequests/{ticketId}
     * - supportTickets/{ticketId}
     */
    suspend fun submitContactRequest(name: String, email: String, issue: String) {
        val uid = currentUserId() ?: return
        val ticketId = firestore.collection(SUPPORT_TICKETS_COLLECTION).document().id
        val createdAt = Timestamp.now()

        val userScopedPayload = mapOf(
            CONTACT_NAME_FIELD to name.trim(),
            CONTACT_EMAIL_FIELD to email.trim(),
            CONTACT_ISSUE_FIELD to issue.trim(),
            CONTACT_CREATED_AT_FIELD to createdAt,
            CONTACT_STATUS_FIELD to CONTACT_STATUS_NEW
        )

        val globalPayload = userScopedPayload + mapOf(
            CONTACT_UID_FIELD to uid
        )

        val userTicketRef = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(CONTACT_REQUESTS_COLLECTION)
            .document(ticketId)

        val globalTicketRef = firestore.collection(SUPPORT_TICKETS_COLLECTION)
            .document(ticketId)

        firestore.batch()
            .set(userTicketRef, userScopedPayload)
            .set(globalTicketRef, globalPayload)
            .commit()
            .await()
    }

    /**
     * Converts Transaction model to cloud map with selective field encryption.
     *
     * **Field Handling**:
     * - `amount` → Encrypted: "enc::BASE64(IV+CIPHERTEXT)"
     * - `note` → Encrypted: "enc::BASE64(IV+CIPHERTEXT)" (or null if empty)
     * - `title`, `category`, `date`, `time`, `isIncome` → Plaintext
     * - `timestamp` → System.currentTimeMillis() (query/sort safe)
     *
     * **To Disable Encryption**:
     * Replace encrypted fields with plaintext equivalents:
     * ```kotlin
     * "amount" to transaction.amount,  // Remove encryption
     * "note" to transaction.note,      // Remove encryption
     * ```
     * Then existing encrypted records will need migration (see restoreTransactions docs).
     *
     * **Backward Compat**:
     * mapToTransaction() auto-decrypts; changing this won't break existing encrypted records,
     * but new records will be plaintext while old ones stay encrypted.
     *
     * @param uid Current user ID (for key derivation in encryption)
     * @param transaction Transaction object to serialize
     * @return Map ready for Firestore set operation
     */
    private fun transactionToMap(uid: String, transaction: Transaction): Map<String, Any?> {
        return mapOf(
            "category" to transaction.category,
            "amount" to cloudEncryptionService.encryptForUser(uid, transaction.amount.toString()),
            "date" to transaction.date,
            "time" to transaction.time,
            "isIncome" to transaction.isIncome,
            "note" to transaction.note?.let { cloudEncryptionService.encryptForUser(uid, it) },
            "timestamp" to Timestamp.now()
        )
    }

    /**
     * Converts cloud map to Transaction model with transparent decryption.
     *
     * **Decryption Logic**:
     * - `amount` → Decrypts "enc::..." or parses plaintext double
     * - `note` → Decrypts "enc::..." or returns plaintext string
     * - Other fields → Read as plaintext
     *
     * **Backward Compatible**:
     * Supports mixed plaintext/encrypted records from same user:
     * - Old cloud txns: plaintext amount/note → returns as-is
     * - New cloud txns: encrypted amount/note → decrypted transparently
     *
     * **Decryption Failures**:
     * If decryption fails (wrong key, corrupted data), mapToTransaction returns null
     * for that doc (logged as skipped in restoreTransactions).
     *
     * **To Migrate to Plaintext**:
     * 1. Call restoreTransactions() → auto-decrypts all records
     * 2. Modify transactionToMap() to remove encryptForUser() calls
     * 3. Call backupTransactions(decrypted list) → uploads as plaintext
     * 4. New transactions now plaintext; old encrypted records untouched
     * 5. Over time, old encrypted records become stale archive data (safe to leave)
     *
     * @param uid Current user ID (for key derivation in decryption)
     * @param id Transaction document ID from Firestore
     * @param data Firestore document map (may have encrypted amount/note)
     * @return Transaction object with decrypted fields, or null if required fields missing
     */
    private fun mapToTransaction(uid: String, id: String, data: Map<String, Any>): Transaction? {
        // Category is required (replaces redundant title field)
        val category = data["category"] as? String ?: return null
        val amount = decodeAmountField(uid, data["amount"]) ?: return null
        val date = data["date"] as? String ?: return null
        val time = data["time"] as? String ?: return null
        val isIncome = data["isIncome"] as? Boolean ?: return null
        val note = decodeStringField(uid, data["note"])

        return Transaction(
            id = id,
            category = category,
            amount = amount,
            date = date,
            time = time,
            isIncome = isIncome,
            note = note
        )
    }

    /**
     * Decodes amount field with backward compatibility for plaintext/encrypted formats.
     *
     * **Handling**:
     * - Number type (old plaintext) → Convert to Double
     * - String type (new encrypted) → Decrypt, then parse as Double; fallback to plaintext parse
     *
     * **Returns**:
     * - Non-null Double if decryption or plaintext parse succeeds
     * - Null only if required amount field is missing or unparseable
     *
     * **Migration Note**:
     * Once all amount fields are plaintext, simplify to just `(raw as? Number)?.toDouble()`
     *
     * @param uid User ID for decryption key
     * @param raw Firestore field value (Number or String with optional "enc::" prefix)
     * @return Parsed Double amount, or null if invalid/missing
     */
    private fun decodeAmountField(uid: String, raw: Any?): Double? {
        return when (raw) {
            is Number -> raw.toDouble()
            is String -> {
                val decrypted = cloudEncryptionService.decryptForUser(uid, raw)
                (decrypted ?: raw).toDoubleOrNull()
            }
            else -> null
        }
    }

    /**
     * Decodes string field (note) with backward compatibility for plaintext/encrypted formats.
     *
     * **Handling**:
     * - If null/missing → return null (optional field)
     * - If String with "enc::" prefix → decrypt
     * - If String plaintext → return as-is
     * - If decryption fails → return original value (safe fallback)
     *
     * **Returns**: String or null; never throws
     *
     * **Migration Note**:
     * Once all note fields are plaintext, simplify to just `raw as? String`
     *
     * @param uid User ID for decryption key
     * @param raw Firestore field value (String with optional "enc::" prefix, or null)
     * @return Decrypted/plaintext string note, or null if field absent
     */
    private fun decodeStringField(uid: String, raw: Any?): String? {
        val value = raw as? String ?: return null
        return cloudEncryptionService.decryptForUser(uid, value) ?: value
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val TRANSACTIONS_COLLECTION = "transactions"
        const val TRANSACTION_COUNTER_LAST_ID_FIELD = "lastId"
        const val CONTACT_REQUESTS_COLLECTION = "contactRequests"
        const val SUPPORT_TICKETS_COLLECTION = "supportTickets"
        const val USER_NAME_FIELD = "name"
        const val ACCOUNT_CREATED_DATE_FIELD = "accountCreatedDate"
        const val USER_PROFILE_UPDATED_AT_FIELD = "profileUpdatedAt"
        const val CONTACT_NAME_FIELD = "name"
        const val CONTACT_EMAIL_FIELD = "email"
        const val CONTACT_ISSUE_FIELD = "issue"
        const val CONTACT_UID_FIELD = "uid"
        const val CONTACT_CREATED_AT_FIELD = "createdAt"
        const val CONTACT_STATUS_FIELD = "status"
        const val CONTACT_STATUS_NEW = "new"
    }
}

