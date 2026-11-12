package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NotoNastaliqFamily

/**
 * Standard Light Shimmer Brush for white/light cards.
 * Linear gradient sweeping smoothly from top-left to bottom-right.
 */
@Composable
fun rememberShimmerBrush(
    targetValue: Float = 1300f,
    durationMillis: Int = 1100
): Brush {
    val shimmerColors = listOf(
        Color(0xFFE2E8F0),
        Color(0xFFF1F5F9),
        Color(0xFFFFFFFF),
        Color(0xFFF1F5F9),
        Color(0xFFE2E8F0)
    )

    val transition = rememberInfiniteTransition(label = "LightShimmerTransition")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "LightShimmerAnimation"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnimation.value - 400f, y = translateAnimation.value - 400f),
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )
}

/**
 * Dark Shimmer Brush for dark green/teal surfaces (e.g. Monthly Salary Card in Dashboard).
 * Perfectly matches Color(0xFF042F2E).
 */
@Composable
fun rememberDarkShimmerBrush(
    targetValue: Float = 1300f,
    durationMillis: Int = 1100
): Brush {
    val shimmerColors = listOf(
        Color(0xFF042F2E),
        Color(0xFF094E48),
        Color(0xFF116D64),
        Color(0xFF094E48),
        Color(0xFF042F2E)
    )

    val transition = rememberInfiniteTransition(label = "DarkShimmerTransition")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DarkShimmerAnimation"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnimation.value - 400f, y = translateAnimation.value - 400f),
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )
}

/**
 * SKELETON 1: FULL DASHBOARD SKELETON
 * Used when login completes and dashboard is loading for the first time.
 * Matches exact sizes, padding, corner radii and layout of HrmisProfileDashboard cards
 * so that when real data arrives, zero screen jumping / layout shift occurs.
 */
@Composable
fun FullDashboardSkeleton(
    modifier: Modifier = Modifier
) {
    val lightBrush = rememberShimmerBrush()
    val darkBrush = rememberDarkShimmerBrush()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .testTag("full_dashboard_skeleton"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Employee Identity Card Skeleton (Matching Card 1 in HrmisProfileDashboard)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top Row: Avatar (Left) + 2 lines + Logout button (Right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatar Circle 54dp
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(lightBrush)
                        )

                        // Name & Designation lines
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(lightBrush)
                            )
                            Box(
                                modifier = Modifier
                                    .width(190.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(lightBrush)
                            )
                        }

                        // Logout placeholder
                        Box(
                            modifier = Modifier
                                .size(width = 72.dp, height = 34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(lightBrush)
                        )
                    }

                    // DDO Bar placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(lightBrush)
                    )

                    // Personal No (Left) & CNIC (Right) chip placeholders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(lightBrush)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(lightBrush)
                        )
                    }
                }
            }

            // 2. Quick Month Selector Skeleton (Matching Month Bar in HrmisProfileDashboard)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(lightBrush)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 160.dp, height = 20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(lightBrush)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(lightBrush)
                        )
                    }
                }
            }

            // 3. Monthly Salary Card Skeleton (Dark Green/Teal)
            SalaryCardSkeleton()

            // 4. GPF Balance Card Skeleton
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(lightBrush)
                        )
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(lightBrush)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 76.dp, height = 28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(lightBrush)
                    )
                }
            }
        }
    }
}

/**
 * SKELETON 2: DARK SALARY SUMMARY CARD SKELETON
 * Used when user clicks to switch months.
 * ONLY this card is replaced with skeleton shimmer;
 * the rest of the dashboard (profile card, month pills, GPF card) stays in place.
 * Exact shape RoundedCornerShape(20.dp), color Color(0xFF042F2E), elevation 3.dp, padding 20.dp.
 */
@Composable
fun SalaryCardSkeleton(
    modifier: Modifier = Modifier
) {
    val darkBrush = rememberDarkShimmerBrush()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("salary_card_skeleton"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF042F2E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title on Left, Month Badges on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(darkBrush)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(width = 64.dp, height = 22.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(darkBrush)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 78.dp, height = 22.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(darkBrush)
                    )
                }
            }

            // Net Payable Hero Display Skeleton
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(darkBrush)
                )
                Box(
                    modifier = Modifier
                        .width(190.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(darkBrush)
                )
            }

            // Gross Pay (Left) & Deductions (Right) Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(darkBrush)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(darkBrush)
                )
            }

            // Progress Bar Box Skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(darkBrush)
            )

            // View Payslip Button Skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(rememberShimmerBrush())
                )
            }
        }
    }
}

/**
 * SKELETON 3: PDF / PAYSLIP DOCUMENT SKELETON
 * Displayed while generating PDF or opening payslip dialog.
 * Exact A4 styled sheet proportions with official lines and tables.
 */
@Composable
fun PayslipDocumentSkeleton(
    modifier: Modifier = Modifier
) {
    val brush = rememberShimmerBrush()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("payslip_document_skeleton"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header lines (Government of KP / DAO)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(brush)
                )
            }

            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

            // Employee Particulars Table Skeleton (4 lines)
            repeat(4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.width(120.dp).height(13.dp).clip(RoundedCornerShape(6.dp)).background(brush))
                    Box(modifier = Modifier.width(100.dp).height(13.dp).clip(RoundedCornerShape(6.dp)).background(brush))
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

            // Wage items & deductions columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.width(80.dp).height(14.dp).clip(RoundedCornerShape(6.dp)).background(brush))
                    repeat(3) {
                        Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(brush))
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.width(80.dp).height(14.dp).clip(RoundedCornerShape(6.dp)).background(brush))
                    repeat(3) {
                        Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(brush))
                    }
                }
            }

            // Net Payable Bar Skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
        }
    }
}

/**
 * SKELETON 4: SEARCH RESULT SKELETON
 * Displayed in guest search mode when query is executing.
 */
@Composable
fun SearchResultSkeleton(
    modifier: Modifier = Modifier
) {
    val brush = rememberShimmerBrush()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("search_result_skeleton"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.width(130.dp).height(16.dp).clip(RoundedCornerShape(8.dp)).background(brush))
                    Box(modifier = Modifier.width(170.dp).height(12.dp).clip(RoundedCornerShape(6.dp)).background(brush))
                }
            }
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.width(100.dp).height(14.dp).clip(RoundedCornerShape(6.dp)).background(brush))
                Box(modifier = Modifier.width(90.dp).height(16.dp).clip(RoundedCornerShape(6.dp)).background(brush))
            }
        }
    }
}

/**
 * Uber Eats Style Skeleton List Row (Retained for list loading)
 */
@Composable
fun SkeletonListRowItem(
    brush: Brush,
    modifier: Modifier = Modifier,
    line1Width: Float = 110f,
    line2Width: Float = 160f
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(brush)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(line1Width.dp)
                    .height(13.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(7.dp))

            Box(
                modifier = Modifier
                    .width(line2Width.dp)
                    .height(13.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
        }
    }
}

/**
 * Uber Eats Skeleton Card (Retained for General Loading Overlays)
 */
@Composable
fun UberEatsSkeletonCard(
    title: String = "براہ کرم انتظار فرمائیں...",
    subtitle: String = "پے سلپ ڈیٹا اور تصدیق کا عمل جاری ہے...",
    itemCount: Int = 4,
    modifier: Modifier = Modifier
) {
    val brush = rememberShimmerBrush()

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("uber_eats_skeleton_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Start
                        )
                    )
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Start
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0D9488).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0D9488))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val line1Widths = listOf(115f, 95f, 130f, 105f)
            val line2Widths = listOf(175f, 150f, 190f, 160f)

            for (i in 0 until itemCount) {
                val idx = i % line1Widths.size
                SkeletonListRowItem(
                    brush = brush,
                    line1Width = line1Widths[idx],
                    line2Width = line2Widths[idx]
                )
            }
        }
    }
}
