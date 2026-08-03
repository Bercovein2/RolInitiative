package com.ocreboy.rolinitiative.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.model.Campaign
import com.ocreboy.rolinitiative.utils.FrameColor

class CampaignAdapter(
    private val listener: Listener
) : RecyclerView.Adapter<CampaignAdapter.ViewHolder>() {

    interface Listener {
        fun onCampaignClick(campaign: Campaign)
        fun onSelectionChanged(selectionMode: Boolean, selectedCount: Int)
    }

    private val campaigns = mutableListOf<Campaign>()
    private val selectedPositions = mutableSetOf<Int>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.textCampaignName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_campaign, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = campaigns.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("CampaignAdapter", "Binding posición $position")
        val campaign = campaigns[position]

        holder.name.text = campaign.name

        FrameColor(holder.itemView.context).changeTextColor(holder.name)

        val selected = selectedPositions.contains(position)

        holder.itemView.isActivated = selected

        holder.itemView.setBackgroundColor(
            if (selected)
                Color.parseColor("#553498DB")
            else
                Color.TRANSPARENT
        )

        holder.itemView.setOnClickListener {

            if (isSelectionMode()) {
                toggleSelection(holder.adapterPosition)
            } else {
                listener.onCampaignClick(campaign)
            }
        }

        holder.itemView.setOnLongClickListener {

            toggleSelection(holder.adapterPosition)

            true
        }
    }

    private fun toggleSelection(position: Int) {

        if (selectedPositions.contains(position))
            selectedPositions.remove(position)
        else
            selectedPositions.add(position)

        notifyItemChanged(position)

        listener.onSelectionChanged(
            selectedPositions.isNotEmpty(),
            selectedPositions.size
        )
    }

    fun isSelectionMode() = selectedPositions.isNotEmpty()

    fun clearSelection() {

        selectedPositions.clear()

        notifyDataSetChanged()

        listener.onSelectionChanged(false, 0)
    }

    fun getSelectedCampaigns(): List<Campaign> =
        selectedPositions.map { campaigns[it] }

    fun getSingleSelected(): Campaign? =
        if (selectedPositions.size == 1)
            campaigns[selectedPositions.first()]
        else
            null

    fun submitList(list: List<Campaign>) {

        campaigns.clear()
        campaigns.addAll(list)

        clearSelection()

        notifyDataSetChanged()
    }
}