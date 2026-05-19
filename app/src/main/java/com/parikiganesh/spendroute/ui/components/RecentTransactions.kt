package com.parikiganesh.spendroute.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parikiganesh.spendroute.data.model.Transaction
import com.parikiganesh.spendroute.data.model.CategoryConstants
import com.parikiganesh.spendroute.data.UserPreferences
import com.parikiganesh.spendroute.ui.theme.LocalTypography
import com.parikiganesh.spendroute.ui.theme.SpendRouteTheme
import com.parikiganesh.spendroute.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RecentTransactions(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    onSeeAllClick: () -> Unit = {},
    onEditTransaction: (Transaction) -> Unit = {},
    onDeleteTransaction: (String) -> Unit = {},
    showActions: Boolean = true,
    showSeeAll: Boolean = true
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val hintTargetIndex = remember { mutableIntStateOf(-1) }

    // Check if user has seen gesture hint on first composition
    // Also reset the hint if the list becomes empty, so it shows again when the next "first" txn is added
    LaunchedEffect(transactions) {
        if (transactions.isEmpty()) {
            userPreferences.setGestureHintSeen(false)
        } else if (!userPreferences.hasSeenGestureHint()) {
            // Animate the first transaction card as a hint
            hintTargetIndex.intValue = 0
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.recent),
                style = LocalTypography.current.titleLargeSemibold,
                color = Color(0xFF1C1B1F)
            )
            if (showSeeAll) {
                Text(
                    text = stringResource(id = R.string.see_all),
                    style = LocalTypography.current.bodyMediumPrimary,
                    color = Color(0xFF5B4B9B),
                    modifier = Modifier.clickable { onSeeAllClick() }
                )
            }
        }

        // Transactions List
        transactions.forEachIndexed { index, transaction ->
            SwipeableTransactionCard(
                transaction = transaction,
                onEdit = { onEditTransaction(transaction) },
                onDelete = { onDeleteTransaction(transaction.id) },
                showActions = showActions,
                shouldAnimateHint = index == hintTargetIndex.intValue,
                onHintAnimationComplete = {
                    userPreferences.setGestureHintSeen()
                    hintTargetIndex.intValue = -1
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTransactionCard(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    showActions: Boolean = true,
    shouldAnimateHint: Boolean = false,
    onHintAnimationComplete: () -> Unit = {}
) {
    val swipeOffset = remember { Animatable(0f) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showDetailSheet = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val cornerRadius = 15.dp
    val scope = rememberCoroutineScope()

    // Hint animation for first-time users
    LaunchedEffect(shouldAnimateHint) {
        if (shouldAnimateHint) {
            delay(1200) // Wait for screen to settle
            swipeOffset.animateTo(
                targetValue = -120f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
            delay(1000)
            swipeOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
            onHintAnimationComplete()
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = {
                Text(
                    text = stringResource(id = R.string.delete_transaction_title),
                    style = LocalTypography.current.bodyLargeSemibold
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.delete_transaction_message),
                    style = LocalTypography.current.bodyMediumRegular
                )
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog.value = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF5B4B9B)
                    )
                ) {
                    Text(stringResource(id = R.string.cancel))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog.value = false
                        onDelete()
                        scope.launch { swipeOffset.snapTo(0f) }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(id = R.string.delete))
                }
            }
        )
    }
    
    // Detail Bottom Sheet
    if (showDetailSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet.value = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = stringResource(R.string.transaction_details),
                    style = LocalTypography.current.titleLargeSemibold,
                    color = Color(0xFF1C1B1F)
                )
                
                // Category and Icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFB8B3E5), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryConstants.getCategoryIcon(transaction.category),
                            contentDescription = transaction.category,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = transaction.title,
                            style = LocalTypography.current.bodyLargeSemibold,
                            color = Color(0xFF1C1B1F)
                        )
                    }
                }
                
                // Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.amount),
                        style = LocalTypography.current.bodyMediumRegular,
                        color = Color(0xFF9E9E9E)
                    )
                    Text(
                        text = if (transaction.isIncome) "+Rs.${transaction.amount.toInt()}" else "-Rs.${transaction.amount.toInt()}",
                        style = LocalTypography.current.bodyLargeSemibold,
                        color = if (transaction.isIncome) Color(0xFF4CAF50) else Color(0xFFE53935)
                    )
                }
                
                // Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.date),
                        style = LocalTypography.current.bodyMediumRegular,
                        color = Color(0xFF9E9E9E)
                    )
                    Text(
                        text = transaction.date,
                        style = LocalTypography.current.bodyMediumPrimary,
                        color = Color(0xFF1C1B1F)
                    )
                }
                
                // Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.time),
                        style = LocalTypography.current.bodyMediumRegular,
                        color = Color(0xFF9E9E9E)
                    )
                    Text(
                        text = transaction.time,
                        style = LocalTypography.current.bodyMediumPrimary,
                        color = Color(0xFF1C1B1F)
                    )
                }
                
                // Notes (if present)
                if (!transaction.note.isNullOrEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.notes),
                            style = LocalTypography.current.bodyMediumRegular,
                            color = Color(0xFF9E9E9E)
                        )
                        Text(
                            text = (transaction.note as String?).orEmpty(),
                            style = LocalTypography.current.bodyMediumRegular,
                            color = Color(0xFF1C1B1F),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )
                    }
                }
                
                // Close button
                Button(
                    onClick = { showDetailSheet.value = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B4B9B)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.close),
                        style = LocalTypography.current.bodyMediumPrimary,
                        color = Color.White
                    )
                }
                
                // Spacer for bottom padding
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 2.dp)
            .height(IntrinsicSize.Min)
    ) {
        // Background with action buttons (revealed on swipe) - Only show if showActions is true
        if (showActions) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(Color.White),
                horizontalArrangement = Arrangement.End
            ) {
                // Edit Button
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF4DB8A8))
                        .clickable { 
                            onEdit()
                            scope.launch { swipeOffset.snapTo(0f) } // Reset after action
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.edit),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Delete Button
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFE53935), RoundedCornerShape(topEnd = cornerRadius, bottomEnd = cornerRadius))
                        .clickable { 
                            showDeleteDialog.value = true // Show dialog instead of deleting
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.delete),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Card (foreground) - NO FIXED HEIGHT, lets content size it naturally
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = if (showActions) swipeOffset.value.dp else 0.dp)
                .clickable { showDetailSheet.value = true }
                .pointerInput(Unit) {
                    if (showActions) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            // Only allow swiping left (negative values)
                            scope.launch {
                                swipeOffset.snapTo((swipeOffset.value + dragAmount).coerceIn(-120f, 0f))
                            }
                            change.consume()
                        }
                    }
                },
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp,
                hoveredElevation = 6.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading: Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(0xFFB8B3E5),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryConstants.getCategoryIcon(transaction.category),
                        contentDescription = transaction.category,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center: Title and subtitle
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = transaction.title,
                        style = LocalTypography.current.bodyLargeSemibold,
                        color = Color(0xFF1C1B1F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${transaction.date} • ${transaction.time}",
                        style = LocalTypography.current.bodySmallNormal,
                        color = Color(0xFF9E9E9E),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Trailing: Amount
                Text(
                    text = if (transaction.isIncome) "+Rs.${transaction.amount.toInt()}" else "-Rs.${transaction.amount.toInt()}",
                    style = LocalTypography.current.bodyLargeSemibold,
                    color = if (transaction.isIncome) Color(0xFF4CAF50) else Color(0xFFE53935),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(100.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecentTransactionsPreview() {
    SpendRouteTheme {
        RecentTransactions(
            transactions = listOf(
                Transaction(
                    id = "1",
                    title = "Salary",
                    category = "Income",
                    amount = 50000.0,
                    date = "Today",
                    isIncome = true,
                    time = "10:00 AM"
                ),
                Transaction(
                    id = "2",
                    title = "Swiggy order",
                    category = "Food",
                    amount = 340.0,
                    date = "Today",
                    isIncome = false,
                    time = "10:00 AM"
                ),
                Transaction(
                    id = "3",
                    title = "Electricity bill",
                    category = "Bills",
                    amount = 1200.0,
                    date = "Yesterday",
                    isIncome = false,
                    time = "10:00 AM"
                )
            )
        )
    }
}
