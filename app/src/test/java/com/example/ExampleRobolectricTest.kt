package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Online Payslip", appName)

    // Test AdsSdkInitializer and UnityAds initialization
    val initializer = com.unity3d.services.core.configuration.AdsSdkInitializer()
    initializer.create(context)
    com.example.util.UnityAdsManager.initialize(context, true)
    Thread.sleep(1500)
  }
}
