package com.example.spendroute.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spendroute.data.local.SpendRouteDatabase
import com.example.spendroute.repository.TransactionRepository
import com.example.spendroute.viewmodel.AnalyticsViewModel

class AnalyticsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            val database = SpendRouteDatabase.getDatabase(application)
            val repository = TransactionRepository(database.transactionDao())
            return AnalyticsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

