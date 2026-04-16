package com.example.spendroute.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spendroute.R
import com.example.spendroute.ui.theme.LocalTypography
import com.example.spendroute.ui.theme.SpendRouteTheme
import com.example.spendroute.viewmodel.SplashViewModel
import com.example.spendroute.viewmodel.factory.SplashViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: (Boolean) -> Unit,
    isOnboardingCompleted: Boolean,
    viewModel: SplashViewModel = viewModel(
        factory = SplashViewModelFactory()
    )
) {
    val state = viewModel.state.collectAsState()
    val s = state.value
    
    val alphaAnim by animateFloatAsState(
        targetValue = s.animationAlpha,
        animationSpec = tween(durationMillis = 1000),
        label = "splash_alpha"
    )

    LaunchedEffect(key1 = isOnboardingCompleted) {
        println("DEBUG: SplashScreen LaunchedEffect, isOnboardingCompleted=$isOnboardingCompleted")
        viewModel.startAnimation()
        delay(2000)
        viewModel.completeSplash(isOnboardingCompleted)
        onSplashComplete(isOnboardingCompleted)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5B4B9B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alphaAnim),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App Logo/Icon
                Box(
                    modifier = Modifier
                        .size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_spendroute_icon_white),
                        contentDescription = stringResource(R.string.spendroute_logo_desc),
                        modifier = Modifier.size(80.dp)
                    )
                }
                
                // App Name
                Text(
                    text = stringResource(R.string.app_name),
                    style = LocalTypography.current.headingMediumSemibold,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
                
                // Tagline
                Text(
                    text = stringResource(R.string.app_tagline),
                    style = LocalTypography.current.bodyMediumRegular,
                    color = Color(0xFFB8B3E5)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Text
            Text(
                modifier = Modifier.padding(bottom = 52.dp),
                text = stringResource(R.string.app_features),
                style = LocalTypography.current.bodyMediumRegular,
                color = Color(0xFFB8B3E5)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SpendRouteTheme {
        SplashScreen(
            onSplashComplete = {},
            isOnboardingCompleted = false
        )
    }
}

