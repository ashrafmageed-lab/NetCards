package com.example.cardsmanager.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardsmanager.data.BatchImportSummary
import com.example.cardsmanager.data.CardEntity
import com.example.cardsmanager.data.CardsRepository
import com.example.cardsmanager.data.CategoryEntity
import com.example.cardsmanager.data.CategoryWithStats
import com.example.cardsmanager.data.SettingsRepository
import com.example.cardsmanager.data.TextFiles
import com.example.cardsmanager.domain.CardTextParser
import com.example.cardsmanager.domain.MessageTemplate
import com.example.cardsmanager.share.WhatsAppShareHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CardsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val categoriesWithStats: StateFlow<List<CategoryWithStats>> =
        repository.getCategoriesWithStats().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val totalAvailableCount: StateFlow<Int> = repository.totalAvailableCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val totalIssuedCount: StateFlow<Int> = repository.totalIssuedCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val lowStockThreshold = settingsRepository.lowStockThreshold
    val messageTemplate = settingsRepository.messageTemplate

    private val _lastDispensedCard = MutableStateFlow<Pair<CardEntity, String>?>(null)
    val lastDispensedCard: StateFlow<Pair<CardEntity, String>?> = _lastDispensedCard.asStateFlow()

    private val _lastDispensedMultiple = MutableStateFlow<Pair<List<CardEntity>, String>?>(null)
    val lastDispensedMultiple: StateFlow<Pair<List<CardEntity>, String>?> = _lastDispensedMultiple.asStateFlow()

    private val _importSummary = MutableStateFlow<Pair<BatchImportSummary, List<CardTextParser.InvalidLine>>?>(null)
    val importSummary: StateFlow<Pair<BatchImportSummary, List<CardTextParser.InvalidLine>>?> = _importSummary.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun dismissDispensedCard() {
        _lastDispensedCard.value = null
    }

    fun dismissDispensedMultiple() {
        _lastDispensedMultiple.value = null
    }

    fun dismissImportSummary() {
        _importSummary.value = null
    }

    val trashCount: StateFlow<Int> = repository.trashCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    fun dispenseNext(context: Context, category: CategoryEntity) {
        viewModelScope.launch {
            val card = repository.dispenseNextCard(category.id)
            if (card != null) {
                com.example.cardsmanager.share.HapticHelper.performSuccess(context)
                _lastDispensedCard.value = Pair(card, category.name)
            } else {
                com.example.cardsmanager.share.HapticHelper.performWarning(context)
                _snackbarMessage.value = Strings.NO_AVAILABLE_CARDS
            }
        }
    }

    fun dispenseMultiple(context: Context, category: CategoryEntity, count: Int) {
        viewModelScope.launch {
            val cards = repository.dispenseNextCards(category.id, count)
            if (cards.isNotEmpty()) {
                com.example.cardsmanager.share.HapticHelper.performSuccess(context)
                val formatted = MessageTemplate.formatMultipleCards(cards, category.name)
                _lastDispensedMultiple.value = Pair(cards, category.name)
                WhatsAppShareHelper.shareToWhatsApp(context, formatted)
            } else {
                com.example.cardsmanager.share.HapticHelper.performWarning(context)
                _snackbarMessage.value = Strings.NO_AVAILABLE_CARDS
            }
        }
    }

    fun undoDispense(context: Context, cardId: Long) {
        viewModelScope.launch {
            com.example.cardsmanager.share.HapticHelper.performClick(context)
            repository.undoDispense(cardId)
            _lastDispensedCard.value = null
            _snackbarMessage.value = "تم استرجاع الكارت للمخزن بنجاح"
        }
    }

    fun rechargeFromText(categoryId: Long, rawText: String, replaceExisting: Boolean) {
        viewModelScope.launch {
            val parseResult = CardTextParser.parse(rawText)
            val summary = repository.importCardsBatch(
                categoryId = categoryId,
                parsedCards = parseResult.validCards,
                replaceExisting = replaceExisting
            )
            _importSummary.value = Pair(
                summary.copy(duplicateSkippedCount = summary.duplicateSkippedCount + parseResult.duplicateCountInFile),
                parseResult.invalidLines
            )
        }
    }

    fun rechargeFromFile(context: Context, categoryId: Long, fileUri: Uri, replaceExisting: Boolean) {
        viewModelScope.launch {
            val text = TextFiles.readTextFromUri(context, fileUri)
            rechargeFromText(categoryId, text, replaceExisting)
        }
    }
}
