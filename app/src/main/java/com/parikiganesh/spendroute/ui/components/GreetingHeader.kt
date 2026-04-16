package com.parikiganesh.spendroute.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.parikiganesh.spendroute.ui.theme.LocalTypography
import com.parikiganesh.spendroute.ui.theme.SpendRouteTheme
import java.util.Calendar

// Function to get greeting based on time of day
private fun getGreetingMessage(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning,"
        hour < 17 -> "Good afternoon,"
        else -> "Good evening,"
    }
}

@Composable
fun GreetingHeader(
    name: String = "",
    initials: String = "",
    modifier: Modifier = Modifier,
    avatarBackgroundColor: Color = Color(0xFF7C6FD4),
    headerBackgroundColor: Color = Color.Transparent,
    title: String? = null,
    subtitle: String? = null,
    showAvatar: Boolean = true,
    greeting: String = getGreetingMessage(),
    boldGreeting: Boolean = false,  // true = greeting bold, name normal | false = greeting normal, name bold
    isSimpleMode: Boolean = title != null && subtitle != null,  // Controls which layout to show
    onAvatarClick: () -> Unit = {}
) {
    // Determine text colors based on background color
    val isLightBackground = headerBackgroundColor == Color.White || headerBackgroundColor == Color.Transparent
    val textColor = if (isLightBackground) Color.Black else Color.White
    val subtitleColor = if (isLightBackground) Color(0xFF9E9E9E) else Color(0xFFB8B3E5)
    
    if (isSimpleMode) {
        // ...existing code...
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(headerBackgroundColor)
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
        ) {
            Column {
                Text(
                    text = title ?: "",
                    style = LocalTypography.current.headingSmallSemibold,
                    color = textColor
                )
                Text(
                    text = subtitle ?: "",
                    style = LocalTypography.current.bodyMediumRegular,
                    color = subtitleColor
                )
            }
        }
    } else {
        // Greeting header mode (used in HomeScreen and ProfileScreen)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(headerBackgroundColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left side: Greeting text and name
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = greeting,
                        style = if (boldGreeting) LocalTypography.current.headingSmallSemibold else LocalTypography.current.bodyMediumRegular,
                        color = textColor
                    )
                    Text(
                        text = name,
                        style = if (boldGreeting) LocalTypography.current.bodyMediumRegular else LocalTypography.current.headingSmallSemibold,
                        color = if (boldGreeting) subtitleColor else textColor
                    )
                }

                // Right side: Avatar
                if (showAvatar) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                color = avatarBackgroundColor,
                                shape = CircleShape
                            )
                            .clickable { onAvatarClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = LocalTypography.current.headingSmallSemibold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingHeaderPreview() {
    SpendRouteTheme {
        GreetingHeader(
            name = "Arjun Kumar",
            initials = "AK"
        )
    }
}



