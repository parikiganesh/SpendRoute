package com.example.spendroute.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendroute.data.model.BalanceInfo
import com.example.spendroute.ui.theme.LocalTypography
import com.example.spendroute.ui.theme.SpendRouteTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Helper function to get current month
private fun getCurrentMonth(): String {
    val dateFormat = SimpleDateFormat("MMMM", Locale.getDefault())
    return dateFormat.format(Date()).uppercase()
}

// Helper function to convert month to title case (first letter uppercase, rest lowercase)
private fun formatMonthTitleCase(month: String): String {
    return month.take(1).uppercase() + month.drop(1).lowercase()
}

// Month list
private val MONTHS = listOf(
    "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
    "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
)

@Composable
fun BalanceCard(
    balanceInfo: BalanceInfo,
    modifier: Modifier = Modifier,
    onMonthSelected: (String) -> Unit = {}  // Callback when month is selected
) {
    // Use current month if not provided
    val initialMonth = if (balanceInfo.month.isEmpty()) getCurrentMonth() else balanceInfo.month
    val selectedMonthState = remember { mutableStateOf(initialMonth) }
    val showMonthPicker = remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF7C6FD4)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: NET BALANCE with clickable "Select month" text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Net Balance — ${formatMonthTitleCase(selectedMonthState.value)}",
                    style = LocalTypography.current.bodySmallNormal,
                    color = Color.White
                )
                
                Text(
                    text = "Select Month",
                    style = LocalTypography.current.bodySmallNormal,
                    color = Color.White,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        showMonthPicker.value = !showMonthPicker.value
                    }
                )
            }
            
            // Month Picker - Horizontal Scrollable (Inline in Card)
            if (showMonthPicker.value) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MONTHS.forEach { month ->
                        val isSelected = month == selectedMonthState.value
                        Card(
                            modifier = Modifier
                                .clickable {
                                    selectedMonthState.value = month
                                    onMonthSelected(month)
                                    showMonthPicker.value = false
                                }
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFB8B3E5) else Color(0xFF4A3A7A)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = month.take(3),  // Show abbreviated month (JAN, FEB, etc.)
                                style = LocalTypography.current.bodySmallNormal,
                                color = if (isSelected) Color(0xFF5B4B9B) else Color(0xFFB8B3E5),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Total Balance Amount
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Rs. ${String.format(Locale.US, "%.0f", balanceInfo.totalBalance).replace(".0", "")}",
                    style = LocalTypography.current.bodyExtraLargeText,
                    color = Color.White,
                    fontSize = 40.sp
                )
            }

            // Income and Expense Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2D5C4F)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Income",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.padding(4.dp)
                                    .background(Color.White, shape = CircleShape)
                            )
                            Text(
                                text = "Income",
                                style = LocalTypography.current.bodySmallNormal,
                                color = Color(0xFFB8E6C9)
                            )
                        }
                        Text(
                            text = "Rs. ${String.format(Locale.US, "%.0f", balanceInfo.income).replace(".0", "")}",
                            style = LocalTypography.current.bodyLargeSemibold,
                            color = Color.White
                        )
                    }
                }

                // Expense Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF5C3B4F)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Expense",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.padding(4.dp)
                                    .background(Color.White, shape = CircleShape)
                            )
                            Text(
                                text = "Expense",
                                style = LocalTypography.current.bodySmallNormal,
                                color = Color(0xFFE8B4C8)
                            )
                        }
                        Text(
                            text = "Rs. ${String.format(Locale.US, "%.0f", balanceInfo.expense).replace(".0", "")}",
                            style = LocalTypography.current.bodyLargeSemibold,
                            color = Color.White
                        )
                    }
                }
            }

            // Budget Section
//            Column(
//                modifier = Modifier.fillMaxWidth(),
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        text = "Budget Rs. ${String.format(Locale.US, "%.0f", balanceInfo.budget).replace(".0", "")}",
//                        style = LocalTypography.current.bodySmallNormal,
//                        color = Color(0xFFB8B3E5)
//                    )
//                    Text(
//                        text = "${balanceInfo.usedPercentage.toInt()}% used",
//                        style = LocalTypography.current.bodySmallNormal,
//                        color = Color(0xFFB8B3E5)
//                    )
//                }
//
//                LinearProgressIndicator(
//                    progress = { balanceInfo.usedPercentage / 100f },
//                    modifier = Modifier.fillMaxWidth(),
//                    color = Color(0xFF4DB8A8),
//                    trackColor = Color(0xFFF8F7FC)
//                )
//            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BalanceCardPreview() {
    SpendRouteTheme {
        BalanceCard(
            balanceInfo = BalanceInfo(
                month = "",  // Empty - will use current month dynamically
                totalBalance = 31550.0,
                income = 50000.0,
                expense = 18450.0,
                budget = 25000.0,
                usedPercentage = 73f
            )
        )
    }
}






