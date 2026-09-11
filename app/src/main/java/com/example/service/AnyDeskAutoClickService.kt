package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.MainActivity
import com.example.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

class AnyDeskAutoClickService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var settingsRepository: SettingsRepository
    private var activeClickJob: Job? = null

    private val lastClickTimes = ConcurrentHashMap<String, Long>()
    private val DEBOUNCE_MS = 1500L // Prevent rapid repeat clicking

    companion object {
        const val TAG = "AnyDeskAutoClick"
        @Volatile
        var isServiceRunning = false
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        settingsRepository = SettingsRepository.getInstance(applicationContext)

        // Immediately cancel any running operations when user pauses / disables auto click
        serviceScope.launch {
            settingsRepository.isAutoClickEnabled.collect { enabled ->
                if (!enabled) {
                    activeClickJob?.cancel()
                    activeClickJob = null
                    Log.i(TAG, "Master auto-click disabled: active click jobs cancelled.")
                }
            }
        }

        try {
            val info = serviceInfo ?: AccessibilityServiceInfo()
            info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            info.notificationTimeout = 20
            info.flags = info.flags or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            serviceInfo = info
        } catch (e: Exception) {
            Log.w(TAG, "Error applying dynamic serviceInfo: ${e.message}")
        }

        Log.i(TAG, "AnyDeskAutoClickService connected and running.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Safety guarantee: Never run auto-click if our own app is currently open in foreground
        if (MainActivity.isAppInForeground) {
            activeClickJob?.cancel()
            activeClickJob = null
            return
        }

        val packageName = event.packageName?.toString() ?: "unknown.package"
        // Safety guarantee: Ignore events from our own app or system settings screen
        if (isExcludedPackage(packageName)) {
            activeClickJob?.cancel()
            activeClickJob = null
            return
        }

        if (!::settingsRepository.isInitialized) {
            settingsRepository = SettingsRepository.getInstance(applicationContext)
        }

        // Check if master auto-click toggle is enabled (both direct synchronous storage and flow)
        if (!isAutoClickAllowed()) {
            activeClickJob?.cancel()
            activeClickJob = null
            return
        }

        // Check against candidate roots
        val roots = collectCandidateRoots(event)
        if (roots.isEmpty()) return

        activeClickJob?.cancel()
        activeClickJob = serviceScope.launch {
            processAutoClickSteps(roots, packageName)
        }
    }

    private fun isExcludedPackage(pkg: String?): Boolean {
        if (pkg.isNullOrBlank()) return false
        val ownPkg = applicationContext.packageName
        if (pkg.equals(ownPkg, ignoreCase = true)) {
            return true
        }
        val lower = pkg.lowercase()
        // Never auto-click inside Android Settings, App Info, or Accessibility settings screens
        if (lower == "com.android.settings" ||
            lower.startsWith("com.android.settings.") ||
            lower == "com.google.android.settings" ||
            lower.equals("com.samsung.android.app.settings", ignoreCase = true) ||
            lower.contains("settings.intelligence")
        ) {
            return true
        }
        return false
    }

    private fun collectCandidateRoots(event: AccessibilityEvent): List<AccessibilityNodeInfo> {
        val candidateRoots = mutableListOf<AccessibilityNodeInfo>()

        // 1. Root from event source (direct origin of event)
        try {
            event.source?.let {
                if (!isExcludedPackage(it.packageName?.toString())) {
                    candidateRoots.add(it)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error getting event.source: ${e.message}")
        }

        // 2. rootInActiveWindow
        try {
            rootInActiveWindow?.let { root ->
                if (!isExcludedPackage(root.packageName?.toString()) && candidateRoots.none { it == root }) {
                    candidateRoots.add(root)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error getting rootInActiveWindow: ${e.message}")
        }

        // 3. All interactive windows (especially system dialogs, alert windows, overlays)
        try {
            windows?.forEach { window ->
                val wRoot = window.root
                if (wRoot != null && !isExcludedPackage(wRoot.packageName?.toString()) && candidateRoots.none { it == wRoot }) {
                    candidateRoots.add(wRoot)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error iterating windows: ${e.message}")
        }

        return candidateRoots
    }

    private fun isAutoClickAllowed(): Boolean {
        if (MainActivity.isAppInForeground) return false
        return ::settingsRepository.isInitialized &&
                settingsRepository.isAutoClickEnabledDirect() &&
                settingsRepository.isAutoClickEnabled.value
    }

    private suspend fun processAutoClickSteps(roots: List<AccessibilityNodeInfo>, packageName: String) {
        if (!isAutoClickAllowed()) return

        val steps = settingsRepository.clickSteps.value
        if (steps.isEmpty()) return
        val delayMs = settingsRepository.clickDelayMs.value

        // Also gather fresh windows roots if available
        val allRoots = mutableListOf<AccessibilityNodeInfo>()
        allRoots.addAll(roots)
        for (fresh in collectCandidateRootsFromService()) {
            if (allRoots.none { it == fresh }) {
                allRoots.add(fresh)
            }
        }

        for ((index, step) in steps.withIndex()) {
            if (!isAutoClickAllowed()) return
            if (step.keywords.isEmpty()) continue

            val stepLabel = "ขั้นตอนที่ ${index + 1}"
            var clicked = false

            // Try each candidate root (dialog, active window, event source)
            for (rootNode in allRoots) {
                if (!isAutoClickAllowed()) return
                if (scanAndPerformClick(rootNode, step.keywords.toSet(), stepLabel, packageName)) {
                    clicked = true
                    break
                }
            }

            // If not found in current roots, query fresh roots again after short wait
            if (!clicked) {
                delay(100L)
                if (!isAutoClickAllowed()) return
                val freshRoots = collectCandidateRootsFromService()
                for (freshRoot in freshRoots) {
                    if (!isAutoClickAllowed()) return
                    if (scanAndPerformClick(freshRoot, step.keywords.toSet(), stepLabel, packageName)) {
                        clicked = true
                        break
                    }
                }
            }

            if (clicked && index < steps.lastIndex) {
                delay(delayMs)
            }
        }
    }

    private fun collectCandidateRootsFromService(): List<AccessibilityNodeInfo> {
        val roots = mutableListOf<AccessibilityNodeInfo>()
        try {
            rootInActiveWindow?.let {
                if (!isExcludedPackage(it.packageName?.toString())) {
                    roots.add(it)
                }
            }
        } catch (_: Exception) {}
        try {
            windows?.forEach { w ->
                w.root?.let {
                    if (!isExcludedPackage(it.packageName?.toString()) && roots.none { r -> r == it }) {
                        roots.add(it)
                    }
                }
            }
        } catch (_: Exception) {}
        return roots
    }

    private suspend fun scanAndPerformClick(
        rootNode: AccessibilityNodeInfo,
        keywords: Set<String>,
        stepName: String,
        packageName: String
    ): Boolean {
        if (!isAutoClickAllowed()) return false
        if (isExcludedPackage(packageName) || isExcludedPackage(rootNode.packageName?.toString())) {
            return false
        }

        for (keyword in keywords) {
            if (!isAutoClickAllowed()) return false
            val cleanKeyword = keyword.trim()
            if (cleanKeyword.isBlank()) continue

            val matchedNodes = mutableListOf<AccessibilityNodeInfo>()

            // 1. Recursive tree search (handles Thai text and custom views reliably)
            findMatchingNodesRecursively(rootNode, cleanKeyword, matchedNodes)

            // 2. System findAccessibilityNodeInfosByText as additional candidate
            try {
                val systemFound = rootNode.findAccessibilityNodeInfosByText(cleanKeyword)
                if (!systemFound.isNullOrEmpty()) {
                    for (node in systemFound) {
                        if (node != null && !isExcludedPackage(node.packageName?.toString()) && matchedNodes.none { it == node }) {
                            matchedNodes.add(node)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "findAccessibilityNodeInfosByText failed: ${e.message}")
            }

            if (matchedNodes.isNotEmpty()) {
                // Prioritize nodes that are clickable or have clickable parents
                matchedNodes.sortByDescending { it.isClickable || (it.parent?.isClickable == true) }

                for (node in matchedNodes) {
                    if (!isAutoClickAllowed()) return false
                    if (isExcludedPackage(node.packageName?.toString())) return false

                    val nodeText = (node.text?.toString() ?: node.contentDescription?.toString() ?: "").trim()
                    val key = "$stepName:$cleanKeyword:$packageName"
                    val lastTime = lastClickTimes[key] ?: 0L
                    val currentTime = System.currentTimeMillis()

                    if (currentTime - lastTime < DEBOUNCE_MS) {
                        continue
                    }

                    val success = performClickWithFallback(node)
                    if (success) {
                        lastClickTimes[key] = currentTime
                        Log.i(TAG, "Successfully clicked [$stepName] for keyword '$cleanKeyword' on '$nodeText' in package $packageName")
                        triggerFeedbackAndLog(stepName, cleanKeyword, packageName, true, "คลิกสำเร็จที่ '$nodeText'")
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun findMatchingNodesRecursively(
        node: AccessibilityNodeInfo?,
        keyword: String,
        results: MutableList<AccessibilityNodeInfo>
    ) {
        if (node == null) return
        if (isExcludedPackage(node.packageName?.toString())) return

        val text = node.text?.toString() ?: ""
        val desc = node.contentDescription?.toString() ?: ""

        val isMatch = containsIgnoreCase(text, keyword) || containsIgnoreCase(desc, keyword)
        if (isMatch && results.none { it == node }) {
            results.add(node)
        }

        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = node.getChild(i) ?: continue
            findMatchingNodesRecursively(child, keyword, results)
        }
    }

    private suspend fun performClickWithFallback(node: AccessibilityNodeInfo): Boolean {
        if (!isAutoClickAllowed()) return false
        if (isExcludedPackage(node.packageName?.toString())) return false

        // Strategy 1: Click the node directly if clickable
        if (node.isClickable) {
            val clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (clicked) return true
        }

        // Strategy 2: Click the clickable ancestor
        var currentParent = node.parent
        while (currentParent != null) {
            if (currentParent.isClickable) {
                val clicked = currentParent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (clicked) return true
            }
            currentParent = currentParent.parent
        }

        // Strategy 3: Try action click on the node anyway (even if isClickable is false)
        if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            return true
        }

        // Strategy 4: Physical gesture tap injection (bypasses OEM / SystemUI dialog restrictions)
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        if (bounds.width() > 0 && bounds.height() > 0 && bounds.centerX() > 0 && bounds.centerY() > 0) {
            val gestureSuccess = performGestureClick(bounds.centerX().toFloat(), bounds.centerY().toFloat())
            if (gestureSuccess) {
                return true
            }
        }

        // Strategy 5: Physical gesture tap on parent bounds
        val parent = node.parent
        if (parent != null) {
            val parentBounds = Rect()
            parent.getBoundsInScreen(parentBounds)
            if (parentBounds.width() > 0 && parentBounds.height() > 0 && parentBounds.centerX() > 0 && parentBounds.centerY() > 0) {
                val gestureSuccess = performGestureClick(parentBounds.centerX().toFloat(), parentBounds.centerY().toFloat())
                if (gestureSuccess) {
                    return true
                }
            }
        }

        return false
    }

    private suspend fun performGestureClick(x: Float, y: Float): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        return suspendCancellableCoroutine { continuation ->
            val path = Path().apply {
                moveTo(x, y)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, 50)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()

            val dispatched = dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    Log.d(TAG, "Gesture tap succeeded at ($x, $y)")
                    if (continuation.isActive) {
                        continuation.resume(true)
                    }
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    Log.w(TAG, "Gesture tap cancelled at ($x, $y)")
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }
            }, null)

            if (!dispatched) {
                Log.w(TAG, "dispatchGesture returned false for ($x, $y)")
                if (continuation.isActive) {
                    continuation.resume(false)
                }
            }
        }
    }

    private fun normalizeString(input: String): String {
        return input
            .replace('\u00A0', ' ') // non-breaking space
            .replace("\u200B", "") // zero-width space
            .replace("\u200C", "") // zero-width non-joiner
            .replace("\u200D", "") // zero-width joiner
            .replace("\uFEFF", "") // zero-width no-break space
            .replace("\\s+".toRegex(), "")
            .lowercase()
    }

    private fun containsIgnoreCase(source: String, target: String): Boolean {
        if (source.isBlank() || target.isBlank()) return false
        val s = normalizeString(source)
        val t = normalizeString(target)
        return s.contains(t) || t.contains(s)
    }

    private fun triggerFeedbackAndLog(
        stepName: String,
        keyword: String,
        packageName: String,
        success: Boolean,
        detail: String
    ) {
        // Haptic feedback
        if (settingsRepository.isVibrationEnabled.value) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    val vibrator = vibratorManager.defaultVibrator
                    vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(120)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Vibration failed: ${e.message}")
            }
        }

        // Update count
        serviceScope.launch {
            try {
                settingsRepository.incrementClickCount()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to increment click count: ${e.message}")
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "AnyDeskAutoClickService interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        Log.i(TAG, "AnyDeskAutoClickService destroyed.")
    }
}
