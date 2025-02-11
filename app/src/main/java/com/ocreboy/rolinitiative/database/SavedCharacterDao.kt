package com.ocreboy.rolinitiative.database

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
interface SavedCharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SavedCharacter): Long

    @Update
    suspend fun update(item: SavedCharacter) : Int

    @Delete
    suspend fun delete(item: SavedCharacter)

    @Query("SELECT * FROM CHARACTERS ORDER BY NAME DESC")
    fun getAllLiveData():LiveData<List<SavedCharacter>>

    @Query("SELECT * FROM CHARACTERS ORDER BY NAME DESC")
    fun getAll():List<SavedCharacter>

//    @Query("SELECT * FROM CHARACTERS WHERE folder = :folder ORDER BY NAME")
//    fun getAllByFolder(folder: Folder):LiveData<List<SavedCharacter>>

    @Query("SELECT * FROM CHARACTERS WHERE NAME LIKE :query")
    fun search(query: String?): LiveData<List<SavedCharacter>>


    @Query("DELETE FROM CHARACTERS WHERE id = :characterId")
    suspend fun deleteCharacterById(characterId: Int)

    // También puedes definir una función para eliminar múltiples personajes por sus IDs
    @Query("DELETE FROM CHARACTERS WHERE id IN (:characterIds)")
    suspend fun deleteCharactersByIds(characterIds: List<Int>)

}