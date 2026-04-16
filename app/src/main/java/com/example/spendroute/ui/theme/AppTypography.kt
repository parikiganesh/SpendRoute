package com.example.spendroute.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.spendroute.R

/**
 * As per design specifications, DM Sans font is used throughout the entire app.
 */
internal val DMSans = FontFamily(
    Font(R.font.dmsans_regular, FontWeight.Normal),
    Font(R.font.dmsans_medium, FontWeight.Medium),
    Font(R.font.dmsans_semibold, FontWeight.SemiBold),
    Font(R.font.dmsans_bold, FontWeight.Bold)
)

data class AppTypography(
    private val font: TextStyle = TextStyle(fontFamily = DMSans),
    private val titleMediumLarge: TextStyle = TextStyle(fontSize = 20.sp),
    private val titleLarge: TextStyle = TextStyle(fontSize = 22.sp),
    private val bodySmallMedium: TextStyle = TextStyle(fontSize = 13.sp),
    private val bodySmallSize: TextStyle = TextStyle(fontSize = 11.sp),
    private val bodyMedium: TextStyle = TextStyle(fontSize = 14.sp, lineHeight = 16.8.sp),
    private val labelMediumFontSize: TextStyle = TextStyle(fontSize = 12.sp),
    private val bodyLarge: TextStyle = TextStyle(fontSize = 16.sp),
    private val headingSmall: TextStyle = TextStyle(fontSize = 18.sp),
    private val headingMedium: TextStyle = TextStyle(fontSize = 28.sp),
    private val bodyExtraLarge: TextStyle = TextStyle(fontSize = 40.sp),
    private val textLarge: TextStyle = TextStyle(fontSize = 24.sp),
    private val bodyMediumLineHeight: TextStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    private val headingExtraLarge: TextStyle = TextStyle(fontSize = 44.sp),
    private val headingLarge: TextStyle = TextStyle(fontSize = 32.sp),

    val bodyExtraLargeText: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp
    ),
    val bodyLargeText: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    val bodyLargeMedium: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    val bodyLargeNormal: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    val bodyMediumSemibold: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        fontStyle = FontStyle.Normal
    ),
    val bodyMediumPrimary: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        fontStyle = FontStyle.Normal
    ),
    val bodySmallMediumSemibold: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        fontStyle = FontStyle.Normal
    ),
    val bodySmallestMediumSemibold: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        fontStyle = FontStyle.Normal
    ),
    val bodySmallNormal: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        fontStyle = FontStyle.Normal
    ),
    val labelLarge: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    ),
    val labelMedium: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp
    ),
    val labelMediumSemibold: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
    ),
    val bodyMediumNormalStyle: TextStyle = TextStyle(
        fontSize = 14.sp,
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal
    ),
    val bodyMediumNormal: SpanStyle = SpanStyle(
        fontSize = 14.sp,
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal
    ),
    val bodyMediumBold: SpanStyle = SpanStyle(
        fontSize = 14.sp,
        textDecoration = TextDecoration.Underline,
        fontFamily = DMSans,
        fontWeight = FontWeight.Bold
    ),
    val bodyLargeSemibold: TextStyle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        fontFamily = DMSans,
        textAlign = TextAlign.Center
    ),
    val bodySmall: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
    ),
    val bodySmallMediumWeight: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
    ),
    val titleLargeSemibold: TextStyle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        fontFamily = DMSans,
        textAlign = TextAlign.Center
    ),
    val titleLargeNormal: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        fontFamily = DMSans,
        textAlign = TextAlign.Center
    ),
    val titleLargeMedium: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        fontFamily = DMSans,
        textAlign = TextAlign.Center
    ),
    val headingSmallSemibold: TextStyle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        fontFamily = DMSans
    ),
    val headingSmallNormal: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        fontFamily = DMSans
    ),
    val headingSmallMedium: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        fontFamily = DMSans
    ),
    val bodyLargeBold: TextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        fontFamily = DMSans,
        textAlign = TextAlign.Center
    ),
    val labelBold: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
    ),
    val spanStyleBold: SpanStyle = SpanStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Bold
    ),
    val headingMediumSemibold: TextStyle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        fontFamily = DMSans,
        textAlign = TextAlign.Center,
    ),
    val headingExtraLargeSemibold: TextStyle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 44.sp,
        textAlign = TextAlign.Center,
        fontFamily = DMSans
    ),
    val bodyMediumNormalWithoutUnderline: TextStyle = TextStyle(
        fontSize = 14.sp,
        fontFamily = DMSans,
        fontWeight = FontWeight.Bold
    ),
    val bodyMediumNormalLineHeight: TextStyle = TextStyle(
        fontSize = 14.sp,
        fontFamily = DMSans,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp
    ),
    val bodyLargeMediumUnderline: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        textDecoration = TextDecoration.Underline,
    ),
    val bodyLargeBoldTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        fontFamily = DMSans
    ),
    val bodyMediumRegular: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        fontStyle = FontStyle.Normal
    ),
    val bodyHeadingRegular: TextStyle = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        fontStyle = FontStyle.Normal
    ),
    val headingNormalWeightFontAndLargeFont: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        fontFamily = DMSans,
        textAlign = TextAlign.Center,
    ),
)

internal val LocalTypography = staticCompositionLocalOf { AppTypography() }

