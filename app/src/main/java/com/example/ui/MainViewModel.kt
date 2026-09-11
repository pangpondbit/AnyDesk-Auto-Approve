package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ClickStep
import com.example.data.SettingsRepository
import com.example.service.AnyDeskAutoClickService
import com.example.util.AccessibilityUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository.getInstance(application)

    private val _isAccessibilityEnabled = MutableStateFlow(false)
    val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled.asStateFlow()

    val isAutoClickEnabled: StateFlow<Boolean> = repository.isAutoClickEnabled
    val clickSteps: StateFlow<List<ClickStep>> = repository.clickSteps
    val clickDelayMs: StateFlow<Long> = repository.clickDelayMs
    val autoClickCount: StateFlow<Int> = repository.autoClickCount
    val isVibrationEnabled: StateFlow<Boolean> = repository.isVibrationEnabled

    init {
        checkAccessibilityStatus()
        viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                checkAccessibilityStatus()
            }
        }
    }

    fun checkAccessibilityStatus() {
        val isEnabled = AccessibilityUtils.isAccessibilityServiceEnabled(
            getApplication(),
            AnyDeskAutoClickService::class.java
        )
        _isAccessibilityEnabled.value = isEnabled
    }

    fun toggleAutoClickEnabled(enabled: Boolean) {
        repository.setAutoClickEnabled(enabled)
    }

    fun addStep() {
        val currentSteps = clickSteps.value.toMutableList()
        val newStep = ClickStep(
            keywords = emptyList()
        )
        currentSteps.add(newStep)
        repository.setClickSteps(currentSteps)
    }

    fun removeStep(stepId: String) {
        val currentSteps = clickSteps.value.filter { it.id != stepId }
        repository.setClickSteps(currentSteps)
    }

    fun moveStepUp(index: Int) {
        if (index <= 0) return
        val currentSteps = clickSteps.value.toMutableList()
        val item = currentSteps.removeAt(index)
        currentSteps.add(index - 1, item)
        repository.setClickSteps(currentSteps)
    }

    fun moveStepDown(index: Int) {
        val currentSteps = clickSteps.value.toMutableList()
        if (index >= currentSteps.lastIndex) return
        val item = currentSteps.removeAt(index)
        currentSteps.add(index + 1, item)
        repository.setClickSteps(currentSteps)
    }

    fun addKeywordToStep(stepId: String, keyword: String) {
        if (keyword.isBlank()) return
        val currentSteps = clickSteps.value.map { step ->
            if (step.id == stepId && !step.keywords.contains(keyword.trim())) {
                step.copy(keywords = step.keywords + keyword.trim())
            } else step
        }
        repository.setClickSteps(currentSteps)
    }

    fun removeKeywordFromStep(stepId: String, keyword: String) {
        val currentSteps = clickSteps.value.map { step ->
            if (step.id == stepId) {
                step.copy(keywords = step.keywords.filter { it != keyword })
            } else step
        }
        repository.setClickSteps(currentSteps)
    }

    fun setClickDelay(delayMs: Long) {
        repository.setClickDelayMs(delayMs)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        repository.setVibrationEnabled(enabled)
    }

    fun resetDefaults() {
        repository.resetToDefaults()
    }
}

