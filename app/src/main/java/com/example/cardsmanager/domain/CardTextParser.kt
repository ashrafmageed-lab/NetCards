package com.example.cardsmanager.domain

object CardTextParser {

    data class ParsedCard(
        val name: String,
        val password: String,
        val plan: String
    )

    data class InvalidLine(
        val lineNumber: Int,
        val rawContent: String,
        val reason: String
    )

    data class ParseResult(
        val validCards: List<ParsedCard>,
        val duplicateCountInFile: Int,
        val invalidLines: List<InvalidLine>,
        val totalLinesCount: Int
    )


    /**
     * Parses raw input text into cards, detecting delimiters and ignoring BOM / comments / headers.
     */
    fun parse(rawText: String, defaultPlan: String = ""): ParseResult {
        // Strip UTF-8 Byte Order Mark if present
        val cleanText = if (rawText.startsWith("\uFEFF")) rawText.substring(1) else rawText
        val lines = cleanText.split(Regex("\\r?\\n"))

        val validCards = mutableListOf<ParsedCard>()
        val invalidLines = mutableListOf<InvalidLine>()
        val seenNames = mutableSetOf<String>()
        var duplicateCountInFile = 0

        for ((index, rawLine) in lines.withIndex()) {
            val lineNumber = index + 1
            val trimmed = rawLine.trim()

            // Skip empty lines and comment lines
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                continue
            }

            // Auto-detect delimiter: tab, semicolon, or comma
            val delimiter = when {
                trimmed.contains('\t') -> "\t"
                trimmed.contains(';') -> ";"
                else -> ","
            }

            val parts = trimmed.split(delimiter).map { it.trim() }

            // Check if this is a header line (e.g. "username,password,plan")
            if (index == 0 && isHeaderLine(parts)) {
                continue
            }

            if (parts.size < 2) {
                invalidLines.add(
                    InvalidLine(
                        lineNumber = lineNumber,
                        rawContent = rawLine,
                        reason = "يجب توفر اسم المستخدم وكلمة المرور على الأقل (مفصولين بفاصلة أو نقطة منقوطة)"
                    )
                )
                continue
            }

            val name = parts[0]
            val password = parts[1]
            val plan = if (parts.size >= 3 && parts[2].isNotEmpty()) parts[2] else defaultPlan

            if (name.isEmpty() || password.isEmpty()) {
                invalidLines.add(
                    InvalidLine(
                        lineNumber = lineNumber,
                        rawContent = rawLine,
                        reason = "اسم المستخدم أو كلمة المرور فارغة"
                    )
                )
                continue
            }

            // Check for duplicate in the same file (case-insensitive)
            val normalizedKey = name.lowercase()
            if (seenNames.contains(normalizedKey)) {
                duplicateCountInFile++
                continue
            }

            seenNames.add(normalizedKey)
            validCards.add(
                ParsedCard(
                    name = name,
                    password = password,
                    plan = plan
                )
            )
        }

        return ParseResult(
            validCards = validCards,
            duplicateCountInFile = duplicateCountInFile,
            invalidLines = invalidLines,
            totalLinesCount = lines.size
        )
    }

    private val USER_HEADERS = setOf("username", "user", "name", "مستخدم", "اسم المستخدم")
    private val PASS_HEADERS = setOf("password", "pass", "pwd", "كلمة المرور", "باسورد", "رمز")

    private fun isHeaderLine(parts: List<String>): Boolean {
        if (parts.size < 2) return false
        val p0 = parts[0].trim().lowercase()
        val p1 = parts[1].trim().lowercase()
        return USER_HEADERS.contains(p0) && PASS_HEADERS.contains(p1)
    }

    /**
     * Formats a list of cards back to standard text lines for exporting / re-importing.
     * Format: username,password,plan
     */
    fun exportToText(cards: List<com.example.cardsmanager.data.CardEntity>): String {
        val sb = StringBuilder()
        for (card in cards) {
            sb.append(card.name).append(",").append(card.password)
            if (card.plan.isNotEmpty()) {
                sb.append(",").append(card.plan)
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}
