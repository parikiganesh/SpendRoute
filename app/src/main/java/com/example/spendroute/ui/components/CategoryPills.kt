package com.example.spendroute.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spendroute.data.model.CategoryExpense
import com.example.spendroute.ui.theme.LocalTypography
import com.example.spendroute.ui.theme.SpendRouteTheme
import java.util.Locale

@Composable
fun CategoryPills(
    categories: List<CategoryExpense>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
    ) {
        items(categories) { category ->
            CategoryPill(
                name = category.name,
                amount = category.amount
            )
        }
    }
}

@Composable
private fun CategoryPill(
    name: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFFF5F1FF)
        )
    ) {
        Text(
            text = "$name Rs.${String.format(Locale.US, "%.1f", amount / 1000)}k",
            style = LocalTypography.current.bodyMediumPrimary,
            color = Color(0xFF5B4B9B),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryPillsPreview() {
    SpendRouteTheme {
        CategoryPills(
            categories = listOf(
                CategoryExpense("Food", 4200.0),
                CategoryExpense("Travel", 3100.0),
                CategoryExpense("Bills", 5800.0)
            )
        )
    }
}



