package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("anydesk_auto_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_AUTO_ENABLED = "auto_enabled"
        const val KEY_CUSTOM_STEPS_JSON = "custom_steps_json"
        const val KEY_CLICK_DELAY = "click_delay_ms"
        const val KEY_AUTO_COUNT = "auto_click_count"
        const val KEY_VIBRATION = "vibration_enabled"

        @Volatile
        private var instance: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: SettingsRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val _isAutoClickEnabled = MutableStateFlow(prefs.getBoolean(KEY_AUTO_ENABLED, true))
    val isAutoClickEnabled: StateFlow<Boolean> = _isAutoClickEnabled.asStateFlow()

    private val _clickSteps = MutableStateFlow(
        ClickStep.listFromJsonString(prefs.getString(KEY_CUSTOM_STEPS_JSON, null))
    )
    val clickSteps: StateFlow<List<ClickStep>> = _clickSteps.asStateFlow()

    private val _clickDelayMs = MutableStateFlow(prefs.getLong(KEY_CLICK_DELAY, 300L))
    val clickDelayMs: StateFlow<Long> = _clickDelayMs.asStateFlow()

    private val _autoClickCount = MutableStateFlow(prefs.getInt(KEY_AUTO_COUNT, 0))
    val autoClickCount: StateFlow<Int> = _autoClickCount.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION, true))
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            KEY_AUTO_ENABLED -> {
                _isAutoClickEnabled.value = prefs.getBoolean(KEY_AUTO_ENABLED, true)
            }
            KEY_CUSTOM_STEPS_JSON -> {
                _clickSteps.value = ClickStep.listFromJsonString(prefs.getString(KEY_CUSTOM_STEPS_JSON, null))
            }
            KEY_CLICK_DELAY -> {
                _clickDelayMs.value = prefs.getLong(KEY_CLICK_DELAY, 300L)
            }
            KEY_AUTO_COUNT -> {
                _autoClickCount.value = prefs.getInt(KEY_AUTO_COUNT, 0)
            }
            KEY_VIBRATION -> {
                _isVibrationEnabled.value = prefs.getBoolean(KEY_VIBRATION, true)
            }
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    fun isAutoClickEnabledDirect(): Boolean {
        return prefs.getBoolean(KEY_AUTO_ENABLED, true)
    }

    fun setAutoClickEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_ENABLED, enabled).commit()
        _isAutoClickEnabled.value = enabled
    }

    fun setClickSteps(steps: List<ClickStep>) {
        val jsonString = ClickStep.listToJsonString(steps)
        prefs.edit().putString(KEY_CUSTOM_STEPS_JSON, jsonString).commit()
        _clickSteps.value = steps
    }

    fun setClickDelayMs(delayMs: Long) {
        prefs.edit().putLong(KEY_CLICK_DELAY, delayMs).commit()
        _clickDelayMs.value = delayMs
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).commit()
        _isVibrationEnabled.value = enabled
    }

    fun incrementClickCount() {
        val newCount = _autoClickCount.value + 1
        prefs.edit().putInt(KEY_AUTO_COUNT, newCount).commit()
        _autoClickCount.value = newCount
    }

    fun resetToDefaults() {
        setClickSteps(ClickStep.getDefaultSteps())
        setClickDelayMs(300L)
        setAutoClickEnabled(true)
        setVibrationEnabled(true)
    }
}

