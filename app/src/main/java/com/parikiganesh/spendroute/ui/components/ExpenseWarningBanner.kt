package com.parikiganesh.spendroute.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parikiganesh.spendroute.ui.theme.LocalTypography

/**
 * Popup dialog shown once when expenses exceed income
 * User can dismiss it
 */
@Composable
fun ExpenseWarningDialog(
    monthlyIncome: Double,
    monthlyExpense: Double,
    onDismiss: () -> Unit
) {
    val overspend = monthlyExpense - monthlyIncome
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFC62828),  // Dark red
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    "Overspending Detected",
                    style = LocalTypography.current.headingSmallSemibold,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828)
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Message
                Text(
                    "Your total expenses have exceeded your income for this period.",
                    style = LocalTypography.current.bodyMediumNormalStyle,
                    color = Color(0xFF5F5F5F),
                    lineHeight = 20.sp
                )

                Text(
                    "Tip: Review your spending habits.",
                    style = LocalTypography.current.labelMediumSemibold,
                    color = Color(0xFF5F5F5F),
                    lineHeight = 20.sp
                )
                
                // Details Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Monthly Income",
                            style = LocalTypography.current.bodySmallNormal,
                            color = Color(0xFF7F7F7F)
                        )
                        Text(
                            "₹${monthlyIncome.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)  // Green
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Monthly Expense",
                            style = LocalTypography.current.bodySmallNormal,
                            color = Color(0xFF7F7F7F)
                        )
                        Text(
                            "₹${monthlyExpense.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)  // Red
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B4B9B)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("I Understand",
                    color = Color.White,
                    style = LocalTypography.current.labelMediumSemibold
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun ExpenseWarningDialogPreview() {
    ExpenseWarningDialog(
        monthlyIncome = 50000.0,
        monthlyExpense = 65000.0,
        onDismiss = {}
    )
}



