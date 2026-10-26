package com.example.cardsmanager.domain

import com.example.cardsmanager.data.CardEntity
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageTemplateTest {

    @Test
    fun formatSingleCard_replacesVariables() {
        val template = "كارت {category}\nمستخدم: {name}\nباسورد: {pass}\nباقة: {plan}"
        val card = CardEntity(
            categoryId = 1,
            name = "user301",
            password = "pass8924",
            plan = "5GB"
        )
        val result = MessageTemplate.formatSingleCard(template, card, "الدور الثالث")
        assertTrue(result.contains("الدور الثالث"))
        assertTrue(result.contains("user301"))
        assertTrue(result.contains("pass8924"))
        assertTrue(result.contains("5GB"))
    }

    @Test
    fun formatMultipleCards_numbersAndSeparatesCards() {
        val cards = listOf(
            CardEntity(categoryId = 1, name = "user01", password = "pass1", plan = "5GB"),
            CardEntity(categoryId = 1, name = "user02", password = "pass2", plan = "5GB")
        )
        val result = MessageTemplate.formatMultipleCards(cards, "الدور الخامس")
        assertTrue(result.contains("(العدد: 2)"))
        assertTrue(result.contains("1) اسم المستخدم: user01"))
        assertTrue(result.contains("2) اسم المستخدم: user02"))
        assertTrue(result.contains("pass1"))
        assertTrue(result.contains("pass2"))
    }
}
