package com.example.cardsmanager.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CardTextParserTest {

    @Test
    fun parse_validCommaSeparated() {
        val input = """
            user01,pass123,5GB
            user02,pass456,10GB
        """.trimIndent()

        val result = CardTextParser.parse(input)
        assertEquals(2, result.validCards.size)
        assertEquals("user01", result.validCards[0].name)
        assertEquals("pass123", result.validCards[0].password)
        assertEquals("5GB", result.validCards[0].plan)
        assertEquals(0, result.invalidLines.size)
        assertEquals(0, result.duplicateCountInFile)
    }

    @Test
    fun parse_detectsSemicolonAndTabDelimiters() {
        val inputSemicolon = "user01;pass123;5GB\nuser02;pass456"
        val resultSemi = CardTextParser.parse(inputSemicolon, defaultPlan = "DefaultPlan")
        assertEquals(2, resultSemi.validCards.size)
        assertEquals("DefaultPlan", resultSemi.validCards[1].plan)

        val inputTab = "user01\tpass123\t5GB"
        val resultTab = CardTextParser.parse(inputTab)
        assertEquals(1, resultTab.validCards.size)
        assertEquals("user01", resultTab.validCards[0].name)
    }

    @Test
    fun parse_stripsBomSuccessfully() {
        val inputWithBom = "\uFEFFuser01,pass123,5GB"
        val result = CardTextParser.parse(inputWithBom)
        assertEquals(1, result.validCards.size)
        assertEquals("user01", result.validCards[0].name)
    }

    @Test
    fun parse_skipsHeaderAndComments() {
        val input = """
            username,password,plan
            # This is a comment
            // Another comment
            user01,pass123,5GB
        """.trimIndent()

        val result = CardTextParser.parse(input)
        assertEquals(1, result.validCards.size)
        assertEquals("user01", result.validCards[0].name)
        assertEquals(0, result.invalidLines.size)
    }

    @Test
    fun parse_skipsDuplicatesWithinSameFileCaseInsensitive() {
        val input = """
            user01,pass123,5GB
            USER01,pass999,5GB
            user02,pass456,10GB
        """.trimIndent()

        val result = CardTextParser.parse(input)
        assertEquals(2, result.validCards.size)
        assertEquals(1, result.duplicateCountInFile)
    }

    @Test
    fun parse_recordsInvalidLinesWithReasons() {
        val input = """
            onlyOneValue
            ,emptyUser
            user01,pass123,5GB
        """.trimIndent()

        val result = CardTextParser.parse(input)
        assertEquals(1, result.validCards.size)
        assertEquals(2, result.invalidLines.size)
        assertEquals(1, result.invalidLines[0].lineNumber)
    }

    @Test
    fun exportToText_formatsCorrectly() {
        val cards = listOf(
            com.example.cardsmanager.data.CardEntity(
                categoryId = 1,
                name = "user01",
                password = "pass1",
                plan = "5GB"
            ),
            com.example.cardsmanager.data.CardEntity(
                categoryId = 1,
                name = "user02",
                password = "pass2",
                plan = ""
            )
        )
        val text = CardTextParser.exportToText(cards)
        assertTrue(text.contains("user01,pass1,5GB"))
        assertTrue(text.contains("user02,pass2"))
    }
}
