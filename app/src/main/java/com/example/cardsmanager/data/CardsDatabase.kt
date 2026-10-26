package com.example.cardsmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        CategoryEntity::class,
        CardEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CardsDatabase : RoomDatabase() {

    abstract fun cardsDao(): CardsDao

    companion object {
        @Volatile
        private var INSTANCE: CardsDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CardsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CardsDatabase::class.java,
                    "hotspot_cards.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(CardsDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class CardsDatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val now = System.currentTimeMillis()

                // Prepopulate 4 default hotspot categories atomically in the DB creation transaction
                db.execSQL("INSERT INTO categories (name, colorArgb, sortOrder, createdAt) VALUES ('الدور الثالث - 5 جيجا', -14327061, 1, $now)")
                db.execSQL("INSERT INTO categories (name, colorArgb, sortOrder, createdAt) VALUES ('الدور الثالث - 10 جيجا', -15887208, 2, $now)")
                db.execSQL("INSERT INTO categories (name, colorArgb, sortOrder, createdAt) VALUES ('الدور الخامس - 5 جيجا', -2525146, 3, $now)")
                db.execSQL("INSERT INTO categories (name, colorArgb, sortOrder, createdAt) VALUES ('الدور الخامس - 10 جيجا', -8635923, 4, $now)")

                // Prepopulate initial demo hotspot cards
                db.execSQL("INSERT INTO cards (categoryId, name, password, plan, status, createdAt) VALUES (1, 'user301', 'pass8924', '5GB', 'AVAILABLE', $now)")
                db.execSQL("INSERT INTO cards (categoryId, name, password, plan, status, createdAt) VALUES (1, 'user302', 'pass1928', '5GB', 'AVAILABLE', $now)")
                db.execSQL("INSERT INTO cards (categoryId, name, password, plan, status, createdAt) VALUES (2, 'user310', 'pass7291', '10GB', 'AVAILABLE', $now)")
                db.execSQL("INSERT INTO cards (categoryId, name, password, plan, status, createdAt) VALUES (3, 'user501', 'pass4810', '5GB', 'AVAILABLE', $now)")
                db.execSQL("INSERT INTO cards (categoryId, name, password, plan, status, createdAt) VALUES (4, 'user510', 'pass9041', '10GB', 'AVAILABLE', $now)")
            }
        }
    }
}
