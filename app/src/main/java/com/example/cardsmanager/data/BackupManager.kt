package com.example.cardsmanager.data

import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    suspend fun createBackupJson(dao: CardsDao): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("app", "HotspotCardsManager")
        root.put("exportTimestamp", System.currentTimeMillis())

        // Categories
        val categories = dao.getAllCategoriesList()
        val catArray = JSONArray()
        for (c in categories) {
            val cObj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("colorArgb", c.colorArgb)
                put("sortOrder", c.sortOrder)
                put("createdAt", c.createdAt)
            }
            catArray.put(cObj)
        }
        root.put("categories", catArray)

        // Cards
        val cards = dao.getAllCardsList()
        val cardArray = JSONArray()
        for (card in cards) {
            val cardObj = JSONObject().apply {
                put("id", card.id)
                put("categoryId", card.categoryId)
                put("name", card.name)
                put("password", card.password)
                put("plan", card.plan)
                put("status", card.status)
                put("issuedAt", card.issuedAt ?: JSONObject.NULL)
                put("createdAt", card.createdAt)
            }
            cardArray.put(cardObj)
        }
        root.put("cards", cardArray)

        return root.toString(2)
    }

    suspend fun restoreBackupJson(dao: CardsDao, jsonString: String): Result<String> {
        return try {
            val root = JSONObject(jsonString)
            val catArray = root.optJSONArray("categories") ?: JSONArray()
            val oldIdToNewId = mutableMapOf<Long, Long>()

            for (i in 0 until catArray.length()) {
                val cObj = catArray.getJSONObject(i)
                val oldId = cObj.optLong("id", 0)
                val category = CategoryEntity(
                    name = cObj.optString("name", "فئة"),
                    colorArgb = cObj.optInt("colorArgb", 0xFF2563EB.toInt()),
                    sortOrder = cObj.optInt("sortOrder", i),
                    createdAt = cObj.optLong("createdAt", System.currentTimeMillis())
                )
                val newId = dao.insertCategory(category)
                if (oldId > 0) {
                    oldIdToNewId[oldId] = newId
                }
            }

            val cardArray = root.optJSONArray("cards") ?: JSONArray()
            var importedCardsCount = 0
            for (i in 0 until cardArray.length()) {
                val cardObj = cardArray.getJSONObject(i)
                val oldCatId = cardObj.optLong("categoryId", 0)
                val targetCatId = oldIdToNewId[oldCatId] ?: oldCatId

                val card = CardEntity(
                    categoryId = targetCatId,
                    name = cardObj.optString("name", ""),
                    password = cardObj.optString("password", ""),
                    plan = cardObj.optString("plan", ""),
                    status = cardObj.optString("status", CardStatus.AVAILABLE.name),
                    issuedAt = if (cardObj.isNull("issuedAt")) null else cardObj.optLong("issuedAt"),
                    createdAt = cardObj.optLong("createdAt", System.currentTimeMillis())
                )
                if (card.name.isNotEmpty() && card.password.isNotEmpty()) {
                    val id = dao.insertCardIgnore(card)
                    if (id != -1L) {
                        importedCardsCount++
                    }
                }
            }

            Result.success("تم استعادة ${catArray.length()} فئة و $importedCardsCount كارت بنجاح")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
