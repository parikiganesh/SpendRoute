package com.parikiganesh.spendroute.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parikiganesh.spendroute.data.UserPreferences
import com.parikiganesh.spendroute.data.model.BalanceInfo
import com.parikiganesh.spendroute.data.model.Transaction
import com.parikiganesh.spendroute.repository.TransactionRepository
import com.parikiganesh.spendroute.utils.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _balanceInfo = MutableStateFlow<BalanceInfo?>(null)
    val balanceInfo: StateFlow<BalanceInfo?> = _balanceInfo.asStateFlow()

    private val _recentTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val recentTransactions: StateFlow<List<Transaction>> = _recentTransactions.asStateFlow()

    private val _categoryExpenses = MutableStateFlow<Map<String, Double>>(emptyMap())
    val categoryExpenses: StateFlow<Map<String, Double>> = _categoryExpenses.asStateFlow()
    
    private val _selectedMonth = MutableStateFlow("")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()
    
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    init {
        loadUserName()
        // Load all transactions for recent section
        loadAllTransactions()
        // Load data for current month on app open
        val currentMonth = DateTimeUtils.getCurrentMonthName()
        _selectedMonth.value = currentMonth
        selectMonth(currentMonth)
    }
    
    /**
     * Load or refresh user name from preferences
     */
    fun loadUserName() {
        _userName.value = userPreferences.getUserName()
    }

    /**
     * Load all transactions (not filtered by month) - for Recent Transactions display
     */
    private fun loadAllTransactions() {
        viewModelScope.launch {
            repository.getAllTransactions().collect { transactions ->
                _recentTransactions.value = transactions.take(5)
            }
        }
    }


    fun selectMonth(month: String) {
        _selectedMonth.value = month
        fetchDataForMonth(month)
    }

    fun fetchDataForMonth(month: String) {
        viewModelScope.launch {
            repository.getAllTransactions().collect { transactions ->
                val monthNumber = DateTimeUtils.getMonthNumber(month)
                
                val filteredTransactions = transactions.filter { transaction ->
                    val transactionMonth = DateTimeUtils.getMonthFromDate(transaction.date)
                    transactionMonth == monthNumber
                }
                
                val income = filteredTransactions.filter { it.isIncome }.sumOf { it.amount }
                val expense = filteredTransactions.filter { !it.isIncome }.sumOf { it.amount }
                val balance = income - expense

                _balanceInfo.value = BalanceInfo(
                    month = month,
                    totalBalance = balance,
                    income = income,
                    expense = expense,
                    budget = 25000.0,
                    usedPercentage = if (expense > 0 && balance > 0) (expense / 25000f * 100).toFloat() else 0f
                )

                // Update category expenses for the selected month
                val expenses = filteredTransactions.filter { !it.isIncome }
                _categoryExpenses.value = expenses.groupBy { it.category }
                    .mapValues { (_, items) -> items.sumOf { it.amount } }
                
                // NOTE: recentTransactions is NOT updated here - it always shows ALL transactions
            }
        }
    }
    
    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transactionId)
                _uiMessage.value = "Transaction deleted successfully!"
            } catch (e: Exception) {
                _uiMessage.value = if (e is IOException && e.message == "No internet connection") {
                    "No internet connection"
                } else {
                    e.message ?: "Failed to delete transaction"
                }
            }
        }
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }
}

