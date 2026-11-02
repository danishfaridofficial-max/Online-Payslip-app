package com.example

import android.app.Application
import android.os.Build
import com.example.service.PayslipFirebaseMessagingService
import com.example.util.AppUpdateManager
import com.example.util.UnityAdsManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PayslipApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Install crash shield and init notification store
        UnityAdsManager.installBackgroundCrashShield(this)
        com.example.data.NotificationStore.init(this)

        // 2. Initialize FirebaseApp explicitly & subscribe to FCM topics immediately
        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
            PayslipFirebaseMessagingService.initializeSubscriptions(this)
        } catch (_: Throwable) {}

        // 3. Pre-warm background services asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val crashlytics = FirebaseCrashlytics.getInstance()
                crashlytics.isCrashlyticsCollectionEnabled = true
                crashlytics.setCustomKey("device_model", "${Build.MANUFACTURER} ${Build.MODEL}")
                crashlytics.setCustomKey("android_version", Build.VERSION.RELEASE ?: "unknown")
                crashlytics.setCustomKey("sdk_int", Build.VERSION.SDK_INT)
                crashlytics.setCustomKey("app_version", AppUpdateManager.CURRENT_APP_VERSION_DISPLAY)
            } catch (_: Throwable) {}
        }
    }
}
