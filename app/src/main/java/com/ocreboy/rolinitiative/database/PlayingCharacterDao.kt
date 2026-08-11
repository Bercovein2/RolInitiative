package com.ocreboy.rolinitiative.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ocreboy.rolinitiative.model.Campaign
import com.ocreboy.rolinitiative.model.PlayingCharacter

@Dao
interface PlayingCharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PlayingCharacter) : Long

    @Update
    suspend fun update(item: PlayingCharacter) : Int

    @Delete
    suspend fun delete(item: PlayingCharacter)

    @Query("SELECT * FROM PLAYING_CHARACTERS ORDER BY NAME DESC")
    fun getAll(): List<PlayingCharacter>

    @Query("SELECT * FROM PLAYING_CHARACTERS WHERE NAME LIKE :query")
    fun search(query: String?): LiveData<List<PlayingCharacter>>

    @Query("DELETE FROM PLAYING_CHARACTERS WHERE id IN (:ids)")
    fun deleteCharacterByIds(ids: List<Int>)

    @Query("SELECT * FROM PLAYING_CHARACTERS WHERE name = :name LIMIT 1")
    suspend fun getCampaignByName(name: String): PlayingCharacter?

    @Query("SELECT * FROM PLAYING_CHARACTERS")
    suspend fun getAllCharacter(): List<PlayingCharacter>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: PlayingCharacter)

    @Delete
    suspend fun deleteCharacter(character: PlayingCharacter)

    @Query("DELETE FROM PLAYING_CHARACTERS WHERE campaignId IN (:ids)")
    suspend fun deletePlayersByCampaignId(ids: List<Int>)

    @Query("SELECT * FROM PLAYING_CHARACTERS WHERE campaignId = :id")
    suspend fun getPlayersByCampaignId(id: Int): List<PlayingCharacter>
}