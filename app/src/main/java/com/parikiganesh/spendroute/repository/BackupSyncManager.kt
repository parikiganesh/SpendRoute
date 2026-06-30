package com.parikiganesh.spendroute.repository

import com.parikiganesh.spendroute.data.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupSyncManager @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val cloudBackupService: CloudBackupService,
    private val userPreferences: UserPreferences
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var profileSyncJob: Job? = null

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
        println("DEBUG: runPostLoginSync started for uid: $uid")
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
        println("DEBUG: Restored ${cloudTransactions.size} transactions from cloud")

        val cloudName = cloudBackupService.restoreUserName()
        println("DEBUG: Restored name from cloud: '$cloudName'")
        val localName = userPreferences.getUserName().trim()
        println("DEBUG: Local name: '$localName'")
        when {
            cloudName.isNotEmpty() -> userPreferences.saveUserName(cloudName)
            localName.isNotEmpty() -> {
                println("DEBUG: Cloud name empty, backing up local name: '$localName'")
                cloudBackupService.backupUserName(localName)
            }
        }

        val cloudAccountCreatedDate = cloudBackupService.restoreAccountCreatedDate()
        println("DEBUG: Restored account created date from cloud: '$cloudAccountCreatedDate'")
        val localAccountCreatedDate = userPreferences.getAccountCreatedDate().trim()
        println("DEBUG: Local account created date: '$localAccountCreatedDate'")
        when {
            cloudAccountCreatedDate.isNotEmpty() -> userPreferences.setAccountCreatedDate(cloudAccountCreatedDate)
            localName.isNotEmpty() && localAccountCreatedDate.isNotEmpty() -> {
                println("DEBUG: Cloud date empty, backing up local date: '$localAccountCreatedDate'")
                cloudBackupService.backupAccountCreatedDate(localAccountCreatedDate)
            }
        }

        userPreferences.setLastAuthenticatedUserId(uid)
        ensureProfileLiveSync()
        println("DEBUG: runPostLoginSync completed")
    }

    private fun ensureProfileLiveSync() {
        if (profileSyncJob?.isActive == true) return

        profileSyncJob = scope.launch {
            cloudBackupService.observeUserProfile()
                .distinctUntilChanged()
                .collect { profile ->
                    if (profile.name.isNotEmpty()) {
                        userPreferences.saveUserName(profile.name)
                    }
                    if (profile.accountCreatedDate.isNotEmpty()) {
                        userPreferences.setAccountCreatedDate(profile.accountCreatedDate)
                    }
                }
        }
    }
}

