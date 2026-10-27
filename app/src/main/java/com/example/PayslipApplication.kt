package com.example

import android.app.Application
import android.os.Build
import com.example.util.AppUpdateManager
import com.example.util.UnityAdsManager
import com.google.firebase.crashlytics.FirebaseCrashlytics

class PayslipApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        UnityAdsManager.installBackgroundCrashShield(this)
        com.example.data.NotificationStore.init(this)

        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.isCrashlyticsCollectionEnabled = true
            crashlytics.setCustomKey("device_model", "${Build.MANUFACTURER} ${Build.MODEL}")
            crashlytics.setCustomKey("android_version", Build.VERSION.RELEASE ?: "unknown")
            crashlytics.setCustomKey("sdk_int", Build.VERSION.SDK_INT)
            crashlytics.setCustomKey("app_version", AppUpdateManager.CURRENT_APP_VERSION_DISPLAY)
        } catch (_: Throwable) {
            // Ignore if Firebase is not yet ready on custom ROMs
        }
    }
}
