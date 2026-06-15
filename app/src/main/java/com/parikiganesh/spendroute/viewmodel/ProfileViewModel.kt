package com.parikiganesh.spendroute.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parikiganesh.spendroute.data.UserPreferences
import com.parikiganesh.spendroute.data.model.Transaction
import com.parikiganesh.spendroute.repository.TransactionRepository
import com.parikiganesh.spendroute.utils.CsvExporter
import com.parikiganesh.spendroute.utils.NotificationPreferences
import com.parikiganesh.spendroute.utils.PdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ProfileViewModel manages all state and business logic for the ProfileScreen
 * 
 * Responsibilities:
 * - Notification preferences management
 * - Export operations (CSV, PDF)
 * - Clear data with undo functionality
 * - User preferences fetching
 * - Permission request handling
 * - Timer/countdown logic
 */

data class ProfileState(
    // Notification state
    val notificationsEnabled: Boolean = false,
    
    // Export dialog state
    val showExportDialog: Boolean = false,
    val isExporting: Boolean = false,
    
    // Clear data state
    val showClearDialog: Boolean = false,
    val showDeleteSnackbar: Boolean = false,
    val deleteCountdown: Int = 5,
    val countdownCompleted: Boolean = false,  // Flag to track when countdown finishes
    
    // Undo state
    val previousNotificationState: Boolean = false,
    val previousUserName: String = "",
    val previousAccountCreatedDate: String = "",
    val previousTransactions: List<Transaction> = emptyList(),
    
    // Privacy dialog state
    val showPrivacyDialog: Boolean = false,
    
    // User info
    val userName: String = "",
    val userInitials: String = "",
    val accountCreatedDate: String = "",
    val appVersion: String = "1.0.0",
    
    // Transactions
    val allTransactions: List<Transaction> = emptyList(),
    
    // Loading state
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val userPreferences: UserPreferences,
    private val notificationPreferences: NotificationPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // State management
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()
    
    init {
        loadUserInfo()
        loadTransactions()
        loadNotificationState()
    }
    
    /**
     * Load or refresh user information from preferences
     */
    fun loadUserInfo() {
        val userName = userPreferences.getUserName()
        val userInitials = userPreferences.getUserInitials()
        val accountCreatedDate = userPreferences.getAccountCreatedDate()
        val appVersion = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
        
        _state.value = _state.value.copy(
            userName = userName,
            userInitials = userInitials,
            accountCreatedDate = accountCreatedDate,
            appVersion = appVersion
        )
    }
    
    /**
     * Load transactions from database
     */
    private fun loadTransactions() {
        viewModelScope.launch {
            repository.getAllTransactions().collect { transactions ->
                _state.value = _state.value.copy(allTransactions = transactions)
            }
        }
    }
    
    /**
     * Load notification state from preferences
     */
    private fun loadNotificationState() {
        val isEnabled = notificationPreferences.isNotificationsEnabled()
        _state.value = _state.value.copy(notificationsEnabled = isEnabled)
    }
    
    // ===== Notification Management =====
    
    /**
     * Enable notifications
     */
    fun enableNotifications() {
        _state.value = _state.value.copy(notificationsEnabled = true)
        notificationPreferences.setNotificationsEnabled(true)
    }
    
    /**
     * Disable notifications
     */
    fun disableNotifications() {
        _state.value = _state.value.copy(notificationsEnabled = false)
        notificationPreferences.setNotificationsEnabled(false)
    }

    // ===== Export Dialog Management =====
    
    /**
     * Show export dialog
     */
    fun showExportDialog() {
        _state.value = _state.value.copy(showExportDialog = true)
    }
    
    /**
     * Hide export dialog
     */
    fun hideExportDialog() {
        _state.value = _state.value.copy(showExportDialog = false)
    }
    
    /**
     * Export transactions as CSV
     */
    fun exportAsCSV() {
        _state.value = _state.value.copy(isExporting = true, errorMessage = null)
        viewModelScope.launch {
            try {
                // Check if there are transactions to export
                if (_state.value.allTransactions.isEmpty()) {
                    _state.value = _state.value.copy(
                        isExporting = false,
                        errorMessage = "No transactions to export"
                    )
                    return@launch
                }
                
                println("DEBUG: Exporting ${_state.value.allTransactions.size} transactions to CSV")
                val fileUri = CsvExporter.exportTransactionsToCSV(context, _state.value.allTransactions)
                println("DEBUG: CSV export returned URI: $fileUri")
                
                if (fileUri != null) {
                    // Add small delay to ensure file is written to disk
                    delay(500)
                    
                    // Share the file on main thread
                    withContext(Dispatchers.Main) {
                        try {
                            println("DEBUG: Sharing CSV file with URI: $fileUri")
                            CsvExporter.shareCSVFile(context, fileUri)
                            _state.value = _state.value.copy(
                                isExporting = false,
                                showExportDialog = false,
                                errorMessage = null
                            )
                        } catch (e: Exception) {
                            println("DEBUG: CSV Share failed: ${e.message}")
                            e.printStackTrace()
                            _state.value = _state.value.copy(
                                isExporting = false,
                                errorMessage = "Failed to share CSV: Check if file sharing apps are available"
                            )
                        }
                    }
                } else {
                    _state.value = _state.value.copy(
                        isExporting = false,
                        errorMessage = "Failed to create CSV file"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("DEBUG: CSV Export exception: ${e.message}")
                _state.value = _state.value.copy(
                    isExporting = false,
                    errorMessage = "Export failed: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    /**
     * Export transactions as PDF
     */
    fun exportAsPDF() {
        _state.value = _state.value.copy(isExporting = true, errorMessage = null)
        viewModelScope.launch {
            try {
                // Check if there are transactions to export
                if (_state.value.allTransactions.isEmpty()) {
                    _state.value = _state.value.copy(
                        isExporting = false,
                        errorMessage = "No transactions to export"
                    )
                    return@launch
                }
                
                println("DEBUG: Exporting ${_state.value.allTransactions.size} transactions to PDF")
                val fileUri = PdfExporter.exportTransactionsToPDF(context, _state.value.allTransactions)
                println("DEBUG: PDF export returned URI: $fileUri")
                
                if (fileUri != null) {
                    // Add small delay to ensure file is written to disk
                    delay(500)
                    
                    // Share the file on main thread
                    withContext(Dispatchers.Main) {
                        try {
                            println("DEBUG: Sharing PDF file with URI: $fileUri")
                            PdfExporter.sharePDFFile(context, fileUri)
                            _state.value = _state.value.copy(
                                isExporting = false,
                                showExportDialog = false,
                                errorMessage = null
                            )
                        } catch (e: Exception) {
                            println("DEBUG: PDF Share failed: ${e.message}")
                            e.printStackTrace()
                            _state.value = _state.value.copy(
                                isExporting = false,
                                errorMessage = "Failed to share PDF: Check if file sharing apps are available"
                            )
                        }
                    }
                } else {
                    _state.value = _state.value.copy(
                        isExporting = false,
                        errorMessage = "Failed to create PDF file"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("DEBUG: PDF Export exception: ${e.message}")
                _state.value = _state.value.copy(
                    isExporting = false,
                    errorMessage = "Export failed: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    // ===== Clear Data Management =====
    
    /**
     * Show clear data confirmation dialog
     */
    fun showClearDialog() {
        _state.value = _state.value.copy(showClearDialog = true)
    }
    
    /**
     * Hide clear data confirmation dialog
     */
    fun hideClearDialog() {
        _state.value = _state.value.copy(showClearDialog = false)
    }
    
    /**
     * Clear all data and show undo snackbar
     */
    fun clearAllData() {
        val state = _state.value
        
        // Save previous state for undo (including transactions backup)
        _state.value = state.copy(
            previousNotificationState = state.notificationsEnabled,
            previousUserName = userPreferences.getUserName(),
            previousAccountCreatedDate = userPreferences.getAccountCreatedDate(),
            previousTransactions = state.allTransactions,  // ✅ Backup transactions before deleting
            showClearDialog = false
        )
        
        // Delete all transactions
        viewModelScope.launch {
            repository.deleteAllTransactions()
            
            // Clear all user data
            userPreferences.clearAllUserData()
            
            // Reset notification state
            notificationPreferences.setNotificationsEnabled(false)
            
            // Clear transactions from UI
            _state.value = _state.value.copy(
                allTransactions = emptyList(),  // ✅ Clear from UI
                showDeleteSnackbar = true,
                deleteCountdown = 5,
                notificationsEnabled = false
            )
            
            // Start countdown timer
            startUndoCountdown()
        }
    }
    
    /**
     * Start the undo countdown timer (5 seconds)
     */
    private suspend fun startUndoCountdown() {
        for (i in 4 downTo 0) {
            delay(1000)
            if (_state.value.showDeleteSnackbar) {
                _state.value = _state.value.copy(deleteCountdown = i)
            }
        }
        
        // When countdown completes without undo, set flag to trigger navigation
        if (_state.value.showDeleteSnackbar) {
            _state.value = _state.value.copy(
                showDeleteSnackbar = false,
                countdownCompleted = true
            )
        }
    }
    
    /**
     * Reset the countdown completed flag (called after navigation is triggered)
     */
    fun resetCountdownCompleted() {
        _state.value = _state.value.copy(countdownCompleted = false)
    }
    
    fun undoClearData() {
        val state = _state.value
        
        viewModelScope.launch {
            // Restore all transactions from backup
            for (transaction in state.previousTransactions) {
                repository.insertTransaction(transaction)
            }
            
            // Restore user preferences
            userPreferences.saveUserName(state.previousUserName)

            // Restore notification state
            notificationPreferences.setNotificationsEnabled(state.previousNotificationState)
            
            // Update UI state with restored data
            _state.value = _state.value.copy(
                allTransactions = state.previousTransactions,
                showDeleteSnackbar = false,
                countdownCompleted = false,
                notificationsEnabled = state.previousNotificationState,
                previousNotificationState = false,
                previousUserName = "",
                previousAccountCreatedDate = "",
                previousTransactions = emptyList()
            )
        }
    }
    
    // ===== Privacy Dialog Management =====
    
    /**
     * Show privacy policy dialog
     */
    fun showPrivacyDialog() {
        _state.value = _state.value.copy(showPrivacyDialog = true)
    }
    
    /**
     * Hide privacy policy dialog
     */
    fun hidePrivacyDialog() {
        _state.value = _state.value.copy(showPrivacyDialog = false)
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}




