package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.theme.NotoNastaliqFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PayslipPortalApp(viewModel: PayslipViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = remember(context) { com.example.util.UnityAdsManager.findActivity(context) }

    val isLoggedIn = state.isHrmisLoggedIn && state.loggedInEmployee != null

    // Modal dialog states
    var showExitDialog by remember { mutableStateOf(false) }
    var showInAppNotificationDialog by remember { mutableStateOf(false) }

    val notifications by com.example.data.NotificationStore.notifications.collectAsStateWithLifecycle()
    val hasUnreadNotifications by com.example.data.NotificationStore.hasUnread.collectAsStateWithLifecycle()
    val openDialogRequested by com.example.data.NotificationStore.openDialogRequested.collectAsStateWithLifecycle()

    LaunchedEffect(openDialogRequested) {
        if (openDialogRequested) {
            showInAppNotificationDialog = true
            com.example.data.NotificationStore.consumeOpenDialogRequest()
        }
    }

    // Remote Force Update state from Master Sheet (M2:O2)
    var updateConfig by remember { mutableStateOf(com.example.util.AppUpdateConfig()) }
    LaunchedEffect(Unit) {
        delay(1000L)
        try {
            updateConfig = com.example.util.AppUpdateManager.checkUpdateFromMasterSheet()
        } catch (_: Throwable) {
            // Never crash if network or sheet check fails
        }
    }

    // Helper Actions
    val onShareAppAction: () -> Unit = {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Online Payslip KP Portal")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "خیبر پختونخوا آن لائن پے سلپ ایپ ڈاؤن لوڈ کریں:\nhttps://github.com/danishfaridofficial-max/Online-Payslip-app/releases/latest/download/OnlinePayslip.apk"
                )
            }
            context.startActivity(Intent.createChooser(shareIntent, "ایپ شیئر کریں"))
        } catch (_: Exception) {
            scope.launch { snackbarHostState.showSnackbar("شیئر کرنے میں مسئلہ پیش آیا") }
        }
    }

    val onOpenWhatsappAction: () -> Unit = {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.bannerLink))
            context.startActivity(intent)
        } catch (_: Exception) {
            scope.launch { snackbarHostState.showSnackbar("لنک کھولنے میں مسئلہ پیش آیا") }
        }
    }

    // Back button behavior: show exit confirmation when on main screen with no overlay open
    BackHandler(
        enabled = !state.showPayslipModal &&
                !state.showWhatsappPopup &&
                !state.showForgotPasswordModal &&
                !state.showChangePasswordModal &&
                !state.showEditEmailModal &&
                !updateConfig.isUpdateRequired &&
                !showInAppNotificationDialog &&
                !showExitDialog
    ) {
        showExitDialog = true
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = Color(0xFFF1F5F9),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.navigationBarsPadding()
                )
            },
            bottomBar = {
                if (isLoggedIn) {
                    UnityBannerAd(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                // Status Bar solid emerald background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(Color(0xFF047857))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    @OptIn(ExperimentalMaterial3Api::class)
                    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                        isRefreshing = state.isPullRefreshing,
                        onRefresh = { viewModel.refreshPortal() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // 1. Unified PortalHeader (Clean single-screen header without drawer button)
                            PortalHeader(
                                currentDate = state.currentDateFormatted,
                                hasUnreadNotifications = hasUnreadNotifications,
                                unreadCount = notifications.count { !it.isRead },
                                onMenuClick = null,
                                onOpenNotificationsClick = {
                                    (context as? com.example.MainActivity)?.requestNotificationPermission()
                                    com.example.data.NotificationStore.markAllAsRead(context)
                                    showInAppNotificationDialog = true
                                },
                                onShareAppClick = onShareAppAction,
                                onOpenWhatsappClick = onOpenWhatsappAction,
                                onOpenSecurityNoticeClick = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("یہ پورٹل ڈیٹا بیس کی تلاش کے لیے محفوظ ترین اصولوں کا استعمال کرتا ہے۔")
                                    }
                                },
                                onRefreshClick = {
                                    viewModel.refreshPortal()
                                }
                            )

                            // 2. Main Content
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 600.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (!isLoggedIn) {
                                    HrmisLoginCard(
                                        state = state,
                                        onCnicChanged = viewModel::onLoginCnicChanged,
                                        onPasswordChanged = viewModel::onLoginPasswordChanged,
                                        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                                        onLoginClick = viewModel::onHrmisLogin,
                                        onForgotPasswordClick = viewModel::onOpenForgotPasswordModal,
                                        onCategorySelected = viewModel::onCategorySelected,
                                        onDistrictSelected = viewModel::onDistrictSelected
                                    )
                                } else {
                                    // Logged-in Single-Screen Dashboard (earlier session layout)
                                    HrmisProfileDashboard(
                                        employee = state.loggedInEmployee!!,
                                        email = state.employeeEmail,
                                        selectedMonth = state.selectedMonth,
                                        availableMonths = state.availableMonths,
                                        hasUnreadNotifications = hasUnreadNotifications,
                                        showSecurityCard = true,
                                        isSalaryLoading = state.isMonthSwitching,
                                        onOpenNotificationsClick = {
                                            (context as? com.example.MainActivity)?.requestNotificationPermission()
                                            com.example.data.NotificationStore.markAllAsRead(context)
                                            showInAppNotificationDialog = true
                                        },
                                        onShareAppClick = onShareAppAction,
                                        onMonthSelect = { month ->
                                            viewModel.switchMonthSummaryOnly(month)
                                        },
                                        onRefreshClick = viewModel::refreshPortal,
                                        onViewPayslipClick = {
                                            viewModel.dismissWhatsappPopup()
                                            viewModel.openPayslipModal()
                                        },
                                        onChangePasswordClick = viewModel::onOpenChangePasswordModal,
                                        onEditEmailClick = viewModel::onOpenEditEmailModal,
                                        onLogoutClick = {
                                            viewModel.onHrmisLogout()
                                        }
                                    )

                                    SearchFormCard(
                                        state = state,
                                        onDistrictSelected = viewModel::onDistrictSelected,
                                        onCategorySelected = viewModel::onCategorySelected,
                                        onMonthSelected = viewModel::onMonthSelected,
                                        onYearSelected = viewModel::onYearSelected,
                                        onSearchQueryChanged = viewModel::onSearchQueryChanged,
                                        onPersonalNumberChanged = viewModel::onPersonalNumberChanged,
                                        onToggleRememberCredentials = viewModel::onToggleRememberCredentials,
                                        onResetClick = viewModel::onResetClick,
                                        onSearchClick = viewModel::onSearchClick,
                                        onDismissAlert = viewModel::clearToast
                                    )

                                    WhatsAppPromotionCard(
                                        heading = state.bannerHeading,
                                        text = state.bannerText,
                                        link = state.bannerLink
                                    )

                                    SecurityNoticeCard()

                                    LiveTrafficWidget(
                                        onlineCount = state.liveTrafficCount,
                                        totalVisits = state.totalVisitsCount
                                    )
                                }
                            }

                            PortalFooter()

                            // Navigation bar bottom spacing
                            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                        }
                    }

                    // Loading Overlay
                    if (state.isLoading) {
                        LoadingOverlay(message = state.loadingMessage)
                    }

                    // HRMIS Forgot Password Overlay
                    if (state.showForgotPasswordModal) {
                        HrmisForgotPasswordOverlay(
                            state = state,
                            onStepSelected = viewModel::onForgotStepSelected,
                            onCnicChanged = viewModel::onForgotCnicChanged,
                            onPersonalNoChanged = viewModel::onForgotPersonalNoChanged,
                            onEmailChanged = viewModel::onForgotEmailChanged,
                            onNewPasswordChanged = viewModel::onForgotNewPasswordChanged,
                            onSubmitVerification = viewModel::onSubmitForgotVerification,
                            onSubmitNewPassword = viewModel::onSubmitForgotNewPassword,
                            onDismiss = viewModel::onDismissForgotPasswordModal
                        )
                    }

                    // HRMIS Change Password Overlay
                    if (state.showChangePasswordModal) {
                        HrmisChangePasswordOverlay(
                            currentPass = state.changeCurrentPassInput,
                            newPass = state.changeNewPassInput,
                            onCurrentPassChange = viewModel::onChangeCurrentPassChanged,
                            onNewPassChange = viewModel::onChangeNewPassChanged,
                            onSubmit = viewModel::onSubmitChangePassword,
                            onDismiss = viewModel::onDismissChangePasswordModal
                        )
                    }

                    // HRMIS Edit Email Overlay
                    if (state.showEditEmailModal) {
                        HrmisEditEmailOverlay(
                            email = state.editEmailInput,
                            onEmailChange = viewModel::onEditEmailInputChanged,
                            onSubmit = viewModel::onSubmitEditEmail,
                            onDismiss = viewModel::onDismissEditEmailModal
                        )
                    }

                    // Payslip Detail Overlay
                    if (state.showPayslipModal && state.currentPayslip != null) {
                        PayslipDetailDialog(
                            data = state.currentPayslip!!,
                            onDismiss = {
                                viewModel.dismissPayslipModal()
                            }
                        )
                    }

                    // In-App Notification Modal Dialog (Clean for users)
                    if (showInAppNotificationDialog) {
                        InAppNotificationDialog(
                            notifications = notifications,
                            onDismiss = { showInAppNotificationDialog = false },
                            onClearAll = {
                                com.example.data.NotificationStore.clearAll(context)
                            }
                        )
                    }

                    // WhatsApp Viral Popup Dialog
                    if (state.showWhatsappPopup) {
                        WhatsAppCongratulationDialog(
                            heading = state.popupHeading,
                            text = state.popupText,
                            link = state.popupLink,
                            onDismiss = {
                                viewModel.dismissWhatsappPopup()
                            }
                        )
                    }

                    // Remote Force Update Dialog
                    if (updateConfig.isUpdateRequired) {
                        com.example.util.ForceUpdateDialog(
                            versionText = updateConfig.remoteVersionText,
                            updateLink = updateConfig.updateLink
                        )
                    }
                }
            }
        }
    }

    // Exit Confirmation Dialog
    if (showExitDialog) {
        ExitConfirmationDialog(
            onDismiss = { showExitDialog = false },
            onConfirmExit = {
                showExitDialog = false
                try {
                    activity?.finishAffinity()
                    activity?.finishAndRemoveTask()
                } catch (_: Throwable) {
                    activity?.finish()
                }
            }
        )
    }

    // Toast Message Snackbar
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }
}

@Composable
fun ExitConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("exit_confirmation_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFFFEF2F2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "ایپ بند کریں؟",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Text(
                        text = "کیا آپ واقعی آن لائن پے سلپ پورٹل سے باہر جانا چاہتے ہیں؟",
                        style = TextStyle(
                            fontFamily = NotoNastaliqFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("exit_dialog_cancel_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF475569)
                        )
                    ) {
                        Text(
                            text = "نہیں",
                            style = TextStyle(
                                fontFamily = NotoNastaliqFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Button(
                        onClick = onConfirmExit,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("exit_dialog_confirm_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        )
                    ) {
                        Text(
                            text = "ہاں",
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

@Composable
fun PortalFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(vertical = 22.dp, horizontal = 16.dp)
            .testTag("portal_footer"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "محفوظ ڈیجیٹل سروسز - آن لائن پے سلپ سسٹم © 2026",
                style = TextStyle(
                    fontFamily = NotoNastaliqFamily,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = "© 2026 Babar Ali. All Rights Reserved | System v${com.example.util.AppUpdateManager.CURRENT_APP_VERSION_DISPLAY}",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
