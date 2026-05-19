package com.parikiganesh.spendroute.viewmodel

import androidx.lifecycle.ViewModel
import com.parikiganesh.spendroute.data.model.Transaction
import com.parikiganesh.spendroute.data.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SRDashboardViewModel @Inject constructor() : ViewModel() {
    private val _currentRoute = MutableStateFlow("home")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()
    
    private val _transactionToEdit = MutableStateFlow<Transaction?>(null)
    val transactionToEdit: StateFlow<Transaction?> = _transactionToEdit.asStateFlow()
    
    private val _initialTransactionType = MutableStateFlow<TransactionType?>(null)
    val initialTransactionType: StateFlow<TransactionType?> = _initialTransactionType.asStateFlow()

    fun navigateTo(route: String) {
        _currentRoute.value = route
    }
    
    fun resetToHome() {
        _currentRoute.value = "home"
    }
    
    fun setTransactionToEdit(transaction: Transaction?) {
        _transactionToEdit.value = transaction
    }
    
    fun setInitialTransactionType(type: TransactionType?) {
        _initialTransactionType.value = type
    }
}
