package com.parikiganesh.spendroute.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.parikiganesh.spendroute.viewmodel.OnboardingViewModel

class OnboardingViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            return OnboardingViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

