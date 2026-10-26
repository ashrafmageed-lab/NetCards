package com.example.cardsmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardsmanager.data.BackupManager
import com.example.cardsmanager.data.CardsDao
import com.example.cardsmanager.data.CardsRepository
import com.example.cardsmanager.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: CardsRepository,
    private val settingsRepository: SettingsRepository,
    private val dao: CardsDao
) : ViewModel() {

    val messageTemplate = settingsRepository.messageTemplate
    val lowStockThreshold = settingsRepository.lowStockThreshold
    val themeMode = settingsRepository.themeMode
    val pinLockEnabled = settingsRepository.pinLockEnabled
    val hapticsEnabled = settingsRepository.hapticsEnabled

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun updateTemplate(newTemplate: String) {
        settingsRepository.updateMessageTemplate(newTemplate)
    }

    fun resetTemplate() {
        settingsRepository.resetMessageTemplate()
    }

    fun updateLowStockThreshold(threshold: Int) {
        settingsRepository.updateLowStockThreshold(threshold)
    }

    fun updateThemeMode(mode: String) {
        settingsRepository.updateThemeMode(mode)
    }

    fun setPin(pin: String) {
        settingsRepository.setPin(pin)
        _statusMessage.value = "تم تعيين رمز PIN بنجاح"
    }

    fun verifyPin(input: String): Boolean {
        return settingsRepository.verifyPin(input)
    }

    fun hasPin(): Boolean {
        return settingsRepository.hasPin()
    }

    fun setPinLockEnabled(enabled: Boolean) {
        settingsRepository.setPinLockEnabled(enabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        settingsRepository.setHapticsEnabled(enabled)
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    suspend fun exportBackupJson(): String {
        return BackupManager.createBackupJson(dao)
    }

    fun restoreBackupJson(jsonString: String) {
        viewModelScope.launch {
            val result = BackupManager.restoreBackupJson(dao, jsonString)
            if (result.isSuccess) {
                _statusMessage.value = result.getOrNull() ?: "تمت استعادة البيانات بنجاح"
            } else {
                _statusMessage.value = "فشل استعادة النسخة الاحتياطية: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _statusMessage.value = "تمت إعادة ضبط ومسح جميع البيانات بنجاح"
        }
    }
}
