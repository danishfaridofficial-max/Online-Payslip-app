package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.NotificationStore
import com.example.service.PayslipFirebaseMessagingService
import com.example.ui.PayslipPortalApp
import com.example.ui.PayslipViewModel
import com.example.ui.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: PayslipViewModel by viewModels()

  @android.annotation.SuppressLint("InvalidFragmentVersionForActivityResult")
  private val requestNotificationPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
      // Permission handled (either granted or denied)
    }

  fun requestNotificationPermission() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (
          ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
          ) != PackageManager.PERMISSION_GRANTED
        ) {
          requestNotificationPermissionLauncher.launch(
            Manifest.permission.POST_NOTIFICATIONS
          )
        }
      }
    } catch (_: Throwable) {}
  }

  override fun onStart() {
    super.onStart()
    requestNotificationPermission()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIncomingNotification(intent)
  }

  private fun handleIncomingNotification(intent: Intent?) {
    if (intent == null) return
    val extras = intent.extras ?: return

    var foundTitle: String? = null
    var foundBody: String? = null

    for (key in extras.keySet()) {
      val value = extras.getString(key) ?: continue
      val lower = key.lowercase()
      if (foundTitle == null && (lower.contains("title") || lower.contains("header") || lower.contains("subject") || lower == "google.c.a.c_l")) {
        foundTitle = value
      } else if (foundBody == null && (lower.contains("body") || lower.contains("message") || lower.contains("text") || lower.contains("desc") || lower.contains("alert"))) {
        foundBody = value
      }
    }

    if (!foundTitle.isNullOrBlank() || !foundBody.isNullOrBlank()) {
      NotificationStore.addNotification(
        this,
        foundTitle ?: "پے سلپ پورٹل اطلاع",
        foundBody ?: "آپ کے لیے نئی سرکاری اپڈیٹ موجود ہے۔"
      )
      NotificationStore.requestOpenDialog()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    // 1. Install crash shield immediately
    com.example.util.UnityAdsManager.installBackgroundCrashShield(this)
    super.onCreate(savedInstanceState)

    // 2. Handle incoming notification if app was opened by tapping a notification
    handleIncomingNotification(intent)

    // 3. Pre-warm FCM subscriptions safely in the background
    try {
      PayslipFirebaseMessagingService.initializeSubscriptions(this)
    } catch (_: Throwable) {}

    // 4. Set emerald status bar color and dark navigation bar safely
    try {
      val emeraldColor = AndroidColor.parseColor("#047857")
      val darkNavColor = AndroidColor.parseColor("#0F172A")

      enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(emeraldColor),
        navigationBarStyle = SystemBarStyle.dark(darkNavColor)
      )

      val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
      windowInsetsController.isAppearanceLightStatusBars = false
      windowInsetsController.isAppearanceLightNavigationBars = false
    } catch (_: Throwable) {
      // Ignore inset styling issues on older custom OEM ROMs
    }

    setContent {
      MyApplicationTheme {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        var showSplash by rememberSaveable { mutableStateOf(true) }

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = Color(0xFFF1F5F9)
        ) {
          Crossfade(
            targetState = showSplash,
            animationSpec = tween(durationMillis = 400),
            label = "splash_portal_crossfade"
          ) { isSplash ->
            if (isSplash) {
              SplashScreen(
                isReady = uiState.isAppReady,
                onTimeout = { showSplash = false }
              )
            } else {
              LaunchedEffect(Unit) {
                requestNotificationPermission()
                NotificationStore.fetchFcmToken(this@MainActivity)
                // Initialize Unity Ads ONLY AFTER app UI has completely loaded
                com.example.util.UnityAdsManager.initialize(this@MainActivity)
              }
              PayslipPortalApp(viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}
