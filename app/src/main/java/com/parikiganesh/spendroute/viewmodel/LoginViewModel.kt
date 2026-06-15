package com.parikiganesh.spendroute.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.parikiganesh.spendroute.data.UserPreferences
import com.parikiganesh.spendroute.repository.BackupSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val hasAcceptedTerms: Boolean = false,
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val backupSyncManager: BackupSyncManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun updateName(value: String) {
        _uiState.value = _uiState.value.copy(name = value, errorMessage = null)
    }

    fun setTermsAccepted(accepted: Boolean) {
        _uiState.value = _uiState.value.copy(hasAcceptedTerms = accepted, errorMessage = null)
    }

    fun setRegisterMode(isRegister: Boolean) {
        _uiState.value = _uiState.value.copy(
            isRegisterMode = isRegister,
            name = "",
            email = "",
            password = "",
            hasAcceptedTerms = false,
            isLoading = false,
            errorMessage = null
        )
    }

    fun resetForm() {
        _uiState.value = LoginUiState(isRegisterMode = _uiState.value.isRegisterMode)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun signInOrRegister(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Email and password are required")
            return
        }

        if (state.isRegisterMode && state.name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Name is required")
            return
        }

        if (state.isRegisterMode && !state.hasAcceptedTerms) {
            _uiState.value = state.copy(errorMessage = "Please accept Terms & Conditions and Privacy Policy")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            try {
                if (state.isRegisterMode) {
                    firebaseAuth.createUserWithEmailAndPassword(state.email.trim(), state.password).await()
                    // Save name to preferences after successful signup
                    userPreferences.saveUserName(state.name.trim())
                } else {
                    firebaseAuth.signInWithEmailAndPassword(state.email.trim(), state.password).await()
                }

                backupSyncManager.runPostLoginSync()
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Authentication failed"
                )
            }
        }
    }

    fun signInWithGoogleIdToken(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(credential).await()
                backupSyncManager.runPostLoginSync()
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Google sign-in failed"
                )
            }
        }
    }
}

