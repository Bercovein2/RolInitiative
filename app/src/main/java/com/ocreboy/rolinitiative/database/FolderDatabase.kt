package com.ocreboy.rolinitiative.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ocreboy.rolinitiative.model.Folder
import com.ocreboy.rolinitiative.model.SavedCharacter

@Database(entities = [Folder::class], version = 1)
abstract class FolderDatabase : RoomDatabase() {


    abstract fun getDao() : FolderDao

    companion object{
        @Volatile
        private var instance : FolderDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context:Context) = instance ?:
        synchronized(LOCK){
            instance?:
            createDatabase(context).also{
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                FolderDatabase::class.java,
                "folder_db"
            ).build()
    }


}