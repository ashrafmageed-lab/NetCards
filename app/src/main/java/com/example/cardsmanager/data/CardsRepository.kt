package com.example.cardsmanager.data

import com.example.cardsmanager.domain.CardTextParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

data class BatchImportSummary(
    val importedCount: Int,
    val duplicateSkippedCount: Int,
    val totalProcessed: Int
)

class CardsRepository(private val dao: CardsDao) {

    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val allCards: Flow<List<CardEntity>> = dao.getAllCards()
    val issuedCards: Flow<List<CardEntity>> = dao.getIssuedCards()

    val totalAvailableCount: Flow<Int> = dao.countTotalAvailable()
    val totalIssuedCount: Flow<Int> = dao.countTotalIssued()

    fun getCardsByCategory(categoryId: Long): Flow<List<CardEntity>> {
        return dao.getCardsByCategory(categoryId)
    }

    fun searchIssuedCards(query: String): Flow<List<CardEntity>> {
        return if (query.isBlank()) {
            dao.getIssuedCards()
        } else {
            dao.searchIssuedCards(query.trim())
        }
    }

    fun getCategoriesWithStats(): Flow<List<CategoryWithStats>> {
        return combine(allCategories, allCards) { categories, cards ->
            categories.map { category ->
                val categoryCards = cards.filter { it.categoryId == category.id }
                val available = categoryCards.count { it.status == CardStatus.AVAILABLE.name }
                val issued = categoryCards.count { it.status == CardStatus.ISSUED.name }
                CategoryWithStats(
                    category = category,
                    availableCount = available,
                    issuedCount = issued,
                    totalCount = categoryCards.size
                )
            }
        }
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return dao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        dao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        dao.deleteCategory(category)
    }

    suspend fun dispenseNextCard(categoryId: Long): CardEntity? {
        return withContext(Dispatchers.IO) {
            dao.dispenseNextCard(categoryId)
        }
    }

    suspend fun dispenseNextCards(categoryId: Long, count: Int): List<CardEntity> {
        return withContext(Dispatchers.IO) {
            dao.dispenseNextCards(categoryId, count)
        }
    }

    suspend fun undoDispense(cardId: Long) {
        withContext(Dispatchers.IO) {
            dao.undoDispense(cardId)
        }
    }

    suspend fun addSingleCard(categoryId: Long, name: String, password: String, plan: String): Boolean {
        return withContext(Dispatchers.IO) {
            val id = dao.insertCardIgnore(
                CardEntity(
                    categoryId = categoryId,
                    name = name.trim(),
                    password = password.trim(),
                    plan = plan.trim()
                )
            )
            id != -1L
        }
    }

    suspend fun deleteCard(card: CardEntity) {
        dao.deleteCard(card)
    }

    suspend fun importCardsBatch(
        categoryId: Long,
        parsedCards: List<CardTextParser.ParsedCard>,
        replaceExisting: Boolean
    ): BatchImportSummary = withContext(Dispatchers.IO) {
        if (replaceExisting) {
            dao.deleteAvailableCards(categoryId)
        }

        var imported = 0
        var duplicates = 0

        for (parsed in parsedCards) {
            val card = CardEntity(
                categoryId = categoryId,
                name = parsed.name.trim(),
                password = parsed.password.trim(),
                plan = parsed.plan.trim()
            )
            val insertId = dao.insertCardIgnore(card)
            if (insertId != -1L) {
                imported++
            } else {
                duplicates++
            }
        }

        BatchImportSummary(
            importedCount = imported,
            duplicateSkippedCount = duplicates,
            totalProcessed = parsedCards.size
        )
    }

    suspend fun getAvailableCardsToExport(categoryId: Long): List<CardEntity> = withContext(Dispatchers.IO) {
        dao.getAvailableCardsList(categoryId)
    }

    suspend fun getAllCardsToExport(categoryId: Long): List<CardEntity> = withContext(Dispatchers.IO) {
        dao.getAllCardsListInCategory(categoryId)
    }

    suspend fun purgeAvailableCards(categoryId: Long): Int = withContext(Dispatchers.IO) {
        dao.deleteAvailableCards(categoryId)
    }

    suspend fun purgeAllCardsInCategory(categoryId: Long): Int = withContext(Dispatchers.IO) {
        dao.deleteAllCardsInCategory(categoryId)
    }

    suspend fun purgeAllAvailableCards(): Int = withContext(Dispatchers.IO) {
        dao.deleteAllAvailableCards()
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dao.clearAllCards()
        dao.clearAllCategories()
    }

    // --- Trash / Soft Delete ---
    val trashCards: Flow<List<CardEntity>> = dao.getTrashCards()
    val trashCount: Flow<Int> = dao.countTrashCards()

    suspend fun softDeleteCard(cardId: Long): Int = withContext(Dispatchers.IO) {
        dao.softDeleteCard(cardId)
    }

    suspend fun softDeleteAvailableCards(categoryId: Long): Int = withContext(Dispatchers.IO) {
        dao.softDeleteAvailableCards(categoryId)
    }

    suspend fun softDeleteAllCardsInCategory(categoryId: Long): Int = withContext(Dispatchers.IO) {
        dao.softDeleteAllCardsInCategory(categoryId)
    }

    suspend fun restoreFromTrash(cardId: Long): Int = withContext(Dispatchers.IO) {
        dao.restoreFromTrash(cardId)
    }

    suspend fun restoreAllFromTrash(): Int = withContext(Dispatchers.IO) {
        dao.restoreAllFromTrash()
    }

    suspend fun emptyTrash(): Int = withContext(Dispatchers.IO) {
        dao.emptyTrash()
    }
}
