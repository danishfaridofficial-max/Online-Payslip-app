package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val NotoNastaliqFamily = FontFamily(
    Font(R.font.noto_nastaliq_urdu_regular, FontWeight.Normal),
    Font(R.font.noto_nastaliq_urdu_regular, FontWeight.Medium),
    Font(R.font.noto_nastaliq_urdu_bold, FontWeight.SemiBold),
    Font(R.font.noto_nastaliq_urdu_bold, FontWeight.Bold)
)

val JameelNooriFamily = NotoNastaliqFamily

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = NotoNastaliqFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 52.sp
    ),
    displayMedium = TextStyle(
        fontFamily = NotoNastaliqFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 46.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = NotoNastaliqFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = NotoNastaliqFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 36.sp
    ),
    titleLarge = TextStyle(
        fontFamily = NotoNastaliqFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 34.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NotoNastaliqFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 30.sp
    ),
    titleSmall = TextStyle(
        fontFamily = NotoNastaliqFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoNastaliqFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 28.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoNastaliqFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 26.sp
    ),
    bodySmall = TextStyle(
        fontFamily = NotoNastaliqFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = NotoNastaliqFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 24.sp
    ),
    labelMedium = TextStyle(
        fontFamily = NotoNastaliqFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 22.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NotoNastaliqFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 20.sp
    )
)
