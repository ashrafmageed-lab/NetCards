package com.example.cardsmanager.domain

import com.example.cardsmanager.data.CardEntity

object MessageTemplate {

    const val DEFAULT_TEMPLATE =
        "كارت إنترنت - {category}\nاسم المستخدم: {name}\nكلمة المرور: {pass}\nالباقة: {plan}"

    fun formatSingleCard(
        template: String,
        card: CardEntity,
        categoryName: String
    ): String {
        var msg = template
        msg = msg.replace("{name}", card.name)
        msg = msg.replace("{pass}", card.password)
        msg = msg.replace("{plan}", card.plan.ifEmpty { "افتراضية" })
        msg = msg.replace("{category}", categoryName)
        return msg
    }

    fun formatMultipleCards(
        cards: List<CardEntity>,
        categoryName: String
    ): String {
        if (cards.isEmpty()) return ""
        if (cards.size == 1) {
            return formatSingleCard(DEFAULT_TEMPLATE, cards[0], categoryName)
        }

        val sb = StringBuilder()
        sb.append("كروت إنترنت - ").append(categoryName).append(" (العدد: ").append(cards.size).append("):\n")
        sb.append("════════════════════════\n")
        cards.forEachIndexed { index, card ->
            sb.append("${index + 1}) اسم المستخدم: ").append(card.name).append("\n")
            sb.append("   كلمة المرور: ").append(card.password).append("\n")
            if (card.plan.isNotEmpty()) {
                sb.append("   الباقة: ").append(card.plan).append("\n")
            }
            if (index < cards.size - 1) {
                sb.append("────────────────────────\n")
            }
        }
        sb.append("════════════════════════")
        return sb.toString()
    }
}
