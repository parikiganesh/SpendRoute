package com.parikiganesh.spendroute

import android.os.Bundle
import android.widget.Toast
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.google.firebase.auth.FirebaseAuth
import com.parikiganesh.spendroute.data.UserPreferences
import com.parikiganesh.spendroute.ui.screens.LoginScreen
import com.parikiganesh.spendroute.ui.screens.SRDashboard
import com.parikiganesh.spendroute.ui.screens.SplashScreen
import com.parikiganesh.spendroute.ui.theme.SpendRouteTheme
import com.parikiganesh.spendroute.utils.InAppUpdateManager
import com.parikiganesh.spendroute.viewmodel.SRDashboardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: SRDashboardViewModel by viewModels()
    private lateinit var userPreferences: UserPreferences
    private lateinit var updateManager: InAppUpdateManager
    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        userPreferences = UserPreferences(this)
        updateManager = InAppUpdateManager(this)
        
        // Check for app updates on app launch
        checkForAppUpdates()
        
        setContent {
            SpendRouteTheme {
                SpendRouteApp(
                    viewModel = viewModel,
                    userPreferences = userPreferences,
                    onBackPressed = { handleBackPress() }
                )
            }
        }
    }

    /**
     * Check for available updates from Play Store
     */
    private fun checkForAppUpdates() {
        updateManager.checkForUpdates(
            onUpdateAvailable = { appUpdateInfo ->
                // Determine update type based on update priority
                val updatePriority = appUpdateInfo.updatePriority()
                
                if (updatePriority >= 4) {
                    // High priority (>=4): IMMEDIATE update (force update)
                    // Critical security or critical bug fix
                    updateManager.startImmediateUpdate(
                        activity = this,
                        appUpdateInfo = appUpdateInfo,
                        requestCode = InAppUpdateManager.IMMEDIATE_UPDATE_REQUEST_CODE
                    )
                } else {
                    // Lower priority (<4): FLEXIBLE update (optional)
                    // New features, minor improvements
                    updateManager.startFlexibleUpdate(
                        activity = this,
                        appUpdateInfo = appUpdateInfo,
                        requestCode = InAppUpdateManager.FLEXIBLE_UPDATE_REQUEST_CODE
                    )
                    
                    // Register listener to track installation
                    val installStateUpdatedListener = InstallStateUpdatedListener { installState ->
                        when (installState.installStatus()) {
                            InstallStatus.DOWNLOADING -> {
                                val bytesDownloaded = installState.bytesDownloaded()
                                val totalBytes = installState.totalBytesToDownload()
                                val progress = (bytesDownloaded * 100) / totalBytes
                                println("Update downloading: $progress%")
                            }
                            InstallStatus.DOWNLOADED -> {
                                // Update ready to install
                                Toast.makeText(
                                    this,
                                    "Update ready to install",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            InstallStatus.INSTALLING -> {
                                println("Update installing...")
                            }
                            InstallStatus.INSTALLED -> {
                                println("Update installed successfully")
                            }
                            else -> {}
                        }
                    }
                    updateManager.registerListener(installStateUpdatedListener)
                }
            },
            onNoUpdateAvailable = {
                println("No update available")
            },
            onError = { exception ->
                exception.printStackTrace()
                println("Error checking for updates: ${exception.message}")
            }
        )
    }

    /**
     * Handle activity result from update flow
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == InAppUpdateManager.IMMEDIATE_UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                // User denied immediate update - can retry or exit
                println("User denied immediate update")
            }
        } else if (requestCode == InAppUpdateManager.FLEXIBLE_UPDATE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // User accepted flexible update
                println("User accepted flexible update")
                // Installation will happen in background
                updateManager.completeFlexibleUpdate()
            } else {
                // User dismissed flexible update - can be re-prompted later
                println("User dismissed flexible update")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister update listener
        updateManager.unregisterListener()
    }

    private fun handleBackPress() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 2000) {
            // Double tap detected within 2 seconds - exit the app
            finish()
        } else {
            // First back press - show toast and update time
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
            backPressedTime = currentTime
        }
    }
}

@Composable
fun SpendRouteApp(
    viewModel: SRDashboardViewModel,
    userPreferences: UserPreferences,
    onBackPressed: () -> Unit = {}
) {
    // Screen state: "splash", "login", "dashboard"
    val currentScreen = remember { mutableStateOf("splash") }
    val isLoggedIn = remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }

    when (currentScreen.value) {
        "splash" -> {
            // Check if user is logged in with a name saved
            val savedName = userPreferences.getUserName().trim()
            val isReady = isLoggedIn.value && savedName.isNotEmpty()

            SplashScreen(
                isOnboardingCompleted = isReady,
                onSplashComplete = { completed ->
                    currentScreen.value = when {
                        !isLoggedIn.value -> "login"
                        else -> "dashboard"
                    }
                }
            )
        }
        "login" -> {
            LoginScreen(
                onLoginSuccess = {
                    isLoggedIn.value = true
                    currentScreen.value = "dashboard"
                },
                onSkipForNow = {
                    currentScreen.value = "dashboard"
                }
            )
        }
        "dashboard" -> {
            SRDashboard(
                viewModel = viewModel,
                onBackPressed = onBackPressed,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    isLoggedIn.value = false
                    viewModel.resetToHome()
                    viewModel.setTransactionToEdit(null)
                    viewModel.setInitialTransactionType(null)
                    currentScreen.value = "login"
                },
                onDeleteAccount = {
                    val auth = FirebaseAuth.getInstance()
                    val user = auth.currentUser
                    if (user != null) {
                        user.delete()
                            .addOnCompleteListener { deleteTask ->
                                if (!deleteTask.isSuccessful) {
                                    Log.w("MainActivity", "Account deletion failed; proceeding with logout", deleteTask.exception)
                                }
                                auth.signOut()
                                isLoggedIn.value = false
                                viewModel.resetToHome()
                                viewModel.setTransactionToEdit(null)
                                viewModel.setInitialTransactionType(null)
                                currentScreen.value = "login"
                            }
                    } else {
                        auth.signOut()
                        isLoggedIn.value = false
                        viewModel.resetToHome()
                        viewModel.setTransactionToEdit(null)
                        viewModel.setInitialTransactionType(null)
                        currentScreen.value = "login"
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SRDashboardPreview() {
    SpendRouteTheme {
        SRDashboard(viewModel = viewModel())
    }
}
