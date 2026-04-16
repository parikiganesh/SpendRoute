package com.parikiganesh.spendroute

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parikiganesh.spendroute.data.UserPreferences
import com.parikiganesh.spendroute.ui.screens.OnboardingScreen
import com.parikiganesh.spendroute.ui.screens.SRDashboard
import com.parikiganesh.spendroute.ui.screens.SplashScreen
import com.parikiganesh.spendroute.ui.theme.SpendRouteTheme
import com.parikiganesh.spendroute.viewmodel.SRDashboardViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SRDashboardViewModel by viewModels()
    private lateinit var userPreferences: UserPreferences
    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        userPreferences = UserPreferences(this)
        
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
    // Screen state: "splash", "onboarding", "dashboard"
    val currentScreen = remember { mutableStateOf("splash") }

    when (currentScreen.value) {
        "splash" -> {
            // Check if onboarding is completed
            val isOnboardingCompleted = userPreferences.isOnboardingCompleted() && userPreferences.getUserName().isNotEmpty()
            
            SplashScreen(
                isOnboardingCompleted = isOnboardingCompleted,
                onSplashComplete = { completed ->
                    currentScreen.value = if (completed) "dashboard" else "onboarding"
                }
            )
        }
        "onboarding" -> {
            OnboardingScreen(
                onContinueClick = { name ->
                    userPreferences.saveUserName(name)
                    userPreferences.setOnboardingCompleted(true)
                    viewModel.resetToHome()
                    currentScreen.value = "dashboard"
                }
            )
        }
        "dashboard" -> {
            SRDashboard(
                viewModel = viewModel,
                onBackPressed = onBackPressed,
                onNavigateToOnboarding = {
                    userPreferences.clearAllUserData()
                    userPreferences.setOnboardingCompleted(false)
                    currentScreen.value = "onboarding"
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
