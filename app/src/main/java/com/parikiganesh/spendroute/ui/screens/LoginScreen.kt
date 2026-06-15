package com.parikiganesh.spendroute.ui.screens

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.parikiganesh.spendroute.R
import com.parikiganesh.spendroute.ui.theme.LocalTypography
import com.parikiganesh.spendroute.ui.theme.SpendRouteTheme
import com.parikiganesh.spendroute.viewmodel.LoginUiState
import com.parikiganesh.spendroute.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSkipForNow: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.resetForm()
    }

    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val showForgotPasswordDialog = remember { mutableStateOf(false) }
    val forgotPasswordEmail = remember { mutableStateOf("") }
    val isSendingReset = remember { mutableStateOf(false) }
    val resetSuccessMessage = remember { mutableStateOf("") }
    val isPreparingGoogleSignIn = remember { mutableStateOf(false) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isPreparingGoogleSignIn.value = false
        val data = result.data
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                Toast.makeText(context, "Google token unavailable", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.signInWithGoogleIdToken(
                    idToken = idToken,
                    googleDisplayName = account.displayName,
                    onSuccess = onLoginSuccess
                )
            }
        } catch (e: ApiException) {
            val message = when (e.statusCode) {
                CommonStatusCodes.NETWORK_ERROR -> "No internet connection"
                else -> "Google sign-in cancelled"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(context, "Google sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    LoginScreenContent(
        state = state,
        onSetRegisterMode = viewModel::setRegisterMode,
        onEmailChange = viewModel::updateEmail,
        onPasswordChange = viewModel::updatePassword,
        onNameChange = viewModel::updateName,
        onTermsCheckedChange = viewModel::setTermsAccepted,
        onOpenTerms = { openExternalLink(context, TERMS_URL) },
        onOpenPrivacy = { openExternalLink(context, PRIVACY_URL) },
        onPrimaryAction = { viewModel.signInOrRegister(onLoginSuccess) },
        onGoogleAction = {
            if (state.isLoading || isPreparingGoogleSignIn.value) {
                return@LoginScreenContent
            }

            if (!hasInternetConnection(context)) {
                Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                return@LoginScreenContent
            }

            val googleClient = buildGoogleSignInClient(context)
            if (googleClient == null) {
                Toast.makeText(context, "Google Sign-In not configured yet", Toast.LENGTH_LONG).show()
            } else {
                isPreparingGoogleSignIn.value = true
                // Wait for sign-out completion to avoid launching sign-in during a stale session transition.
                googleClient.signOut().addOnCompleteListener {
                    isPreparingGoogleSignIn.value = false
                    googleLauncher.launch(googleClient.signInIntent)
                }
            }
        },
        onForgotPasswordClick = { showForgotPasswordDialog.value = true },
        onSkipForNow = onSkipForNow
    )

    if (showForgotPasswordDialog.value) {
        ForgotPasswordDialog(
            email = forgotPasswordEmail.value,
            onEmailChange = { forgotPasswordEmail.value = it },
            isSending = isSendingReset.value,
            successMessage = resetSuccessMessage.value,
            onReset = {
                isSendingReset.value = true
                resetSuccessMessage.value = ""
                FirebaseAuth.getInstance().sendPasswordResetEmail(forgotPasswordEmail.value)
                    .addOnCompleteListener { task ->
                        isSendingReset.value = false
                        if (task.isSuccessful) {
                            resetSuccessMessage.value = "Password reset email sent to ${forgotPasswordEmail.value}. Check your spam folder if you don't see it."
                        } else {
                            Toast.makeText(
                                context,
                                "Error: ${task.exception?.message ?: "Unable to send reset email"}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            },
            onDismiss = {
                showForgotPasswordDialog.value = false
                forgotPasswordEmail.value = ""
                resetSuccessMessage.value = ""
            }
        )
    }
}

@Composable
private fun LoginScreenContent(
    state: LoginUiState,
    onSetRegisterMode: (Boolean) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNameChange: (String) -> Unit = {},
    onTermsCheckedChange: (Boolean) -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onPrimaryAction: () -> Unit,
    onGoogleAction: () -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    onSkipForNow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5B4B9B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 56.dp, start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_spendroute_icon_white),
                contentDescription = "SpendRoute icon",
                modifier = Modifier
                    .size(64.dp)
                    .align(androidx.compose.ui.Alignment.CenterHorizontally)
            )
            Text(
                text = "SpendRoute",
                style = LocalTypography.current.headingSmallSemibold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Track smarter, spend better",
                style = LocalTypography.current.bodyMediumRegular,
                color = Color(0xFFD5D0FF),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 20.dp),
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(containerColor = Color(0xFFF5F5FA))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 18.dp)
                    .navigationBarsPadding(),
                 verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE9E8F2), RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onSetRegisterMode(true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isRegisterMode) Color(0xFF5B4B9B) else Color.Transparent,
                            contentColor = if (state.isRegisterMode) Color.White else Color(0xFF7A7A86)
                        ),
                        elevation = null,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Create account",
                            style = LocalTypography.current.bodyLargeSemibold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    }
                    Button(
                        onClick = { onSetRegisterMode(false) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!state.isRegisterMode) Color(0xFF5B4B9B) else Color.Transparent,
                            contentColor = if (!state.isRegisterMode) Color.White else Color(0xFF7A7A86)
                        ),
                        elevation = null,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Log in",
                            style = LocalTypography.current.bodyLargeSemibold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    }
                }

                // Full name group — only show in Create Account mode
                if (state.isRegisterMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Full Name", style = LocalTypography.current.bodyLargeSemibold)
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = onNameChange,
                            placeholder = { Text("Enter your name") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFEFEEF5),
                                unfocusedContainerColor = Color(0xFFEFEEF5),
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color(0xFFCFCBE7)
                            )
                        )
                    }
                }

                // Email group — label tight to field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Email", style = LocalTypography.current.bodyLargeSemibold)
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = onEmailChange,
                        placeholder = { Text("you@example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFEFEEF5),
                            unfocusedContainerColor = Color(0xFFEFEEF5),
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color(0xFFCFCBE7)
                        )
                    )
                }

                // Password group — label tight to field, forgot link tight below
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Password", style = LocalTypography.current.bodyLargeSemibold)
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        placeholder = { Text(if (state.isRegisterMode) "At least 6 characters" else "Enter your password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFEFEEF5),
                            unfocusedContainerColor = Color(0xFFEFEEF5),
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color(0xFFCFCBE7)
                        )
                    )

                    // Forgot Password link - only show in Log in mode
                    if (!state.isRegisterMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Forgot password?",
                                color = Color(0xFF5B4B9B),
                                style = LocalTypography.current.bodyLargeSemibold,
                                modifier = Modifier.clickable { onForgotPasswordClick() }
                            )
                        }
                    }
                }

                if (!state.errorMessage.isNullOrEmpty()) {
                    Text(
                        text = state.errorMessage.orEmpty(),
                        color = Color(0xFFE53935),
                        style = LocalTypography.current.bodySmallMediumSemibold
                    )
                }

                if (state.isRegisterMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = state.hasAcceptedTerms,
                            onCheckedChange = onTermsCheckedChange
                        )
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                        ) {
                            Text(
                                text = "I accept ",
                                style = LocalTypography.current.bodySmallNormal,
                                color = Color(0xFF6E7485)
                            )
                            Text(
                                text = "T&C",
                                style = LocalTypography.current.bodySmallNormal.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF5B4B9B),
                                modifier = Modifier.clickable { onOpenTerms() }
                            )
                            Text(
                                text = " and ",
                                style = LocalTypography.current.bodySmallNormal,
                                color = Color(0xFF6E7485)
                            )
                            Text(
                                text = "Privacy Policy",
                                style = LocalTypography.current.bodySmallNormal.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF5B4B9B),
                                modifier = Modifier.clickable { onOpenPrivacy() }
                            )
                        }
                    }
                }

                Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B4B9B)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (state.isRegisterMode) "Create account" else "Log in", style = LocalTypography.current.bodyLargeSemibold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFDBD7EA))
                    Text("OR", color = Color(0xFF9D99AD), style = LocalTypography.current.bodySmallNormal)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFDBD7EA))
                }

                Button(
                    onClick = onGoogleAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(1.dp, Color(0xFFD8D4EA), RoundedCornerShape(14.dp)),
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5FA), contentColor = Color(0xFF2C2C30))
                ) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.google_icon),
                            contentDescription = "Google icon",
                            modifier = Modifier
                                .size(38.dp)
                                .padding(end = 10.dp)
                        )
                        Text("Continue with Google", style = LocalTypography.current.bodyLargeSemibold)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Skip for now - ",
                        color = Color(0xFF8E8E99),
                        style = LocalTypography.current.bodyMediumRegular
                    )
                    Text(
                        text = "continue without account",
                        color = Color(0xFF5B4B9B),
                        style = LocalTypography.current.bodyLargeSemibold,
                        modifier = Modifier.clickable { onSkipForNow() }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun LoginScreenPreview() {
    SpendRouteTheme {
        LoginScreenContent(
            state = LoginUiState(
                isRegisterMode = true,
                email = "",
                password = "",
                name = ""
            ),
            onSetRegisterMode = {},
            onEmailChange = {},
            onPasswordChange = {},
            onNameChange = {},
            onTermsCheckedChange = {},
            onOpenTerms = {},
            onOpenPrivacy = {},
            onPrimaryAction = {},
            onGoogleAction = {},
            onForgotPasswordClick = {},
            onSkipForNow = {}
        )
    }
}

private fun openExternalLink(context: Context, url: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
    }
}

private const val TERMS_URL = "https://sites.google.com/view/spendroute-tc-privacy-policy"
private const val PRIVACY_URL = "https://sites.google.com/view/spendroute-tc-privacy-policy"

@Composable
private fun ForgotPasswordDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    isSending: Boolean,
    successMessage: String = "",
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reset Password",
                style = LocalTypography.current.headingSmallSemibold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Enter your email address and we'll send you instructions to reset your password.",
                    style = LocalTypography.current.bodyMediumRegular,
                    color = Color(0xFF6E7485)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    placeholder = { Text("you@example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5FA),
                        unfocusedContainerColor = Color(0xFFF5F5FA),
                        focusedBorderColor = Color(0xFFCFCBE7),
                        unfocusedBorderColor = Color(0xFFCFCBE7)
                    )
                )

                // Show success message below the text field
                if (successMessage.isNotEmpty()) {
                    Text(
                        text = successMessage,
                        style = LocalTypography.current.bodySmallMediumSemibold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onReset,
                enabled = email.isNotEmpty() && !isSending && successMessage.isEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B4B9B))
            ) {
                Text(if (isSending) "Sending..." else "Send Reset Link")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                enabled = !isSending,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5FA), contentColor = Color(0xFF5B4B9B))
            ) {
                Text("Cancel")
            }
        }
    )
}

private fun buildGoogleSignInClient(context: Context): GoogleSignInClient? = run {
    val webClientIdRes = context.resources.getIdentifier(
        "default_web_client_id",
        "string",
        context.packageName
    )
    if (webClientIdRes == 0) {
        null
    } else {
        val webClientId = context.getString(webClientIdRes)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(webClientId)
            .build()
        GoogleSignIn.getClient(context, gso)
    }
}

private fun hasInternetConnection(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

