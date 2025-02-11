package com.ocreboy.rolinitiative.database

import android.icu.text.CaseMap.Fold
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ocreboy.rolinitiative.model.Folder
import com.ocreboy.rolinitiative.model.SavedCharacter

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Folder)

    @Update
    suspend fun update(item: Folder)

    @Delete
    suspend fun delete(item: Folder)

    @Query("SELECT * FROM FOLDERS ORDER BY NAME DESC")
    fun getAll():List<Folder>

    @Query("SELECT * FROM FOLDERS WHERE NAME LIKE :query")
    fun search(query: String?): LiveData<List<Folder>>

    @Query("DELETE FROM FOLDERS WHERE id IN (:ids)")
    fun deleteFoldersByIds(ids: List<Int>)

    @Query("SELECT * FROM FOLDERS WHERE name = :name LIMIT 1")
    suspend fun getFolderByName(name: String): Folder?
}