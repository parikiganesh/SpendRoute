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
    
    // ⭐ New: Warning dialog state for expense > income
    private val _showExpenseWarning = MutableStateFlow(false)
    val showExpenseWarning: StateFlow<Boolean> = _showExpenseWarning.asStateFlow()
    
    private val _monthlyIncome = MutableStateFlow(0.0)
    val monthlyIncome: StateFlow<Double> = _monthlyIncome.asStateFlow()
    
    private val _monthlyExpense = MutableStateFlow(0.0)
    val monthlyExpense: StateFlow<Double> = _monthlyExpense.asStateFlow()
    
    // Track previous state to detect when expense exceeds income (only show on change)
    private val _isFirstLoad = MutableStateFlow(true)  // Track if this is first load

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

                // ⭐ Update monthly values
                _monthlyIncome.value = income
                _monthlyExpense.value = expense
                
                // ⭐ Show alert if:
                // 1. NOT first load (skip on app launch)
                // 2. Expense > Income
                // Alert shows every time the condition is true
                if (!_isFirstLoad.value && expense > income) {
                    _showExpenseWarning.value = true
                } else {
                    _showExpenseWarning.value = false
                }
                
                // ⭐ Mark first load as complete (only on first call)
                if (_isFirstLoad.value) {
                    _isFirstLoad.value = false
                }

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
    
    /**
     * Dismiss the warning dialog
     * Called when user clicks "I Understand" button
     */
    fun dismissWarningDialog() {
        _showExpenseWarning.value = false
    }
    
    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
        }
    }
}

