package com.parikiganesh.spendroute.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parikiganesh.spendroute.R
import com.parikiganesh.spendroute.navigation.NavigationGraph
import com.parikiganesh.spendroute.navigation.SRNavigation
import com.parikiganesh.spendroute.ui.theme.LocalTypography
import com.parikiganesh.spendroute.ui.theme.SpendRouteTheme
import com.parikiganesh.spendroute.utils.PermissionUtils
import com.parikiganesh.spendroute.viewmodel.SRDashboardViewModel
import java.util.jar.Manifest


@Composable
fun SRDashboard(
    viewModel: SRDashboardViewModel,
    onBackPressed: () -> Unit = {},
    onLogout: () -> Unit = {},
    onDeleteAccount: () -> Unit = {}
) {
    val currentRoute = viewModel.currentRoute.collectAsState()
    val transactionToEdit = viewModel.transactionToEdit.collectAsState()
    val initialTransactionType = viewModel.initialTransactionType.collectAsState()
    val context = LocalContext.current
    
    // Permission launcher for notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _: Boolean ->
        // Permission result handled - no action needed here
        // The app will continue to work whether permission is granted or not
    }
    
    // Request notification permission when dashboard loads
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionUtils.hasNotificationPermission(context)) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    // Handle back button
    BackHandler {
        when (currentRoute.value) {
            SRNavigation.Home.route -> {
                // If already on home, trigger the exit logic (double tap check)
                onBackPressed()
            }
            else -> {
                // Navigate back to home from any other screen
                viewModel.navigateTo(SRNavigation.Home.route)
            }
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationGraph.navigationItems.forEachIndexed { index, item ->
                    if (index != 2) {
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = stringResource(item.labelResId)) },
                            label = { Text(stringResource(item.labelResId), style = LocalTypography.current.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            selected = currentRoute.value == item.route.route,
                            onClick = { viewModel.navigateTo(item.route.route) }
                        )
                    } else {
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = stringResource(item.labelResId)) },
                            label = { Text(stringResource(item.labelResId), style = LocalTypography.current.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            selected = currentRoute.value == item.route.route,
                            onClick = { 
                                // Clear any previous edit transaction when clicking Add button for fresh add
                                viewModel.setTransactionToEdit(null)
                                viewModel.navigateTo(item.route.route)
                            },
                            enabled = true
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRoute.value) {
                SRNavigation.Home.route -> HomeScreen(
                    onNavigateToTransactions = {
                        viewModel.navigateTo(SRNavigation.Transactions.route)
                    },
                    onNavigateToProfile = {
                        viewModel.navigateTo(SRNavigation.Profile.route)
                    },
                    onNavigateToAddEdit = { transaction, transactionType ->
                        viewModel.setTransactionToEdit(transaction)
                        viewModel.setInitialTransactionType(transactionType)
                        viewModel.navigateTo(SRNavigation.AddExpense.route)
                    }
                )
                SRNavigation.Transactions.route -> TransactionsScreen(
                    onNavigateToAddEdit = { transaction, transactionType ->
                        viewModel.setTransactionToEdit(transaction)
                        viewModel.setInitialTransactionType(transactionType)
                        viewModel.navigateTo(SRNavigation.AddExpense.route)
                    }
                )
                SRNavigation.AddExpense.route -> AddTransactionScreen(
                    transactionToEdit = transactionToEdit.value,
                    initialTransactionType = initialTransactionType.value,
                    onNavigateToHome = {
                        viewModel.navigateTo(SRNavigation.Home.route)
                    },
                    onClearEdit = {
                        viewModel.setTransactionToEdit(null)
                        viewModel.setInitialTransactionType(null)
                    }
                )
                SRNavigation.Analytics.route -> AnalyticsScreen()
                SRNavigation.Profile.route -> ProfileScreen(
                    onLogout = onLogout,
                    onDeleteAccount = onDeleteAccount
                )
                else -> HomeScreen()
            }
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

