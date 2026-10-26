package com.example.cardsmanager.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CardStatus {
    AVAILABLE,
    ISSUED,
    TRASH
}

@Entity(
    tableName = "categories"
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorArgb: Int = 0xFF2563EB.toInt(),
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["categoryId", "name"], unique = true),
        Index(value = ["categoryId", "status"]),
        Index(value = ["issuedAt"])
    ]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val name: String,
    val password: String,
    val plan: String = "",
    val status: String = CardStatus.AVAILABLE.name,
    val issuedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isAvailable: Boolean get() = status == CardStatus.AVAILABLE.name
    val isIssued: Boolean get() = status == CardStatus.ISSUED.name
    val isTrash: Boolean get() = status == CardStatus.TRASH.name
}

data class CategoryWithStats(
    val category: CategoryEntity,
    val availableCount: Int,
    val issuedCount: Int,
    val totalCount: Int
) {
    val usagePercentage: Float
        get() = if (totalCount > 0) (issuedCount.toFloat() / totalCount.toFloat()) else 0f
}
