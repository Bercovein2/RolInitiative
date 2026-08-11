package com.ocreboy.rolinitiative.repository

import com.ocreboy.rolinitiative.database.PlayingCharacterDatabase
import com.ocreboy.rolinitiative.model.Campaign
import com.ocreboy.rolinitiative.model.PlayingCharacter

class PlayingCharacterRepository(
    private val playerCharacterDB: PlayingCharacterDatabase
) {

    suspend fun insert(character: PlayingCharacter): Long = playerCharacterDB.getDao().insert(character)

    suspend fun update(campaign: PlayingCharacter): Int = playerCharacterDB.getDao().update(campaign)

    suspend fun delete(campaign: PlayingCharacter) = playerCharacterDB.getDao().delete(campaign)
    suspend fun deletePlayersByCampaignId(ids: List<Int>) = playerCharacterDB.getDao().deletePlayersByCampaignId(ids)
    suspend fun getPlayersByCampaignId(id: Int): List<PlayingCharacter> = playerCharacterDB.getDao().getPlayersByCampaignId(id)

}