package com.ocreboy.rolinitiative.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ocreboy.rolinitiative.model.Campaign
import com.ocreboy.rolinitiative.model.PlayingCharacter

@Database(entities = [PlayingCharacter::class], version = 4)
abstract class PlayingCharacterDatabase : RoomDatabase() {

    abstract fun getDao(): PlayingCharacterDao

    companion object {
        @Volatile
        private var instance: PlayingCharacterDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                PlayingCharacterDatabase::class.java,
                "playing_character_db"
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}