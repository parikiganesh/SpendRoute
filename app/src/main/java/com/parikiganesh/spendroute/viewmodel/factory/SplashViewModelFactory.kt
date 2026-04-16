package com.parikiganesh.spendroute.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.parikiganesh.spendroute.viewmodel.SplashViewModel

class SplashViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            return SplashViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

