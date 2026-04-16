package com.example.spendroute.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * OnboardingViewModel manages the state for the onboarding flow
 * 
 * Responsibilities:
 * - Form state management (name input)
 * - Input validation
 * - Onboarding completion logic
 */

data class OnboardingFormState(
    val name: String = "",
    val isNameValid: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

class OnboardingViewModel : ViewModel() {

    private val _formState = MutableStateFlow(OnboardingFormState())
    val formState: StateFlow<OnboardingFormState> = _formState.asStateFlow()
    
    /**
     * Update the name field and validate
     */
    fun updateName(name: String) {
        val isValid = name.isNotEmpty() && name.length >= 2
        _formState.value = _formState.value.copy(
            name = name,
            isNameValid = isValid,
            errorMessage = when {
                name.isEmpty() -> null
                name.length < 2 -> "Name must be at least 2 characters"
                else -> null
            }
        )
    }
    
    /**
     * Complete onboarding with the entered name
     * Returns the name if valid, otherwise returns null
     */
    fun completeOnboarding(): String? {
        val state = _formState.value
        
        return if (state.isNameValid) {
            state.name
        } else {
            _formState.value = state.copy(
                errorMessage = "Please enter a valid name"
            )
            null
        }
    }
    
    /**
     * Reset the form to initial state
     */
    fun resetForm() {
        _formState.value = OnboardingFormState()
    }
}

