package com.parikiganesh.spendroute.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    companion object {
        private const val PASSWORD_PROVIDER_ID = "password"
    }

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
                    println("DEBUG: Email signup successful, user created")
                    firebaseAuth.currentUser?.sendEmailVerification()?.await()
                    println("DEBUG: Verification email sent")
                    // Save name to preferences after successful signup
                    val name = state.name.trim()
                    userPreferences.saveUserName(name)
                    println("DEBUG: Name saved locally: '$name'")
                    try {
                        backupSyncManager.backupCurrentUserProfile(
                            name = name,
                            accountCreatedDate = userPreferences.getAccountCreatedDate()
                        )
                        println("DEBUG: Name backed up to cloud")
                    } catch (e: Exception) {
                        println("DEBUG: Cloud profile backup failed during signup: ${e.message}")
                        e.printStackTrace()
                    }
                    firebaseAuth.signOut()
                    println("DEBUG: User signed out waiting for email verification")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegisterMode = false,
                        password = "",
                        hasAcceptedTerms = false,
                        errorMessage = "Verification email sent. Verify your email, then log in. Check your spam folder if you don't see it."
                    )
                    return@launch
                } else {
                    firebaseAuth.signInWithEmailAndPassword(state.email.trim(), state.password).await()
                    println("DEBUG: Email login successful")
                    val user = firebaseAuth.currentUser
                    user?.reload()?.await()
                    if (user != null && requiresEmailVerification(user) && !user.isEmailVerified) {
                        user.sendEmailVerification().await()
                        firebaseAuth.signOut()
                        println("DEBUG: Email not verified, new verification email sent")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Please verify your email before logging in. We sent a new verification email. Check your spam folder if you don't see it."
                        )
                        return@launch
                    }
                    println("DEBUG: Email is verified, proceeding with post-login sync")
                }

                backupSyncManager.runPostLoginSync()
                println("DEBUG: Post-login sync completed")
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

    private fun requiresEmailVerification(user: FirebaseUser?): Boolean {
        return user?.providerData?.any { it.providerId == PASSWORD_PROVIDER_ID } == true
    }

    fun signInWithGoogleIdToken(
        idToken: String,
        googleDisplayName: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(credential).await()
                val resolvedName = googleDisplayName?.trim().orEmpty()
                    .ifEmpty { firebaseAuth.currentUser?.displayName?.trim().orEmpty() }
                if (resolvedName.isNotEmpty()) {
                    userPreferences.saveUserName(resolvedName)
                    try {
                        backupSyncManager.backupCurrentUserProfile(
                            name = resolvedName,
                            accountCreatedDate = userPreferences.getAccountCreatedDate()
                        )
                    } catch (e: Exception) {
                        println("DEBUG: Cloud profile backup failed during Google sign-in: ${e.message}")
                    }
                }
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

