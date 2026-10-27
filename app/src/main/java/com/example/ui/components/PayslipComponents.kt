package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.NotoNastaliqFamily
import com.example.data.model.PayItem
import com.example.data.model.PayslipData
import com.example.ui.theme.*
import com.example.util.PdfUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Gradient Header matching HTML header in mobile view exactly:
 * Left: Portal Urdu Title & Subtitle
 * Right: Translucent rounded card with emerald document/dollar icon
 * Bottom Center: Calendar date in English (e.g. Tuesday, September 22, 2026)
 */
@Composable
fun PortalHeader(
    currentDate: String,
    hasUnreadNotifications: Boolean = false,
    onOpenWhatsappClick: (() -> Unit)? = null,
    onOpenSecurityNoticeClick: (() -> Unit)? = null,
    onOpenNotificationsClick: (() -> Unit)? = null,
    onRefreshClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF047857), // emerald-700
                        Color(0xFF0F766E), // teal-700
                        Color(0xFF155E75)  // cyan-800
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFF0D9488).copy(alpha = 0.5f)
            )
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .testTag("portal_header")
    ) {
        // In-App Notification Bell Button (Top End) with Dynamic Badge Dot
        IconButton(
            onClick = { onOpenNotificationsClick?.invoke() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(42.dp)
                .background(Color.White.copy(alpha = 0.18f), CircleShape)
                .testTag("portal_notification_bell_btn")
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                if (hasUnreadNotifications) {
                    // Red badge indicator dot only when there's an actual unread notification
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(Color(0xFFEF4444), CircleShape)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Centered Soft rounded container with matching official logo
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_payslip_logo),
                    contentDescription = "Portal Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(13.dp))
                )
            }

            // Portal Title in Urdu (Centered)
            Text(
                text = "پے سلپ آن لائن پورٹل",
                style = TextStyle(
                    fontFamily = NotoNastaliqFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.White,
                    lineHeight = 40.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Subtitle in Urdu (Centered)
            Text(
                text = "ڈیجیٹل سروسز برائے سرکاری ملازمین",
                style = TextStyle(
                    fontFamily = NotoNastaliqFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp,
                    color = Color(0xFFCCFBF1).copy(alpha = 0.95f),
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Centered Date Display Pill: 📅 Tuesday, September 22, 2026
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.18f))
                    .border(0.8.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = Color(0xFFA7F3D0).copy(alpha = 0.9f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentDate,
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFA7F3D0).copy(alpha = 0.95f)
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * In-App Notification Banner with Notification Icon, Badge and clickable details
 */
@Composable
fun InAppNotificationBanner(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("in_app_notification_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        border = BorderStroke(1.2.dp, Color(0xFF86EFAC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Notification Bell / Loudspeaker Icon with pulse effect
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Notification",
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF16A34A), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "اہم نوٹیفیکیشن",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Text(
                        text = "تازہ ترین خبر",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = text.ifBlank { "تازہ ترین تنخواہ کی پے سلپس پورٹل پر دستیاب ہیں۔ تفصیلات کے لیے کلک کریں۔" },
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontSize = 12.5.sp,
                        color = Color(0xFF166534),
                        lineHeight = 20.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Arrow to details
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Details",
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * In-App Notification Modal Dialog showing real Firebase Cloud Messaging notifications
 */
@Composable
fun InAppNotificationDialog(
    notifications: List<com.example.data.AppNotification>,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .testTag("in_app_notification_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFDCFCE7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "نوٹیفیکیشنز (Firebase)",
                                style = TextStyle(
                                    fontFamily = NotoNastaliqFamily,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F766E)
                                )
                            )
                            Text(
                                text = if (notifications.isEmpty()) "کوئی نیا پیغام نہیں" else "${notifications.size} پیغامات موصول ہوئے",
                                style = TextStyle(
                                    fontFamily = NotoNastaliqFamily,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بند کریں",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                if (notifications.isEmpty()) {
                    // Clean Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFF1F5F9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "فی الحال کوئی نیا نوٹیفکیشن نہیں ہے",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF334155)
                            )
                        )
                        Text(
                            text = "جب آپ فائر بیس کنسول (Firebase Messaging) سے کوئی نیا کیمپین یا نوٹیفکیشن بھیجیں گے، تو وہ خودکار طور پر یہاں دکھائی دے گا۔",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 19.sp
                            )
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
                    ) {
                        Text("ٹھیک ہے", fontFamily = NotoNastaliqFamily, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Active Notifications List
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        notifications.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF0FDF4),
                                border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFFDCFCE7), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = Color(0xFF16A34A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            style = TextStyle(
                                                fontFamily = NotoNastaliqFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                color = Color(0xFF14532D)
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.message,
                                            style = TextStyle(
                                                fontFamily = NotoNastaliqFamily,
                                                fontSize = 12.sp,
                                                color = Color(0xFF334155),
                                                lineHeight = 19.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClearAll,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("صاف کریں", fontFamily = NotoNastaliqFamily)
                        }

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
                        ) {
                            Text("بند کریں", fontFamily = NotoNastaliqFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Scrolling / Announcement News Ticker matching HTML:
 * Background: #ecfdf5, border-bottom: 2px solid #5eead4, text: #dc2626
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewsTickerBar(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(Color(0xFFECFDF5))
            .border(width = 1.5.dp, color = Color(0xFF5EEAD4))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = NotoNastaliqFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFFDC2626)
            ),
            maxLines = 1,
            modifier = Modifier.basicMarquee(
                iterations = Int.MAX_VALUE,
                initialDelayMillis = 500,
                velocity = 45.dp
            )
        )
    }
}

/**
 * Dynamic WhatsApp Channel Card matching HTML
 */
@Composable
fun WhatsAppPromotionCard(
    heading: String,
    text: String,
    link: String
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("whatsapp_banner_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFBBF7D0)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF22C55E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "WhatsApp",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = heading,
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF14532D),
                            lineHeight = 26.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = text,
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.5.sp,
                            color = Color(0xFF475569),
                            lineHeight = 22.sp
                        )
                    )
                }
            }

            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Cannot open link: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("join_whatsapp_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "چینل جوائن کریں",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

/**
 * Security Instructions Box matching HTML
 */
@Composable
fun SecurityNoticeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
        border = BorderStroke(width = 1.dp, color = Color(0xFFCCFBF1))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Security",
                tint = Color(0xFF0D9488),
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "حفاظتی ہدایات",
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = Color(0xFF134E4A),
                        lineHeight = 24.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "یہ پورٹل ڈیٹا بیس کی تلاش کے لیے محفوظ ترین اصولوں کا استعمال کرتا ہے۔ کسی بھی تکنیکی یا ملازم کے ڈیٹا میں غلطی کی صورت میں اپنے ڈسٹرکٹ آفس/اکاؤنٹس برانچ سے فوری رابطہ کریں۔",
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 22.sp
                    )
                )
            }
        }
    }
}

/**
 * Live Traffic & Visitors Widget matching HTML:
 * White container, border-r-4 in teal-600, LTR layout
 */
@Composable
fun LiveTrafficWidget(
    onlineCount: Int,
    totalVisits: Long
) {
    // Lightweight single-value pulse for smooth 60fps performance on budget phones
    val infiniteTransition = rememberInfiniteTransition(label = "LivePulse")
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corePulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("traffic_widget"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(width = 1.dp, color = Color(0xFFE2E8F0))
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val barWidth = 4.dp.toPx()
                        drawRect(
                            color = Color(0xFF0D9488),
                            topLeft = Offset(size.width - barWidth, 0f),
                            size = Size(barWidth, size.height)
                        )
                    }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 20.dp, top = 14.dp, bottom = 14.dp)
                ) {
                    // Row 1: Employee(s) Online with Live Pulse
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFCCFBF1).copy(alpha = 0.9f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .graphicsLayer(
                                        scaleX = corePulse,
                                        scaleY = corePulse
                                    )
                                    .clip(CircleShape)
                                    .background(Color(0xFF0D9488))
                            )
                        }
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = Color(0xFF0D9488),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                ) {
                                    append(onlineCount.toString())
                                }
                                append(" Employee(s) Online (App)")
                            },
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // Row 2: Total Visits
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = "Visits",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = buildAnnotatedString {
                                append("App Visits: ")
                                withStyle(
                                    SpanStyle(
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                ) {
                                    val formattedVisits = try {
                                        java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(totalVisits)
                                    } catch (e: Exception) {
                                        totalVisits.toString()
                                    }
                                    append(formattedVisits)
                                }
                            },
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean Rewarded Video Ad Modal popup shown before PDF download / share
 */
@Composable
fun DownloadRewardedAdModal(
    title: String = "پے سلپ ڈاؤن لوڈ کریں",
    message: String = "آپ کی سیلری سلپ تیار ہے۔ ڈاؤن لوڈ کرنے کے لیے مختصر ویڈیو دیکھیں۔\nWatch a quick ad to download your PDF.",
    buttonText: String = "ویڈیو دیکھیں اور ڈاؤن لوڈ کریں (Watch Video)",
    onWatchAdClick: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .testTag("download_rewarded_ad_modal"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.2.dp, Color(0xFFCCFBF1))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Icon: Play / Video in circular emerald container
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFCCFBF1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = "Watch Ad",
                        tint = Color(0xFF0F766E),
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Title
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F766E),
                        textAlign = TextAlign.Center
                    )
                )

                // Message
                Text(
                    text = message,
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Primary Button: Watch Video
                Button(
                    onClick = onWatchAdClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("watch_video_and_download_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buttonText,
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        )
                    }
                }

                // Secondary Button: کینسل (Cancel)
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("cancel_download_ad_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B))
                ) {
                    Text(
                        text = "کینسل (Cancel)",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

/**
 * Official Formatted Payslip Dialog matching District Accounts Office Bannu format
 */
@Composable
fun PayslipDetailDialog(
    data: PayslipData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var showRewardedAdDialog by remember { mutableStateOf(false) }
    var pendingActionType by remember { mutableStateOf("download") } // "download" or "share"

    val triggerDownloadAction: () -> Unit = {
        isGeneratingPdf = true
        scope.launch {
            val file = withContext(Dispatchers.IO) {
                PdfUtil.generatePayslipPdf(context, data)
            }
            isGeneratingPdf = false
            if (file != null) {
                Toast.makeText(context, "پی ڈی ایف Downloads فولڈر میں محفوظ ہو گئی ہے!", Toast.LENGTH_LONG).show()
                PdfUtil.openPdf(context, file)
            } else {
                Toast.makeText(context, "پی ڈی ایف بنانے میں خرابی", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val triggerShareAction: () -> Unit = {
        isGeneratingPdf = true
        scope.launch {
            val file = withContext(Dispatchers.IO) {
                PdfUtil.generatePayslipPdf(context, data)
            }
            isGeneratingPdf = false
            if (file != null) {
                PdfUtil.sharePdf(context, file)
            } else {
                Toast.makeText(context, "پی ڈی ایف بنانے میں خرابی", Toast.LENGTH_SHORT).show()
            }
        }
    }

    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp, horizontal = 8.dp)
                .testTag("payslip_detail_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF8FAFC),
            shadowElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dialog Top Header
                Surface(
                    color = Color(0xFFF1F5F9),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0D9488)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "پے سلپ کامیابی سے مل گئی ہے",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                )
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("close_payslip_dialog_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        // Top Action Buttons directly accessible without scrolling
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isGeneratingPdf) return@Button
                                    pendingActionType = "download"
                                    showRewardedAdDialog = true
                                },
                                enabled = !isGeneratingPdf,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(44.dp)
                                    .testTag("top_download_pdf_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ڈاؤن لوڈ کریں (Save)",
                                    style = TextStyle(
                                        fontFamily = NotoNastaliqFamily,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    if (isGeneratingPdf) return@OutlinedButton
                                    pendingActionType = "share"
                                    showRewardedAdDialog = true
                                },
                                enabled = !isGeneratingPdf,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("top_share_pdf_btn"),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.2.dp, Color(0xFF0284C7)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7)),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "شیئر کریں",
                                    style = TextStyle(
                                        fontFamily = NotoNastaliqFamily,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // Official Document Paper Sheet (SAP / PIFRA format is LTR)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    val verticalScroll = rememberScrollState()
                    val horizontalScroll = rememberScrollState()

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFE2E8F0)) // subtle desk background like PDF viewers
                            .verticalScroll(verticalScroll)
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(horizontalScroll)
                        ) {
                            Card(
                                modifier = Modifier
                                    .width(580.dp) // Fixed width exactly matching official A4 PDF aspect ratio
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(2.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                border = BorderStroke(1.2.dp, Color(0xFF1E293B))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                            // Official Header
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(width = 0.dp, color = Color.Transparent)
                                    .padding(bottom = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = data.officeTitle.ifEmpty { "Dist. Govt. KP-Provincial" },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                                Text(
                                    text = data.officeSubTitle.ifEmpty { "District Accounts Office Bannu" },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                                Text(
                                    text = "Monthly Salary Statement (${data.monthYear})",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(thickness = 2.dp, color = Color.Black)
                            }

                            // Personal Info Header
                            Text(
                                text = "Personal Information of ${data.name}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // Personal Info Table
                            TableGrid(
                                rows = listOf(
                                    listOf("Personnel Number:", data.personnelNo, "CNIC:", data.cnic),
                                    listOf("Date of Birth:", data.dob, "Length of Service:", data.lengthOfService)
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Employment Category: ${data.employmentCategory}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // Employment / DDO Table
                            TableGrid(
                                rows = listOf(
                                    listOf("Designation:", data.designation, "DDO:", data.ddoTitle),
                                    listOf("DDO Code:", data.ddoCode, "", ""),
                                    listOf("Payroll Section:", data.payrollSection, "GPF Section:", data.gpfSection),
                                    listOf("GPF A/C No:", data.gpfAccountNo, "GPF Interest:", data.gpfInterestApplied),
                                    listOf("GPF Balance:", PdfUtil.formatAmount(data.gpfBalance), "", "")
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Pay & Allowances Header
                            Text(
                                text = "Pay and Allowances: Pay scale: ${data.payScale}   Type: ${data.payScaleType}   BPS: ${data.bps}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )

                            // Allowances Dual-Column Table
                            DualColumnWageTable(
                                title = "Wage type",
                                items = data.payItems
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Deductions General
                            Text(
                                text = "Deductions - General",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )

                            DualColumnWageTable(
                                title = "Deductions",
                                items = data.deductions
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Income Tax
                            Text(
                                text = "Deductions - Income Tax",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )

                            TableGrid(
                                rows = listOf(
                                    listOf(
                                        "Payable:", PdfUtil.formatAmount(data.incomeTax.payable),
                                        "Recovered:", PdfUtil.formatAmount(data.incomeTax.recoveredTill)
                                    ),
                                    listOf(
                                        "Exempted:", data.incomeTax.exempted,
                                        "Recoverable:", PdfUtil.formatAmount(data.incomeTax.recoverable)
                                    )
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Summary Box (Gross Pay, Deductions, Net Pay) - Single-pass drawBehind dividers for zero lag
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.2.dp, Color.Black)
                                    .drawBehind {
                                        val third = size.width / 3f
                                        val stroke = 1.dp.toPx()
                                        drawLine(
                                            color = Color.Black,
                                            start = Offset(third, 0f),
                                            end = Offset(third, size.height),
                                            strokeWidth = stroke
                                        )
                                        drawLine(
                                            color = Color.Black,
                                            start = Offset(third * 2f, 0f),
                                            end = Offset(third * 2f, size.height),
                                            strokeWidth = stroke
                                        )
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Cell 1: Gross Pay
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Gross Pay (Rs.):", style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black))
                                    Text(PdfUtil.formatAmount(data.grossPay), style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black))
                                }

                                // Cell 2: Deductions
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Deductions (Rs.):", style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black))
                                    Text(PdfUtil.formatAmount(data.totalDeductions), style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black))
                                }

                                // Cell 3: Net Pay
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Net Pay (Rs.):", style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black))
                                    Text(PdfUtil.formatAmount(data.netPay), style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Bank Details
                            TableGrid(
                                rows = listOf(
                                    listOf("Payee Name:", data.payeeName.ifEmpty { data.name }, "", ""),
                                    listOf("Account Number:", data.accountNo, "", ""),
                                    listOf("Bank Details:", data.bankDetails, "", "")
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = data.footerNote1.ifEmpty { "System generated document" },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = Color.DarkGray
                                )
                            )
                            Text(
                                text = "* All amounts are in Pak Rupees",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = Color.DarkGray
                                )
                            )
                        }
                    }
                }
            }
        }
    }
    }
}

    // High-Converting Rewarded Video Modal before PDF Generation / Download / Share
    if (showRewardedAdDialog) {
        DownloadRewardedAdModal(
            onWatchAdClick = {
                showRewardedAdDialog = false
                val act = com.example.util.UnityAdsManager.findActivity(context)
                com.example.util.UnityAdsManager.showRewardedForDownload(
                    activity = act,
                    onRewardGranted = {
                        if (pendingActionType == "download") {
                            triggerDownloadAction()
                        } else {
                            triggerShareAction()
                        }
                    },
                    onRewardSkipped = {
                        Toast.makeText(
                            context,
                            "مکمل اشتہار دیکھنا ضروری ہے / Please watch the full ad to unlock download.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            },
            onDismiss = {
                showRewardedAdDialog = false
            }
        )
    }
}

/**
 * 4-column Table Grid for info pairs
 */
@Composable
fun TableGrid(rows: List<List<String>>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (row.size >= 2) {
                    Row(
                        modifier = Modifier.weight(1.1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = row[0],
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontSize = 10.5.sp,
                                color = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.width(115.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = row[1],
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                if (row.size >= 4 && row[2].isNotEmpty()) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = row[2],
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontSize = 10.5.sp,
                                color = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.width(110.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = row[3],
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else if (row.size >= 4) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Dual Column Wage / Allowance / Deduction Table matching official print
 */
@Composable
fun DualColumnWageTable(title: String, items: List<PayItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black)
    ) {
        // Table Header - Single-pass drawBehind center divider (no IntrinsicSize.Min overhead)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9))
                .border(1.dp, Color.Black)
                .drawBehind {
                    val midX = size.width / 2f
                    drawLine(
                        color = Color.Black,
                        start = Offset(midX, 0f),
                        end = Offset(midX, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, color = Color.Black),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp, horizontal = 6.dp)
            )
            Text(
                text = title,
                style = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, color = Color.Black),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp, horizontal = 6.dp)
            )
        }

        var i = 0
        while (i < items.size) {
            val left = items[i]
            val right = if (i + 1 < items.size) items[i + 1] else null

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.6.dp, Color.Black)
                    .drawBehind {
                        val midX = size.width / 2f
                        drawLine(
                            color = Color.Black,
                            start = Offset(midX, 0f),
                            end = Offset(midX, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Col: Code & Label | Amount
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp, vertical = 2.5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${left.code}-${left.label.take(24)}",
                        style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 10.sp, color = Color.Black),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = PdfUtil.formatAmount(left.amount),
                        style = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Black)
                    )
                }

                // Right Col: Code & Label | Amount
                if (right != null) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp, vertical = 2.5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${right.code}-${right.label.take(24)}",
                            style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 10.sp, color = Color.Black),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = PdfUtil.formatAmount(right.amount),
                            style = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Black)
                        )
                    }
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
            }
            i += 2
        }
    }
}

/**
 * WhatsApp Congratulatory Popup Modal (In-window overlay to prevent stacked Dialog Window crashes)
 */
@Composable
fun WhatsAppCongratulationDialog(
    heading: String,
    text: String,
    link: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .testTag("whatsapp_popup_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.5.dp, Color(0xFFD1FAE5))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Top close cross icon
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "بند کریں",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Emerald circular WhatsApp Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1FAE5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "WhatsApp",
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Heading
                Text(
                    text = heading.ifEmpty { "واٹس ایپ چینل جوائن کریں!" },
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Body text
                Text(
                    text = text.ifEmpty { "اگست کی پے سلپس حاصل کرنے اور دیگر اہم نوٹیفیکیشنز بروقت پانے کے لیے ہمارا واٹس ایپ چینل لازمی جوائن کریں۔" },
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.5.sp,
                        color = Color(0xFF475569),
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Join Button
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open link: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("popup_join_now_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF059669),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "ابھی جوائن کریں",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Later / Dismiss Button - Clearly visible and elegant
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("popup_later_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFF8FAFC),
                        contentColor = Color(0xFF334155)
                    )
                ) {
                    Text(
                        text = "بعد میں کریں گے",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF475569)
                        )
                    )
                }
            }
        }
    }
}
}
