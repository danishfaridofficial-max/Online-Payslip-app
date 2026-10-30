package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.PayslipData
import com.example.ui.PayslipUiState
import com.example.ui.theme.NotoNastaliqFamily

/**
 * Top Tab switcher between HRMIS Login/Profile and Guest Direct Search
 */
@Composable
fun HrmisPortalModeSelector(
    activeMode: String,
    onModeSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("portal_mode_selector"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Tab 1: KPESE HRMIS
            val isHrmis = activeMode == "hrmis"
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onModeSelected("hrmis") }
                    .testTag("tab_hrmis"),
                shape = RoundedCornerShape(12.dp),
                color = if (isHrmis) Color.White else Color.Transparent,
                shadowElevation = if (isHrmis) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = if (isHrmis) Color(0xFF0F766E) else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "آن لائن پے سلپ لاگ ان",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 13.sp,
                            fontWeight = if (isHrmis) FontWeight.Bold else FontWeight.Normal,
                            color = if (isHrmis) Color(0xFF0F766E) else Color(0xFF64748B)
                        )
                    )
                }
            }

            // Tab 2: Direct Search
            val isSearch = activeMode == "search"
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onModeSelected("search") }
                    .testTag("tab_search"),
                shape = RoundedCornerShape(12.dp),
                color = if (isSearch) Color.White else Color.Transparent,
                shadowElevation = if (isSearch) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = if (isSearch) Color(0xFF0F766E) else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ڈائریکٹ تلاش",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 13.sp,
                            fontWeight = if (isSearch) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSearch) Color(0xFF0F766E) else Color(0xFF64748B)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Official Online Payslip Portal Login Form
 */
@Composable
fun HrmisLoginCard(
    state: PayslipUiState,
    onCnicChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSwitchToGuestSearch: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hrmis_login_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Official Emblem / Logo (Matching App Launcher Icon)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFF0D9488).copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(18.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_payslip_logo),
                        contentDescription = "Online Payslip Portal Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
            }

            Text(
                text = "آن لائن پے سلپ پورٹل",
                style = TextStyle(
                    fontFamily = NotoNastaliqFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF134E4A),
                    lineHeight = 36.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Online Payslip Portal - Login",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Info note about default password rule (KPESE standard)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF0D9488),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "پہلی بار لاگ ان کے لیے یوزرنیم اور پاس ورڈ دونوں آپ کا شناختی کارڈ (CNIC) نمبر ہیں۔",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 11.5.sp,
                            color = Color(0xFF166534),
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Right
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Dynamic Alert / Error Banner inside Login Card
            AnimatedVisibility(
                visible = !state.toastMessage.isNullOrBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                state.toastMessage?.let { msg ->
                    val isError = state.toastType == "error"
                    val isWarning = state.toastType == "warning"
                    val isSuccess = state.toastType == "success"

                    val alertBg = when {
                        isError -> Color(0xFFFEF2F2)
                        isWarning -> Color(0xFFFFFBEB)
                        isSuccess -> Color(0xFFECFDF5)
                        else -> Color(0xFFF0FDFA)
                    }
                    val alertBorder = when {
                        isError -> Color(0xFFFECACA)
                        isWarning -> Color(0xFFFDE68A)
                        isSuccess -> Color(0xFFA7F3D0)
                        else -> Color(0xFF99F6E4)
                    }
                    val alertText = when {
                        isError -> Color(0xFFB91C1C)
                        isWarning -> Color(0xFFB45309)
                        isSuccess -> Color(0xFF047857)
                        else -> Color(0xFF0F766E)
                    }
                    val alertIcon = when {
                        isError -> Icons.Default.ErrorOutline
                        isWarning -> Icons.Default.WarningAmber
                        isSuccess -> Icons.Default.CheckCircle
                        else -> Icons.Default.Info
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_error_banner"),
                        shape = RoundedCornerShape(12.dp),
                        color = alertBg,
                        border = BorderStroke(1.5.dp, alertBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = alertIcon,
                                contentDescription = null,
                                tint = alertText,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = msg,
                                style = TextStyle(
                                    fontFamily = NotoNastaliqFamily,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = alertText,
                                    lineHeight = 22.sp,
                                    textAlign = TextAlign.Right
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Field 1: CNIC Number
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "شناختی کارڈ نمبر (CNIC):",
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Right
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    BasicTextField(
                        value = state.loginCnic,
                        onValueChange = onCnicChanged,
                        textStyle = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(
                                        width = 1.dp,
                                        color = if (state.loginCnic.isNotBlank()) Color(0xFF0D9488) else Color(0xFFCBD5E1),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 13.dp)
                                    .testTag("login_cnic_input")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (state.loginCnic.isEmpty()) {
                                            Text(
                                                text = "13 ہندسوں کا شناختی کارڈ نمبر درج کریں...",
                                                style = TextStyle(
                                                    fontFamily = NotoNastaliqFamily,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = if (state.loginCnic.isNotBlank()) Color(0xFF0D9488) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Field 2: Password
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onForgotPasswordClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "پاس ورڈ بھول گئے؟",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D9488)
                            )
                        )
                    }

                    Text(
                        text = "پاس ورڈ (Password):",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Right
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))

                val isPasswordError = state.toastType == "error" && (state.toastMessage?.contains("پاس ورڈ") == true)

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    BasicTextField(
                        value = state.loginPassword,
                        onValueChange = onPasswordChanged,
                        textStyle = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        ),
                        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onLoginClick()
                            }
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isPasswordError) Color(0xFFFEF2F2) else Color(0xFFF8FAFC))
                                    .border(
                                        width = if (isPasswordError) 1.5.dp else 1.dp,
                                        color = when {
                                            isPasswordError -> Color(0xFFDC2626)
                                            state.loginPassword.isNotBlank() -> Color(0xFF0D9488)
                                            else -> Color(0xFFCBD5E1)
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 13.dp)
                                    .testTag("login_password_input")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (state.loginPassword.isEmpty()) {
                                            Text(
                                                text = "پاس ورڈ درج کریں...",
                                                style = TextStyle(
                                                    fontFamily = NotoNastaliqFamily,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                    IconButton(
                                        onClick = onTogglePasswordVisibility,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password",
                                            tint = if (isPasswordError) Color(0xFFDC2626) else Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }

                if (isPasswordError) {
                    Text(
                        text = "غلط پاس ورڈ! پہلی بار لاگ ان کے لیے اپنا 13 ہندسوں کا شناختی کارڈ نمبر لکھیں۔",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626),
                            textAlign = TextAlign.Right
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }

            // Login Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onLoginClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("hrmis_login_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D9488),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "پورٹل لاگ ان کریں",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * HRMIS Employee Profile & Dashboard
 */
@Composable
fun HrmisProfileDashboard(
    employee: PayslipData,
    email: String?,
    selectedMonth: String = "Oct",
    availableMonths: List<String> = emptyList(),
    hasUnreadNotifications: Boolean = false,
    onOpenNotificationsClick: () -> Unit = {},
    onShareAppClick: () -> Unit = {},
    onMonthSelect: (String) -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onViewPayslipClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onEditEmailClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hrmis_profile_dashboard"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Employee Identity Card (Executive Top Bar - Style A)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            val clipboardManager = LocalClipboardManager.current
            var copiedLabel by remember { mutableStateOf<String?>(null) }
            val initials = remember(employee.name) {
                val words = employee.name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                when {
                    words.size >= 2 -> "${words[0].take(1)}${words[1].take(1)}".uppercase()
                    words.size == 1 -> words[0].take(2).uppercase()
                    else -> "ME"
                }
            }

            LaunchedEffect(copiedLabel) {
                if (copiedLabel != null) {
                    kotlinx.coroutines.delay(2000L)
                    copiedLabel = null
                }
            }

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Row: Avatar (with online indicator) + 3-Tier Employee Info + Logout Button
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Profile Avatar (Left) with Verified Active Dot
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF0F766E), Color(0xFF14B8A6))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    style = TextStyle(
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                            // Active Verified Indicator
                            Box(
                                modifier = Modifier
                                    .size(15.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }

                        // 3-Tier Info Hierarchy (Greeting, Full Name, Designation/Scale)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Line 1: Greeting & Active Status
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "خوش آمدید 👋",
                                    style = TextStyle(
                                        fontFamily = NotoNastaliqFamily,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF0D9488),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "• فعال ملازم",
                                    style = TextStyle(
                                        fontFamily = NotoNastaliqFamily,
                                        fontSize = 10.sp,
                                        color = Color(0xFF16A34A),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            // Line 2: Prominent Full Name
                            Text(
                                text = employee.name,
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Line 3: Designation & Scale Tag
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF0FDFA),
                                border = BorderStroke(0.5.dp, Color(0xFF99F6E4))
                            ) {
                                Text(
                                    text = "${employee.designation} • BPS-${employee.bps}",
                                    style = TextStyle(
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F766E)
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Logout Button (Soft Red Styled)
                        Surface(
                            onClick = onLogoutClick,
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier.testTag("dashboard_logout_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Logout",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "لاگ آؤٹ",
                                    style = TextStyle(
                                        fontFamily = NotoNastaliqFamily,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626)
                                    )
                                )
                            }
                        }
                    }
                }

                // Middle: DDO / Office Station Banner
                val officeLabel = if (employee.ddoTitle.isNotBlank()) {
                    "DDO: ${employee.ddoTitle}${if (employee.ddoCode.isNotBlank()) " (${employee.ddoCode})" else ""}"
                } else {
                    employee.officeSubTitle
                }
                if (officeLabel.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Color(0xFF0D9488),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = officeLabel,
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.5.sp,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Bottom: 1-Tap Copyable Identity Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CopyableDetailChip(
                        title = "شناختی کارڈ (CNIC)",
                        value = employee.cnic,
                        isCopied = copiedLabel == "cnic",
                        onClick = {
                            clipboardManager.setText(AnnotatedString(employee.cnic.replace("-", "")))
                            copiedLabel = "cnic"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    CopyableDetailChip(
                        title = "پرسنل نمبر (Personal No)",
                        value = employee.personnelNo,
                        isCopied = copiedLabel == "personal",
                        onClick = {
                            clipboardManager.setText(AnnotatedString(employee.personnelNo))
                            copiedLabel = "personal"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Quick Month Selector (Horizontal Pills Bar)
        val quickMonths = if (availableMonths.isNotEmpty()) availableMonths else listOf("Oct", "Sep", "Aug", "Jul", "Jun", "May")
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFF0F766E),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "مہینہ تبدیل کریں (فوری سیلری چیک):",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF134E4A)
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "سال 2026",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                quickMonths.forEach { m ->
                    val isSelected = selectedMonth.equals(m, ignoreCase = true) || employee.monthYear.contains(m, ignoreCase = true)
                    Surface(
                        onClick = { onMonthSelect(m) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF0D9488) else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF0F766E) else Color(0xFFCBD5E1)),
                        shadowElevation = if (isSelected) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF5EEAD4),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "$m 2026",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF334155)
                                )
                            )
                        }
                    }
                }
            }
        }

        // 3. Monthly Salary Overview Card (with Visual Take-Home Breakdown Bar)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF042F2E)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header: Month Badge & Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0D9488)
                        ) {
                            Text(
                                text = employee.monthYear.ifBlank { "Current Month" },
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF14B8A6).copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, Color(0xFF2DD4BF))
                        ) {
                            Text(
                                text = "موجودہ سلپ",
                                style = TextStyle(
                                    fontFamily = NotoNastaliqFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5EEAD4)
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "ماہانہ سیلری سمری",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCCFBF1)
                        )
                    )
                }

                // Net Payable Hero Display (Full Width, No Collision)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "خالص تنخواہ (Net Payable)",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF99F6E4)
                        )
                    )
                    val displayNetPay = remember(employee.netPay) {
                        val clean = employee.netPay.replace(",", "").trim()
                        val num = clean.toDoubleOrNull()
                        if (num != null) {
                            String.format(java.util.Locale.ENGLISH, "%,.2f", num)
                        } else {
                            employee.netPay
                        }
                    }
                    Text(
                        text = "Rs. $displayNetPay",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                // Gross Pay & Deductions in 2 Clean Responsive Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Deductions Chip
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = BorderStroke(0.8.dp, Color(0xFFF87171).copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "کل کٹوتیاں (Deductions)",
                                style = TextStyle(
                                    fontFamily = NotoNastaliqFamily,
                                    fontSize = 10.sp,
                                    color = Color(0xFFFCA5A5)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Rs. ${employee.totalDeductions}",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFECACA)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Gross Pay Chip
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF14B8A6).copy(alpha = 0.15f),
                        border = BorderStroke(0.8.dp, Color(0xFF2DD4BF).copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "مجموعی تنخواہ (Gross)",
                                style = TextStyle(
                                    fontFamily = NotoNastaliqFamily,
                                    fontSize = 10.sp,
                                    color = Color(0xFF99F6E4)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Rs. ${employee.grossPay}",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Visual Income vs Deductions Progress Bar (Correct Decimal & Currency Parsing)
                val cleanNet = employee.netPay.replace(",", "").trim()
                val netNum = cleanNet.toDoubleOrNull() ?: cleanNet.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0

                val cleanDed = employee.totalDeductions.replace(",", "").trim()
                val dedNum = cleanDed.toDoubleOrNull() ?: cleanDed.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0

                val cleanGross = employee.grossPay.replace(",", "").trim()
                val grossNum = cleanGross.toDoubleOrNull() ?: cleanGross.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: (netNum + dedNum)

                val netPct = if (grossNum > 0) ((netNum / grossNum) * 100).toInt().coerceIn(5, 95) else 78
                val dedPct = (100 - netPct).coerceAtLeast(5)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF02201E))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "خالص وصولی (Take-home): $netPct%",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5EEAD4)
                            ),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "کٹوتیاں (Deductions): $dedPct%",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFCA5A5),
                                textAlign = TextAlign.End
                            ),
                            maxLines = 1
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF134E4A))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight((netPct / 100f).coerceIn(0.1f, 0.95f))
                                .fillMaxHeight()
                                .background(Color(0xFF2DD4BF))
                        )
                        Box(
                            modifier = Modifier
                                .weight((dedPct / 100f).coerceIn(0.05f, 0.9f))
                                .fillMaxHeight()
                                .background(Color(0xFFF87171))
                        )
                    }
                }

                // View Payslip Action Button
                Button(
                    onClick = onViewPayslipClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0F766E)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color(0xFF0F766E),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مکمل پے سلپ دیکھیں اور PDF ڈاؤنلوڈ کریں",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F766E)
                        )
                    )
                }
            }
        }

        // 4. GPF Balance Card
        if (employee.gpfBalance.isNotBlank()) {
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
                    Column {
                        Text(
                            text = "جی پی فنڈ بیلنس (GPF Balance)",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        )
                        Text(
                            text = "Rs. ${employee.gpfBalance}",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D9488)
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0FDFA),
                        border = BorderStroke(1.dp, Color(0xFFCCFBF1))
                    ) {
                        Text(
                            text = "Acc: ${employee.gpfAccountNo.ifBlank { "Active" }}",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                color = Color(0xFF0F766E),
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 5. Expandable Full Employee Service Profile Drawer
        var isServiceProfileExpanded by remember { mutableStateOf(false) }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isServiceProfileExpanded = !isServiceProfileExpanded }
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isServiceProfileExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF0F766E),
                        modifier = Modifier.size(24.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ملازم کی سروس تفصیلات اور پے سکیل",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF134E4A)
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = Color(0xFF0D9488),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isServiceProfileExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Row 1: Father Name & Date of Birth
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoMiniTile(
                                title = "والد / سرپرست کا نام",
                                value = employee.guardianName ?: "—",
                                modifier = Modifier.weight(1f)
                            )
                            InfoMiniTile(
                                title = "تاریخ پیدائش (DOB)",
                                value = employee.dob.ifBlank { "—" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 2: Service Duration & Employment Category
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoMiniTile(
                                title = "سروس کی مدت",
                                value = employee.lengthOfService.ifBlank { "—" },
                                modifier = Modifier.weight(1f)
                            )
                            InfoMiniTile(
                                title = "ملازمت کی نوعیت",
                                value = employee.employmentCategory.ifBlank { "مستقل (Regular)" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 3: Pay Scale & Stage
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoMiniTile(
                                title = "پے سکیل (Scale)",
                                value = "${employee.payScale} (${employee.payScaleType})",
                                modifier = Modifier.weight(1f)
                            )
                            InfoMiniTile(
                                title = "پے اسٹیج (Stage)",
                                value = employee.payStage ?: "—",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 4: Income Tax details
                        if (employee.incomeTax.recoveredTill.isNotBlank() || employee.incomeTax.payable.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InfoMiniTile(
                                    title = "وصول شدہ انکم ٹیکس",
                                    value = "Rs. ${employee.incomeTax.recoveredTill.ifBlank { "0" }}",
                                    modifier = Modifier.weight(1f)
                                )
                                InfoMiniTile(
                                    title = "واجب الادا ٹیکس",
                                    value = "Rs. ${employee.incomeTax.payable.ifBlank { "0" }}",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Security & Account Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "سیکیورٹی اور پاس ورڈ سیٹنگز",
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Right
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Change Password Action
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onChangePasswordClick() },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "اپنا نیا خفیہ پاس ورڈ سیٹ کریں",
                                style = TextStyle(
                                    fontFamily = NotoNastaliqFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E293B)
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.LockReset,
                                contentDescription = null,
                                tint = Color(0xFF0D9488),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Recovery Email Action
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onEditEmailClick() },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (email.isNullOrBlank()) "ریکوری ای میل شامل کریں (ای میل ری سیٹ کے لیے)" else "ای میل: $email",
                                style = TextStyle(
                                    fontFamily = NotoNastaliqFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E293B)
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.MailOutline,
                                contentDescription = null,
                                tint = Color(0xFF0D9488),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Share App Action Tile
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onShareAppClick() }
                        .testTag("dashboard_share_app_tile"),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEFF6FF)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = Color(0xFF2563EB)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ایپ دوستوں اور ساتھیوں کے ساتھ شیئر کریں",
                                style = TextStyle(
                                    fontFamily = NotoNastaliqFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D4ED8)
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // In-App Notifications Action Tile
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenNotificationsClick() }
                        .testTag("dashboard_notifications_tile"),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDFA)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (hasUnreadNotifications) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEE2E2)
                                ) {
                                    Text(
                                        text = "نیا پیغام",
                                        style = TextStyle(
                                            fontFamily = NotoNastaliqFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = null,
                                tint = Color(0xFF0F766E)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "نوٹیفکیشنز اور اہم اعلانات",
                                style = TextStyle(
                                    fontFamily = NotoNastaliqFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F766E)
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFF0D9488),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CopyableDetailChip(
    title: String,
    value: String,
    isCopied: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (isCopied) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (isCopied) Color(0xFF86EFAC) else Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                )
                Text(
                    text = value.ifBlank { "—" },
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCopied) Color(0xFF15803D) else Color(0xFF1E293B)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = if (isCopied) Color(0xFF16A34A) else Color(0xFF94A3B8),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun InfoMiniTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = NotoNastaliqFamily,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            )
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Dual Forgot Password Overlay: Reset via Personnel Number OR Email
 */
@Composable
fun HrmisForgotPasswordOverlay(
    state: PayslipUiState,
    onStepSelected: (String) -> Unit,
    onCnicChanged: (String) -> Unit,
    onPersonalNoChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onSubmitVerification: () -> Unit,
    onSubmitNewPassword: () -> Unit,
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
                .fillMaxWidth(0.92f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .testTag("forgot_password_modal"),
            shape = RoundedCornerShape(20.dp),
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
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B)
                        )
                    }

                    Text(
                        text = "پاس ورڈ ری سیٹ (Online Payslip)",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F766E),
                            textAlign = TextAlign.Right
                        )
                    )
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                when (state.forgotStep) {
                    "method" -> {
                        Text(
                            text = "اپنا پاس ورڈ ری سیٹ کرنے کے لیے آسان طریقہ منتخب کریں:",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                textAlign = TextAlign.Right
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Option 1: Personnel Number (Recommended / Instant)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onStepSelected("personal_no") },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color(0xFF0D9488),
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "پرسنل نمبر کے ذریعے ری سیٹ کریں (فوری)",
                                        style = TextStyle(
                                            fontFamily = NotoNastaliqFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF166534),
                                            textAlign = TextAlign.Right
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "اپنا شناختی کارڈ اور 8 ہندسوں کا پرسنل نمبر درج کر کے 1 سیکنڈ میں نیا پاس ورڈ بنائیں۔",
                                        style = TextStyle(
                                            fontFamily = NotoNastaliqFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFF475569),
                                            lineHeight = 18.sp,
                                            textAlign = TextAlign.Right
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // Option 2: Email (KPESE standard)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onStepSelected("email") },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MailOutline,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "رجسٹرڈ ای میل کے ذریعے ری سیٹ کریں",
                                        style = TextStyle(
                                            fontFamily = NotoNastaliqFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF334155),
                                            textAlign = TextAlign.Right
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "اگر آپ نے اپنی پروفائل میں ای میل شامل کی ہے تو ای میل تصدیق کے ذریعے ری سیٹ کریں۔",
                                        style = TextStyle(
                                            fontFamily = NotoNastaliqFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            lineHeight = 18.sp,
                                            textAlign = TextAlign.Right
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    "personal_no" -> {
                        Text(
                            text = "شناختی کارڈ اور پرسنل نمبر کی تصدیق:",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                textAlign = TextAlign.Right
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = state.forgotCnicInput,
                                    onValueChange = onCnicChanged,
                                    label = { Text("CNIC (شناختی کارڈ نمبر)") },
                                    placeholder = { Text("13 digit CNIC") },
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F172A)
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF0F172A),
                                        focusedContainerColor = Color(0xFFF8FAFC),
                                        unfocusedContainerColor = Color(0xFFF8FAFC),
                                        focusedBorderColor = Color(0xFF0D9488),
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedLabelColor = Color(0xFF0D9488),
                                        unfocusedLabelColor = Color(0xFF475569),
                                        cursorColor = Color(0xFF0D9488)
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                OutlinedTextField(
                                    value = state.forgotPersonalNoInput,
                                    onValueChange = onPersonalNoChanged,
                                    label = { Text("Personnel No (پرسنل نمبر)") },
                                    placeholder = { Text("8 digit personal number") },
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F172A)
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF0F172A),
                                        focusedContainerColor = Color(0xFFF8FAFC),
                                        unfocusedContainerColor = Color(0xFFF8FAFC),
                                        focusedBorderColor = Color(0xFF0D9488),
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedLabelColor = Color(0xFF0D9488),
                                        unfocusedLabelColor = Color(0xFF475569),
                                        cursorColor = Color(0xFF0D9488)
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }

                        Button(
                            onClick = onSubmitVerification,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                        ) {
                            Text(
                                text = "تصدیق کریں اور آگے بڑھیں",
                                style = TextStyle(fontFamily = NotoNastaliqFamily, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    "email" -> {
                        Text(
                            text = "شناختی کارڈ اور رجسٹرڈ ای میل کی تصدیق:",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                textAlign = TextAlign.Right
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = state.forgotCnicInput,
                                    onValueChange = onCnicChanged,
                                    label = { Text("CNIC (شناختی کارڈ نمبر)") },
                                    placeholder = { Text("13 digit CNIC") },
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F172A)
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF0F172A),
                                        focusedContainerColor = Color(0xFFF8FAFC),
                                        unfocusedContainerColor = Color(0xFFF8FAFC),
                                        focusedBorderColor = Color(0xFF0D9488),
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedLabelColor = Color(0xFF0D9488),
                                        unfocusedLabelColor = Color(0xFF475569),
                                        cursorColor = Color(0xFF0D9488)
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                OutlinedTextField(
                                    value = state.forgotEmailInput,
                                    onValueChange = onEmailChanged,
                                    label = { Text("Registered Email (رجسٹرڈ ای میل)") },
                                    placeholder = { Text("e.g. name@gmail.com") },
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F172A)
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF0F172A),
                                        focusedContainerColor = Color(0xFFF8FAFC),
                                        unfocusedContainerColor = Color(0xFFF8FAFC),
                                        focusedBorderColor = Color(0xFF0D9488),
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedLabelColor = Color(0xFF0D9488),
                                        unfocusedLabelColor = Color(0xFF475569),
                                        cursorColor = Color(0xFF0D9488)
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )
                            }
                        }

                        Button(
                            onClick = onSubmitVerification,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                        ) {
                            Text(
                                text = "ای میل تصدیق کریں",
                                style = TextStyle(fontFamily = NotoNastaliqFamily, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    "new_password" -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "✓ تصدیق کامیاب! اب اپنی مرضی کا نیا پاس ورڈ سیٹ کریں:",
                                style = TextStyle(
                                    fontFamily = NotoNastaliqFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF047857),
                                    textAlign = TextAlign.Right
                                ),
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            OutlinedTextField(
                                value = state.forgotNewPasswordInput,
                                onValueChange = onNewPasswordChanged,
                                label = { Text("New Password (نیا خفیہ پاس ورڈ)") },
                                placeholder = { Text("Enter new password") },
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF0F172A),
                                    unfocusedTextColor = Color(0xFF0F172A),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedBorderColor = Color(0xFF0D9488),
                                    unfocusedBorderColor = Color(0xFFCBD5E1),
                                    focusedLabelColor = Color(0xFF0D9488),
                                    unfocusedLabelColor = Color(0xFF475569),
                                    cursorColor = Color(0xFF0D9488)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = PasswordVisualTransformation()
                            )
                        }

                        Button(
                            onClick = onSubmitNewPassword,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                        ) {
                            Text(
                                text = "نیا پاس ورڈ محفوظ کریں",
                                style = TextStyle(fontFamily = NotoNastaliqFamily, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Overlay to set / change password (shown directly after login or from profile)
 * Features an explicit 'X' (close) button so user can dismiss without setting password if desired
 */
@Composable
fun HrmisChangePasswordOverlay(
    currentPass: String,
    newPass: String,
    onCurrentPassChange: (String) -> Unit,
    onNewPassChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    var isCurrentVisible by remember { mutableStateOf(false) }
    var isNewVisible by remember { mutableStateOf(false) }

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
                ),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            // Force Left-to-Right (LTR) so password typing and card layout are intuitive
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header: Icon + Title on Left, Close 'X' Button on Right
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
                                    .size(38.dp)
                                    .background(Color(0xFFCCFBF1), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockReset,
                                    contentDescription = null,
                                    tint = Color(0xFF0F766E),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Set New Password",
                                    style = TextStyle(
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                )
                                Text(
                                    text = "نیا پاس ورڈ سیٹ کریں",
                                    style = TextStyle(
                                        fontFamily = NotoNastaliqFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0D9488)
                                    )
                                )
                            }
                        }

                        // Explicit 'X' (Close) icon button on the right
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFF1F5F9), CircleShape)
                                .testTag("close_change_password_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "بند کریں (Close)",
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Subtitle explanation
                    Text(
                        text = "اکاؤنٹ کو محفوظ رکھنے کے لیے نیا پاس ورڈ سیٹ کریں۔ اگر ابھی نہیں کرنا چاہتے تو اوپر دیے گئے (✕) پر کلک کر کے بند کر سکتے ہیں۔",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 19.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Field 1: Current Password
                    OutlinedTextField(
                        value = currentPass,
                        onValueChange = onCurrentPassChange,
                        label = { Text("Current Password (موجودہ پاس ورڈ)") },
                        placeholder = { Text("Current / Default CNIC password") },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = Color(0xFF0D9488),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedLabelColor = Color(0xFF0D9488),
                            unfocusedLabelColor = Color(0xFF475569),
                            focusedPlaceholderColor = Color(0xFF94A3B8),
                            unfocusedPlaceholderColor = Color(0xFF94A3B8),
                            cursorColor = Color(0xFF0D9488)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (isCurrentVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isCurrentVisible = !isCurrentVisible }) {
                                Icon(
                                    imageVector = if (isCurrentVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = Color(0xFF64748B)
                                )
                            }
                        }
                    )

                    // Field 2: New Password
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = onNewPassChange,
                        label = { Text("New Password (نیا پاس ورڈ)") },
                        placeholder = { Text("Min 4 characters / کم از کم 4 ہندسے") },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = Color(0xFF0D9488),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedLabelColor = Color(0xFF0D9488),
                            unfocusedLabelColor = Color(0xFF475569),
                            focusedPlaceholderColor = Color(0xFF94A3B8),
                            unfocusedPlaceholderColor = Color(0xFF94A3B8),
                            cursorColor = Color(0xFF0D9488)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (isNewVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isNewVisible = !isNewVisible }) {
                                Icon(
                                    imageVector = if (isNewVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle new password visibility",
                                    tint = Color(0xFF64748B)
                                )
                            }
                        }
                    )

                    // Action Buttons: Dismiss & Save
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF64748B)
                            )
                        ) {
                            Text(
                                text = "Skip / بند کریں",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = onSubmit,
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                        ) {
                            Text(
                                text = "Save / محفوظ کریں",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Overlay to add/edit recovery email
 */
@Composable
fun HrmisEditEmailOverlay(
    email: String,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
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
                .fillMaxWidth(0.88f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "ریکوری ای میل سیٹ کریں",
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F766E),
                        textAlign = TextAlign.Right
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "اگر آپ مستقبل میں پاس ورڈ بھول جائیں تو اس ای میل پر پاس ورڈ ری سیٹ کیا جا سکے گا۔",
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Right
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("Email Address (ای میل ایڈریس)") },
                        placeholder = { Text("e.g. name@gmail.com") },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A)
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = Color(0xFF0D9488),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedLabelColor = Color(0xFF0D9488),
                            unfocusedLabelColor = Color(0xFF475569),
                            cursorColor = Color(0xFF0D9488)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("منسوخ", fontFamily = NotoNastaliqFamily)
                    }

                    Button(
                        onClick = onSubmit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                    ) {
                        Text("محفوظ کریں", fontFamily = NotoNastaliqFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
