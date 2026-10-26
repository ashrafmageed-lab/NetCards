package com.example.cardsmanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardsDao {

    // --- Categories ---
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, id ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesList(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    // --- Cards Queries ---
    @Query("SELECT * FROM cards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE categoryId = :categoryId ORDER BY status ASC, id ASC")
    fun getCardsByCategory(categoryId: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE categoryId = :categoryId AND status = 'AVAILABLE' ORDER BY id ASC")
    fun getAvailableCardsByCategory(categoryId: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE categoryId = :categoryId AND status = 'AVAILABLE' ORDER BY id ASC")
    suspend fun getAvailableCardsList(categoryId: Long): List<CardEntity>

    @Query("SELECT * FROM cards WHERE categoryId = :categoryId ORDER BY id ASC")
    suspend fun getAllCardsListInCategory(categoryId: Long): List<CardEntity>

    @Query("SELECT * FROM cards WHERE status = 'ISSUED' ORDER BY issuedAt DESC, id DESC")
    fun getIssuedCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE status = 'ISSUED' AND (name LIKE '%' || :query || '%' OR plan LIKE '%' || :query || '%') ORDER BY issuedAt DESC")
    fun searchIssuedCards(query: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards")
    suspend fun getAllCardsList(): List<CardEntity>

    // Counts
    @Query("SELECT COUNT(*) FROM cards WHERE categoryId = :categoryId AND status = 'AVAILABLE'")
    fun countAvailableCards(categoryId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE categoryId = :categoryId AND status = 'ISSUED'")
    fun countIssuedCards(categoryId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE categoryId = :categoryId")
    fun countTotalCards(categoryId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE status = 'AVAILABLE'")
    fun countTotalAvailable(): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE status = 'ISSUED'")
    fun countTotalIssued(): Flow<Int>

    // Card Mutations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCardIgnore(card: CardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity): Long

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    // --- Atomic Card Dispensing ---
    @Query("SELECT * FROM cards WHERE categoryId = :categoryId AND status = 'AVAILABLE' ORDER BY id ASC LIMIT 1")
    suspend fun getOldestAvailableCard(categoryId: Long): CardEntity?

    @Query("SELECT * FROM cards WHERE categoryId = :categoryId AND status = 'AVAILABLE' ORDER BY id ASC LIMIT :limit")
    suspend fun getOldestAvailableCards(categoryId: Long, limit: Int): List<CardEntity>

    @Query("UPDATE cards SET status = :status, issuedAt = :issuedAt WHERE id = :cardId")
    suspend fun updateCardStatus(cardId: Long, status: String, issuedAt: Long?)

    @Transaction
    suspend fun dispenseNextCard(categoryId: Long): CardEntity? {
        val next = getOldestAvailableCard(categoryId) ?: return null
        val now = System.currentTimeMillis()
        updateCardStatus(next.id, CardStatus.ISSUED.name, now)
        return next.copy(status = CardStatus.ISSUED.name, issuedAt = now)
    }

    @Transaction
    suspend fun dispenseNextCards(categoryId: Long, count: Int): List<CardEntity> {
        val availableList = getOldestAvailableCards(categoryId, count)
        val now = System.currentTimeMillis()
        val result = mutableListOf<CardEntity>()
        for (card in availableList) {
            updateCardStatus(card.id, CardStatus.ISSUED.name, now)
            result.add(card.copy(status = CardStatus.ISSUED.name, issuedAt = now))
        }
        return result
    }

    @Query("UPDATE cards SET status = 'AVAILABLE', issuedAt = NULL WHERE id = :cardId")
    suspend fun undoDispense(cardId: Long)

    // --- Batch Purge Operations ---
    @Query("DELETE FROM cards WHERE categoryId = :categoryId AND status = 'AVAILABLE'")
    suspend fun deleteAvailableCards(categoryId: Long): Int

    @Query("DELETE FROM cards WHERE categoryId = :categoryId")
    suspend fun deleteAllCardsInCategory(categoryId: Long): Int

    @Query("DELETE FROM cards WHERE status = 'AVAILABLE'")
    suspend fun deleteAllAvailableCards(): Int

    @Query("DELETE FROM cards")
    suspend fun clearAllCards(): Int

    @Query("DELETE FROM categories")
    suspend fun clearAllCategories(): Int

    // --- Trash / Soft Delete Operations ---
    @Query("SELECT * FROM cards WHERE status = 'TRASH' ORDER BY id DESC")
    fun getTrashCards(): Flow<List<CardEntity>>

    @Query("SELECT COUNT(*) FROM cards WHERE status = 'TRASH'")
    fun countTrashCards(): Flow<Int>

    @Query("UPDATE cards SET status = 'TRASH' WHERE id = :cardId")
    suspend fun softDeleteCard(cardId: Long): Int

    @Query("UPDATE cards SET status = 'TRASH' WHERE categoryId = :categoryId AND status = 'AVAILABLE'")
    suspend fun softDeleteAvailableCards(categoryId: Long): Int

    @Query("UPDATE cards SET status = 'TRASH' WHERE categoryId = :categoryId")
    suspend fun softDeleteAllCardsInCategory(categoryId: Long): Int

    @Query("UPDATE cards SET status = 'AVAILABLE' WHERE id = :cardId")
    suspend fun restoreFromTrash(cardId: Long): Int

    @Query("UPDATE cards SET status = 'AVAILABLE' WHERE status = 'TRASH'")
    suspend fun restoreAllFromTrash(): Int

    @Query("DELETE FROM cards WHERE status = 'TRASH'")
    suspend fun emptyTrash(): Int
}
