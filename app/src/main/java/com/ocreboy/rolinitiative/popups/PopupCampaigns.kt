package com.ocreboy.rolinitiative.popups

import com.ocreboy.rolinitiative.R
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ocreboy.rolinitiative.MainActivity
import com.ocreboy.rolinitiative.adapter.CampaignAdapter
import com.ocreboy.rolinitiative.database.CampaignDatabase
import com.ocreboy.rolinitiative.model.Campaign
import com.ocreboy.rolinitiative.repository.CampaignRepository
import com.ocreboy.rolinitiative.utils.FrameColor
import com.ocreboy.rolinitiative.utils.PopupUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PopupCampaigns(
    private val mainActivity: MainActivity,
    private var campaigns: MutableList<Campaign>,
    private val onCampaignSelected: (Campaign) -> Unit
) {
    private val repository: CampaignRepository
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private lateinit var popupWindow: PopupWindow

    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonClose: ImageButton
    private lateinit var buttonCreate: ImageButton
    private lateinit var buttonSelectAll: Button
    private lateinit var buttonSelectNone: Button
    private lateinit var buttonDelete: Button
    private lateinit var buttonOpen: Button
    private lateinit var textEmpty: TextView
    private lateinit var textTitle: TextView
    lateinit var frameColor: FrameColor

    private lateinit var adapter: CampaignAdapter

    init {
        val dbCampaign = CampaignDatabase.invoke(mainActivity)
        repository = CampaignRepository(dbCampaign)
    }

    fun show(anchor: View) {

        val popupView = LayoutInflater.from(mainActivity)
            .inflate(R.layout.popup_campaigns_list, null)

        val displayMetrics = mainActivity.resources.displayMetrics
        frameColor = FrameColor(mainActivity)

        popupWindow = PopupWindow(
            popupView,
            (displayMetrics.widthPixels * 0.9).toInt(),
            (displayMetrics.heightPixels * 0.9).toInt(),
            true
        )
        popupView.setBackgroundResource(frameColor.getFrameColor())
        popupWindow.elevation = 20f

        bindViews(popupView)

        setupRecycler()

        setupButtons()

        popupWindow.showAtLocation(anchor, Gravity.CENTER, 0, 0)
        PopupUtils.dimBehind(mainActivity, popupWindow)

        loadCampaignsFromDatabase()

    }

    private fun bindViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewCampaigns)
        buttonClose = view.findViewById(R.id.buttonCampaignClose)
        buttonCreate = view.findViewById(R.id.buttonCampaignCreate)
        buttonDelete = view.findViewById(R.id.buttonDeleteCampaign)
        buttonOpen = view.findViewById(R.id.buttonOpenCampaign)
        buttonSelectAll = view.findViewById(R.id.buttonSelectAll)
        buttonSelectNone = view.findViewById(R.id.buttonSelectNone)
        textEmpty = view.findViewById(R.id.textViewEmptyCampaigns)
        textTitle = view.findViewById(R.id.textViewCampaignTitle)
    }

    private fun setupRecycler() {
        adapter = CampaignAdapter(object : CampaignAdapter.Listener {

            override fun onCampaignClick(campaign: Campaign) {
                onCampaignSelected(campaign)
                // Eliminar esta línea para evitar que el popup se cierre
                // popupWindow.dismiss()
            }

            override fun onSelectionChanged(selectionMode: Boolean, selectedCount: Int) {
                textTitle.text = if (selectionMode) "$selectedCount " + mainActivity.getString(R.string.selected_campaign) else mainActivity.getString(R.string.my_campaigns)

                when {
                    selectedCount == 0 -> {
                        enableOpenButton(false)
                        enableDeleteButton(false)
                    }
                    selectedCount == 1 -> {
                        enableOpenButton(true)
                        enableDeleteButton(true)
                    }
                    selectedCount > 1 -> {
                        enableOpenButton(false)
                        enableDeleteButton(true)
                    }
                }
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(mainActivity)
        recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        frameColor.changeVectorColorDarkLightGray(buttonCreate)
        enableOpenButton(false)
        enableDeleteButton(false)

        buttonClose.setOnClickListener { popupWindow.dismiss() }

        buttonCreate.setOnClickListener {
            PopupCreateCampaign(
                mainActivity,
                onCreateNew = { /* Create a new campaign */ },
                onUseCurrent = { /* Save and use the current campaign */ }
            ).show()
        }

        buttonDelete.setOnClickListener {
            val selectedCampaigns = adapter.getSelectedCampaigns()
            if (selectedCampaigns.isNotEmpty()) {
                showDeleteConfirmationDialog(selectedCampaigns)
            }
        }

        buttonOpen.setOnClickListener {
            adapter.getSingleSelected()?.let {
                onCampaignSelected(it)
                popupWindow.dismiss()
            }
        }

        buttonSelectAll.setOnClickListener {
            adapter.selectAll()
        }

        buttonSelectNone.setOnClickListener {
            adapter.clearSelection()
        }
    }

    private fun enableOpenButton (isAnySelected: Boolean){
        buttonOpen.isEnabled = isAnySelected
    }

    private fun enableDeleteButton (isAnySelected: Boolean){
        buttonDelete.isEnabled = isAnySelected
    }

    private fun createCampaign() {

        // TODO
        // Mostrar diálogo para crear campaña

    }

    private fun deleteSelectedCampaigns(selectedCampaigns : List<Campaign>) {

        if (selectedCampaigns.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    // Eliminar campañas de la base de datos
                    repository.deleteCampaignsByIds(selectedCampaigns.map { it.id })

                    withContext(Dispatchers.Main) {
                        // Eliminar campañas de la lista local
                        campaigns.removeAll(selectedCampaigns.toSet())
                        adapter.submitList(campaigns.toList())

                        // Actualizar visibilidad del texto vacío
                        textEmpty.visibility = if (campaigns.isEmpty()) View.VISIBLE else View.GONE
                    }
                } catch (e: Exception) {
                    Log.e("PopupCampaign", "Error al eliminar campañas", e)
                }
            }
        }

    }

    private fun mockCampaigns(): MutableList<Campaign> {
        val mockCampaigns: MutableList<Campaign> = mutableListOf(
            Campaign(id = 1, name = "Campaign 1"),
            Campaign(id = 2, name = "Campaign 2"),
            Campaign(id = 1, name = "Campaign 1"),
            Campaign(id = 2, name = "Campaign 2"),
            Campaign(id = 1, name = "Campaign 1"),
            Campaign(id = 2, name = "Campaign 2"),
            Campaign(id = 1, name = "Campaign 1"),
            Campaign(id = 2, name = "Campaign 2"),
            Campaign(id = 1, name = "Campaign 1"),
            Campaign(id = 2, name = "Campaign 2"),
            Campaign(id = 1, name = "Campaign 1"),
            Campaign(id = 2, name = "Campaign 2"),
            Campaign(id = 1, name = "Campaign 1"),
            Campaign(id = 2, name = "Campaign 2"),
            Campaign(id = 1, name = "Campaign 1"),
            Campaign(id = 2, name = "Campaign 2"),
            Campaign(id = 3, name = "Campaign 3")
        )
        return mockCampaigns
    }

    private fun loadCampaignsFromDatabase() {
        Log.d("PopupCampaign", "Entró a loadCampaignsFromDatabase")
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val campaignsFromDb = repository.getAllCampaigns()

                withContext(Dispatchers.Main) {
                    campaigns.clear()
                    campaigns.addAll(campaignsFromDb)
                    campaigns.addAll(mockCampaigns())

                    Log.d("POPUP", "Cantidad = ${campaigns.size}")

                    adapter.submitList(campaigns.toList())
                    Log.d("PopupCampaign", "Cantidad de campañas: ${campaigns.size}")
                    textEmpty.visibility =
                        if (campaigns.isEmpty()) View.VISIBLE else View.GONE
                }

            } catch (e: Exception) {
                Log.e("POPUP", "Error", e)
            }
        }
    }

    private fun showDeleteConfirmationDialog(selectedCampaigns: List<Campaign>) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(mainActivity)
        builder.setTitle(R.string.accept_buttons)
        builder.setMessage("¿Está seguro que desea eliminar la/s campaña/s seleccionada/s?")
        builder.setPositiveButton("Eliminar") { _, _ ->
            // Llamar al método para eliminar campañas
            deleteSelectedCampaigns(selectedCampaigns)
        }
        builder.setNegativeButton(R.string.cancel_buttons) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

}