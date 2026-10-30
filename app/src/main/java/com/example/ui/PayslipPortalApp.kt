package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    // Exit confirmation dialog state
    var showExitDialog by remember { mutableStateOf(false) }
    var showInAppNotificationDialog by remember { mutableStateOf(false) }
    var showViewPayslipRewardedModal by remember { mutableStateOf(false) }

    val notifications by com.example.data.NotificationStore.notifications.collectAsStateWithLifecycle()
    val hasUnreadNotifications by com.example.data.NotificationStore.hasUnread.collectAsStateWithLifecycle()

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

    // Intercept back button if no child popups/sheets are open
    BackHandler(enabled = !state.showPayslipModal && !state.showWhatsappPopup && !updateConfig.isUpdateRequired && !showInAppNotificationDialog && !showViewPayslipRewardedModal) {
        showExitDialog = true
    }

    Scaffold(
        containerColor = Color(0xFFF1F5F9),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. Header with Gradient and Notification Bell Icon Button
                    PortalHeader(
                        currentDate = state.currentDateFormatted,
                        onOpenNotificationsClick = {
                            (context as? com.example.MainActivity)?.requestNotificationPermission()
                            showInAppNotificationDialog = true
                        },
                        onOpenWhatsappClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.bannerLink))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                scope.launch { snackbarHostState.showSnackbar("لنک کھولنے میں مسئلہ پیش آیا") }
                            }
                        },
                        onOpenSecurityNoticeClick = {
                            scope.launch { snackbarHostState.showSnackbar("یہ پورٹل ڈیٹا بیس کی تلاش کے لیے محفوظ ترین اصولوں کا استعمال کرتا ہے۔") }
                        },
                        onRefreshClick = {
                            viewModel.onResetClick()
                            scope.launch { snackbarHostState.showSnackbar("پورٹل ریفریش ہو گیا ہے") }
                        }
                    )

                    // Main Content Body
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        if (!state.isHrmisLoggedIn || state.loggedInEmployee == null) {
                            // Official Online Payslip Login Card (Strictly Gated - No Bypass)
                            HrmisLoginCard(
                                state = state,
                                onCnicChanged = viewModel::onLoginCnicChanged,
                                onPasswordChanged = viewModel::onLoginPasswordChanged,
                                onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                                onLoginClick = viewModel::onHrmisLogin,
                                onForgotPasswordClick = viewModel::onOpenForgotPasswordModal
                            )
                        } else {
                            // 1. Logged-in Employee Portal Dashboard (Executive Top Bar + Quick Month Pills + Breakdown + Service Profile)
                            HrmisProfileDashboard(
                                employee = state.loggedInEmployee!!,
                                email = state.employeeEmail,
                                selectedMonth = state.selectedMonth,
                                hasUnreadNotifications = hasUnreadNotifications,
                                onOpenNotificationsClick = {
                                    (context as? com.example.MainActivity)?.requestNotificationPermission()
                                    showInAppNotificationDialog = true
                                },
                                onShareAppClick = {
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
                                },
                                onMonthSelect = { month ->
                                    viewModel.switchMonthSummaryOnly(month)
                                },
                                onViewPayslipClick = {
                                    showViewPayslipRewardedModal = true
                                },
                                onChangePasswordClick = viewModel::onOpenChangePasswordModal,
                                onEditEmailClick = viewModel::onOpenEditEmailModal,
                                onLogoutClick = viewModel::onHrmisLogout
                            )

                            // 2. Payslip Month & District Search Form (Clean - No Personal Number needed)
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

                            // 3. WhatsApp Channel Banner Card
                            WhatsAppPromotionCard(
                                heading = state.bannerHeading,
                                text = state.bannerText,
                                link = state.bannerLink
                            )

                            // 4. Security Instructions
                            SecurityNoticeCard()

                            // 5. Live Traffic Counter Widget
                            LiveTrafficWidget(
                                onlineCount = state.liveTrafficCount,
                                totalVisits = state.totalVisitsCount
                            )
                        }
                    }

                    // 6. Footer (Full Width Edge-to-Edge)
                    PortalFooter()
                }

                // Loading Overlay (In-window overlay: prevents multi-Dialog Window crashes on Realme/ColorOS)
                if (state.isLoading) {
                    LoadingOverlay(message = state.loadingMessage)
                }

                // HRMIS Forgot Password Overlay (Dual Reset: Personnel No OR Email)
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

                // Rewarded Video Confirmation Modal for Viewing Complete Payslip
                if (showViewPayslipRewardedModal) {
                    DownloadRewardedAdModal(
                        title = "مکمل پے سلپ دیکھیں",
                        message = "مکمل سیلری سلپ، الاؤنسز اور کٹوتیوں کی تفصیل دیکھنے کے لیے مختصر ویڈیو اشتہار دیکھیں۔\nWatch a quick ad to view your complete payslip.",
                        buttonText = "ویڈیو دیکھیں اور پے سلپ دیکھیں",
                        onWatchAdClick = {
                            showViewPayslipRewardedModal = false
                            com.example.util.UnityAdsManager.showRewardedForDownload(
                                activity = activity,
                                onRewardGranted = {
                                    viewModel.dismissWhatsappPopup()
                                    viewModel.openPayslipModal()
                                },
                                onRewardSkipped = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("مکمل پے سلپ دیکھنے کے لیے پورا اشتہار دیکھنا ضروری ہے۔")
                                    }
                                }
                            )
                        },
                        onDismiss = {
                            showViewPayslipRewardedModal = false
                        }
                    )
                }

                // Payslip Detail Overlay
                if (state.showPayslipModal && state.currentPayslip != null) {
                    LaunchedEffect(state.currentPayslip) {
                        // Wait 700ms so the Payslip overlay renders smoothly first without window collision, then show Ad
                        delay(700L)
                        com.example.util.UnityAdsManager.onSuccessfulSearch(activity)
                    }
                    PayslipDetailDialog(
                        data = state.currentPayslip!!,
                        onDismiss = {
                            viewModel.dismissPayslipModal()
                        }
                    )
                }

                // In-App Notification Modal Dialog
                if (showInAppNotificationDialog) {
                    InAppNotificationDialog(
                        notifications = notifications,
                        onDismiss = { showInAppNotificationDialog = false },
                        onClearAll = {
                            com.example.data.NotificationStore.clearAll(context)
                        }
                    )
                }

                // WhatsApp Congratulation Overlay
                if (state.showWhatsappPopup) {
                    WhatsAppCongratulationDialog(
                        heading = state.popupHeading,
                        text = state.popupText,
                        link = state.popupLink,
                        onDismiss = viewModel::dismissWhatsappPopup
                    )
                }

                // App Exit Confirmation Overlay
                if (showExitDialog) {
                    AppExitDialog(
                        onConfirmExit = {
                            showExitDialog = false
                            try {
                                activity?.finishAffinity()
                                activity?.finishAndRemoveTask()
                            } catch (_: Throwable) {
                                activity?.finish()
                            }
                        },
                        onDismiss = { showExitDialog = false }
                    )
                }

                // Force Update Overlay (Controlled by Master Sheet M2:O2)
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

/**
 * Professional Urdu Exit Confirmation Overlay (In-window, zero Dialog window allocation overhead)
 */
@Composable
fun AppExitDialog(
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit
) {
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
                .fillMaxWidth(0.86f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .testTag("app_exit_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF2F2))
                        .border(1.5.dp, Color(0xFFFECACA), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Exit Application",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "ایپلی کیشن بند کریں؟",
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    )
                )

                Text(
                    text = "کیا آپ واقعی پے سلپ آن لائن پورٹل بند کرنا چاہتے ہیں؟",
                    style = TextStyle(
                        fontFamily = NotoNastaliqFamily,
                        fontSize = 13.5.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
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
                            .height(46.dp)
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
