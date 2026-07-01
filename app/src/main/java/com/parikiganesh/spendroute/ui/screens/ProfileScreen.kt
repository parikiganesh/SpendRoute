package com.parikiganesh.spendroute.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.parikiganesh.spendroute.R
import com.parikiganesh.spendroute.ui.components.GreetingHeader
import com.parikiganesh.spendroute.ui.theme.LocalTypography
import com.parikiganesh.spendroute.ui.theme.SpendRouteTheme
import com.parikiganesh.spendroute.utils.PermissionUtils
import com.parikiganesh.spendroute.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state = viewModel.state.collectAsState()
    val s = state.value
    
    // Pre-compute stringResource values to avoid Composable context errors
    val notificationsEnabledText = stringResource(R.string.notifications_enabled)
    val notificationPermissionDeniedText = stringResource(R.string.notification_permission_denied)
    val notificationsDisabledText = stringResource(R.string.notifications_disabled)
    val exportingCsvText = stringResource(R.string.exporting_csv)
    val exportingPdfText = stringResource(R.string.exporting_pdf)
    val dataRestoredText = stringResource(R.string.data_restored)
    
    // Refresh user info when screen is displayed (in case it was updated)
    LaunchedEffect(Unit) {
        viewModel.loadUserInfo()
        
        // Sync notification toggle state with actual Android permission status
        val hasPermission = PermissionUtils.hasNotificationPermission(context)
        if (hasPermission && !s.notificationsEnabled) {
            // Permission is granted but toggle is off - turn it on
            viewModel.enableNotifications()
        } else if (!hasPermission && s.notificationsEnabled) {
            // Permission is denied but toggle is on - turn it off
            viewModel.disableNotifications()
        }
    }
    
    // Permission launcher for notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        @Suppress("InlinedApi")
        val notificationPermissionGranted = permissions[android.Manifest.permission.POST_NOTIFICATIONS] ?: false
        if (notificationPermissionGranted) {
            viewModel.enableNotifications()
            Toast.makeText(context, notificationsEnabledText, Toast.LENGTH_SHORT).show()
        } else {
            viewModel.disableNotifications()
            Toast.makeText(context, notificationPermissionDeniedText, Toast.LENGTH_SHORT).show()
        }
    }
    
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error toast if export fails
    LaunchedEffect(s.errorMessage) {
        if (!s.errorMessage.isNullOrEmpty()) {
            Toast.makeText(context, s.errorMessage, Toast.LENGTH_LONG).show()
            // Clear error after showing
            viewModel.clearError()
        }
    }

    LaunchedEffect(s.contactSubmitMessage) {
        if (!s.contactSubmitMessage.isNullOrEmpty()) {
            Toast.makeText(context, s.contactSubmitMessage, Toast.LENGTH_LONG).show()
            viewModel.clearContactSubmitMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F7FC))
        ) {
            // Fixed Header
            GreetingHeader(
                name = stringResource(R.string.tracking_since, s.accountCreatedDate),
                initials = s.userInitials,
                avatarBackgroundColor = Color(0xFF7C6FD4),
                showAvatar = true,
                greeting = s.userName,
                boldGreeting = true
            )

            // Scrollable content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F7FC))
            ) {
                // PREFERENCES Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.preferences).uppercase(),
                            style = LocalTypography.current.bodySmallNormal,
                            color = Color(0xFF9E9E9E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Notifications Toggle
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = stringResource(R.string.notifications),
                                            style = LocalTypography.current.bodyMediumPrimary,
                                            color = Color(0xFF1C1B1F)
                                        )
                                        Text(
                                            text = stringResource(R.string.notifications_desc),
                                            style = LocalTypography.current.bodySmallNormal,
                                            color = Color(0xFF9E9E9E),
                                            fontSize = 12.sp
                                        )
                                    }
                                    Switch(
                                        checked = s.notificationsEnabled,
                                        onCheckedChange = { newValue ->
                                            if (newValue) {
                                                if (!PermissionUtils.hasNotificationPermission(context)) {
                                                    permissionLauncher.launch(PermissionUtils.getNotificationPermissions())
                                                } else {
                                                    viewModel.enableNotifications()
                                                    Toast.makeText(context, notificationsEnabledText, Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                viewModel.disableNotifications()
                                                Toast.makeText(context, notificationsDisabledText, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF5B4B9B),
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = Color(0xFFBDBDBD)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // DATA Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.data).uppercase(),
                            style = LocalTypography.current.bodySmallNormal,
                            color = Color(0xFF9E9E9E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Export Data
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.showExportDialog() }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.export_data),
                                        style = LocalTypography.current.bodyMediumPrimary,
                                        color = Color(0xFF1C1B1F)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = stringResource(R.string.export_icon_desc),
                                        tint = Color.Black
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFFF0F0F0))
                                )

                                // Clear All Data
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.showClearDialog() }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.clear_all_data),
                                        style = LocalTypography.current.bodyMediumPrimary,
                                        color = Color.Black
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = stringResource(R.string.clear_icon_desc),
                                        tint = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                // ABOUT Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.about).uppercase(),
                            style = LocalTypography.current.bodySmallNormal,
                            color = Color(0xFF9E9E9E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // App Version
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.app_version),
                                        style = LocalTypography.current.bodyMediumPrimary,
                                        color = Color(0xFF1C1B1F)
                                    )
                                    Text(
                                        text = s.appVersion,
                                        style = LocalTypography.current.bodySmallNormal,
                                        color = Color(0xFF9E9E9E),
                                        fontSize = 12.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFFF0F0F0))
                                )

                                // Privacy Policy
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.showPrivacyDialog() }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.privacy_policy),
                                        style = LocalTypography.current.bodyMediumPrimary,
                                        color = Color(0xFF1C1B1F)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = stringResource(R.string.privacy_icon_desc),
                                        tint = Color.Black
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFFF0F0F0))
                                )

                                // Contact Us
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.showContactDialog() }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.contact_us),
                                        style = LocalTypography.current.bodyMediumPrimary,
                                        color = Color(0xFF1C1B1F)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = stringResource(R.string.contact_us),
                                        tint = Color.Black
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFFF0F0F0))
                                )

                                // Logout
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onLogout() }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.logout),
                                        style = LocalTypography.current.bodyMediumPrimary,
                                        color = Color(0xFFE53935)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = stringResource(R.string.logout),
                                        tint = Color(0xFFE53935)
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom spacing
                item {
                    Box(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        )
    }

    // Export Dialog
    if (s.showExportDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideExportDialog() },
            title = {
                Text(
                    text = stringResource(R.string.export_title),
                    style = LocalTypography.current.headingSmallSemibold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.export_message),
                    style = LocalTypography.current.bodyMediumRegular,
                    color = Color.Black
                )
            },
            confirmButton = {
                Button(
                    onClick = { 
                        Toast.makeText(context, exportingCsvText, Toast.LENGTH_SHORT).show()
                        viewModel.exportAsCSV()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B4B9B),
                        contentColor = Color.White
                    ),
                    enabled = !s.isExporting
                ) {
                    Text(stringResource(R.string.csv))
                }
            },
            dismissButton = {
                Button(
                    onClick = { 
                        Toast.makeText(context, exportingPdfText, Toast.LENGTH_SHORT).show()
                        viewModel.exportAsPDF()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B4B9B),
                        contentColor = Color.White
                    ),
                    enabled = !s.isExporting
                ) {
                    Text(stringResource(R.string.pdf))
                }
            }
        )
    }

    // Clear Data Confirmation Dialog
    if (s.showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideClearDialog() },
            title = {
                Text(
                    text = stringResource(R.string.clear_data_title),
                    style = LocalTypography.current.headingSmallSemibold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.clear_data_message),
                    style = LocalTypography.current.bodyMediumRegular,
                    color = Color.Black
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.hideClearDialog() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color(0xFF5B4B9B)
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete Snackbar with Undo
    if (s.showDeleteSnackbar) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 10.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                containerColor = Color(0xFF323232),
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.deleting_data),
                            style = LocalTypography.current.bodyMediumPrimary,
                            color = Color.White
                        )
                        Text(
                            text = "${s.deleteCountdown}s",
                            style = LocalTypography.current.bodySmallNormal,
                            color = Color(0xFFB0B0B0),
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.undoClearData()
                            Toast.makeText(context, dataRestoredText, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5B4B9B),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.undo),
                            style = LocalTypography.current.bodySmallNormal,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Privacy Dialog
    if (s.showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hidePrivacyDialog() },
            title = {
                Text(
                    text = stringResource(R.string.privacy_policy),
                    style = LocalTypography.current.headingSmallSemibold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.privacy_content),
                    style = LocalTypography.current.bodyMediumRegular,
                    color = Color(0xFF1C1B1F)
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.hidePrivacyDialog() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B4B9B),
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    if (s.showContactDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideContactDialog() },
            title = {
                Text(
                    text = stringResource(R.string.contact_us),
                    style = LocalTypography.current.headingSmallSemibold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.contact_us_message),
                        style = LocalTypography.current.bodySmallNormal,
                        color = Color(0xFF5F5F5F)
                    )
                    OutlinedTextField(
                        value = s.contactName,
                        onValueChange = viewModel::updateContactName,
                        label = { Text(stringResource(R.string.name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = s.contactEmail,
                        onValueChange = viewModel::updateContactEmail,
                        label = { Text(stringResource(R.string.email)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = s.contactIssue,
                        onValueChange = viewModel::updateContactIssue,
                        label = { Text(stringResource(R.string.brief_issue)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.submitContactRequest() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B4B9B),
                        contentColor = Color.White
                    ),
                    enabled = !s.isSubmittingContact
                ) {
                    Text(
                        if (s.isSubmittingContact) {
                            stringResource(R.string.submitting)
                        } else {
                            stringResource(R.string.submit)
                        }
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.hideContactDialog() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color(0xFF5B4B9B)
                    ),
                    enabled = !s.isSubmittingContact
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Permanently delete cloud account when clear-data countdown completes without undo
    LaunchedEffect(s.countdownCompleted) {
        if (s.countdownCompleted) {
            onDeleteAccount()
            // Reset the flag to prevent repeated triggers
            viewModel.resetCountdownCompleted()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    SpendRouteTheme {
        ProfileScreen()
    }
}
