package com.example.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

object AccessibilityUtils {

    fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false

        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedComponentName = "${context.packageName}/${serviceClass.name}"

        for (service in enabledServices) {
            val resolveInfo = service.resolveInfo
            val componentName = "${resolveInfo.serviceInfo.packageName}/${resolveInfo.serviceInfo.name}"
            if (componentName.equals(expectedComponentName, ignoreCase = true)) {
                return true
            }
        }

        // Fallback check using Settings.Secure
        try {
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            return enabledServicesSetting.lowercase().contains(context.packageName.lowercase())
        } catch (e: Exception) {
            return false
        }
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
