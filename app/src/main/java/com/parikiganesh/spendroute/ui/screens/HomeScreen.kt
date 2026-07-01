package com.parikiganesh.spendroute.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.parikiganesh.spendroute.data.UserPreferences
import com.parikiganesh.spendroute.data.model.BalanceInfo
import com.parikiganesh.spendroute.data.model.Transaction
import com.parikiganesh.spendroute.data.model.TransactionType
import com.parikiganesh.spendroute.ui.components.BalanceCard
import com.parikiganesh.spendroute.ui.components.GreetingHeader
import com.parikiganesh.spendroute.ui.components.RecentTransactions
import com.parikiganesh.spendroute.ui.theme.SpendRouteTheme
import com.parikiganesh.spendroute.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAddEdit: (Transaction?, TransactionType?) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = hiltViewModel()
) {
    val balanceInfo = viewModel.balanceInfo.collectAsState()
    val recentTransactions = viewModel.recentTransactions.collectAsState()
    val userName = viewModel.userName.collectAsState()
    val uiMessage = viewModel.uiMessage.collectAsState()

    // Get user info from ViewModel or preferences for initials
    val context = LocalContext.current
    val userPreferences = UserPreferences(context)
    val userInitials = userPreferences.getUserInitials()
    
    // Refresh user name when screen is displayed (in case it was updated)
    LaunchedEffect(Unit) {
        viewModel.loadUserName()
    }

    LaunchedEffect(uiMessage.value) {
        uiMessage.value?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearUiMessage()
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FC))
    ) {
        // Fixed Greeting Header - stays at top while scrolling
        GreetingHeader(
            name = userName.value,
            initials = userInitials,
            avatarBackgroundColor = Color(0xFF7C6FD4),
            onAvatarClick = { onNavigateToProfile() }
        )

        // Scrollable content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F7FC))
        ) {
            // Balance Card - fetched from database
            item {
                if (balanceInfo.value != null) {
                    BalanceCard(
                        balanceInfo = balanceInfo.value!!,
                        onMonthSelected = { month ->
                            viewModel.selectMonth(month)
                        },
                        onIncomeClick = {
                            // Navigate to add income screen
                            onNavigateToAddEdit(null, TransactionType.INCOME)
                        },
                        onExpenseClick = {
                            // Navigate to add expense screen
                            onNavigateToAddEdit(null, TransactionType.EXPENSE)
                        }
                    )
                } else {
                    BalanceCard(
                        balanceInfo = BalanceInfo(
                            month = "",
                            totalBalance = 0.0,
                            income = 0.0,
                            expense = 0.0,
                            budget = 25000.0,
                            usedPercentage = 0f
                        ),
                        onMonthSelected = { month ->
                            viewModel.selectMonth(month)
                        },
                        onIncomeClick = {
                            // Navigate to add income screen
                            onNavigateToAddEdit(null, TransactionType.INCOME)
                        },
                        onExpenseClick = {
                            // Navigate to add expense screen
                            onNavigateToAddEdit(null, TransactionType.EXPENSE)
                        }
                    )
                }
            }

            // Recent Transactions
            if (recentTransactions.value.isNotEmpty()) {
                item {
                    RecentTransactions(
                        transactions = recentTransactions.value,
                        onSeeAllClick = {
                            onNavigateToTransactions()
                        },
                        onEditTransaction = { transaction ->
                            onNavigateToAddEdit(transaction, null)
                        },
                        onDeleteTransaction = { transactionId ->
                            viewModel.deleteTransaction(transactionId)
                        },
                        showActions = true
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SpendRouteTheme {
        HomeScreen()
    }
}
