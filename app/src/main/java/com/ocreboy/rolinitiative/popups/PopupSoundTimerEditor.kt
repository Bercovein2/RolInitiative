package com.ocreboy.rolinitiative.popups

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.ocreboy.rolinitiative.Character
import com.ocreboy.rolinitiative.GlobalVariables
import com.ocreboy.rolinitiative.MainActivity
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.sounds.SoundAdapter
import com.ocreboy.rolinitiative.sounds.SoundManager
import com.ocreboy.rolinitiative.sounds.SoundPlayer
import com.ocreboy.rolinitiative.utils.PutIntoString

class PopupSoundTimerEditor(private val context: MainActivity) {

    lateinit var buttonAddNewSound: TextView
    val soundManager = SoundManager(context)
    lateinit var popupView: View
    val soundNames: MutableList<String> = mutableListOf()
    val soundUris: MutableList<Uri?> = mutableListOf()
    var selectedPositionUniqueTimer: Int = -1

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("InflateParams")
    fun showSoundSelectorPopup(mainLayout: View, selectedSoundUri: Uri?) {

        val soundPlayer = SoundPlayer(context)

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(R.layout.popup_sound_selector, null)

        // Obtener la lista de sonidos desde la carpeta
        soundUris.clear()
        val sounds = soundManager.getAudioFiles()
        soundNames.clear()
        soundNames.addAll(listOf(GlobalVariables.noSoundName) + sounds.map { soundManager.getFileNameFromUri(it) })
        soundUris.addAll(listOf<Uri?>(null) + sounds)

        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true

        val listView: ListView = popupView.findViewById(R.id.soundListView)

        val selectedName = GlobalVariables.sharedPreferences.getString("TIMER_SOUND_FILENAME", "").orEmpty()
        selectedPositionUniqueTimer = soundNames.indexOf(selectedName) // Solo se obtiene al inicio

        val adapter = SoundAdapter(context, soundUris, soundNames, selectedPositionUniqueTimer,
            onSoundSelected = { selectedUri ->
                // Centralizar la lógica de selección y reproducción aquí
                if (selectedUri == null) {
                    context.selectedSoundResourceUri = null
                    GlobalVariables.sharedPreferences.edit().putString("TIMER_SOUND_FILENAME", "").apply()
                    soundPlayer.release()
                    Toast.makeText(context, R.string.selected_no_sound, Toast.LENGTH_SHORT).show()
                } else {
                    context.selectedSoundResourceUri = selectedUri
                    val soundName = soundManager.getFileNameFromUri(context.selectedSoundResourceUri)
                    GlobalVariables.sharedPreferences.edit().putString("TIMER_SOUND_FILENAME", soundName).apply()
                    soundPlayer.setSound(selectedUri)
                    soundPlayer.playSound()
                    Toast.makeText(context, PutIntoString(context).put(R.string.selected, soundName), Toast.LENGTH_SHORT).show()

                }
            },
            onDeleteSound = { position ->
                val fileName = soundManager.getFileNameFromUri(context.selectedSoundResourceUri)
                val soundName = soundNames[position]

                //cambia el sonido si se intenta borrar el sonido seleccionado
                if (fileName == soundName){
                    GlobalVariables.sharedPreferences.edit().putString("TIMER_SOUND_FILENAME", GlobalVariables.noSoundName).apply()
                    context.selectedSoundResourceUri = null
                    val firstSoundName = soundNames.getOrNull(0) // Evitar IndexOutOfBoundsException
                    firstSoundName?.let { name ->
                        val uri = soundManager.getAudioFiles().firstOrNull { soundManager.getFileNameFromUri(it) == name }
                        uri?.let { context.soundPlayer.setSound(it) }
                        selectedPositionUniqueTimer = soundNames.indexOf(firstSoundName)
                    }
                }

                deleteSong(position, listView)
            }
        )

        listView.adapter = adapter

        val selectedPosition = soundUris.indexOf(selectedSoundUri)
        if (selectedPosition != -1) {
            listView.setSelection(selectedPosition)
            adapter.notifyDataSetChanged()
        }

        buttonAddNewSound = popupView.findViewById(R.id.buttonAddNewSound)

        buttonAddNewSound.setOnClickListener {
            soundManager.openFilePicker { newUri ->
                adapter.addSoundToActualList(newUri)
            }
        }
        Toast.makeText(context, R.string.manage_session_sound_timer, Toast.LENGTH_SHORT).show()

        popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun deleteSong(position: Int, listView: ListView) {
        if (position < soundUris.size) {
            val soundToRemove = soundUris[position]

            if (soundToRemove != null) {
                Log.d("deleteSong", "Preparando para eliminar: $soundToRemove")

                // Verificar si el URI está en la lista antes de intentar eliminarlo
                val adapter = listView.adapter as? SoundAdapter
                if (adapter != null) {
                    if (adapter.soundUris.contains(soundToRemove)) {
                        Log.d("deleteSong", "El URI existe en la lista del Adapter. Procediendo a eliminar.")
                    } else {
                        Log.e("deleteSong", "El URI no se encuentra en la lista del Adapter antes de eliminar.")
                    }

                    adapter.deleteSoundFromActualList(soundToRemove)
                    listView.invalidateViews()
                } else {
                    Log.e("deleteSong", "No se encontró el Adapter correcto.")
                }

                soundManager.deleteSoundFile(context, soundToRemove)
                Toast.makeText(context, R.string.sound_deleted, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun showSoundSelectorCharacterPopup(mainLayout: View, character: Character) {
        val soundManager = SoundManager(context)
        val soundPlayer = SoundPlayer(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(R.layout.popup_sound_selector, null)

        // Obtener la lista de sonidos desde la carpeta
        val sounds = soundManager.getAudioFiles()
        soundUris.clear()
        soundNames.clear()
        soundNames.addAll(listOf(GlobalVariables.noSoundName) + sounds.map { soundManager.getFileNameFromUri(it) })
        soundUris.addAll(listOf<Uri?>(null) + sounds)

        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
        popupWindow.isFocusable = true

        val listView: ListView = popupView.findViewById(R.id.soundListView)

        var selectedPositionCharacterTimer = soundNames.indexOf(character.timerSoundName)

        val adapter = SoundAdapter(context, soundUris, soundNames, selectedPositionCharacterTimer,
            onSoundSelected = { selectedFile ->
                if (selectedFile == null) {
                    character.timerSoundName = GlobalVariables.noSoundName
                    soundPlayer.release()
                    Toast.makeText(context, R.string.selected_no_sound, Toast.LENGTH_SHORT).show()
                } else {
                    character.timerSoundName = soundManager.getFileNameFromUri(selectedFile)
                    soundPlayer.setSound(selectedFile)
                    soundPlayer.playSound()
                    Toast.makeText(context, PutIntoString(context).put(R.string.selected, character.timerSoundName.orEmpty()), Toast.LENGTH_SHORT).show()
                }

                // Actualizar el sonido para el personaje
                context.saveCharacterList()
            },
            onDeleteSound = { position ->

                if (character.timerSoundName == soundNames[position]){
                    val firstSoundName = soundNames.getOrNull(0) // Evitar IndexOutOfBoundsException
                    firstSoundName?.let { name ->
                        character.timerSoundName = firstSoundName
                        context.saveCharacterList()
                    }
                }

                deleteSong(position, listView)
            }
        )

        listView.adapter = adapter


        val selectedPosition = soundNames.indexOf(character.timerSoundName)
        if (selectedPosition != -1) {
            listView.setSelection(selectedPosition)
            adapter.notifyDataSetChanged()
        }

        buttonAddNewSound = popupView.findViewById(R.id.buttonAddNewSound)

        buttonAddNewSound.setOnClickListener {
            soundManager.openFilePicker { newFile ->
                adapter.addSoundToActualList(newFile)
            }
        }
        Toast.makeText(context, PutIntoString(context).put(R.string.manage_sound_timer_for, character.name), Toast.LENGTH_SHORT).show()


        popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0)

    }



    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("InflateParams")
    fun showSoundSelectorAllCharacterPopup(mainLayout: View, selectedSoundFile: Uri?) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(R.layout.popup_sound_selector, null)

        val soundManager = SoundManager(context)
        // Agregar "No sound" a las listas
        val sounds = soundManager.getAudioFiles()
        soundUris.clear()
        soundNames.clear()
        soundNames.addAll(listOf(GlobalVariables.noSoundName) + sounds.map { soundManager.getFileNameFromUri(it) })
        soundUris.addAll(listOf<Uri?>(null) + sounds)


        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true

        val listView: ListView = popupView.findViewById(R.id.soundListView)
        val selectedSoundName = GlobalVariables.sharedPreferences.getString("ALL_TIMERS_SOUND_FILENAME", GlobalVariables.noSoundName).orEmpty()

        soundNames.indexOf(selectedSoundName)

        val adapter = SoundAdapter(context, soundUris, soundNames, soundNames.indexOf(selectedSoundName),
            onSoundSelected = { selectedFile ->
                var soundName: String? = GlobalVariables.noSoundName
                if (selectedFile == null) {
                    context.selectedSoundResourceAllTimersUri = null
                    GlobalVariables.sharedPreferences.edit().putString("ALL_TIMERS_SOUND_FILENAME", "").apply()
                    Toast.makeText(context, R.string.selected_no_sound, Toast.LENGTH_SHORT).show()
                } else {
                    context.selectedSoundResourceAllTimersUri = selectedFile
                    soundName = soundManager.getFileNameFromUri(selectedFile)
                    GlobalVariables.sharedPreferences.edit().putString("ALL_TIMERS_SOUND_FILENAME", soundName).apply()
                    Toast.makeText(context, PutIntoString(context).put(R.string.selected, soundName), Toast.LENGTH_SHORT).show()

                }

                // Realizar la actualización para todos los personajes
                context.characterList.forEach { character ->
                    character.timerSoundName = soundName
                }
                context.saveCharacterList()

                // Reproducir el sonido si no es "No sound"
                if (selectedFile != null && soundName != GlobalVariables.noSoundName) {
                    val soundPlayer = SoundPlayer(context)
                    soundPlayer.setSound(selectedFile)
                    soundPlayer.playSound()
                }
            },
            onDeleteSound = { position ->

                if (soundManager.getFileNameFromUri(context.selectedSoundResourceAllTimersUri) == soundNames[position]){
                    GlobalVariables.sharedPreferences.edit().putString("ALL_TIMERS_SOUND_FILENAME", GlobalVariables.noSoundName).apply()
                    context.selectedSoundResourceAllTimersUri = null
                    val firstSoundName = soundNames.getOrNull(0) // Evitar IndexOutOfBoundsException
                    firstSoundName?.let { name ->
                        context.characterList.forEach { character ->
                            character.timerSoundName = firstSoundName
                        }
                        context.saveCharacterList()
                    }
                }

                deleteSong(position, listView)
            }
        )


        listView.adapter = adapter


        val selectedPosition = soundUris.indexOf(selectedSoundFile)
        if (selectedPosition != -1) {
            listView.setSelection(selectedPosition)
            adapter.notifyDataSetChanged()
        }

        buttonAddNewSound = popupView.findViewById(R.id.buttonAddNewSound)

        buttonAddNewSound.setOnClickListener {
            soundManager.openFilePicker { newFile ->
                adapter.addSoundToActualList(newFile)
            }
        }

        Toast.makeText(context, R.string.manage_sound_for_all_characters, Toast.LENGTH_SHORT).show()

        popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0)
    }

}
