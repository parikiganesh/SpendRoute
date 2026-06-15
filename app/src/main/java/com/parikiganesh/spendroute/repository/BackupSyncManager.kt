package com.parikiganesh.spendroute.repository

import com.parikiganesh.spendroute.data.UserPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupSyncManager @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val cloudBackupService: CloudBackupService,
    private val userPreferences: UserPreferences
) {

    suspend fun runPostLoginSync() {
        val uid = cloudBackupService.currentUserId() ?: return

        // First login migration: push existing local data to cloud first.
        if (!userPreferences.isInitialCloudMigrationDone(uid)) {
            val local = transactionRepository.getAllTransactionsSnapshot()
            if (local.isNotEmpty()) {
                cloudBackupService.backupTransactions(local)
            }
            userPreferences.setInitialCloudMigrationDone(uid, true)
        }

        // Restore cloud snapshot into local DB after auth.
        val cloudTransactions = cloudBackupService.restoreTransactions()
        if (cloudTransactions.isNotEmpty()) {
            transactionRepository.replaceLocalTransactions(cloudTransactions)
        }
    }
}

