package com.ocreboy.rolinitiative.popups

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.ocreboy.rolinitiative.Character
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.database.FolderDatabase
import com.ocreboy.rolinitiative.database.SavedCharacterDatabase
import com.ocreboy.rolinitiative.model.Folder
import com.ocreboy.rolinitiative.model.SavedCharacter
import com.ocreboy.rolinitiative.repository.CharacterRepository
import com.ocreboy.rolinitiative.utils.Filters
import com.ocreboy.rolinitiative.utils.FrameColor
import com.ocreboy.rolinitiative.utils.PutIntoString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PopupSaveCharacter(private val context: Context) {

    private val repository: CharacterRepository
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    lateinit var buttonClose: ImageButton
    lateinit var buttonFolderCreate: ImageButton
    lateinit var foldersWithCharacters: Map<Folder, List<SavedCharacter>>

    lateinit var frameColor: FrameColor

    init {
        val dbCharacter = SavedCharacterDatabase.invoke(context)
        val dbFolder = FolderDatabase.invoke(context)
        repository = CharacterRepository(dbCharacter, dbFolder)
    }

    @SuppressLint("InflateParams")
    fun showPopupWindow(character: Character) {
        // Infla el layout del popup
        frameColor = FrameColor(context)
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.popup_save_character_to_folder, null)
        buttonClose = popupView.findViewById(R.id.buttonAddToFolderClose)
        buttonFolderCreate = popupView.findViewById(R.id.buttonAddToFolderCreate)

        frameColor.changeVectorColorDarkLightGray(buttonFolderCreate)

        // Crea el popup
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupView.setBackgroundResource(frameColor.getFrameColor())

        buttonClose.setOnClickListener {
            popupWindow.dismiss()
        }

        coroutineScope.launch {
            foldersWithCharacters = withContext(Dispatchers.IO) {
                repository.getFoldersWithCharacters()
            }

            // Configurar el Spinner con las carpetas
            val spinnerFolders: Spinner = popupView.findViewById(R.id.spinnerFolders)
            val folderItems = foldersWithCharacters.keys.map { it.name }.toMutableList()
            folderItems.sort()

            val adapterSpinner = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                folderItems
            )
            adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFolders.adapter = adapterSpinner

            // Configurar el botón Cancelar
            val buttonCancel: Button = popupView.findViewById(R.id.buttonCancel)
            buttonCancel.setOnClickListener {
                popupWindow.dismiss()
            }

            if(!folderItems.isEmpty()) {
                // Configurar el botón Guardar
                val buttonSave: Button = popupView.findViewById(R.id.buttonSave)
                buttonSave.setOnClickListener {
                    val selectedFolder = spinnerFolders.selectedItem as String
                    val folderChosen: Folder? = foldersWithCharacters.keys.find { it.name == selectedFolder }

                    val savedCharacter = folderChosen?.let {
                        character.convertToSavedCharacter(it.id)
                    }

                    if (savedCharacter != null) {
                        // Lanzar una corrutina para insertar el personaje en la base de datos
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                repository.insert(savedCharacter)
                            }

                            // Actualizar la UI en el hilo principal
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, R.string.character_saved_in_folder, Toast.LENGTH_SHORT).show()
                                popupWindow.dismiss()
                            }
                        }
                    } else {
                        // Cierra el popup si no se selecciona ninguna carpeta
                        Toast.makeText(context, R.string.character_cant_save_in_folder, Toast.LENGTH_SHORT).show()
                        popupWindow.dismiss()
                    }
                }
            } else {
                // Cierra el popup si no se selecciona ninguna carpeta
                Toast.makeText(context, R.string.create_folder_first, Toast.LENGTH_SHORT).show()
                popupWindow.dismiss()
            }

        }

        coroutineScope.launch {
            buttonFolderCreate.setOnClickListener {
                val dialogView =
                    LayoutInflater.from(context).inflate(R.layout.popup_create_folder, null)
                val editTextInput = dialogView.findViewById<EditText>(R.id.editTextInput)
                val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancelCreateFolder)
                val buttonOk = dialogView.findViewById<Button>(R.id.buttonCreateFolder)

                // Cancel button to close the popup without saving
                buttonCancel.setOnClickListener {
                    popupWindow.dismiss()
                }
                // Inicialmente deshabilitar el botón
                buttonOk.isEnabled = false

                // Añadir un TextWatcher al EditText
                editTextInput.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        // Habilitar el botón si hay texto, deshabilitarlo si no lo hay
                        buttonOk.isEnabled = s?.isNotBlank() == true
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                editTextInput.filters = Filters.textNotEmptyToMax()

                editTextInput.setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        // Bloquear el evento de tecla si es un salto de línea
                        return@setOnKeyListener true
                    }
                    false
                }

                val alertDialog = AlertDialog.Builder(context)
                    .setTitle(R.string.create_new_folder)
                    .setView(dialogView)
                    .create()

                buttonOk.setOnClickListener {
                    val inputText = editTextInput.text.toString()
                    if (inputText.isNotBlank()) {
                        coroutineScope.launch(Dispatchers.IO) {

                            val folderExists = repository.isFolderNameExists(inputText)

                            if (!folderExists) {
                                val newFolder = Folder(0, inputText, 0)
                                repository.insertFolder(newFolder)

                                // Aquí volvemos a obtener los folders actualizados de la base de datos
                                val updatedFoldersWithCharacters = repository.getFoldersWithCharacters()

                                // Actualizar la variable global foldersWithCharacters con los datos nuevos
                                foldersWithCharacters = updatedFoldersWithCharacters

                                withContext(Dispatchers.Main) {
                                    val updatedFolderItems = updatedFoldersWithCharacters.keys.map { it.name }.sorted()

                                    // Actualizar el adaptador del Spinner
                                    val spinnerFolders: Spinner = popupView.findViewById(R.id.spinnerFolders)
                                    val adapterSpinner = ArrayAdapter(
                                        context,
                                        android.R.layout.simple_spinner_item,
                                        updatedFolderItems
                                    )
                                    adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    spinnerFolders.adapter = adapterSpinner

                                    // Seleccionar la nueva carpeta en el Spinner
                                    val newFolderPosition = updatedFolderItems.indexOf(newFolder.name)
                                    spinnerFolders.setSelection(newFolderPosition)

                                    Toast.makeText(context, R.string.new_folder_created, Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, R.string.folder_name_in_use, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    alertDialog.dismiss()
                }

                alertDialog.show()
            }
        }

        // Mostrar el popup en el centro de la vista
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
    }

    @SuppressLint("InflateParams")
    fun showPopupWindowToAddAll(characters: List<Character>) {
        // Infla el layout del popup
        frameColor = FrameColor(context)
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.popup_save_character_to_folder, null)

        buttonFolderCreate = popupView.findViewById(R.id.buttonAddToFolderCreate)

        frameColor.changeVectorColorDarkLightGray(buttonFolderCreate)

        if(characters.isEmpty()) {
            Toast.makeText(context, R.string.no_character_to_add, Toast.LENGTH_SHORT).show()
            return
        }

        // Crea el popup
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupView.setBackgroundResource(frameColor.getFrameColor())

        coroutineScope.launch {
            foldersWithCharacters = withContext(Dispatchers.IO) {
                repository.getFoldersWithCharacters()
            }

            // Configurar el Spinner con las carpetas
            val spinnerFolders: Spinner = popupView.findViewById(R.id.spinnerFolders)
            val folderItems = foldersWithCharacters.keys.map { it.name }.toMutableList()
            folderItems.sort()

            if(folderItems.isEmpty()) {
                Toast.makeText(context, R.string.create_folder_first, Toast.LENGTH_SHORT).show()
                return@launch
            }

            val adapterSpinner = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                folderItems
            )
            adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFolders.adapter = adapterSpinner

            // Configurar el botón Cancelar
            val buttonCancel: Button = popupView.findViewById(R.id.buttonCancel)
            buttonCancel.setOnClickListener {
                popupWindow.dismiss()
            }


            // Configurar el botón Guardar
            val buttonSave: Button = popupView.findViewById(R.id.buttonSave)
            buttonSave.setOnClickListener {
                val selectedFolder = spinnerFolders.selectedItem as String
                val folderChosen: Folder? = foldersWithCharacters.keys.find { it.name == selectedFolder }

                if (folderChosen != null) {
                    // Lanzar una corrutina para insertar todos los personajes en la base de datos
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            // Convertir cada personaje a SavedCharacter y guardarlo en la base de datos
                            characters.forEach { character ->
                                val savedCharacter = character.convertToSavedCharacter(folderChosen.id)
                                repository.insert(savedCharacter)
                            }
                        }

                        // Actualizar la UI en el hilo principal
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, R.string.all_character_saved_in_folder, Toast.LENGTH_SHORT).show()
                            popupWindow.dismiss()
                        }
                    }
                } else {
                    // Cierra el popup si no se selecciona ninguna carpeta
                    Toast.makeText(context, R.string.folder_doesnt_exist, Toast.LENGTH_SHORT).show()
                    popupWindow.dismiss()
                }
            }
        }

        coroutineScope.launch {
            buttonFolderCreate.setOnClickListener {
                val dialogView =
                    LayoutInflater.from(context).inflate(R.layout.popup_create_folder, null)
                val editTextInput = dialogView.findViewById<EditText>(R.id.editTextInput)
                val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancelCreateFolder)
                val buttonOk = dialogView.findViewById<Button>(R.id.buttonCreateFolder)

                // Cancel button to close the popup without saving
                buttonCancel.setOnClickListener {
                    popupWindow.dismiss()
                }
                // Inicialmente deshabilitar el botón
                buttonOk.isEnabled = false

                // Añadir un TextWatcher al EditText
                editTextInput.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        // Habilitar el botón si hay texto, deshabilitarlo si no lo hay
                        buttonOk.isEnabled = s?.isNotBlank() == true
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                editTextInput.filters = Filters.textNotEmptyToMax()

                editTextInput.setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        // Bloquear el evento de tecla si es un salto de línea
                        return@setOnKeyListener true
                    }
                    false
                }

                val alertDialog = AlertDialog.Builder(context)
                    .setTitle(R.string.create_new_folder)
                    .setView(dialogView)
                    .create()

                buttonOk.setOnClickListener {
                    val inputText = editTextInput.text.toString()
                    if (inputText.isNotBlank()) {
                        coroutineScope.launch(Dispatchers.IO) {

                            val folderExists = repository.isFolderNameExists(inputText)

                            if (!folderExists) {
                                val newFolder = Folder(0, inputText, 0)
                                repository.insertFolder(newFolder)

                                // Aquí volvemos a obtener los folders actualizados de la base de datos
                                val updatedFoldersWithCharacters = repository.getFoldersWithCharacters()

                                // Actualizar la variable global foldersWithCharacters con los datos nuevos
                                foldersWithCharacters = updatedFoldersWithCharacters

                                withContext(Dispatchers.Main) {
                                    val updatedFolderItems = updatedFoldersWithCharacters.keys.map { it.name }.sorted()

                                    // Actualizar el adaptador del Spinner
                                    val spinnerFolders: Spinner = popupView.findViewById(R.id.spinnerFolders)
                                    val adapterSpinner = ArrayAdapter(
                                        context,
                                        android.R.layout.simple_spinner_item,
                                        updatedFolderItems
                                    )
                                    adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    spinnerFolders.adapter = adapterSpinner

                                    // Seleccionar la nueva carpeta en el Spinner
                                    val newFolderPosition = updatedFolderItems.indexOf(newFolder.name)
                                    spinnerFolders.setSelection(newFolderPosition)

                                    Toast.makeText(context, R.string.new_folder_created, Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, R.string.folder_name_in_use, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    alertDialog.dismiss()
                }

                alertDialog.show()
            }
        }

        // Mostrar el popup en el centro de la vista
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
    }
}
