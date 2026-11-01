package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Optimized: Single Regular font used. Android graphics engine (Skia) automatically applies
// synthetic bolding when FontWeight.Bold or SemiBold is specified, saving ~50% font RAM footprint.
val NotoNastaliqFamily = FontFamily(
    Font(R.font.noto_nastaliq_urdu_regular, FontWeight.Normal)
)

val JameelNooriFamily = NotoNastaliqFamily

@Composable
fun rememberUrduTypography(): Typography {
    val urduFont = remember {
        FontFamily(
            Font(R.font.noto_nastaliq_urdu_regular, FontWeight.Normal)
        )
    }
    return remember(urduFont) {
        buildUrduTypography(urduFont)
    }
}

fun buildUrduTypography(fontFamily: FontFamily): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 52.sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 46.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 40.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 36.sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 34.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 30.sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 28.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 28.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 26.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 24.sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 24.sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 22.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 20.sp
        )
    )
}

val Typography = buildUrduTypography(NotoNastaliqFamily)

