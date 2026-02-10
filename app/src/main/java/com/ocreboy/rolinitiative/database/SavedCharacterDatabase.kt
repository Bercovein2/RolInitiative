package com.ocreboy.rolinitiative.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ocreboy.rolinitiative.model.SavedCharacter

@Database(
    entities = [SavedCharacter::class],
    version = 3,
    exportSchema = false
)
abstract class SavedCharacterDatabase : RoomDatabase() {

    abstract fun getDao(): SavedCharacterDao

    companion object {
        @Volatile
        private var instance: SavedCharacterDatabase? = null
        private val LOCK = Any()

        /**
         * MIGRATION 1 -> 2
         * Se agregó el campo initiative
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE characters ADD COLUMN initiative INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        /**
         * MIGRATION 2 -> 3
         * Se agrega el campo imageUri (nullable)
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE characters ADD COLUMN imageUri TEXT"
                )
            }
        }

        operator fun invoke(context: Context): SavedCharacterDatabase =
            instance ?: synchronized(LOCK) {
                instance ?: createDatabase(context).also {
                    instance = it
                }
            }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                SavedCharacterDatabase::class.java,
                "character_db"
            )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3
                )
                .build()
    }
}
