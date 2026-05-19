package com.parikiganesh.spendroute.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * SplashViewModel manages the state for the splash screen
 * 
 * Responsibilities:
 * - Animation state management
 * - Splash duration timing
 * - Onboarding status tracking
 */

data class SplashState(
    val animationAlpha: Float = 0f,
    val isComplete: Boolean = false,
    val isOnboardingCompleted: Boolean = false
)

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()
    
    /**
     * Start the splash animation
     */
    fun startAnimation() {
        _state.value = _state.value.copy(animationAlpha = 1f)
    }
    
    /**
     * Mark splash as complete (ready to navigate)
     */
    fun completeSplash(isOnboardingCompleted: Boolean) {
        _state.value = _state.value.copy(
            isComplete = true,
            isOnboardingCompleted = isOnboardingCompleted
        )
    }
    
    /**
     * Reset splash screen
     */
    fun resetSplash() {
        _state.value = SplashState()
    }
}

