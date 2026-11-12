package com.example.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsLoadOptions
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Safely unwraps ContextWrapper to find the host Activity without throwing.
 */
fun Context.findActivity(): Activity? {
    return try {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        null
    } catch (_: Throwable) {
        null
    }
}

/**
 * Robust Unity Ads Manager featuring:
 * 1. Placement Candidate Fallback: Automatically tests standard Android placements
 *    (e.g. "Rewarded_Android", "rewardedVideo") so placement naming differences in Unity Dashboard work seamlessly.
 * 2. Intelligent Circuit Breakers: Permanently stops loading requests if placements are not configured
 *    on the Unity Dashboard, eliminating `E/UnityAds: placement not found` logcat errors.
 * 3. Zero-Friction User Experience: Instant graceful pass-through on PDF downloads so employees
 *    are NEVER blocked from getting their salary statements.
 * 4. Background crash shielding for rock-solid stability across diverse Android devices.
 */
object UnityAdsManager {
    private const val TAG = "UnityAdsManager"
    private val mainHandler = Handler(Looper.getMainLooper())

    fun findActivity(context: Context): Activity? = context.findActivity()

    // =========================================================================
    // 1. Unity Ads Configuration & Candidate Placements
    // =========================================================================
    const val DEFAULT_GAME_ID = "800388050"
    const val DEFAULT_REWARDED_PLACEMENT = "BP_Rewarded_Android"
    const val DEFAULT_INTERSTITIAL_PLACEMENT = "BP_Interstitial_Android"
    const val DEFAULT_BANNER_PLACEMENT = "BP_Banner_Android"

    @Volatile private var configuredGameId: String? = null
    val GAME_ID: String get() = configuredGameId ?: DEFAULT_GAME_ID

    val TEST_MODE: Boolean get() = com.example.BuildConfig.DEBUG

    // Priority placement candidates: Exact Unity Dashboard Placement IDs first, fallbacks second
    val REWARDED_CANDIDATES = listOf(
        "BP_Rewarded_Android",
        "Rewarded_Android",
        "rewardedVideo",
        "Rewarded"
    )

    val INTERSTITIAL_CANDIDATES = listOf(
        "BP_Interstitial_Android",
        "Interstitial_Android",
        "video",
        "Interstitial"
    )

    val BANNER_CANDIDATES = listOf(
        "BP_Banner_Android",
        "Banner_Android",
        "banner",
        "Banner"
    )

    private val _isSdkInitialized = MutableStateFlow(false)
    val isSdkInitialized: StateFlow<Boolean> = _isSdkInitialized.asStateFlow()

    // Circuit Breakers & Rewarded Ad state
    @Volatile var isRewardedSupported: Boolean = true
        private set
    @Volatile private var rewardedCandidateIndex = 0
    @Volatile var activeRewardedPlacement: String = REWARDED_CANDIDATES[0]
        private set
    @Volatile private var rewardedObjectId: String = ""
    @Volatile var isRewardedLoaded: Boolean = false
        private set
    @Volatile private var isLoadingRewarded: Boolean = false

    // Circuit Breakers & Interstitial Ad state
    @Volatile var isInterstitialSupported: Boolean = true
        private set
    @Volatile private var interstitialCandidateIndex = 0
    @Volatile var activeInterstitialPlacement: String = INTERSTITIAL_CANDIDATES[0]
        private set
    @Volatile private var interstitialObjectId: String = ""
    @Volatile var isInterstitialLoaded: Boolean = false
        private set
    @Volatile private var isLoadingInterstitial: Boolean = false

    // Circuit Breakers & Banner Ad state
    @Volatile var isBannerSupported: Boolean = true
        private set
    @Volatile private var bannerCandidateIndex = 0
    @Volatile var activeBannerPlacement: String = BANNER_CANDIDATES[0]
        private set

    @Volatile private var isInitializing = false
    @Volatile private var crashShieldInstalled = false
    private var searchCount = 0
    private var currentTestMode = TEST_MODE

    /**
     * Programmatically configure or update Unity Ads credentials if provided by the user.
     * Automatically resets circuit breakers to retry loading with the new credentials.
     */
    fun configure(
        gameId: String? = null,
        rewardedPlacement: String? = null,
        interstitialPlacement: String? = null,
        bannerPlacement: String? = null,
        context: Context? = null
    ) {
        if (!gameId.isNullOrBlank()) {
            configuredGameId = gameId.trim()
        }
        if (!rewardedPlacement.isNullOrBlank()) {
            activeRewardedPlacement = rewardedPlacement.trim()
        }
        if (!interstitialPlacement.isNullOrBlank()) {
            activeInterstitialPlacement = interstitialPlacement.trim()
        }
        if (!bannerPlacement.isNullOrBlank()) {
            activeBannerPlacement = bannerPlacement.trim()
        }

        // Reset circuit breakers to allow trying new configuration
        isRewardedSupported = true
        isInterstitialSupported = true
        isBannerSupported = true
        rewardedCandidateIndex = 0
        interstitialCandidateIndex = 0
        bannerCandidateIndex = 0

        if (context != null) {
            initialize(context, currentTestMode)
        }
    }

    /**
     * Installs a global UncaughtExceptionHandler so that background SDK threads
     * never crash the application on Android devices.
     */
    fun installBackgroundCrashShield(context: Context) {
        if (crashShieldInstalled) return
        crashShieldInstalled = true

        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val isMainThread = thread == Looper.getMainLooper().thread
                val stackTraceString = Log.getStackTraceString(throwable)
                val isAdOrSdkCrash = stackTraceString.contains("com.unity3d", ignoreCase = true) ||
                        stackTraceString.contains("datastore", ignoreCase = true) ||
                        stackTraceString.contains("protobuf", ignoreCase = true) ||
                        stackTraceString.contains("androidx.work", ignoreCase = true) ||
                        stackTraceString.contains("cronet", ignoreCase = true) ||
                        stackTraceString.contains("chromium", ignoreCase = true) ||
                        stackTraceString.contains("WebView", ignoreCase = true) ||
                        stackTraceString.contains("adservices", ignoreCase = true)

                if (!isMainThread || isAdOrSdkCrash) {
                    Log.w(TAG, "Shielded app from background SDK crash on thread ${thread.name}: ${throwable.message}")
                    try {
                        val crashlytics = com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                        crashlytics.log("Shielded background crash on thread: ${thread.name}")
                        crashlytics.recordException(throwable)
                    } catch (_: Throwable) {}
                    return@setDefaultUncaughtExceptionHandler
                }
            } catch (_: Throwable) {
                return@setDefaultUncaughtExceptionHandler
            }

            try {
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(throwable)
            } catch (_: Throwable) {}
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    /**
     * Checks if the current device is a low-RAM device (e.g. Android Go or <= 2GB RAM).
     */
    fun isLowRamDevice(context: Context): Boolean {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            am?.isLowRamDevice == true
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Checks if running on an Android emulator to avoid loading ads in tests.
     */
    fun isEmulator(): Boolean {
        return try {
            val buildFingerprint = android.os.Build.FINGERPRINT ?: ""
            val buildModel = android.os.Build.MODEL ?: ""
            val buildBrand = android.os.Build.BRAND ?: ""
            val buildDevice = android.os.Build.DEVICE ?: ""
            val buildProduct = android.os.Build.PRODUCT ?: ""
            val buildHardware = android.os.Build.HARDWARE ?: ""
            buildFingerprint.startsWith("generic") ||
                    buildFingerprint.startsWith("unknown") ||
                    buildModel.contains("google_sdk", ignoreCase = true) ||
                    buildModel.contains("Emulator", ignoreCase = true) ||
                    buildModel.contains("Android SDK built for x86", ignoreCase = true) ||
                    buildHardware.contains("goldfish", ignoreCase = true) ||
                    buildHardware.contains("ranchu", ignoreCase = true) ||
                    buildBrand.startsWith("generic") && buildDevice.startsWith("generic") ||
                    "google_sdk" == buildProduct
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Initializes Unity Ads SDK and preloads Rewarded and Interstitial ads.
     * Skips initialization on low-RAM devices or emulators to avoid WebView OOM / AdServices binding errors.
     */
    fun initialize(context: Context, testMode: Boolean = TEST_MODE) {
        installBackgroundCrashShield(context)
        val appContext = context.applicationContext ?: return

        // Skip Unity Ads in emulator environment to prevent AdServices MEASUREMENT_SERVICE binding errors
        if (isEmulator()) {
            Log.i(TAG, "Emulator environment detected. Skipping Unity Ads initialization to preserve stability.")
            return
        }

        // Skip Unity Ads on low-RAM devices to protect stability and prevent OS kill
        if (isLowRamDevice(appContext)) {
            Log.i(TAG, "Low RAM device detected. Skipping Unity Ads initialization to preserve memory.")
            return
        }

        if (GAME_ID.isBlank()) {
            Log.i(TAG, "Unity Ads Game ID is empty; skipping ad initialization.")
            return
        }

        if (isInitializing) return
        currentTestMode = testMode

        mainHandler.post {
            try {
                if (UnityAds.isInitialized) {
                    _isSdkInitialized.value = true
                    preloadAllAds()
                    return@post
                }

                isInitializing = true
                UnityAds.initialize(
                    appContext,
                    GAME_ID,
                    testMode,
                    object : IUnityAdsInitializationListener {
                        override fun onInitializationComplete() {
                            isInitializing = false
                            _isSdkInitialized.value = true
                            Log.d(TAG, "Unity Ads initialized successfully for Game ID: $GAME_ID")
                            mainHandler.postDelayed({ preloadAllAds() }, 500L)
                        }

                        override fun onInitializationFailed(
                            error: UnityAds.UnityAdsInitializationError?,
                            message: String?
                        ) {
                            isInitializing = false
                            Log.w(TAG, "Unity Ads init failed: $error - $message")
                        }
                    }
                )
            } catch (t: Throwable) {
                isInitializing = false
                Log.w(TAG, "Unity Ads init safely caught: ${t.message}")
            }
        }
    }

    /**
     * Preloads both Rewarded and Interstitial ads if supported on current dashboard.
     */
    fun preloadAllAds() {
        if (isRewardedSupported && !isLoadingRewarded && !isRewardedLoaded) {
            loadRewardedAd()
        }
        if (isInterstitialSupported && !isLoadingInterstitial && !isInterstitialLoaded) {
            loadInterstitialAd()
        }
    }

    // =========================================================================
    // 2. Rewarded Video Loading with Candidate Cycling & Circuit Breaker
    // =========================================================================
    fun loadRewardedAd() {
        try {
            if (!UnityAds.isInitialized || !isRewardedSupported || isLoadingRewarded || isRewardedLoaded) return
            if (rewardedCandidateIndex >= REWARDED_CANDIDATES.size) {
                isRewardedSupported = false
                Log.i(TAG, "All rewarded ad placement candidates exhausted on Unity Dashboard. Pausing further rewarded requests.")
                return
            }

            val placement = REWARDED_CANDIDATES[rewardedCandidateIndex]
            isLoadingRewarded = true

            val objectId = UUID.randomUUID().toString()
            rewardedObjectId = objectId
            val loadOptions = UnityAdsLoadOptions().apply {
                setObjectId(objectId)
            }

            Log.d(TAG, "Attempting load for Rewarded candidate: $placement (index $rewardedCandidateIndex)")
            UnityAds.load(
                placement,
                loadOptions,
                object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placementId: String?) {
                        isLoadingRewarded = false
                        isRewardedLoaded = true
                        isRewardedSupported = true
                        activeRewardedPlacement = placementId ?: placement
                        Log.d(TAG, "Rewarded Ad LOADED successfully: $activeRewardedPlacement")
                    }

                    override fun onUnityAdsFailedToLoad(
                        placementId: String?,
                        error: UnityAds.UnityAdsLoadError?,
                        message: String?
                    ) {
                        isLoadingRewarded = false
                        isRewardedLoaded = false
                        Log.i(TAG, "Rewarded candidate $placementId not available ($error): $message")

                        // Try next candidate placement name
                        rewardedCandidateIndex++
                        if (rewardedCandidateIndex < REWARDED_CANDIDATES.size) {
                            mainHandler.postDelayed({ loadRewardedAd() }, 300L)
                        } else {
                            // Trip circuit breaker: all placements exhausted for current Game ID
                            isRewardedSupported = false
                            Log.i(TAG, "Rewarded placements not configured on Unity Dashboard for Game ID $GAME_ID. Pausing requests.")
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            isLoadingRewarded = false
            isRewardedLoaded = false
            Log.w(TAG, "loadRewardedAd safely caught: ${t.message}")
        }
    }

    // =========================================================================
    // 3. Interstitial Loading with Candidate Cycling & Circuit Breaker
    // =========================================================================
    fun loadInterstitialAd() {
        try {
            if (!UnityAds.isInitialized || !isInterstitialSupported || isLoadingInterstitial || isInterstitialLoaded) return
            if (interstitialCandidateIndex >= INTERSTITIAL_CANDIDATES.size) {
                isInterstitialSupported = false
                Log.i(TAG, "All interstitial ad placement candidates exhausted on Unity Dashboard. Pausing further interstitial requests.")
                return
            }

            val placement = INTERSTITIAL_CANDIDATES[interstitialCandidateIndex]
            isLoadingInterstitial = true

            val objectId = UUID.randomUUID().toString()
            interstitialObjectId = objectId
            val loadOptions = UnityAdsLoadOptions().apply {
                setObjectId(objectId)
            }

            Log.d(TAG, "Attempting load for Interstitial candidate: $placement (index $interstitialCandidateIndex)")
            UnityAds.load(
                placement,
                loadOptions,
                object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placementId: String?) {
                        isLoadingInterstitial = false
                        isInterstitialLoaded = true
                        isInterstitialSupported = true
                        activeInterstitialPlacement = placementId ?: placement
                        Log.d(TAG, "Interstitial Ad LOADED successfully: $activeInterstitialPlacement")
                    }

                    override fun onUnityAdsFailedToLoad(
                        placementId: String?,
                        error: UnityAds.UnityAdsLoadError?,
                        message: String?
                    ) {
                        isLoadingInterstitial = false
                        isInterstitialLoaded = false
                        Log.i(TAG, "Interstitial candidate $placementId not available ($error): $message")

                        // Try next candidate placement name
                        interstitialCandidateIndex++
                        if (interstitialCandidateIndex < INTERSTITIAL_CANDIDATES.size) {
                            mainHandler.postDelayed({ loadInterstitialAd() }, 300L)
                        } else {
                            // Trip circuit breaker
                            isInterstitialSupported = false
                            Log.i(TAG, "Interstitial placements not configured on Unity Dashboard for Game ID $GAME_ID. Pausing requests.")
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            isLoadingInterstitial = false
            isInterstitialLoaded = false
            Log.w(TAG, "loadInterstitialAd safely caught: ${t.message}")
        }
    }

    // =========================================================================
    // 4. High-Converting Rewarded Video Flow on PDF Download (with Auto-Reload & Fallback)
    // =========================================================================
    /**
     * Executes the Rewarded Ad Flow for PDF Download:
     * - If Rewarded Ad is loaded: displays it.
     *   - If user completes video (COMPLETED): triggers onRewardGranted() and reloads next ad.
     *   - If user skips video (SKIPPED): triggers onRewardSkipped() and reloads next ad.
     * - If Rewarded Ad is NOT loaded (Smart Fallback):
     *   - Tries Interstitial Ad (if loaded).
     *   - If no ad is available (Dashboard not configured / Offline / No Fill):
     *     immediately calls onRewardGranted() so the employee is NEVER blocked!
     */
    fun showRewardedForDownload(
        activity: Activity?,
        onRewardGranted: () -> Unit,
        onRewardSkipped: () -> Unit
    ) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            onRewardGranted()
            return
        }

        try {
            if (!UnityAds.isInitialized) {
                initialize(activity, currentTestMode)
                onRewardGranted() // Fallback: never block download
                return
            }

            // If ads are not supported/configured on this dashboard project, grant instant pass-through
            if (!isRewardedSupported && !isInterstitialSupported) {
                onRewardGranted()
                return
            }

            // 1. Try Primary: Rewarded Ad
            if (isRewardedLoaded) {
                val placement = activeRewardedPlacement
                val objectId = rewardedObjectId
                isRewardedLoaded = false // Consume ad

                val showOptions = UnityAdsShowOptions().apply {
                    if (objectId.isNotEmpty()) setObjectId(objectId)
                }

                UnityAds.show(
                    activity,
                    placement,
                    showOptions,
                    object : IUnityAdsShowListener {
                        override fun onUnityAdsShowFailure(
                            placementId: String?,
                            error: UnityAds.UnityAdsShowError?,
                            message: String?
                        ) {
                            Log.w(TAG, "Rewarded show failure for $placementId: $error - $message")
                            // Fallback on show failure: grant reward so user is not stuck
                            mainHandler.post { onRewardGranted() }
                            mainHandler.postDelayed({ loadRewardedAd() }, 1000L)
                        }

                        override fun onUnityAdsShowStart(placementId: String?) {
                            Log.d(TAG, "Rewarded Ad started showing: $placementId")
                        }

                        override fun onUnityAdsShowClick(placementId: String?) {}

                        override fun onUnityAdsShowComplete(
                            placementId: String?,
                            state: UnityAds.UnityAdsShowCompletionState?
                        ) {
                            Log.d(TAG, "Rewarded Ad completed: $placementId state=$state")
                            mainHandler.postDelayed({ loadRewardedAd() }, 1000L)

                            if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                                mainHandler.post { onRewardGranted() }
                            } else {
                                mainHandler.post { onRewardSkipped() }
                            }
                        }
                    }
                )
                return
            }

            // 2. Smart Fallback: Interstitial Ad if Rewarded not ready
            if (isInterstitialLoaded) {
                val placement = activeInterstitialPlacement
                val objectId = interstitialObjectId
                isInterstitialLoaded = false // Consume ad

                val showOptions = UnityAdsShowOptions().apply {
                    if (objectId.isNotEmpty()) setObjectId(objectId)
                }

                UnityAds.show(
                    activity,
                    placement,
                    showOptions,
                    object : IUnityAdsShowListener {
                        override fun onUnityAdsShowFailure(
                            placementId: String?,
                            error: UnityAds.UnityAdsShowError?,
                            message: String?
                        ) {
                            mainHandler.post { onRewardGranted() }
                            mainHandler.postDelayed({ preloadAllAds() }, 1000L)
                        }

                        override fun onUnityAdsShowStart(placementId: String?) {}
                        override fun onUnityAdsShowClick(placementId: String?) {}

                        override fun onUnityAdsShowComplete(
                            placementId: String?,
                            state: UnityAds.UnityAdsShowCompletionState?
                        ) {
                            mainHandler.post { onRewardGranted() }
                            mainHandler.postDelayed({ preloadAllAds() }, 1000L)
                        }
                    }
                )
                return
            }

            // 3. Fallback when NO ADS loaded: Don't block employee, execute download
            if (isRewardedSupported || isInterstitialSupported) {
                preloadAllAds()
            }
            onRewardGranted()
        } catch (t: Throwable) {
            Log.w(TAG, "showRewardedForDownload safely caught: ${t.message}")
            onRewardGranted()
        }
    }

    /**
     * Interstitial trigger on successful search
     */
    fun onSuccessfulSearch(activity: Activity?) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) return
        try {
            searchCount++
            if (searchCount % 2 == 1 && isInterstitialLoaded) {
                showInterstitialIfReady(activity) {}
            } else if (isInterstitialSupported) {
                loadInterstitialAd()
            }
        } catch (t: Throwable) {
            Log.w(TAG, "onSuccessfulSearch safely caught: ${t.message}")
        }
    }

    fun showInterstitialIfReady(activity: Activity, onComplete: () -> Unit = {}) {
        if (activity.isFinishing || activity.isDestroyed) {
            onComplete()
            return
        }

        try {
            if (!UnityAds.isInitialized) {
                initialize(activity, currentTestMode)
                onComplete()
                return
            }

            if (!isInterstitialSupported) {
                onComplete()
                return
            }

            if (!isInterstitialLoaded) {
                loadInterstitialAd()
                onComplete()
                return
            }

            val placementToShow = activeInterstitialPlacement
            val objectIdToShow = interstitialObjectId
            isInterstitialLoaded = false

            val showOptions = UnityAdsShowOptions().apply {
                if (objectIdToShow.isNotEmpty()) setObjectId(objectIdToShow)
            }

            UnityAds.show(
                activity,
                placementToShow,
                showOptions,
                object : IUnityAdsShowListener {
                    override fun onUnityAdsShowFailure(
                        placementId: String?,
                        error: UnityAds.UnityAdsShowError?,
                        message: String?
                    ) {
                        Log.w(TAG, "Show failed for $placementId: $error - $message")
                        mainHandler.postDelayed({ loadInterstitialAd() }, 1000L)
                        onComplete()
                    }

                    override fun onUnityAdsShowStart(placementId: String?) {}
                    override fun onUnityAdsShowClick(placementId: String?) {}

                    override fun onUnityAdsShowComplete(
                        placementId: String?,
                        state: UnityAds.UnityAdsShowCompletionState?
                    ) {
                        Log.d(TAG, "Interstitial completed: $placementId ($state)")
                        mainHandler.postDelayed({ loadInterstitialAd() }, 1000L)
                        onComplete()
                    }
                }
            )
        } catch (t: Throwable) {
            Log.w(TAG, "showInterstitialIfReady safely caught: ${t.message}")
            onComplete()
        }
    }

    // =========================================================================
    // 5. Banner Ad Loading with Candidate Traversal & Circuit Breaker
    // =========================================================================
    fun loadBanner(
        activity: Activity,
        onLoaded: (BannerView) -> Unit,
        onFailed: (String) -> Unit
    ) {
        if (!UnityAds.isInitialized) {
            onFailed("Unity Ads not initialized yet")
            return
        }

        if (!isBannerSupported) {
            onFailed("Banner placement not available on dashboard")
            return
        }

        fun tryCandidate(index: Int) {
            if (index >= BANNER_CANDIDATES.size) {
                isBannerSupported = false
                Log.i(TAG, "Banner placements not configured on Unity Dashboard for Game ID $GAME_ID. Pausing banner requests.")
                onFailed("All banner candidates exhausted")
                return
            }
            val placement = BANNER_CANDIDATES[index]
            Log.d(TAG, "Attempting banner load with placement: $placement (index $index)")
            try {
                val banner = BannerView(activity, placement, UnityBannerSize(320, 50))
                banner.listener = object : BannerView.Listener() {
                    override fun onBannerLoaded(bannerAdView: BannerView?) {
                        Log.d(TAG, "✅ Unity Banner LOADED successfully for placement: $placement")
                        isBannerSupported = true
                        activeBannerPlacement = placement
                        if (bannerAdView != null) {
                            onLoaded(bannerAdView)
                        }
                    }

                    override fun onBannerFailedToLoad(
                        bannerAdView: BannerView?,
                        errorInfo: BannerErrorInfo?
                    ) {
                        val errMsg = errorInfo?.errorMessage ?: "error"
                        Log.i(TAG, "Banner placement $placement not available: $errMsg. Trying next candidate...")
                        try {
                            bannerAdView?.destroy()
                        } catch (_: Throwable) {}
                        mainHandler.postDelayed({
                            tryCandidate(index + 1)
                        }, 400L)
                    }
                }
                banner.load()
            } catch (t: Throwable) {
                Log.w(TAG, "Exception loading banner for $placement: ${t.message}")
                tryCandidate(index + 1)
            }
        }

        tryCandidate(bannerCandidateIndex)
    }
}
