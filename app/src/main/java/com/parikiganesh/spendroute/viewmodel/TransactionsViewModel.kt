package com.parikiganesh.spendroute.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parikiganesh.spendroute.data.model.Transaction
import com.parikiganesh.spendroute.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

enum class FilterType {
    ALL, INCOME, EXPENSE
}

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _filterType = MutableStateFlow(FilterType.ALL)
    val filterType: StateFlow<FilterType> = _filterType.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactions: StateFlow<List<Transaction>> = _allTransactions.asStateFlow()

    private val _filteredTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val filteredTransactions: StateFlow<List<Transaction>> = _filteredTransactions.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            repository.getAllTransactions().collect { transactions ->
                _allTransactions.value = transactions
                applyFiltersAndSearch()
            }
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    fun setFilterType(filter: FilterType) {
        _filterType.value = filter
        applyFiltersAndSearch()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFiltersAndSearch()
    }

    private fun applyFiltersAndSearch() {
        var transactions = _allTransactions.value

        // Apply filter
        transactions = when (_filterType.value) {
            FilterType.ALL -> transactions
            FilterType.INCOME -> transactions.filter { it.isIncome }
            FilterType.EXPENSE -> transactions.filter { !it.isIncome }
        }

        // Apply search
        if (_searchQuery.value.isNotEmpty()) {
            transactions = transactions.filter { transaction ->
                transaction.title.contains(_searchQuery.value, ignoreCase = true) ||
                        transaction.category.contains(_searchQuery.value, ignoreCase = true)
            }
        }

        _filteredTransactions.value = transactions
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

    fun deleteAllTransactions() {
        viewModelScope.launch {
            try {
                repository.deleteAllTransactions()
                _uiMessage.value = "All transactions deleted"
            } catch (e: Exception) {
                _uiMessage.value = if (e is IOException && e.message == "No internet connection") {
                    "No internet connection"
                } else {
                    e.message ?: "Failed to clear transactions"
                }
            }
        }
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }
}


