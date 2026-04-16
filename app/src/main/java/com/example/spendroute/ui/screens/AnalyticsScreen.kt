package com.example.spendroute.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spendroute.R
import com.example.spendroute.ui.components.GreetingHeader
import com.example.spendroute.ui.theme.LocalTypography
import com.example.spendroute.ui.theme.SpendRouteTheme
import com.example.spendroute.viewmodel.AnalyticsViewModel
import com.example.spendroute.viewmodel.factory.AnalyticsViewModelFactory
import java.util.Locale

@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModelFactory(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val analyticsData = viewModel.analyticsData.collectAsState()
    val selectedPeriod = viewModel.selectedPeriod.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FC))
    ) {
        // Fixed Purple Header
        GreetingHeader(
            title = stringResource(R.string.analytics),
            subtitle = stringResource(R.string.insights),
            showAvatar = false
        )

        // Scrollable content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F7FC))
        ) {
            // Period Selection Buttons
            item {
                PeriodToggleButtons(
                    selectedPeriod = selectedPeriod.value,
                    onPeriodSelected = { period ->
                        viewModel.selectPeriod(period)
                    }
                )
            }

            // Summary Cards Row
            item {
                SummaryCardsRow(
                    totalIncome = analyticsData.value.totalIncome,
                    totalExpense = analyticsData.value.totalExpense,
                    incomeChangePercent = analyticsData.value.incomeChangePercent,
                    expenseChangePercent = analyticsData.value.expenseChangePercent,
                    selectedPeriod = selectedPeriod.value
                )
            }

            // Income vs Expense Chart
            item {
                IncomeVsExpenseChart(
                    monthlyData = analyticsData.value.monthlyData
                )
            }

            // Expense by Category
            item {
                ExpenseByCategorySection(
                    categoryExpenses = analyticsData.value.categoryExpenses,
                    totalExpense = analyticsData.value.totalExpense
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PeriodToggleButtons(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(stringResource(R.string.monthly), stringResource(R.string.yearly)).forEach { period ->
            val isSelected = period == selectedPeriod
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onPeriodSelected(period) },
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (isSelected) Color(0xFF7C6FD4) else Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = period,
                    style = LocalTypography.current.bodySmallNormal,
                    color = if (isSelected) Color.White else Color(0xFF7C6FD4),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryCardsRow(
    totalIncome: Double,
    totalExpense: Double,
    incomeChangePercent: Float,
    expenseChangePercent: Float,
    selectedPeriod: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Income Card
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.total_income),
            amount = totalIncome,
            changePercent = incomeChangePercent,
            backgroundColor = Color(0xFFDEF3D8),
            textColor = Color(0xFF2D5C4F),
            selectedPeriod = selectedPeriod
        )

        // Expense Card
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.total_expense),
            amount = totalExpense,
            changePercent = expenseChangePercent,
            backgroundColor = Color(0xFFFDEDED),
            textColor = Color(0xFF5C3B4F),
            selectedPeriod = selectedPeriod
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Double,
    changePercent: Float,
    backgroundColor: Color,
    textColor: Color,
    selectedPeriod: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = LocalTypography.current.bodySmallNormal,
                color = textColor,
                fontSize = 12.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Rs. ${String.format(Locale.US, "%.0f", amount).replace(".0", "")}",
                    style = LocalTypography.current.bodyLargeSemibold,
                    color = textColor,
                    fontSize = 18.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val changeText = if (changePercent >= 0) {
                    "+${String.format(Locale.US, "%.0f", changePercent)}%"
                } else {
                    "${String.format(Locale.US, "%.0f", changePercent)}%"
                }
                val changeColor = if (changePercent >= 0) Color(0xFF4CAF50) else Color(0xFFE53935)
                val comparisonText = if (selectedPeriod.contains("Monthly", ignoreCase = true)) {
                    stringResource(R.string.vs_last_month)
                } else {
                    stringResource(R.string.vs_last_year)
                }

                Text(
                    text = changeText,
                    style = LocalTypography.current.bodySmallNormal,
                    color = changeColor,
                    fontSize = 10.sp
                )

                Text(
                    text = comparisonText,
                    style = LocalTypography.current.bodySmallNormal,
                    color = textColor,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun IncomeVsExpenseChart(
    monthlyData: List<com.example.spendroute.viewmodel.MonthlyChartData>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.income_vs_expense_title),
            style = LocalTypography.current.bodyLargeSemibold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = stringResource(R.string.last_6_months),
            style = LocalTypography.current.bodySmallNormal,
            color = Color(0xFF999999),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (monthlyData.isEmpty()) {
            Text(
                text = stringResource(R.string.no_data_available),
                style = LocalTypography.current.bodySmallNormal,
                color = Color(0xFF999999),
                modifier = Modifier.padding(vertical = 32.dp)
            )
            return
        }

        val maxValue = monthlyData.maxOf { maxOf(it.income, it.expense) }
        val scale = 200f / maxValue.coerceAtLeast(1.0).toFloat()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                monthlyData.forEach { monthData ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(200.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Income bar
                        Spacer(
                            modifier = Modifier
                                .width(7.dp)
                                .height((monthData.income * scale).dp)
                                .background(Color(0xFF4ECDC4), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )

                        // Expense bar
                        Spacer(
                            modifier = Modifier
                                .width(7.dp)
                                .height((monthData.expense * scale).dp)
                                .background(Color(0xFF7C6FD4), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                monthlyData.forEach { monthData ->
                    Text(
                        text = monthData.month,
                        style = LocalTypography.current.bodySmallNormal,
                        color = Color(0xFF999999),
                        fontSize = 10.sp,
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.padding(end = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Spacer(
                    modifier = Modifier
                        .width(12.dp)
                        .height(12.dp)
                        .background(Color(0xFF4ECDC4), RoundedCornerShape(2.dp))
                )
                Text(
                    text = stringResource(R.string.income_legend),
                    style = LocalTypography.current.bodySmallNormal,
                    color = Color.Black,
                    fontSize = 11.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Spacer(
                    modifier = Modifier
                        .width(12.dp)
                        .height(12.dp)
                        .background(Color(0xFF7C6FD4), RoundedCornerShape(2.dp))
                )
                Text(
                    text = stringResource(R.string.expense_legend),
                    style = LocalTypography.current.bodySmallNormal,
                    color = Color.Black,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ExpenseByCategorySection(
    categoryExpenses: List<com.example.spendroute.data.model.CategoryExpense>,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.expense_by_category),
            style = LocalTypography.current.bodyLargeSemibold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (categoryExpenses.isEmpty()) {
            Text(
                text = stringResource(R.string.no_expenses_yet),
                style = LocalTypography.current.bodySmallNormal,
                color = Color(0xFF999999),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            categoryExpenses.forEach { category ->
                ExpenseCategoryItem(
                    name = category.name,
                    amount = category.amount,
                    percentage = if (totalExpense > 0) (category.amount / totalExpense * 100).toFloat() else 0f
                )
            }
        }
    }
}

@Composable
private fun ExpenseCategoryItem(
    name: String,
    amount: Double,
    percentage: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = LocalTypography.current.bodySmallNormal,
                color = Color.Black,
                fontSize = 14.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rs. ${String.format(Locale.US, "%.0f", amount).replace(".0", "")}",
                    style = LocalTypography.current.bodySmallNormal,
                    color = Color.Black,
                    fontSize = 12.sp
                )

                Text(
                    text = "- ${String.format(Locale.US, "%.0f", percentage)}%",
                    style = LocalTypography.current.bodySmallNormal,
                    color = Color(0xFF999999),
                    fontSize = 12.sp
                )
            }
        }

        // Progress bar with fixed category color
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = getCategoryColor(name),
            trackColor = Color(0xFFE8E8E8)
        )
    }
}

@Composable
private fun getCategoryColor(categoryName: String): Color {
    return when (categoryName.lowercase()) {
        "food" -> Color(0xFFFF6B6B)        // Red
        "travel" -> Color(0xFF4ECDC4)       // Teal
        "entertainment" -> Color(0xFFFFD93D) // Yellow
        "shopping" -> Color(0xFF6BCB77)      // Green
        "bills" -> Color(0xFF4D96FF)         // Blue
        "healthcare" -> Color(0xFFFF6D9D)   // Pink
        "education" -> Color(0xFF9D7C6F)    // Brown
        "utilities" -> Color(0xFF7C6FD4)    // Purple
        else -> Color(0xFF999999)            // Gray default
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    SpendRouteTheme {
        AnalyticsScreen()
    }
}