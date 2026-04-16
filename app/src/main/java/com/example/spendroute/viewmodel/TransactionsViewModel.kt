package com.example.spendroute.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendroute.data.model.Transaction
import com.example.spendroute.data.local.SpendRouteDatabase
import com.example.spendroute.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class FilterType {
    ALL, INCOME, EXPENSE
}

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SpendRouteDatabase.getDatabase(application)
    private val repository = TransactionRepository(database.transactionDao())

    private val _filterType = MutableStateFlow(FilterType.ALL)
    val filterType: StateFlow<FilterType> = _filterType.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactions: StateFlow<List<Transaction>> = _allTransactions.asStateFlow()

    private val _filteredTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val filteredTransactions: StateFlow<List<Transaction>> = _filteredTransactions.asStateFlow()

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
            repository.deleteTransaction(transactionId)
        }
    }

    fun deleteAllTransactions() {
        viewModelScope.launch {
            repository.deleteAllTransactions()
        }
    }
}


