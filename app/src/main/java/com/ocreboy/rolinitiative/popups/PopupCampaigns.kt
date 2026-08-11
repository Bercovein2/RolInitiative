package com.ocreboy.rolinitiative.popups

import com.ocreboy.rolinitiative.R
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ocreboy.rolinitiative.Character
import com.ocreboy.rolinitiative.MainActivity
import com.ocreboy.rolinitiative.adapter.CampaignAdapter
import com.ocreboy.rolinitiative.database.CampaignDatabase
import com.ocreboy.rolinitiative.database.PlayingCharacterDatabase
import com.ocreboy.rolinitiative.model.Campaign
import com.ocreboy.rolinitiative.model.PlayingCharacter
import com.ocreboy.rolinitiative.repository.CampaignRepository
import com.ocreboy.rolinitiative.repository.PlayingCharacterRepository
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
    private val campaignRepository: CampaignRepository
    private val playersRepository: PlayingCharacterRepository
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
        val dbPlayingCharacter = PlayingCharacterDatabase.invoke(mainActivity)
        campaignRepository = CampaignRepository(dbCampaign)
        playersRepository = PlayingCharacterRepository(dbPlayingCharacter)
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
                onUseCurrent = { saveCurrentCampaign()}
            ).show()
        }

        buttonDelete.setOnClickListener {
            val selectedCampaigns = adapter.getSelectedCampaigns()
            if (selectedCampaigns.isNotEmpty()) {
                showDeleteConfirmationDialog(selectedCampaigns)
            }
        }

        buttonOpen.setOnClickListener {
            adapter.getSingleSelected()?.let { selectedCampaign ->
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        // Obtener la campaña seleccionada (opcional si necesitas más datos)
                        val campaign = campaignRepository.getCampaignById(selectedCampaign.id)

                        // Obtener los personajes asociados a la campaña
                        val playingCharacters = playersRepository.getPlayersByCampaignId(selectedCampaign.id)

                        withContext(Dispatchers.Main) {
                            // Limpiar la lista actual de personajes
                            mainActivity.characterList.clear()

                            // Convertir los PlayingCharacters a Character y agregarlos a la lista principal
                            val characters = playingCharacters.map { playingCharacter ->
                                Character(
                                    name = playingCharacter.name,
                                    initiative = playingCharacter.initiative,
                                    armorClass = playingCharacter.armorClass,
                                    armorTouch = playingCharacter.armorTouch,
                                    armorFlatFooted = playingCharacter.armorFlatFooted,
                                    isSelected = playingCharacter.isSelected,
                                    life = playingCharacter.life,
                                    isDead = playingCharacter.isDead,
                                    hasActiveTimer = playingCharacter.hasActiveTimer,
                                    isPaused = playingCharacter.isPaused,
                                    timeLeftInSeconds = playingCharacter.timeLeftInSeconds,
                                    originalTimer = playingCharacter.originalTimer,
                                    isTimerRunning = playingCharacter.isTimerRunning,
                                    timerSoundName = playingCharacter.timerSoundName,
                                    imageUri = playingCharacter.imageUri
                                )
                            }
                            mainActivity.characterList.addAll(characters)

                            // Notificar al adaptador para actualizar la vista
                            mainActivity.characterAdapter.notifyDataSetChanged()

                            // Actualizar el nombre de la campaña en la pantalla principal
                            mainActivity.editGameName.setText(campaign.name)

                            // Cerrar el popup
                            popupWindow.dismiss()
                        }
                    } catch (e: Exception) {
                        Log.e("PopupCampaigns", "Error al cargar la campaña", e)
                    }
                }
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
                    playersRepository.deletePlayersByCampaignId(selectedCampaigns.map { it.id })

                    campaignRepository.deleteCampaignsByIds(selectedCampaigns.map { it.id })

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
            Campaign(id = 1, name = "Campaign 1", null),
            Campaign(id = 2, name = "Campaign 2", null),
            Campaign(id = 1, name = "Campaign 1", null),
            Campaign(id = 2, name = "Campaign 2", null),
            Campaign(id = 1, name = "Campaign 1", null),
            Campaign(id = 2, name = "Campaign 2", null),
            Campaign(id = 1, name = "Campaign 1", null),
            Campaign(id = 2, name = "Campaign 2", null),
            Campaign(id = 1, name = "Campaign 1", null),
            Campaign(id = 2, name = "Campaign 2", null),
            Campaign(id = 1, name = "Campaign 1", null),
            Campaign(id = 2, name = "Campaign 2", null),
            Campaign(id = 1, name = "Campaign 1", null),
            Campaign(id = 2, name = "Campaign 2", null),
            Campaign(id = 1, name = "Campaign 1", null),
            Campaign(id = 2, name = "Campaign 2", null),
            Campaign(id = 3, name = "Campaign 3", null)
        )
        return mockCampaigns
    }

    private fun loadCampaignsFromDatabase() {
        Log.d("PopupCampaign", "Entró a loadCampaignsFromDatabase")
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val campaignsFromDb = campaignRepository.getAllCampaigns()

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
        builder.setMessage(mainActivity.getString(R.string.campaign_delete_question))
        builder.setPositiveButton(mainActivity.getString(R.string.delete)) { _, _ ->
            // Llamar al método para eliminar campañas
            deleteSelectedCampaigns(selectedCampaigns)
        }
        builder.setNegativeButton(R.string.cancel_buttons) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun saveCurrentCampaign() {
        var campaignName = mainActivity.editGameName.text.toString().trim()
        var actualCampaignTimer = mainActivity.timerTextView.text.toString().trim()

        if (campaignName.isEmpty()) {
            showNameRequiredDialog()
            return
        }

        val characters = mainActivity.characterList

        coroutineScope.launch(Dispatchers.IO) {
            try {
                // Crear una nueva campaña
                val newCampaign = Campaign(0,name = campaignName, actualCampaignTimer)

                // Guardar la campaña en la base de datos
                val campaignId = campaignRepository.insert(newCampaign)

                // Convertir los personajes a PlayingCharacter y asignarles el campaignId
                val playingCharacters = convertCharactersToPlayingCharacters(characters, campaignId)

                playingCharacters.forEach {
                    playersRepository.insert(it)
                }

                withContext(Dispatchers.Main) {
                    Log.d("PopupCampaigns", mainActivity.getString(R.string.campaign_saved_success))
                    popupWindow.dismiss()
                }
            } catch (e: Exception) {
                Log.e("PopupCampaigns", mainActivity.getString(R.string.campaign_saved_error), e)
            }
        }
    }

    private fun convertCharactersToPlayingCharacters(characters: List<Character>, campaignId: Long): List<PlayingCharacter> {
        return characters.mapIndexed { index, character ->
            PlayingCharacter(
                id = 0,
                name = character.name,
                initiative = character.initiative,
                armorClass = character.armorClass,
                armorTouch = character.armorTouch,
                armorFlatFooted = character.armorFlatFooted,
                isSelected = character.isSelected,
                life = character.life,
                isDead = character.isDead,
                hasActiveTimer = character.hasActiveTimer,
                isPaused = character.isPaused,
                timeLeftInSeconds = character.timeLeftInSeconds,
                originalTimer = character.originalTimer,
                isTimerRunning = character.isTimerRunning,
                timerSoundName = character.timerSoundName,
                imageUri = character.imageUri,
                order = index + 1, // Asignar el orden ascendente
                campaignId = campaignId
            )
        }
    }

    private fun showNameRequiredDialog() {
        val dialogView = LayoutInflater.from(mainActivity).inflate(R.layout.dialog_input_name, null)
        val editTextCampaignName = dialogView.findViewById<EditText>(R.id.editTextCampaignName)

        val builder = androidx.appcompat.app.AlertDialog.Builder(mainActivity)
        builder.setTitle(R.string.campaign_name_required_title)
        builder.setView(dialogView)
        builder.setPositiveButton(R.string.accept_buttons) { dialog, _ ->
            val inputName = editTextCampaignName.text.toString().trim()
            if (inputName.isNotEmpty()) {
                mainActivity.editGameName.setText(inputName)
                saveCurrentCampaign()
            } else {
                showNameRequiredDialog() // Reopen if input is empty
            }
            mainActivity.hideKeyboardIfOpen()
            dialog.dismiss()


        }
        builder.setNegativeButton(R.string.cancel_buttons) { dialog, _ ->
            mainActivity.hideKeyboardIfOpen()
            dialog.dismiss()
        }
        builder.create().show()
    }
}