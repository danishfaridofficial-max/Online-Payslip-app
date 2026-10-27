package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.NotoNastaliqFamily
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation states
    val scale = remember { Animatable(0.75f) }
    val alpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }

    // Start 2-second splash timer and animations
    LaunchedEffect(Unit) {
        // Guaranteed wall-clock timeout after 2 seconds even if animations are disabled/throttled
        delay(2000L)
        onTimeout()
    }

    LaunchedEffect(Unit) {
        try {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )
        } catch (_: Throwable) {}
    }

    LaunchedEffect(Unit) {
        try {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500)
            )
        } catch (_: Throwable) {}
    }

    LaunchedEffect(Unit) {
        try {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1900, easing = LinearEasing)
            )
        } catch (_: Throwable) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF064E3B), // Deep Forest Emerald
                        Color(0xFF047857), // Emerald 700
                        Color(0xFF0F766E), // Teal 700
                        Color(0xFF022C22)  // Dark Emerald Base
                    )
                )
            )
            .testTag("splash_screen")
    ) {
        // Ambient background glow decorative circles
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 60.dp)
                .clip(CircleShape)
                .background(Color(0xFF34D399).copy(alpha = 0.05f))
        )

        // Top "Skip / چھوڑیں" button for convenience
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .clickable { onTimeout() }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("splash_skip_button")
            ) {
                Text(
                    text = "چھوڑیں (Skip)",
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                )
            }
        }

        // Center Content Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .scale(scale.value)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / Insignia Badge
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .shadow(16.dp, CircleShape, spotColor = Color(0xFF059669))
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, Color(0xFF34D399), CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer gold/emerald ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFF047857)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_payslip_logo),
                        contentDescription = "Payslip Official Emblem",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Urdu Title
            Text(
                text = "پے سلپ آن لائن پورٹل",
                style = TextStyle(
                    fontFamily = NotoNastaliqFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color.White,
                    lineHeight = 52.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // English Uppercase Title Subheading
            Text(
                text = "PAYSLIP ONLINE PORTAL",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 2.5.sp,
                    color = Color(0xFFA7F3D0), // emerald-200
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Urdu Subtitle
            Text(
                text = "حکومتِ خیبر پختونخوا • ڈیجیٹل سروسز برائے ملازمین",
                style = TextStyle(
                    fontFamily = NotoNastaliqFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color(0xFFCCFBF1), // teal-100
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Badges Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Security Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(0.8.dp, Color(0xFF6EE7B7).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF6EE7B7),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "محفوظ و تصدیق شدہ",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        )
                    }
                }

                // Official Portal Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(0.8.dp, Color(0xFF6EE7B7).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFFFDE047), // Gold
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "تنخواہ و الاؤنسز",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Bottom Loading & Branding Section
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Smooth Progress Bar (2 seconds timer visual)
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF34D399),
                trackColor = Color.White.copy(alpha = 0.2f),
            )

            Text(
                text = "سسٹم لوڈ ہو رہا ہے، برائے مہربانی انتظار فرمائیں...",
                style = TextStyle(
                    fontFamily = NotoNastaliqFamily,
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )
            )

            Text(
                text = "© 2026 Online Payslip • Official Salary Portal v1.0",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
