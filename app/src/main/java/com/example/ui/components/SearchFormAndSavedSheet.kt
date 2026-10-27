package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.PayslipUiState
import com.example.ui.theme.NotoNastaliqFamily

/**
 * Main Search Card matching the HTML form #searchForm and screenshot
 */
@Composable
fun SearchFormCard(
    state: PayslipUiState,
    onDistrictSelected: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onMonthSelected: (String) -> Unit,
    onYearSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onPersonalNumberChanged: (String) -> Unit = {},
    onToggleRememberCredentials: (Boolean) -> Unit = {},
    onResetClick: () -> Unit,
    onSearchClick: () -> Unit,
    onDismissAlert: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var districtExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    val allCategories = listOf("District", "Federal", "Provincial")
    val allMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val allYears = listOf("2026", "2025")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("search_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(width = 1.dp, color = Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Title in Urdu (Centered like Web App)
            Text(
                text = if (state.isHrmisLoggedIn) "ماہانہ پے سلپ ریکارڈ (سابقہ و موجودہ مہینے)" else "اپنا ضلع منتخب کریں اور پے سلپ تلاش کریں",
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

            // Subtitle Description (Centered like Web App)
            Text(
                text = if (state.isHrmisLoggedIn) {
                    "مطلوبہ مہینہ اور سال منتخب کر کے پے سلپ حاصل کریں۔ آپ کا ریکارڈ آپ کے شناختی کارڈ سے محفوظ اور منسلک ہے۔"
                } else {
                    "اپنی مطلوبہ پے سلپ تلاش کرنے کے لیے نیچے دیے گئے فارم سے متعلقہ ضلع، کیٹیگری، مہینہ، سال کا انتخاب کریں اور نمبر درج کریں۔"
                },
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

            // Row 1: District (Left) + Category (Right) in 2 columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Field 1: District Selector (50% Width)
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel(
                        icon = Icons.Default.LocationOn,
                        label = "ضلع منتخب کریں:"
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        DropdownSelectionBox(
                            value = state.selectedDistrict,
                            testTag = "district_selector",
                            isExpanded = districtExpanded,
                            onClick = { districtExpanded = !districtExpanded }
                        )
                        DropdownMenu(
                            expanded = districtExpanded,
                            onDismissRequest = { districtExpanded = false },
                            modifier = Modifier
                                .widthIn(min = 160.dp, max = 260.dp)
                                .heightIn(max = 240.dp)
                                .background(Color.White),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            state.availableDistricts.forEach { dist ->
                                val isActive = state.activeDistricts.contains(dist)
                                val isSelected = state.selectedDistrict == dist
                                val label = if (isActive) dist else "$dist (جلد آرہا ہے)"
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = label,
                                                style = TextStyle(
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        !isActive -> Color(0xFF94A3B8)
                                                        isSelected -> Color(0xFF0D9488)
                                                        else -> Color(0xFF1E293B)
                                                    }
                                                )
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color(0xFF0D9488),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (isActive) {
                                            onDistrictSelected(dist)
                                            districtExpanded = false
                                        }
                                    },
                                    enabled = isActive,
                                    modifier = Modifier
                                        .testTag("district_option_$dist")
                                        .background(if (isSelected) Color(0xFFF0FDFA) else Color.Transparent)
                                )
                            }
                        }
                    }
                }

                // Field 2: Category Selector (50% Width)
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel(
                        icon = Icons.Default.Layers,
                        label = "کیٹیگری:"
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        DropdownSelectionBox(
                            value = state.selectedCategory,
                            testTag = "category_selector",
                            isExpanded = categoryExpanded,
                            onClick = { categoryExpanded = !categoryExpanded }
                        )
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier
                                .widthIn(min = 150.dp, max = 220.dp)
                                .heightIn(max = 240.dp)
                                .background(Color.White),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            allCategories.forEach { cat ->
                                val isSelected = state.selectedCategory == cat
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = cat,
                                                style = TextStyle(
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color(0xFF0D9488) else Color(0xFF1E293B)
                                                )
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color(0xFF0D9488),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onCategorySelected(cat)
                                        categoryExpanded = false
                                    },
                                    modifier = Modifier
                                        .testTag("category_option_$cat")
                                        .background(if (isSelected) Color(0xFFF0FDFA) else Color.Transparent)
                                )
                            }
                        }
                    }
                }
            }

            // Row 2: Month (Left) + Year (Right) in 2 columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Field 3: Month Selector (50% Width)
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel(
                        icon = Icons.Default.CalendarToday,
                        label = "مہینہ:"
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        DropdownSelectionBox(
                            value = state.selectedMonth,
                            testTag = "month_selector",
                            isExpanded = monthExpanded,
                            onClick = { monthExpanded = !monthExpanded }
                        )
                        DropdownMenu(
                            expanded = monthExpanded,
                            onDismissRequest = { monthExpanded = false },
                            modifier = Modifier
                                .widthIn(min = 140.dp, max = 200.dp)
                                .background(Color.White),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            allMonths.forEach { m ->
                                val isSelected = state.selectedMonth == m
                                DropdownMenuItem(
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = m,
                                                style = TextStyle(
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontSize = 13.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color(0xFF0D9488) else Color(0xFF1E293B)
                                                )
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color(0xFF0D9488),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onMonthSelected(m)
                                        monthExpanded = false
                                    },
                                    modifier = Modifier
                                        .height(38.dp)
                                        .testTag("month_option_$m")
                                        .background(if (isSelected) Color(0xFFF0FDFA) else Color.Transparent)
                                )
                            }
                        }
                    }
                }

                // Field 4: Year Selector (50% Width)
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel(
                        icon = Icons.Default.DateRange,
                        label = "سال:"
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        DropdownSelectionBox(
                            value = state.selectedYear,
                            testTag = "year_selector",
                            isExpanded = yearExpanded,
                            onClick = { yearExpanded = !yearExpanded }
                        )
                        DropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false },
                            modifier = Modifier
                                .widthIn(min = 140.dp, max = 200.dp)
                                .heightIn(max = 240.dp)
                                .background(Color.White),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            allYears.forEach { y ->
                                val isSelected = state.selectedYear == y
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = y,
                                                style = TextStyle(
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color(0xFF0D9488) else Color(0xFF1E293B)
                                                )
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color(0xFF0D9488),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onYearSelected(y)
                                        yearExpanded = false
                                    },
                                    modifier = Modifier
                                        .testTag("year_option_$y")
                                        .background(if (isSelected) Color(0xFFF0FDFA) else Color.Transparent)
                                )
                            }
                        }
                    }
                }
            }

            // Field 5: CNIC / Personal No Input (Locked when logged in for 100% privacy and security)
            if (state.isHrmisLoggedIn) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FieldLabel(
                        icon = Icons.Default.Lock,
                        label = "لاگ ان شدہ شناختی کارڈ (محفوظ):"
                    )

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF0FDF4))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF86EFAC),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .testTag("locked_cnic_badge")
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
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "Verified Logged In",
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = state.loggedInEmployee?.cnic?.ifBlank { state.searchQuery } ?: state.searchQuery,
                                        style = TextStyle(
                                            fontFamily = FontFamily.SansSerif,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF14532D)
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "لاگ ان شدہ 🔒",
                                        style = TextStyle(
                                            fontFamily = NotoNastaliqFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D)
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FieldLabel(
                        icon = Icons.Default.Badge,
                        label = "شناختی کارڈ نمبر درج کریں:"
                    )

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        BasicTextField(
                            value = state.searchQuery,
                            onValueChange = onSearchQueryChanged,
                            textStyle = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B),
                                textAlign = TextAlign.Start
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    focusManager.clearFocus()
                                    onSearchClick()
                                }
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
                                            color = Color(0xFFCBD5E1),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 13.dp)
                                        .testTag("search_query_input")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (state.searchQuery.isEmpty()) {
                                                Text(
                                                    text = "نمبر درج کریں...",
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
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = null,
                                            tint = if (state.searchQuery.isNotBlank()) Color(0xFF0D9488) else Color(0xFF94A3B8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                // Save for Next Time (Checkbox - only needed for guest search)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleRememberCredentials(!state.rememberCredentials) }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = state.rememberCredentials,
                        onCheckedChange = onToggleRememberCredentials,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF0D9488),
                            uncheckedColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "اس فون پر میری معلومات محفوظ رکھیں (اگلی بار کے لیے)",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155),
                            textAlign = TextAlign.Right
                        )
                    )
                }
            }

            // Modern Professional Error / Alert Banner
            AnimatedVisibility(
                visible = !state.toastMessage.isNullOrBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                state.toastMessage?.let { msg ->
                    val isError = state.toastType == "error"
                    val isWarning = state.toastType == "warning"
                    val isSuccess = state.toastType == "success"

                    val bgColor = when {
                        isError -> Color(0xFFFEF2F2)
                        isWarning -> Color(0xFFFFFBEB)
                        isSuccess -> Color(0xFFECFDF5)
                        else -> Color(0xFFF0FDF4)
                    }
                    val borderColor = when {
                        isError -> Color(0xFFFECACA)
                        isWarning -> Color(0xFFFDE68A)
                        isSuccess -> Color(0xFFA7F3D0)
                        else -> Color(0xFFBBF7D0)
                    }
                    val contentColor = when {
                        isError -> Color(0xFFB91C1C)
                        isWarning -> Color(0xFFB45309)
                        isSuccess -> Color(0xFF047857)
                        else -> Color(0xFF15803D)
                    }
                    val alertIcon = when {
                        isError -> Icons.Default.ErrorOutline
                        isWarning -> Icons.Default.WarningAmber
                        isSuccess -> Icons.Default.CheckCircle
                        else -> Icons.Default.Info
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                            .testTag("inline_error_alert"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = alertIcon,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(19.dp)
                        )
                        Text(
                            text = msg,
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = contentColor,
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Right
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissAlert,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "بند کریں",
                                tint = contentColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Action Buttons: Reset and Search
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button: "صاف کریں"
                Button(
                    onClick = onResetClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("reset_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF1F5F9),
                        contentColor = Color(0xFF475569)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "صاف کریں",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF475569)
                            ),
                            maxLines = 1
                        )
                    }
                }

                // Search Button: "تلاش کریں"
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onSearchClick()
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("search_btn"),
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
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.isHrmisLoggedIn) "پے سلپ دیکھیں" else "تلاش کریں",
                                style = TextStyle(
                                    fontFamily = NotoNastaliqFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color.White
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable field label with icon on the right in RTL
 */
@Composable
private fun FieldLabel(
    icon: ImageVector,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF0D9488),
            modifier = Modifier.size(17.dp)
        )
        Text(
            text = label,
            style = TextStyle(
                fontFamily = NotoNastaliqFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = Color(0xFF334155)
            )
        )
    }
}

/**
 * Reusable dropdown selection box with value on left and chevron on right
 */
@Composable
private fun DropdownSelectionBox(
    value: String,
    testTag: String,
    isExpanded: Boolean = false,
    onClick: () -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "arrowRotation"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isExpanded) Color(0xFFF0FDFA) else Color(0xFFF8FAFC))
                .border(
                    width = if (isExpanded) 1.5.dp else 1.dp,
                    color = if (isExpanded) Color(0xFF0D9488) else Color(0xFFCBD5E1),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 13.dp)
                .testTag(testTag)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.5.sp,
                        fontWeight = if (isExpanded) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isExpanded) Color(0xFF0F766E) else Color(0xFF1E293B)
                    )
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Dropdown",
                    tint = if (isExpanded) Color(0xFF0D9488) else Color(0xFF64748B),
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer(rotationZ = rotationState)
                )
            }
        }
    }
}

/**
 * Fullscreen loading overlay matching HTML #loadingOverlay:
 * fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center
 * Inner card: bg-white rounded-2xl p-6 shadow-2xl max-w-sm w-full text-center
 * Spinner: border-4 border-teal-200 border-t-teal-600 rounded-full animate-spin
 * Heading: براہ کرm انتظار فرمائیں...
 * Subtitle: آپ کی پے سلپ تلاش کی جا رہی ہے
 */
@Composable
fun LoadingOverlay(message: String) {
    androidx.activity.compose.BackHandler(enabled = true) {
        // Block back press while actively searching
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .testTag("loading_backdrop_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .widthIn(max = 340.dp)
                .padding(16.dp)
                .testTag("loading_modal_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        color = Color(0xFFCCFBF1),
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(56.dp)
                    )
                    CircularProgressIndicator(
                        color = Color(0xFF0D9488),
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "براہ کرم انتظار فرمائیں...",
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF134E4A),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (message.isNotBlank() && !message.contains("براہ کرم")) message else "آپ کی پے سلپ تلاش کی جا رہی ہے",
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

