package com.example.cardsmanager.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CardsDaoRobolectricTest {

    private lateinit var db: CardsDatabase
    private lateinit var dao: CardsDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, CardsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.cardsDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun uniqueConstraint_skipsDuplicateUsernameInSameCategoryCaseInsensitive() = runBlocking {
        val catId = dao.insertCategory(CategoryEntity(name = "الدور الثالث"))

        val id1 = dao.insertCardIgnore(
            CardEntity(categoryId = catId, name = "user01", password = "pass1", plan = "5GB")
        )
        assertTrue(id1 != -1L)

        // Try inserting same username with uppercase
        val id2 = dao.insertCardIgnore(
            CardEntity(categoryId = catId, name = "USER01", password = "pass2", plan = "5GB")
        )
        // Should be ignored by unique constraint index
        assertEquals(-1L, id2)

        // Different category can have the same username
        val otherCatId = dao.insertCategory(CategoryEntity(name = "الدور الخامس"))
        val id3 = dao.insertCardIgnore(
            CardEntity(categoryId = otherCatId, name = "user01", password = "pass3", plan = "5GB")
        )
        assertTrue(id3 != -1L)
    }

    @Test
    fun atomicDispense_marksCardAsIssuedAndSetsTimestamp() = runBlocking {
        val catId = dao.insertCategory(CategoryEntity(name = "الدور الثالث"))
        dao.insertCard(CardEntity(categoryId = catId, name = "userA", password = "pA"))
        dao.insertCard(CardEntity(categoryId = catId, name = "userB", password = "pB"))

        val dispensed = dao.dispenseNextCard(catId)
        assertNotNull(dispensed)
        assertEquals("userA", dispensed!!.name)
        assertEquals(CardStatus.ISSUED.name, dispensed.status)
        assertNotNull(dispensed.issuedAt)

        // Second dispense should give userB
        val secondDispensed = dao.dispenseNextCard(catId)
        assertNotNull(secondDispensed)
        assertEquals("userB", secondDispensed!!.name)

        // Third dispense should be null (empty)
        val thirdDispensed = dao.dispenseNextCard(catId)
        assertNull(thirdDispensed)
    }

    @Test
    fun undoDispense_restoresCardToAvailable() = runBlocking {
        val catId = dao.insertCategory(CategoryEntity(name = "الدور الثالث"))
        dao.insertCard(CardEntity(categoryId = catId, name = "userX", password = "pX"))

        val dispensed = dao.dispenseNextCard(catId)!!
        assertEquals(CardStatus.ISSUED.name, dispensed.status)

        // Undo
        dao.undoDispense(dispensed.id)

        // Should be available to dispense again
        val reDispensed = dao.dispenseNextCard(catId)
        assertNotNull(reDispensed)
        assertEquals("userX", reDispensed!!.name)
    }

    @Test
    fun cascadeDelete_deletesAllCardsInCategory() = runBlocking {
        val catId = dao.insertCategory(CategoryEntity(name = "الدور الثالث"))
        dao.insertCard(CardEntity(categoryId = catId, name = "u1", password = "p1"))
        dao.insertCard(CardEntity(categoryId = catId, name = "u2", password = "p2"))

        val category = dao.getCategoryById(catId)!!
        dao.deleteCategory(category)

        val cardsLeft = dao.getAllCardsListInCategory(catId)
        assertTrue(cardsLeft.isEmpty())
    }

    @Test
    fun softDelete_movesCardToTrashAndRestoresIt() = runBlocking {
        val catId = dao.insertCategory(CategoryEntity(name = "الدور الثالث"))
        val cardId = dao.insertCard(CardEntity(categoryId = catId, name = "userT", password = "pT"))

        // Soft delete
        dao.softDeleteCard(cardId)
        val available = dao.getAvailableCardsList(catId)
        assertTrue(available.isEmpty())

        // Restore
        dao.restoreFromTrash(cardId)
        val restored = dao.getAvailableCardsList(catId)
        assertEquals(1, restored.size)
        assertEquals("userT", restored[0].name)
    }

    @Test
    fun emptyTrash_permanentlyDeletesOnlyTrashCards() = runBlocking {
        val catId = dao.insertCategory(CategoryEntity(name = "الدور الثالث"))
        val card1Id = dao.insertCard(CardEntity(categoryId = catId, name = "u1", password = "p1"))
        val card2Id = dao.insertCard(CardEntity(categoryId = catId, name = "u2", password = "p2"))

        // Put u1 in trash
        dao.softDeleteCard(card1Id)

        // Empty trash
        val deletedCount = dao.emptyTrash()
        assertEquals(1, deletedCount)

        // u2 should still exist
        val remaining = dao.getAvailableCardsList(catId)
        assertEquals(1, remaining.size)
        assertEquals("u2", remaining[0].name)
    }
}
