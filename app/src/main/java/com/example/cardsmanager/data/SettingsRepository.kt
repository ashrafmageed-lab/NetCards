package com.example.cardsmanager.data

import android.content.Context
import android.content.SharedPreferences
import com.example.cardsmanager.domain.MessageTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("hotspot_cards_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_MESSAGE_TEMPLATE = "message_template"
        private const val KEY_LOW_STOCK_THRESHOLD = "low_stock_threshold"
        private const val KEY_THEME_MODE = "theme_mode" // SYSTEM, LIGHT, DARK
        private const val KEY_PIN_LOCK_ENABLED = "pin_lock_enabled"
        private const val KEY_SAVED_PIN = "saved_pin"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
    }

    private val _messageTemplate = MutableStateFlow(
        prefs.getString(KEY_MESSAGE_TEMPLATE, MessageTemplate.DEFAULT_TEMPLATE) ?: MessageTemplate.DEFAULT_TEMPLATE
    )
    val messageTemplate: StateFlow<String> = _messageTemplate.asStateFlow()

    private val _lowStockThreshold = MutableStateFlow(
        prefs.getInt(KEY_LOW_STOCK_THRESHOLD, 5)
    )
    val lowStockThreshold: StateFlow<Int> = _lowStockThreshold.asStateFlow()

    private val _themeMode = MutableStateFlow(
        prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _pinLockEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_PIN_LOCK_ENABLED, false)
    )
    val pinLockEnabled: StateFlow<Boolean> = _pinLockEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
    )
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    fun updateMessageTemplate(newTemplate: String) {
        prefs.edit().putString(KEY_MESSAGE_TEMPLATE, newTemplate).apply()
        _messageTemplate.value = newTemplate
    }

    fun resetMessageTemplate() {
        updateMessageTemplate(MessageTemplate.DEFAULT_TEMPLATE)
    }

    fun updateLowStockThreshold(threshold: Int) {
        val safeThreshold = threshold.coerceAtLeast(1)
        prefs.edit().putInt(KEY_LOW_STOCK_THRESHOLD, safeThreshold).apply()
        _lowStockThreshold.value = safeThreshold
    }

    fun updateThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setPin(pin: String) {
        prefs.edit().putString(KEY_SAVED_PIN, pin).apply()
    }

    fun verifyPin(input: String): Boolean {
        val saved = prefs.getString(KEY_SAVED_PIN, "") ?: ""
        return saved.isNotEmpty() && saved == input
    }

    fun hasPin(): Boolean {
        return (prefs.getString(KEY_SAVED_PIN, "") ?: "").isNotEmpty()
    }

    fun setPinLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PIN_LOCK_ENABLED, enabled).apply()
        _pinLockEnabled.value = enabled
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
        _hapticsEnabled.value = enabled
    }
}
