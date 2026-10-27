package com.example

import android.Manifest
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.service.PayslipFirebaseMessagingService
import com.example.ui.PayslipPortalApp
import com.example.ui.PayslipViewModel
import com.example.ui.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
  private val viewModel: PayslipViewModel by viewModels()

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

  override fun onCreate(savedInstanceState: Bundle?) {
    // Install background thread & SDK crash shield immediately before anything else runs
    com.example.util.UnityAdsManager.installBackgroundCrashShield(this)
    super.onCreate(savedInstanceState)

    // Set emerald status bar color and dark navigation bar safely
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
        var showSplash by rememberSaveable { mutableStateOf(true) }

        if (showSplash) {
          SplashScreen(onTimeout = { showSplash = false })
        } else {
          // Initialize heavy background services right after splash transition so UI stays 60fps smooth
          LaunchedEffect(Unit) {
            requestNotificationPermission()
            delay(200L)
            com.example.util.UnityAdsManager.initialize(this@MainActivity)

            delay(200L)
            try {
              PayslipFirebaseMessagingService.initializeSubscriptions()
            } catch (_: Throwable) {}
          }

          PayslipPortalApp(viewModel = viewModel)
        }
      }
    }
  }
}
