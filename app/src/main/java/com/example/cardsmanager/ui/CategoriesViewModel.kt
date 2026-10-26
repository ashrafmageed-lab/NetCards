package com.example.cardsmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardsmanager.data.BatchImportSummary
import com.example.cardsmanager.data.CardEntity
import com.example.cardsmanager.data.CardsRepository
import com.example.cardsmanager.data.CategoryEntity
import com.example.cardsmanager.data.CategoryWithStats
import com.example.cardsmanager.domain.CardTextParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: CardsRepository
) : ViewModel() {

    val categoriesWithStats: StateFlow<List<CategoryWithStats>> =
        repository.getCategoriesWithStats().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedCategoryCards: StateFlow<List<CardEntity>> =
        _selectedCategoryId.flatMapLatest { id ->
            if (id != null) {
                repository.getCardsByCategory(id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _importSummary = MutableStateFlow<Pair<BatchImportSummary, List<CardTextParser.InvalidLine>>?>(null)
    val importSummary: StateFlow<Pair<BatchImportSummary, List<CardTextParser.InvalidLine>>?> = _importSummary.asStateFlow()

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun dismissImportSummary() {
        _importSummary.value = null
    }

    fun addCategory(name: String, colorArgb: Int) {
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name,
                    colorArgb = colorArgb
                )
            )
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            if (_selectedCategoryId.value == category.id) {
                _selectedCategoryId.value = null
            }
            repository.deleteCategory(category)
        }
    }

    fun deleteCard(card: CardEntity) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }

    fun addSingleCard(categoryId: Long, name: String, pass: String, plan: String) {
        viewModelScope.launch {
            repository.addSingleCard(categoryId, name, pass, plan)
        }
    }

    fun importBatch(categoryId: Long, rawText: String, replaceExisting: Boolean) {
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

    suspend fun getCardsExportText(categoryId: Long, availableOnly: Boolean): String {
        val cards = if (availableOnly) {
            repository.getAvailableCardsToExport(categoryId)
        } else {
            repository.getAllCardsToExport(categoryId)
        }
        return CardTextParser.exportToText(cards)
    }

    val trashCards = repository.trashCards.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val trashCount = repository.trashCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    fun purgeCategory(categoryId: Long, includeIssued: Boolean) {
        viewModelScope.launch {
            if (includeIssued) {
                repository.softDeleteAllCardsInCategory(categoryId)
            } else {
                repository.softDeleteAvailableCards(categoryId)
            }
        }
    }

    fun softDeleteCard(cardId: Long) {
        viewModelScope.launch {
            repository.softDeleteCard(cardId)
        }
    }

    fun restoreTrashCard(cardId: Long) {
        viewModelScope.launch {
            repository.restoreFromTrash(cardId)
        }
    }

    fun restoreAllFromTrash() {
        viewModelScope.launch {
            repository.restoreAllFromTrash()
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }
}
