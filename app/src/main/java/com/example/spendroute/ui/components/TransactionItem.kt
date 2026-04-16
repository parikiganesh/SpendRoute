//package com.example.spendroute.ui.components
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ShoppingCart
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import com.example.spendroute.data.model.Transaction
//import com.example.spendroute.ui.theme.LocalTypography
//import com.example.spendroute.ui.theme.SpendRouteTheme
//
//@Composable
//fun TransactionItem(
//    transaction: Transaction,
//    icon: ImageVector = Icons.Default.ShoppingCart,
//    iconBackgroundColor: Color = Color(0xFFFFF3E0),
//    modifier: Modifier = Modifier
//) {
//    Row(
//        modifier = modifier
//            .fillMaxWidth()
//            .background(
//                color = Color.White,
//                shape = RoundedCornerShape(12.dp)
//            )
//            .padding(12.dp),
//        horizontalArrangement = Arrangement.spacedBy(12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Icon
//        Box(
//            modifier = Modifier
//                .size(40.dp)
//                .background(
//                    color = iconBackgroundColor,
//                    shape = CircleShape
//                ),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                imageVector = icon,
//                contentDescription = transaction.category,
//                tint = Color(0xFFB8860B),
//                modifier = Modifier.size(20.dp)
//            )
//        }
//
//        // Title and Category
//        Column(
//            modifier = Modifier
//                .weight(1f),
//            verticalArrangement = Arrangement.spacedBy(4.dp)
//        ) {
//            Text(
//                text = transaction.title,
//                style = LocalTypography.current.bodyLargeSemibold,
//                color = Color(0xFF1C1B1F)
//            )
//            Text(
//                text = "${transaction.category} • ${transaction.date}",
//                style = LocalTypography.current.bodySmallNormal,
//                color = Color(0xFF9E9E9E)
//            )
//        }
//
//        // Amount
//        Text(
//            text = if (transaction.isIncome) "+Rs.${transaction.amount.toInt()}" else "-Rs.${transaction.amount.toInt()}",
//            style = LocalTypography.current.bodyLargeSemibold,
//            color = if (transaction.isIncome) Color(0xFF4CAF50) else Color(0xFFE53935)
//        )
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun TransactionItemPreview() {
//    SpendRouteTheme {
//        TransactionItem(
//            transaction = Transaction(
//                id = "1",
//                title = "Salary",
//                category = "Income",
//                amount = 50000.0,
//                date = "Today",
//                isIncome = true,
//                time = "10:00 AM"
//            ),
//            iconBackgroundColor = Color(0xFFE8F5E9)
//        )
//    }
//}
//
