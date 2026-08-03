package com.ocreboy.rolinitiative.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ocreboy.rolinitiative.model.Campaign

@Dao
interface CampaignDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Campaign) : Long

    @Update
    suspend fun update(item: Campaign) : Int

    @Delete
    suspend fun delete(item: Campaign)

    @Query("SELECT * FROM CAMPAIGNS ORDER BY NAME DESC")
    fun getAll(): List<Campaign>

    @Query("SELECT * FROM CAMPAIGNS WHERE NAME LIKE :query")
    fun search(query: String?): LiveData<List<Campaign>>

    @Query("DELETE FROM CAMPAIGNS WHERE id IN (:ids)")
    fun deleteCampaignsByIds(ids: List<Int>)

    @Query("SELECT * FROM CAMPAIGNS WHERE name = :name LIMIT 1")
    suspend fun getCampaignByName(name: String): Campaign?

    @Query("SELECT * FROM campaigns")
    suspend fun getAllCampaigns(): List<Campaign>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign)

    @Delete
    suspend fun deleteCampaign(campaign: Campaign)
}