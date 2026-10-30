package com.example.data.api

import com.example.data.model.BannerResponse
import com.example.data.model.DistrictsResponse
import com.example.data.model.PasswordResponse
import com.example.data.model.PayslipResponse
import com.example.data.model.TrafficResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object PayslipApiClient {
    const val DEFAULT_WEB_APP_URL = "https://script.google.com/macros/s/AKfycbwCsuhGCUK5udkwRy-s6nxTVbAupICjOOm2KL9KdTEaEFx-9UaR8eqrG4s681yuOXk-6g/exec"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    suspend fun getDistricts(webAppUrl: String = DEFAULT_WEB_APP_URL): DistrictsResponse = withContext(Dispatchers.IO) {
        try {
            val url = "$webAppUrl?action=districts"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val adapter = moshi.adapter(DistrictsResponse::class.java)
                adapter.fromJson(body) ?: DistrictsResponse(success = true, districts = listOf("Bannu"))
            }
        } catch (e: Exception) {
            DistrictsResponse(success = false, districts = listOf("Bannu"), error = e.localizedMessage)
        }
    }

    suspend fun getTraffic(userId: String, webAppUrl: String = DEFAULT_WEB_APP_URL): TrafficResponse = withContext(Dispatchers.IO) {
        try {
            val url = "$webAppUrl?action=traffic&source=app&platform=android&userId=${URLEncoder.encode(userId, "UTF-8")}"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string()?.trim() ?: ""
                val adapter = moshi.adapter(TrafficResponse::class.java)
                val parsed = try {
                    adapter.fromJson(body)
                } catch (e: Exception) {
                    null
                }
                parsed ?: TrafficResponse(success = false, userId = userId, error = "Failed to parse traffic response")
            }
        } catch (e: Exception) {
            TrafficResponse(success = false, userId = userId, error = e.localizedMessage)
        }
    }

    suspend fun getBanner(webAppUrl: String = DEFAULT_WEB_APP_URL): BannerResponse = withContext(Dispatchers.IO) {
        try {
            val url = "$webAppUrl?action=banner"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val adapter = moshi.adapter(BannerResponse::class.java)
                adapter.fromJson(body) ?: BannerResponse(
                    success = true,
                    bannerHeading = "اہم اپڈیٹ: ہمارا واٹس ایپ چینل جوائن کریں!",
                    bannerText = "خیبر ایلیمنٹری اینڈ سیکنڈری ایجوکیشن کی تازہ ترین خبروں، نوٹیفیکیشنز اور تبادلوں کی اپڈیٹس کے لیے ابھی ہمارا آفیشل واٹس ایپ چینل جوائن کریں۔",
                    bannerLink = "https://whatsapp.com/channel/0029VaCK4y0EquiKbkl09S1A",
                    popupStatus = "Show",
                    popupHeading = "مبارک ہو! آپ کی پے سلپ مل گئی ہے",
                    popupText = "اگر آپ چاہتے ہیں کہ اگلے مہینے کی سیلری اپڈیٹ نوٹیفیکیشنز سب سے پہلے آپ کے واٹس ایپ پر ملیں، تو ابھی جوائن کریں!",
                    popupLink = "https://whatsapp.com/channel/0029VaCK4y0EquiKbkl09S1A"
                )
            }
        } catch (e: Exception) {
            BannerResponse(
                success = false,
                bannerHeading = "اہم اپڈیٹ: ہمارا واٹس ایپ چینل جوائن کریں!",
                bannerText = "خیبر ایلیمنٹری اینڈ سیکنڈری ایجوکیشن کی تازہ ترین خبروں، نوٹیفیکیشنز اور تبادلوں کی اپڈیٹس کے لیے ابھی ہمارا آفیشل واٹس ایپ چینل جوائن کریں۔",
                bannerLink = "https://whatsapp.com/channel/0029VaCK4y0EquiKbkl09S1A",
                popupStatus = "Show",
                popupHeading = "مبارک ہو! آپ کی پے سلپ مل گئی ہے",
                popupText = "اگر آپ چاہتے ہیں کہ اگلے مہینے کی سیلری اپڈیٹ نوٹیفیکیشنز سب سے پہلے آپ کے واٹس ایپ پر ملیں، تو ابھی جوائن کریں!",
                popupLink = "https://whatsapp.com/channel/0029VaCK4y0EquiKbkl09S1A",
                error = e.localizedMessage
            )
        }
    }

    suspend fun searchPaySlip(
        district: String,
        category: String,
        month: String,
        year: String,
        query: String,
        webAppUrl: String = DEFAULT_WEB_APP_URL
    ): PayslipResponse = withContext(Dispatchers.IO) {
        try {
            val encodedDistrict = URLEncoder.encode(district, "UTF-8")
            val encodedCategory = URLEncoder.encode(category, "UTF-8")
            val encodedMonth = URLEncoder.encode(month, "UTF-8")
            val encodedYear = URLEncoder.encode(year, "UTF-8")
            val cleanQuery = query.replace(Regex("[-\\s]"), "")
            val encodedQuery = URLEncoder.encode(cleanQuery, "UTF-8")

            val url = "$webAppUrl?action=search&district=$encodedDistrict&category=$encodedCategory&month=$encodedMonth&year=$encodedYear&query=$encodedQuery"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val adapter = moshi.adapter(PayslipResponse::class.java)
                val res = adapter.fromJson(body)
                res ?: PayslipResponse(success = false, message = "سرور سے جواب موصول نہیں ہوا۔")
            }
        } catch (e: Exception) {
            PayslipResponse(success = false, message = e.localizedMessage ?: "کنکشن کی خرابی")
        }
    }

    suspend fun verifyPassword(
        cnic: String,
        password: String,
        webAppUrl: String = DEFAULT_WEB_APP_URL
    ): PasswordResponse = withContext(Dispatchers.IO) {
        try {
            val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
            val encodedCnic = URLEncoder.encode(cleanCnic, "UTF-8")
            val encodedPass = URLEncoder.encode(password.trim(), "UTF-8")
            val url = "$webAppUrl?action=verifyPassword&cnic=$encodedCnic&password=$encodedPass"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val adapter = moshi.adapter(PasswordResponse::class.java)
                adapter.fromJson(body) ?: PasswordResponse(success = false, message = "سرور سے تصدیق موصول نہیں ہوئی۔")
            }
        } catch (e: Exception) {
            PasswordResponse(success = false, error = e.localizedMessage)
        }
    }

    suspend fun savePassword(
        cnic: String,
        password: String,
        email: String = "",
        dateTime: String = "",
        webAppUrl: String = DEFAULT_WEB_APP_URL
    ): PasswordResponse = withContext(Dispatchers.IO) {
        try {
            val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
            val encodedCnic = URLEncoder.encode(cleanCnic, "UTF-8")
            val encodedPass = URLEncoder.encode(password.trim(), "UTF-8")
            val encodedEmail = URLEncoder.encode(email.trim(), "UTF-8")
            val encodedDateTime = URLEncoder.encode(dateTime.trim(), "UTF-8")
            val url = "$webAppUrl?action=savePassword&cnic=$encodedCnic&password=$encodedPass&email=$encodedEmail&dateTime=$encodedDateTime"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val adapter = moshi.adapter(PasswordResponse::class.java)
                adapter.fromJson(body) ?: PasswordResponse(success = false, message = "سرور پر محفوظ نہیں ہو سکا۔")
            }
        } catch (e: Exception) {
            PasswordResponse(success = false, error = e.localizedMessage)
        }
    }

    suspend fun saveRecoveryEmail(
        cnic: String,
        email: String,
        dateTime: String = "",
        webAppUrl: String = DEFAULT_WEB_APP_URL
    ): PasswordResponse = withContext(Dispatchers.IO) {
        try {
            val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
            val encodedCnic = URLEncoder.encode(cleanCnic, "UTF-8")
            val encodedEmail = URLEncoder.encode(email.trim(), "UTF-8")
            val encodedDateTime = URLEncoder.encode(dateTime.trim(), "UTF-8")
            val url = "$webAppUrl?action=saveRecoveryEmail&cnic=$encodedCnic&email=$encodedEmail&dateTime=$encodedDateTime"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val adapter = moshi.adapter(PasswordResponse::class.java)
                adapter.fromJson(body) ?: PasswordResponse(success = false, message = "سرور پر محفوظ نہیں ہو سکا۔")
            }
        } catch (e: Exception) {
            PasswordResponse(success = false, error = e.localizedMessage)
        }
    }
}
