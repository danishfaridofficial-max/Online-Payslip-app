package com.example.util

import android.app.Activity
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
 * 1. Preloading & Auto-Reload for High-Converting Rewarded Video on PDF Download.
 * 2. Dedicated fallback to Interstitial and instant graceful pass-through (No Fill never blocks user).
 * 3. Background crash shielding for rock-solid stability.
 */
object UnityAdsManager {
    private const val TAG = "UnityAdsManager"
    private val mainHandler = Handler(Looper.getMainLooper())

    fun findActivity(context: Context): Activity? = context.findActivity()

    // =========================================================================
    // 1. Unity Ads Configuration Placeholders (Easily updateable)
    // =========================================================================
    const val GAME_ID = "800388050"
    const val REWARDED_PLACEMENT_ID = "Rewarded_Android"
    const val INTERSTITIAL_PLACEMENT_ID = "Interstitial_Android"
    const val TEST_MODE = false

    // Priority candidate lists for fail-safe fill rates
    private val REWARDED_CANDIDATES = listOf(
        REWARDED_PLACEMENT_ID,
        "BP_Rewarded_Android",
        "rewardedVideo",
        "Rewarded"
    )

    private val INTERSTITIAL_CANDIDATES = listOf(
        INTERSTITIAL_PLACEMENT_ID,
        "BP_Interstitial_Android",
        "Interstitial",
        "video"
    )

    // Rewarded Ad state
    @Volatile private var rewardedCandidateIndex = 0
    @Volatile private var activeRewardedPlacement: String = REWARDED_CANDIDATES[0]
    @Volatile private var rewardedObjectId: String = ""
    @Volatile var isRewardedLoaded: Boolean = false
        private set
    @Volatile private var isLoadingRewarded: Boolean = false

    // Interstitial Ad state
    @Volatile private var interstitialCandidateIndex = 0
    @Volatile private var activeInterstitialPlacement: String = INTERSTITIAL_CANDIDATES[0]
    @Volatile private var interstitialObjectId: String = ""
    @Volatile var isInterstitialLoaded: Boolean = false
        private set
    @Volatile private var isLoadingInterstitial: Boolean = false

    @Volatile private var isInitializing = false
    @Volatile private var crashShieldInstalled = false
    private var searchCount = 0
    private var currentTestMode = TEST_MODE

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
     * Initializes Unity Ads SDK and preloads Rewarded and Interstitial ads.
     */
    fun initialize(context: Context, testMode: Boolean = TEST_MODE) {
        installBackgroundCrashShield(context)
        if (isInitializing) return
        currentTestMode = testMode
        val appContext = context.applicationContext ?: return

        mainHandler.post {
            try {
                if (UnityAds.isInitialized) {
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
     * Preloads both Rewarded and Interstitial ads in the background.
     */
    fun preloadAllAds() {
        loadRewardedAd()
        loadInterstitialAd()
    }

    // =========================================================================
    // 2. Rewarded Video Loading & Auto-Reload
    // =========================================================================
    fun loadRewardedAd() {
        try {
            if (!UnityAds.isInitialized || isLoadingRewarded || isRewardedLoaded) return
            if (rewardedCandidateIndex >= REWARDED_CANDIDATES.size) {
                rewardedCandidateIndex = 0
                return
            }

            val placement = REWARDED_CANDIDATES[rewardedCandidateIndex]
            isLoadingRewarded = true

            val objectId = UUID.randomUUID().toString()
            rewardedObjectId = objectId
            val loadOptions = UnityAdsLoadOptions().apply {
                setObjectId(objectId)
            }

            UnityAds.load(
                placement,
                loadOptions,
                object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placementId: String?) {
                        isLoadingRewarded = false
                        isRewardedLoaded = true
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
                        Log.w(TAG, "Failed to load Rewarded Ad $placementId: $error - $message")

                        if (rewardedCandidateIndex + 1 < REWARDED_CANDIDATES.size) {
                            rewardedCandidateIndex++
                            mainHandler.postDelayed({ loadRewardedAd() }, 400L)
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
    // 3. Interstitial Loading & Auto-Reload
    // =========================================================================
    fun loadInterstitialAd() {
        try {
            if (!UnityAds.isInitialized || isLoadingInterstitial || isInterstitialLoaded) return
            if (interstitialCandidateIndex >= INTERSTITIAL_CANDIDATES.size) {
                interstitialCandidateIndex = 0
                return
            }

            val placement = INTERSTITIAL_CANDIDATES[interstitialCandidateIndex]
            isLoadingInterstitial = true

            val objectId = UUID.randomUUID().toString()
            interstitialObjectId = objectId
            val loadOptions = UnityAdsLoadOptions().apply {
                setObjectId(objectId)
            }

            UnityAds.load(
                placement,
                loadOptions,
                object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placementId: String?) {
                        isLoadingInterstitial = false
                        isInterstitialLoaded = true
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
                        Log.w(TAG, "Failed to load Interstitial Ad $placementId: $error - $message")

                        if (interstitialCandidateIndex + 1 < INTERSTITIAL_CANDIDATES.size) {
                            interstitialCandidateIndex++
                            mainHandler.postDelayed({ loadInterstitialAd() }, 400L)
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
     *   - If no ad is available (No Fill / Offline): immediately calls onRewardGranted() so user is NEVER blocked!
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
                            // Auto-reload next rewarded ad in background immediately
                            mainHandler.postDelayed({ loadRewardedAd() }, 1000L)

                            if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                                mainHandler.post { onRewardGranted() }
                            } else {
                                // User skipped without finishing
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

            // 3. Fallback when NO ADS loaded: Don't block employee, execute download & trigger preload
            Log.d(TAG, "No ads currently loaded, triggering instant download fallback")
            preloadAllAds()
            onRewardGranted()
        } catch (t: Throwable) {
            Log.w(TAG, "showRewardedForDownload safely caught: ${t.message}")
            onRewardGranted()
        }
    }

    /**
     * Interstitial on successful search
     */
    fun onSuccessfulSearch(activity: Activity?) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) return
        try {
            searchCount++
            if (searchCount % 2 == 1 && isInterstitialLoaded) {
                showInterstitialIfReady(activity) {}
            } else {
                preloadAllAds()
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
}
