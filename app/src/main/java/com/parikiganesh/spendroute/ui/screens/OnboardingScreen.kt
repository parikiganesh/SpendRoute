package com.parikiganesh.spendroute.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.parikiganesh.spendroute.R
import com.parikiganesh.spendroute.ui.theme.LocalTypography
import com.parikiganesh.spendroute.ui.theme.SpendRouteTheme
import com.parikiganesh.spendroute.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    onContinueClick: (String) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val formState = viewModel.formState.collectAsState()
    val state = formState.value
    
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    
    // Detect if keyboard is visible
    val imeHeight = WindowInsets.ime.getBottom(density)
    val isKeyboardVisible = imeHeight > 0
    
    // Reset form when screen is displayed (in case it's being re-shown after data clear)
    LaunchedEffect(Unit) {
        viewModel.resetForm()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5B4B9B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (isKeyboardVisible) Arrangement.Top else Arrangement.Center
        ) {
            // Add top spacing when keyboard is visible
            if (isKeyboardVisible) {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Welcome Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_spendroute_icon_white),
                    contentDescription = stringResource(R.string.spendroute_logo_desc),
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Welcome Text
            Text(
                text = stringResource(R.string.welcome_to_spendroute),
                style = LocalTypography.current.headingSmallSemibold,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.track_smarter_spend_better),
                style = LocalTypography.current.bodyMediumRegular,
                color = Color(0xFFB8B3E5),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Name Input Label
            Text(
                text = stringResource(R.string.what_should_we_call_you),
                style = LocalTypography.current.bodyMediumPrimary,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Name Input Field
            TextField(
                value = state.name,
                onValueChange = { viewModel.updateName(it) },
                textStyle = LocalTypography.current.bodyMediumPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp)),
                placeholder = {
                    Text(
                        text = stringResource(R.string.enter_your_name),
                        style = LocalTypography.current.bodyMediumRegular,
                        color = Color(0xFFBDBDBD)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Error message
            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.errorMessage,
                    color = Color(0xFFFF6B6B),
                    style = LocalTypography.current.bodySmallNormal,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Continue Button
            Button(
                onClick = {
                    // Use the current state value directly
                    if (state.isNameValid && state.name.isNotEmpty()) {
                        onContinueClick(state.name)
                    } else {
                        viewModel.updateName(state.name) // Trigger error message
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color(0xFFB8B3E5)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = state.isNameValid
            ) {
                Text(
                    text = stringResource(R.string.continue_button),
                    style = LocalTypography.current.headingSmallSemibold,
                    color = Color(0xFF5B4B9B),
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Bottom spacing for keyboard
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    SpendRouteTheme {
        OnboardingScreen(
            onContinueClick = {}
        )
    }
}

