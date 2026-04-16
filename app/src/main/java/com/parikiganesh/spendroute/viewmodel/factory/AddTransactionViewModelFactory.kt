package com.parikiganesh.spendroute.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.parikiganesh.spendroute.data.local.SpendRouteDatabase
import com.parikiganesh.spendroute.repository.TransactionRepository
import com.parikiganesh.spendroute.viewmodel.AddTransactionViewModel

class AddTransactionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTransactionViewModel::class.java)) {
            val database = SpendRouteDatabase.getDatabase(application)
            val repository = TransactionRepository(database.transactionDao())
            return AddTransactionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

