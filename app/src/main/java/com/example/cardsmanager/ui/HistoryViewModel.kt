package com.example.cardsmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardsmanager.data.CardEntity
import com.example.cardsmanager.data.CardsRepository
import com.example.cardsmanager.data.CategoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: CardsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<Long?>(null)
    val selectedCategoryFilter: StateFlow<Long?> = _selectedCategoryFilter.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val issuedCardsList: StateFlow<List<CardEntity>> =
        _searchQuery.flatMapLatest { query ->
            repository.searchIssuedCards(query)
        }.combine(_selectedCategoryFilter) { cards, filterId ->
            if (filterId == null) cards else cards.filter { it.categoryId == filterId }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryFilterChange(categoryId: Long?) {
        _selectedCategoryFilter.value = categoryId
    }

    fun undoDispense(cardId: Long) {
        viewModelScope.launch {
            repository.undoDispense(cardId)
        }
    }

    fun deleteCard(card: CardEntity) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }
}
