package com.ocreboy.rolinitiative.repository

import com.ocreboy.rolinitiative.database.CampaignDatabase
import com.ocreboy.rolinitiative.model.Campaign

class CampaignRepository(
    private val campaignDB: CampaignDatabase
) {

    suspend fun insert(campaign: Campaign): Long = campaignDB.getDao().insert(campaign)

    suspend fun update(campaign: Campaign): Int = campaignDB.getDao().update(campaign)

    suspend fun delete(campaign: Campaign) = campaignDB.getDao().delete(campaign)

    suspend fun getAllCampaigns(): List<Campaign> = campaignDB.getDao().getAllCampaigns()

    suspend fun getCampaignByName(name: String): Campaign? = campaignDB.getDao().getCampaignByName(name)

    suspend fun deleteCampaignsByIds(ids: List<Int>) = campaignDB.getDao().deleteCampaignsByIds(ids)

    suspend fun searchCampaigns(query: String?) = campaignDB.getDao().search(query)
    suspend fun getCampaignById(id: Int): Campaign = campaignDB.getDao().getCampaignById(id)
}