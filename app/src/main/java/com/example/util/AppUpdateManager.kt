package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.TimeUnit

data class AppUpdateConfig(
    val isUpdateRequired: Boolean = false,
    val remoteVersionText: String = "1",
    val updateLink: String = ""
)

sealed class UpdateDownloadResult {
    data class VerifiedApk(val file: File) : UpdateDownloadResult()
    data object SignatureMismatch : UpdateDownloadResult()
    data object FallbackToBrowser : UpdateDownloadResult()
}

object AppUpdateManager {
    // Current version of this APK. When M2 in Google Sheet is greater than 1 (e.g., 2) and N2 is "on", Force Update triggers!
    const val CURRENT_APP_VERSION = 1.0
    const val CURRENT_APP_VERSION_DISPLAY = "1.0"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun isTrustedHttpsUrl(rawUrl: String?): Boolean {
        if (rawUrl.isNullOrBlank()) return false
        return try {
            val uri = Uri.parse(rawUrl.trim())
            uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }

    private fun extractSignatureSha256Digests(signatures: Array<out Signature>?): Set<String> {
        if (signatures.isNullOrEmpty()) return emptySet()
        val md = MessageDigest.getInstance("SHA-256")
        return signatures.map { sig ->
            md.digest(sig.toByteArray()).joinToString("") { "%02x".format(it) }
        }.toSet()
    }

    @Suppress("DEPRECATION")
    fun verifyApkSignature(context: Context, apkFile: File): Boolean {
        if (!apkFile.exists() || apkFile.length() < 100 * 1024) return false
        return try {
            val pm = context.packageManager
            val installedDigests: Set<String>
            val archiveDigests: Set<String>

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val installedInfo = pm.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                val installedSigs = installedInfo.signingInfo?.let { info ->
                    if (info.hasMultipleSigners()) info.apkContentsSigners else info.signingCertificateHistory
                }
                installedDigests = extractSignatureSha256Digests(installedSigs)

                val archiveInfo = pm.getPackageArchiveInfo(
                    apkFile.absolutePath,
                    PackageManager.GET_SIGNING_CERTIFICATES
                ) ?: return false
                if (archiveInfo.packageName != context.packageName) return false

                val archiveSigs = archiveInfo.signingInfo?.let { info ->
                    if (info.hasMultipleSigners()) info.apkContentsSigners else info.signingCertificateHistory
                }
                archiveDigests = extractSignatureSha256Digests(archiveSigs)
            } else {
                val installedInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
                installedDigests = extractSignatureSha256Digests(installedInfo.signatures)

                val archiveInfo = pm.getPackageArchiveInfo(
                    apkFile.absolutePath,
                    PackageManager.GET_SIGNATURES
                ) ?: return false
                if (archiveInfo.packageName != context.packageName) return false
                archiveDigests = extractSignatureSha256Digests(archiveInfo.signatures)
            }

            installedDigests.isNotEmpty() &&
                archiveDigests.isNotEmpty() &&
                archiveDigests.intersect(installedDigests).isNotEmpty()
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Reads cells M2 (App Version), N2 (Update Status), and O2 (Update APK Link)
     * securely via Apps Script so the Google Master Sheet can stay Private (Restricted).
     */
    suspend fun checkUpdateFromMasterSheet(): AppUpdateConfig = withContext(Dispatchers.IO) {
        try {
            val res = com.example.data.api.PayslipApiClient.getAppUpdate()
            if (res.success) {
                val versionStr = res.version?.trim() ?: "1"
                val statusStr = res.status?.trim()?.lowercase(Locale.ROOT) ?: "off"
                val linkStr = res.updateLink?.trim() ?: ""

                val remoteVersion = versionStr.toDoubleOrNull() ?: 1.0
                val isStatusActive = statusStr in setOf("on", "show", "true", "yes", "active", "force", "update")

                if (isStatusActive && remoteVersion > CURRENT_APP_VERSION && isTrustedHttpsUrl(linkStr)) {
                    return@withContext AppUpdateConfig(
                        isUpdateRequired = true,
                        remoteVersionText = versionStr,
                        updateLink = linkStr
                    )
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        AppUpdateConfig(isUpdateRequired = false)
    }

    /**
     * Resolves a MediaFire share URL to its direct HTTPS download link if possible.
     */
    private fun resolveDirectApkUrl(originalUrl: String): String {
        if (!isTrustedHttpsUrl(originalUrl)) return ""
        if (!originalUrl.contains("mediafire.com", ignoreCase = true)) {
            return originalUrl
        }
        return try {
            val req = Request.Builder()
                .url(originalUrl)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
                )
                .build()
            httpClient.newCall(req).execute().use { res ->
                val html = res.body?.string() ?: return originalUrl
                // Match MediaFire direct HTTPS download button href="https://download....mediafire.com/..."
                val regex = Regex("""href=["'](https://download[0-9]*\.mediafire\.com/[^"']+)["']""", RegexOption.IGNORE_CASE)
                val match = regex.find(html)
                val resolved = match?.groupValues?.getOrNull(1)
                if (isTrustedHttpsUrl(resolved)) resolved!! else originalUrl
            }
        } catch (e: Exception) {
            originalUrl
        }
    }

    /**
     * Downloads the APK with live progress callback (0f..1f, downloadedMB, totalMB),
     * enforces HTTPS-only URLs, and verifies the downloaded APK's signing certificate.
     */
    suspend fun downloadUpdateApk(
        context: Context,
        updateUrl: String,
        onProgress: (progress: Float, downloadedMb: String, totalMb: String, statusText: String) -> Unit
    ): UpdateDownloadResult = withContext(Dispatchers.IO) {
        try {
            if (!isTrustedHttpsUrl(updateUrl)) {
                return@withContext UpdateDownloadResult.FallbackToBrowser
            }

            withContext(Dispatchers.Main) {
                onProgress(0.05f, "0.0 MB", "...", "اپ ڈیٹ لنک چیک کیا جا رہا ہے...")
            }

            val directUrl = resolveDirectApkUrl(updateUrl)
            if (!isTrustedHttpsUrl(directUrl)) {
                return@withContext UpdateDownloadResult.FallbackToBrowser
            }

            val request = Request.Builder()
                .url(directUrl)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
                )
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext UpdateDownloadResult.FallbackToBrowser
                if (!response.request.url.isHttps) return@withContext UpdateDownloadResult.FallbackToBrowser

                val contentType = response.header("Content-Type") ?: ""
                if (contentType.contains("text/html", ignoreCase = true)) {
                    // Server returned a web page instead of raw APK binary -> fallback to browser after progress
                    return@withContext UpdateDownloadResult.FallbackToBrowser
                }

                val body = response.body ?: return@withContext UpdateDownloadResult.FallbackToBrowser
                val totalBytes = body.contentLength()
                val totalMbStr = if (totalBytes > 0) {
                    String.format(Locale.US, "%.1f MB", totalBytes / (1024.0 * 1024.0))
                } else {
                    "APK"
                }

                val outFile = File(context.cacheDir, "Online_Payslip_Update.apk")
                if (outFile.exists()) outFile.delete()

                body.byteStream().use { input ->
                    FileOutputStream(outFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var downloadedBytes = 0L
                        var read: Int
                        var lastEmitTime = 0L

                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloadedBytes += read

                            val now = System.currentTimeMillis()
                            if (now - lastEmitTime > 80) {
                                lastEmitTime = now
                                val progress = if (totalBytes > 0) {
                                    (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0.05f, 0.99f)
                                } else {
                                    0.6f
                                }
                                val dlMbStr = String.format(Locale.US, "%.1f MB", downloadedBytes / (1024.0 * 1024.0))
                                withContext(Dispatchers.Main) {
                                    onProgress(progress, dlMbStr, totalMbStr, "نیا ورژن ڈاؤن لوڈ ہو رہا ہے...")
                                }
                            }
                        }
                        output.flush()
                    }
                }

                // Verify file is a valid APK (> 100 KB) and signed with matching certificate
                if (outFile.exists() && outFile.length() > 100 * 1024) {
                    if (!verifyApkSignature(context, outFile)) {
                        outFile.delete()
                        return@withContext UpdateDownloadResult.SignatureMismatch
                    }
                    val finalMbStr = String.format(Locale.US, "%.1f MB", outFile.length() / (1024.0 * 1024.0))
                    withContext(Dispatchers.Main) {
                        onProgress(1.0f, finalMbStr, finalMbStr, "ڈاؤن لوڈ مکمل! انسٹالر کھولا جا رہا ہے...")
                    }
                    return@withContext UpdateDownloadResult.VerifiedApk(outFile)
                } else if (outFile.exists()) {
                    outFile.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        UpdateDownloadResult.FallbackToBrowser
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            if (!verifyApkSignature(context, apkFile)) {
                if (apkFile.exists()) apkFile.delete()
                Toast.makeText(
                    context,
                    "سیکیورٹی الرٹ: اپ ڈیٹ فائل کی تصدیق ناکام ہو گئی",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val permIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(permIntent)
                    return
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openInBrowser(context: Context, url: String) {
        try {
            val trimmed = url.trim()
            if (!isTrustedHttpsUrl(trimmed)) {
                Toast.makeText(
                    context,
                    "سیکیورٹی الرٹ: صرف محفوظ HTTPS لنک کی اجازت ہے",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trimmed)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun ForceUpdateDialog(
    versionText: String,
    updateLink: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isDownloading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var downloadedMb by remember { mutableStateOf("0.0 MB") }
    var totalMb by remember { mutableStateOf("...") }
    var statusText by remember { mutableStateOf("اپ ڈیٹ کے لیے تیار") }
    var downloadedApkFile by remember { mutableStateOf<File?>(null) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 250),
        label = "update_progress"
    )

    // Non-dismissible Force Update Overlay (In-window to prevent Dialog window crashes)
    androidx.activity.compose.BackHandler(enabled = true) {
        // Locked: Cannot be dismissed without updating
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .widthIn(max = 420.dp)
                .testTag("force_update_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Emerald Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF065F46), Color(0xFF047857))
                            )
                        )
                        .padding(vertical = 22.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f))
                                .border(2.dp, Color(0xFFFBBF24), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "Update Available",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = Color(0xFFFBBF24),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "NEW UPDATE v$versionText AVAILABLE",
                                color = Color(0xFF064E3B),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "نیا اپ ڈیٹ ورژن دستیاب ہے!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Body Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "بہتر کارکردگی، نئی امپروومنٹس اور بگز فکس کے لیے ایپ کا نیا ورژن اپ ڈیٹ کریں۔",
                        color = Color(0xFF334155),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Progress Bar Section (Visible when downloading or completed)
                    if (isDownloading || progress > 0f) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF0FDF4))
                                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = statusText,
                                    color = Color(0xFF065F46),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${(animatedProgress * 100).toInt()}%",
                                    color = Color(0xFF047857),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = Color(0xFF10B981),
                                trackColor = Color(0xFFD1FAE5)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$downloadedMb / $totalMb",
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Official APK v$versionText",
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Primary Action Button: Update Now / Install Now
                    Button(
                        onClick = {
                            val existingFile = downloadedApkFile
                            if (existingFile != null && existingFile.exists()) {
                                AppUpdateManager.installApk(context, existingFile)
                                return@Button
                            }

                            if (!isDownloading) {
                                isDownloading = true
                                scope.launch {
                                    when (val dlResult = AppUpdateManager.downloadUpdateApk(
                                        context = context,
                                        updateUrl = updateLink,
                                        onProgress = { p, dlMb, totMb, msg ->
                                            progress = p
                                            downloadedMb = dlMb
                                            totalMb = totMb
                                            statusText = msg
                                        }
                                    )) {
                                        is UpdateDownloadResult.VerifiedApk -> {
                                            downloadedApkFile = dlResult.file
                                            isDownloading = false
                                            AppUpdateManager.installApk(context, dlResult.file)
                                        }
                                        is UpdateDownloadResult.SignatureMismatch -> {
                                            downloadedApkFile = null
                                            progress = 0f
                                            statusText = "سیکیورٹی الرٹ: اپ ڈیٹ فائل کی تصدیق ناکام ہو گئی"
                                            isDownloading = false
                                            Toast.makeText(
                                                context,
                                                "سیکیورٹی الرٹ: اپ ڈیٹ فائل کی تصدیق ناکام ہو گئی",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        is UpdateDownloadResult.FallbackToBrowser -> {
                                            if (AppUpdateManager.isTrustedHttpsUrl(updateLink)) {
                                                for (step in 15..100 step 15) {
                                                    progress = step / 100f
                                                    statusText = "ڈاؤن لوڈ سرور سے منسلک ہو رہا ہے..."
                                                    delay(90)
                                                }
                                                progress = 1f
                                                statusText = "ڈاؤن لوڈ لنک کھولا جا رہا ہے..."
                                                isDownloading = false
                                                AppUpdateManager.openInBrowser(context, updateLink)
                                            } else {
                                                progress = 0f
                                                statusText = "غیر محفوظ اپ ڈیٹ لنک مسترد کر دیا گیا"
                                                isDownloading = false
                                                Toast.makeText(
                                                    context,
                                                    "سیکیورٹی الرٹ: صرف محفوظ HTTPS لنک کی اجازت ہے",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isDownloading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("update_now_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            disabledContainerColor = Color(0xFF6EE7B7)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Download Update",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when {
                                downloadedApkFile != null -> "ابھی انسٹال کریں (Install Update)"
                                isDownloading -> "ڈاؤن لوڈ ہو رہا ہے... (${(animatedProgress * 100).toInt()}%)"
                                else -> "ابھی اپ ڈیٹ کریں (Update Now)"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Direct Browser Fallback Button
                    TextButton(
                        onClick = { AppUpdateManager.openInBrowser(context, updateLink) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("update_browser_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Open in Browser",
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ڈائریکٹ براؤزر سے ڈاؤن لوڈ کریں (MediaFire Link)",
                            color = Color(0xFF047857),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
