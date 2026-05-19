package com.parikiganesh.spendroute.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parikiganesh.spendroute.R
import com.parikiganesh.spendroute.ui.components.GreetingHeader
import com.parikiganesh.spendroute.ui.components.RecentTransactions
import com.parikiganesh.spendroute.ui.theme.LocalTypography
import com.parikiganesh.spendroute.ui.theme.SpendRouteTheme
import com.parikiganesh.spendroute.viewmodel.FilterType
import com.parikiganesh.spendroute.viewmodel.TransactionsViewModel
import com.parikiganesh.spendroute.data.model.Transaction
import com.parikiganesh.spendroute.data.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddEdit: (Transaction?, TransactionType?) -> Unit = { _, _ -> },
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val searchQuery = viewModel.searchQuery.collectAsState()
    val selectedFilter = viewModel.filterType.collectAsState()
    val filteredTransactions = viewModel.filteredTransactions.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FC))
    ) {
        // Fixed Purple Header - stays at top while scrolling
        GreetingHeader(
            title = stringResource(R.string.transactions),
            subtitle = stringResource(R.string.income_expenses),
            showAvatar = false
        )

        // Scrollable content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F7FC))
        ) {
            // Search Bar - Using Material 3 SearchBar
            item {
                SearchBar(
                    query = searchQuery.value,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    onSearch = {},
                    active = false,
                    onActiveChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_transactions),
                            style = LocalTypography.current.bodyMediumRegular,
                            color = Color.Gray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_transactions),
                            tint = Color.Gray
                        )
                    },
                    windowInsets = WindowInsets(0.dp)
                ) {}
            }

        // Filter Pills - Using FilterChip from Material 3
        item {
            val filterButtons = listOf(
                Pair(stringResource(R.string.filter_all), FilterType.ALL),
                Pair(stringResource(R.string.filter_income), FilterType.INCOME),
                Pair(stringResource(R.string.filter_expense), FilterType.EXPENSE)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterButtons.forEach { (label, filterType) ->
                    FilterChip(
                        selected = selectedFilter.value == filterType,
                        onClick = { viewModel.setFilterType(filterType) },
                        label = {
                            Text(
                                text = label,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFFF5F5F5),
                            selectedContainerColor = Color(0xFF5B4B9B),
                            selectedLabelColor = Color.White,
                            labelColor = Color(0xFF5B4B9B)
                        )
                    )
                }
            }
        }

        // Transaction List - Using RecentTransactions Component
        item {
            val transactionDeletedText = stringResource(R.string.transaction_deleted)
            
            if (filteredTransactions.value.isNotEmpty()) {
                RecentTransactions(
                    transactions = filteredTransactions.value,
                    onSeeAllClick = {},
                    showSeeAll = false,
                    onEditTransaction = { transaction ->
                        onNavigateToAddEdit(transaction, null)
                    },
                    onDeleteTransaction = { transactionId ->
                        viewModel.deleteTransaction(transactionId)
                        Toast.makeText(context, transactionDeletedText, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Empty State
        if (filteredTransactions.value.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_transactions_found),
                        style = LocalTypography.current.bodyMediumPrimary,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        }

        // Bottom spacing for navigation bar
        item {
            Box(modifier = Modifier.height(80.dp))
        }
    }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionsScreenPreview() {
    SpendRouteTheme {
        TransactionsScreen()
    }
}
