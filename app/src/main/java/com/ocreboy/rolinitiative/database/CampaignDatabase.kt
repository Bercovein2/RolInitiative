package com.ocreboy.rolinitiative.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ocreboy.rolinitiative.model.Campaign

@Database(entities = [Campaign::class], version = 1)
abstract class CampaignDatabase : RoomDatabase() {

    abstract fun getDao(): CampaignDao

    companion object {
        @Volatile
        private var instance: CampaignDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                CampaignDatabase::class.java,
                "campaign_db"
            ).build()
    }
}