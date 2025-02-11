package com.ocreboy.rolinitiative.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ocreboy.rolinitiative.model.SavedCharacter

@Database(entities = [SavedCharacter::class], version = 2)
abstract class SavedCharacterDatabase : RoomDatabase() {

    abstract fun getDao(): SavedCharacterDao

    companion object {
        @Volatile
        private var instance: SavedCharacterDatabase? = null
        private val LOCK = Any()

        // Definir la migración de la versión 1 a la versión 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Agregar la nueva columna 'initiative' con un valor por defecto
                db.execSQL("ALTER TABLE characters ADD COLUMN initiative INTEGER NOT NULL DEFAULT 0")
            }
        }

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
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
                // Añadir la migración cuando crees la base de datos
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
