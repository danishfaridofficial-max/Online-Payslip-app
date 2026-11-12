package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PayslipData
import com.example.ui.theme.NotoNastaliqFamily

/**
 * 1. Bottom Navigation Bar for Logged-in State (Home, Profile, Settings)
 * Note: Uses SansSerif font for labels as requested to avoid Nastaliq text truncation on small tab labels.
 */
@Composable
fun PortalBottomBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("portal_bottom_bar"),
        color = Color.White,
        shadowElevation = 8.dp,
        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
    ) {
        NavigationBar(
            containerColor = Color.White,
            contentColor = Color(0xFF047857),
            tonalElevation = 0.dp,
            windowInsets = NavigationBarDefaults.windowInsets,
            modifier = Modifier.fillMaxWidth()
        ) {
            val sansFont = FontFamily.SansSerif

            // Tab 1: Home
            NavigationBarItem(
                selected = selectedTab == "home",
                onClick = { onTabSelected("home") },
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (selectedTab == "home") Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = "ہوم"
                    )
                },
                label = {
                    Text(
                        text = "ہوم",
                        style = TextStyle(
                            fontFamily = sansFont,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == "home") FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF047857),
                    selectedTextColor = Color(0xFF047857),
                    indicatorColor = Color(0xFFD1FAE5),
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B)
                ),
                modifier = Modifier.testTag("tab_bottom_home")
            )

            // Tab 2: Profile
            NavigationBarItem(
                selected = selectedTab == "profile",
                onClick = { onTabSelected("profile") },
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (selectedTab == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                        contentDescription = "پروفائل"
                    )
                },
                label = {
                    Text(
                        text = "پروفائل",
                        style = TextStyle(
                            fontFamily = sansFont,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == "profile") FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF047857),
                    selectedTextColor = Color(0xFF047857),
                    indicatorColor = Color(0xFFD1FAE5),
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B)
                ),
                modifier = Modifier.testTag("tab_bottom_profile")
            )

            // Tab 3: Settings
            NavigationBarItem(
                selected = selectedTab == "settings",
                onClick = { onTabSelected("settings") },
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (selectedTab == "settings") Icons.Filled.Settings else Icons.Outlined.Settings,
                        contentDescription = "سیٹنگز"
                    )
                },
                label = {
                    Text(
                        text = "سیٹنگز",
                        style = TextStyle(
                            fontFamily = sansFont,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == "settings") FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF047857),
                    selectedTextColor = Color(0xFF047857),
                    indicatorColor = Color(0xFFD1FAE5),
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B)
                ),
                modifier = Modifier.testTag("tab_bottom_settings")
            )
        }
    }
}

/**
 * 2. Slide Menu (ModalNavigationDrawer Content)
 * Opens from right-hand side in RTL mode.
 */
@Composable
fun PortalDrawerContent(
    employee: PayslipData?,
    onShareAppClick: () -> Unit,
    onOpenWhatsappClick: () -> Unit,
    onCheckUpdateClick: () -> Unit,
    onOpenHelpClick: () -> Unit,
    onOpenAboutClick: () -> Unit,
    onOpenPrivacyClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val initials = remember(employee?.name) {
        val name = employee?.name?.trim() ?: "KP"
        val words = name.split(Regex("\\s+")).filter { it.isNotBlank() }
        when {
            words.size >= 2 -> "${words[0].take(1)}${words[1].take(1)}".uppercase()
            words.size == 1 -> words[0].take(2).uppercase()
            else -> "KP"
        }
    }

    ModalDrawerSheet(
        modifier = Modifier
            .widthIn(max = 310.dp)
            .fillMaxHeight()
            .testTag("portal_drawer_sheet"),
        drawerContainerColor = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Emerald Gradient Header with Employee Bio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF047857),
                                Color(0xFF0F766E),
                                Color(0xFF155E75)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 26.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Avatar + Status Badge
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                                .border(2.dp, Color.White, CircleShape)
                        )
                    }

                    // Name & Designation
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = employee?.name ?: "سرکاری ملازم",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = employee?.designation ?: "خیبر پختونخوا ایجوکیشن",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 13.sp,
                                color = Color(0xFFE2E8F0)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Drawer Navigation Action Items (Scrollable)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DrawerItemRow(
                    icon = Icons.Outlined.Share,
                    label = "ایپ شیئر کریں",
                    onClick = {
                        onCloseDrawer()
                        onShareAppClick()
                    }
                )

                DrawerItemRow(
                    icon = Icons.Outlined.Campaign,
                    label = "آفیشل واٹس ایپ چینل",
                    onClick = {
                        onCloseDrawer()
                        onOpenWhatsappClick()
                    }
                )

                DrawerItemRow(
                    icon = Icons.Outlined.SystemUpdate,
                    label = "اپ ڈیٹس چیک کریں",
                    onClick = {
                        onCloseDrawer()
                        onCheckUpdateClick()
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFE2E8F0)
                )

                DrawerItemRow(
                    icon = Icons.AutoMirrored.Filled.Help,
                    label = "مدد اور رابطہ",
                    onClick = {
                        onCloseDrawer()
                        onOpenHelpClick()
                    }
                )

                DrawerItemRow(
                    icon = Icons.Outlined.Info,
                    label = "پورٹل کے بارے میں",
                    onClick = {
                        onCloseDrawer()
                        onOpenAboutClick()
                    }
                )

                DrawerItemRow(
                    icon = Icons.Outlined.Security,
                    label = "پرائیویسی پالیسی",
                    onClick = {
                        onCloseDrawer()
                        onOpenPrivacyClick()
                    }
                )
            }

            // Bottom Footer: App Version + Logout Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "آن لائن پے سلپ",
                            fontSize = 13.sp,
                            fontFamily = NotoNastaliqFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "ورژن 1.0 (آفیشل)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFF64748B)
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            onCloseDrawer()
                            onLogoutClick()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFFEE2E2),
                            contentColor = Color(0xFFDC2626)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("drawer_logout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "لاگ آؤٹ",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "لاگ آؤٹ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerItemRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF047857),
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B)
                )
            )
        }
    }
}

/**
 * 3. Profile Tab Screen
 * Full official profile displaying loaded employee information with existing Change Password, Edit Email & Logout actions.
 */
@Composable
fun ProfileTabScreen(
    employee: PayslipData,
    email: String?,
    onChangePasswordClick: () -> Unit,
    onEditEmailClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = remember(context) { context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager }

    fun copyToClipboard(text: String, label: String) {
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "$label کاپی ہو گیا", Toast.LENGTH_SHORT).show()
    }

    val initials = remember(employee.name) {
        val words = employee.name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        when {
            words.size >= 2 -> "${words[0].take(1)}${words[1].take(1)}".uppercase()
            words.size == 1 -> words[0].take(2).uppercase()
            else -> "KP"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF047857), Color(0xFF0F766E))
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = employee.name,
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = employee.designation,
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 13.5.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        )
                        val scaleLabel = employee.bps.ifBlank { employee.payScale.ifBlank { "N/A" } }
                        val officeLabel = employee.officeSubTitle.ifBlank { employee.officeTitle }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "اسکیل: $scaleLabel | $officeLabel",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Official Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "سرکاری ملازمت کی معلومات",
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                ProfileDataRow(
                    label = "شناختی کارڈ نمبر (CNIC)",
                    value = employee.cnic,
                    onCopy = { copyToClipboard(employee.cnic, "CNIC") }
                )

                ProfileDataRow(
                    label = "پرسنل نمبر (Personal No)",
                    value = employee.personnelNo,
                    onCopy = { copyToClipboard(employee.personnelNo, "Personal No") }
                )

                ProfileDataRow(
                    label = "جی پی ایف نمبر (GPF No)",
                    value = employee.gpfAccountNo.ifBlank { "درج نہیں ہے" },
                    onCopy = if (employee.gpfAccountNo.isNotBlank()) { { copyToClipboard(employee.gpfAccountNo, "GPF No") } } else null
                )

                ProfileDataRow(
                    label = "ڈی ڈی او کوڈ (DDO Code)",
                    value = "${employee.ddoCode} - ${employee.ddoTitle}".trim().trimEnd('-').trim()
                )

                ProfileDataRow(
                    label = "ریکوری ای میل",
                    value = email ?: "کوئی ای میل سیٹ نہیں",
                    highlight = email != null
                )

                if (employee.accountNo.isNotBlank() || employee.bankDetails.isNotBlank()) {
                    ProfileDataRow(
                        label = "بینک اور اکاؤنٹ",
                        value = "${employee.bankDetails} ${employee.accountNo}".trim()
                    )
                }
            }
        }

        // Security & Account Recovery Card (Relocated from Home to Profile for pristine UX)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سیکیورٹی اور اکاؤنٹ ریکوری",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFD1FAE5)
                    ) {
                        Text(
                            text = "محفوظ لاگ ان",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Item 1: Login Password Tile
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD1FAE5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF047857),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "خفیہ لاگ ان پاس ورڈ",
                                    style = TextStyle(
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                )
                                Text(
                                    text = "پاس ورڈ محفوظ ہے اور فعال ہے",
                                    style = TextStyle(
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = onChangePasswordClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("profile_change_password_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF047857),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "اپنا نیا خفیہ پاس ورڈ سیٹ کریں",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                // Item 2: Recovery Email Tile (Prominent & Clear)
                val hasEmail = !email.isNullOrBlank()
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (hasEmail) Color(0xFFF0FDF4) else Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, if (hasEmail) Color(0xFFBBF7D0) else Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (hasEmail) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = if (hasEmail) Color(0xFF16A34A) else Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "ریکوری ای میل",
                                        style = TextStyle(
                                            fontFamily = FontFamily.SansSerif,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (hasEmail) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                    ) {
                                        Text(
                                            text = if (hasEmail) "فعال" else "شامل نہیں",
                                            style = TextStyle(
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (hasEmail) Color(0xFF16A34A) else Color(0xFFDC2626)
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (hasEmail) email else "پاس ورڈ ری سیٹ کے لیے ریکوری ای میل شامل کریں",
                                    style = TextStyle(
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 12.sp,
                                        color = if (hasEmail) Color(0xFF15803D) else Color(0xFFB45309),
                                        fontWeight = if (hasEmail) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = onEditEmailClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("profile_edit_email_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasEmail) Color(0xFF0D9488) else Color(0xFFD97706),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = if (hasEmail) Icons.Default.Edit else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasEmail) "ریکوری ای میل تبدیل کریں" else "ریکوری ای میل شامل کریں",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                // Logout Button
                OutlinedButton(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("profile_logout_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFDC2626)
                    )
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "پورٹل سے لاگ آؤٹ کریں",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileDataRow(
    label: String,
    value: String,
    highlight: Boolean = false,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )
            )
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (highlight) Color(0xFF047857) else Color(0xFF0F172A)
                )
            )
        }

        if (onCopy != null) {
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "کاپی کریں",
                    tint = Color(0xFF047857),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * 4. Settings Tab Screen
 * Rows: Remember me, Saved CNIC delete, App version, Update check, About, Privacy Policy, Share App, WhatsApp Channel.
 */
@Composable
fun SettingsTabScreen(
    rememberMe: Boolean,
    onToggleRememberMe: (Boolean) -> Unit,
    onClearSavedCnic: () -> Unit,
    onCheckUpdateClick: () -> Unit,
    onShareAppClick: () -> Unit,
    onOpenWhatsappClick: () -> Unit,
    onOpenAboutClick: () -> Unit,
    onOpenPrivacyClick: () -> Unit,
    onOpenNotificationsClick: () -> Unit = {},
    hasUnreadNotifications: Boolean = false,
    appVersion: String = "1.0"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Group 1: Preferences & Storage
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "ترتیبات اور اسٹوریج",
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Remember Me Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "لاگ ان کریڈنشلز یاد رکھیں",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        )
                        Text(
                            text = "شناختی کارڈ نمبر کو ڈیوائس میں خودکار محفوظ رکھیں",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        )
                    }
                    Switch(
                        checked = rememberMe,
                        onCheckedChange = onToggleRememberMe,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF047857)
                        ),
                        modifier = Modifier.testTag("settings_remember_switch")
                    )
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Delete Saved Credentials
                Surface(
                    onClick = onClearSavedCnic,
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "محفوظ شدہ لاگ ان ڈیٹا صاف کریں",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            )
                            Text(
                                text = "فون سے محفوظ شناختی کارڈ نمبر اور پرسنل نمبر ڈیلیٹ ہو جائے گا",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF991B1B)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Group 2: Updates & Sharing
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "اپ ڈیٹس اور سپورٹ",
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                SettingsActionRow(
                    icon = Icons.Default.NotificationsActive,
                    title = "نوٹیفکیشنز اور اہم اعلانات",
                    subtitle = if (hasUnreadNotifications) "نیا سرکاری پیغام موصول ہوا ہے" else "سرکاری اعلانات اور الرٹس دیکھیں",
                    badge = if (hasUnreadNotifications) "نیا پیغام" else null,
                    onClick = onOpenNotificationsClick
                )

                SettingsActionRow(
                    icon = Icons.Default.SystemUpdate,
                    title = "اپ ڈیٹس چیک کریں",
                    subtitle = "نئے فیچرز اور سیلری فارمیٹس کے لیے ورژن چیک کریں",
                    onClick = onCheckUpdateClick
                )

                SettingsActionRow(
                    icon = Icons.Default.Campaign,
                    title = "آفیشل واٹس ایپ چینل",
                    subtitle = "تازہ ترین تنخواہ، نوٹیفیکیشنز اور الاؤنسز کی خبریں",
                    onClick = onOpenWhatsappClick
                )

                SettingsActionRow(
                    icon = Icons.Default.Share,
                    title = "ایپ شیئر کریں",
                    subtitle = "ساتھی سرکاری اساتذہ اور ملازمین کو ایپ بھیجیں",
                    onClick = onShareAppClick
                )
            }
        }

        // Group 3: Legal, About & Version
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "معلومات اور پالیسی",
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                SettingsActionRow(
                    icon = Icons.Default.Info,
                    title = "ایپ کے بارے میں (About)",
                    subtitle = "آن لائن پے سلپ کے پی پورٹل کا تعارف",
                    onClick = onOpenAboutClick
                )

                SettingsActionRow(
                    icon = Icons.Default.PrivacyTip,
                    title = "پرائیویسی پالیسی (Privacy Policy)",
                    subtitle = "ڈیٹا سیکیورٹی اور زیرو شیئرنگ کے اصول",
                    onClick = onOpenPrivacyClick
                )

                // Version display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ایپ کا موجودہ ورژن",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 13.5.sp,
                            color = Color(0xFF64748B)
                        )
                    )
                    Text(
                        text = "v$appVersion",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFD1FAE5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF047857),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                    )
                    if (badge != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFEE2E2)
                        ) {
                            Text(
                                text = badge,
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 5. About Portal Dialog
 */
@Composable
fun AboutPortalDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "آن لائن پے سلپ کے پی پورٹل",
                fontFamily = NotoNastaliqFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF047857),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "خیبر پختونخوا کے تمام سرکاری اساتذہ اور ملازمین کے لیے آسان، تیز رفتار اور محفوظ پے سلپ پورٹل۔",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.5.sp,
                    color = Color(0xFF334155),
                    lineHeight = 20.sp
                )
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Text(
                    text = "• ماہانہ تنخواہ اور الاؤنسز کی مکمل تفصیلات\n• ڈسٹرکٹ اکاؤنٹس آفس کی تصدیق شدہ پی ڈی ایف ڈاؤن لوڈنگ\n• محفوظ پاس ورڈ انکرپشن اور خودکار بیک اپ پروٹیکشن",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.5.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 18.sp
                )
                Text(
                    text = "ورژن: 1.0 (Official Government Edition)",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF047857)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ٹھیک ہے", fontWeight = FontWeight.Bold, color = Color(0xFF047857))
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

/**
 * 6. Privacy Policy Dialog
 */
@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "پرائیویسی اور ڈیٹا سیکیورٹی",
                fontFamily = NotoNastaliqFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF047857),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "1. ڈیٹا کی حفاظت:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "آپ کا شناختی کارڈ نمبر اور پاس ورڈ محفوظ ترین SHA-256 انکرپشن کے ساتھ ویریفائی ہوتا ہے۔",
                    fontSize = 12.5.sp,
                    color = Color(0xFF475569)
                )

                Text(
                    text = "2. کوئی پرائیویٹ ڈیٹا شیئرنگ نہیں:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "پورٹل کسی بھی تیسرے فریق کے ساتھ ملازمین کی تنخواہ یا شناختی تفصیلات شیئر نہیں کرتا۔",
                    fontSize = 12.5.sp,
                    color = Color(0xFF475569)
                )

                Text(
                    text = "3. خودکار کلاؤڈ بیک اپ کا تحفظ:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "ایپ سے غیر مجاز یو ایس بی یا بیرونی بیک اپ نکالنا بند کر دیا گیا ہے تاکہ فون کے علاوہ ڈیٹا چوری نہ ہو سکے۔",
                    fontSize = 12.5.sp,
                    color = Color(0xFF475569)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("سمجھ گیا", fontWeight = FontWeight.Bold, color = Color(0xFF047857))
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

/**
 * 7. Help & Contact Dialog
 */
@Composable
fun HelpAndContactDialog(
    onDismiss: () -> Unit,
    onOpenWhatsappClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "مدد اور رہنمائی",
                fontFamily = NotoNastaliqFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF047857),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "اگر آپ کو لاگ ان یا پے سلپ دیکھنے میں کوئی دشواری پیش آئے تو درج ذیل طریقے سے رہنمائی حاصل کریں:",
                    fontSize = 13.sp,
                    color = Color(0xFF334155)
                )
                Text(
                    text = "1. اگر پاس ورڈ بھول گئے ہوں تو 'پاس ورڈ بھول گئے' پر کلک کر کے اپنے 8 ہندسوں کے پرسنل نمبر کے ذریعے ری سیٹ کریں۔",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "2. لائیو اپڈیٹس، الاؤنسز اور سرکاری احکامات کے لیے آفیشل واٹس ایپ چینل جوائن کریں۔",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = {
                        onDismiss()
                        onOpenWhatsappClick()
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFD1FAE5),
                        contentColor = Color(0xFF047857)
                    )
                ) {
                    Text("واٹس ایپ چینل کھولیں", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDismiss) {
                    Text("بند کریں", color = Color(0xFF64748B))
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}
