package com.example.spendroute.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spendroute.data.local.SpendRouteDatabase
import com.example.spendroute.repository.TransactionRepository
import com.example.spendroute.viewmodel.TransactionsViewModel

class TransactionsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
            val database = SpendRouteDatabase.getDatabase(application)
            val repository = TransactionRepository(database.transactionDao())
            return TransactionsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

