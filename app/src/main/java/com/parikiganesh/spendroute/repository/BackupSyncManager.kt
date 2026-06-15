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

    suspend fun backupCurrentUserProfile(name: String, accountCreatedDate: String) {
        if (name.trim().isNotEmpty()) {
            cloudBackupService.backupUserName(name)
        }
        if (accountCreatedDate.trim().isNotEmpty()) {
            cloudBackupService.backupAccountCreatedDate(accountCreatedDate)
        }
    }

    suspend fun runPostLoginSync() {
        val uid = cloudBackupService.currentUserId() ?: return
        val lastAuthenticatedUid = userPreferences.getLastAuthenticatedUserId()

        // Prevent cross-account leakage: wipe previous authenticated user's local cache.
        if (lastAuthenticatedUid != null && lastAuthenticatedUid != uid) {
            transactionRepository.replaceLocalTransactions(emptyList())
            userPreferences.clearLocalUserProfileCache()
        }

        // First authenticated session on this device: migrate existing guest local data.
        if (lastAuthenticatedUid == null && !userPreferences.isInitialCloudMigrationDone(uid)) {
            val local = transactionRepository.getAllTransactionsSnapshot()
            if (local.isNotEmpty()) {
                cloudBackupService.backupTransactions(local)
            }
            userPreferences.setInitialCloudMigrationDone(uid, true)
        }

        // Restore cloud snapshot into local DB after auth.
        val cloudTransactions = cloudBackupService.restoreTransactions()
        transactionRepository.replaceLocalTransactions(cloudTransactions)

        val cloudName = cloudBackupService.restoreUserName()
        val localName = userPreferences.getUserName().trim()
        when {
            cloudName.isNotEmpty() -> userPreferences.saveUserName(cloudName)
            localName.isNotEmpty() -> cloudBackupService.backupUserName(localName)
        }

        val cloudAccountCreatedDate = cloudBackupService.restoreAccountCreatedDate()
        val localAccountCreatedDate = userPreferences.getAccountCreatedDate().trim()
        when {
            cloudAccountCreatedDate.isNotEmpty() -> userPreferences.setAccountCreatedDate(cloudAccountCreatedDate)
            localName.isNotEmpty() && localAccountCreatedDate.isNotEmpty() -> {
                cloudBackupService.backupAccountCreatedDate(localAccountCreatedDate)
            }
        }

        userPreferences.setLastAuthenticatedUserId(uid)
    }
}

