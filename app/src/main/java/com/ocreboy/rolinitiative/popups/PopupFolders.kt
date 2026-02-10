package com.ocreboy.rolinitiative.popups

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.ocreboy.rolinitiative.GlobalVariables
import com.ocreboy.rolinitiative.InputFilterMinMax
import com.ocreboy.rolinitiative.MainActivity
import com.ocreboy.rolinitiative.MyApplication
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.adapter.FoldersCharactersAdapter
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PopupFolders(private val mainActivity: MainActivity) {

    private val repository: CharacterRepository
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    lateinit var expandableListView: ExpandableListView
    lateinit var buttonFolderClose: ImageButton
    lateinit var buttonFolderSendToFight: Button
    lateinit var buttonTrash: ImageButton
    lateinit var frameColor: FrameColor
    lateinit var nameText: EditText
    lateinit var armorText: EditText
    lateinit var lifeText: EditText
    lateinit var buttonFolderCreate: ImageButton
    lateinit var buttonAdd: Button
    lateinit var spinnerFolders: Spinner

    lateinit var foldersWithCharacters : Map<Folder, List<SavedCharacter>>

    init {
        val dbCharacter = SavedCharacterDatabase.invoke(mainActivity)
        val dbFolder = FolderDatabase.invoke(mainActivity)
        repository = CharacterRepository(dbCharacter, dbFolder)
    }

    @SuppressLint("InflateParams", "NotifyDataSetChanged")
    fun showPopupWindow() {
        frameColor = FrameColor(mainActivity)
        val inflater = LayoutInflater.from(mainActivity)
        val popupView = inflater.inflate(R.layout.popup_folders_characters, null)
        val displayMetrics = mainActivity.resources.displayMetrics

        val popupWindow = PopupWindow(
            popupView,
            (displayMetrics.widthPixels * 0.9).toInt(),
            (displayMetrics.heightPixels * 0.9).toInt(),
            true
        )

        popupView.setBackgroundResource(frameColor.getFrameColor())

        // Obtener el ViewTreeObserver para escuchar cambios en el layout
        popupView.viewTreeObserver.addOnGlobalLayoutListener {
            // Obtener la altura de la vista raíz y la altura del contenido visible
            val rect = Rect()
            popupView.getWindowVisibleDisplayFrame(rect)
            val visibleHeight = rect.height()

            // Verificar si el teclado está visible
            val isKeyboardVisible = displayMetrics.heightPixels - visibleHeight > 100

            if (isKeyboardVisible) {
                // Ajustar el tamaño del PopupWindow si el teclado está visible
                popupWindow.update(
                    (displayMetrics.widthPixels * 0.9).toInt(),
                    visibleHeight
                )
            } else {
                // Restaurar el tamaño original del PopupWindow cuando el teclado no esté visible
                popupWindow.update(
                    (displayMetrics.widthPixels * 0.9).toInt(),
                    (displayMetrics.heightPixels * 0.9).toInt()
                )
            }
        }

        expandableListView = popupView.findViewById(R.id.expandableListViewFoldersCharacters)
        buttonFolderClose = popupView.findViewById(R.id.buttonFolderClose)
        buttonFolderSendToFight = popupView.findViewById(R.id.buttonFolderSendToFight)
        buttonTrash = popupView.findViewById(R.id.buttonDeleteFromFolder)
        buttonAdd = popupView.findViewById(R.id.buttonFolderAdd)
        buttonFolderCreate = popupView.findViewById(R.id.buttonFolderCreate)

        frameColor.changeVectorColorDarkLightGray(buttonFolderCreate)

        nameText = popupView.findViewById(R.id.editTextNameFolder)
        armorText = popupView.findViewById(R.id.editTextArmorClassFolder)
        lifeText = popupView.findViewById(R.id.editTextLifeFolder)

        nameText.filters = Filters.textNotEmptyToMax()

        nameText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // No es necesario hacer nada aquí
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No es necesario hacer nada aquí
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Verifica si el texto ha cambiado
                // Puedes agregar lógica adicional aquí si es necesario
            }
        })

        armorText.filters = arrayOf(
            InputFilter.LengthFilter(MyApplication.instance.maxCantDigitsInNumbers),
            InputFilterMinMax(
                MyApplication.instance.minNumberInInputNumbers,
                MyApplication.instance.maxNumberInInputNumbers)
        )
        lifeText.filters = arrayOf(
            InputFilter.LengthFilter(MyApplication.instance.maxCantDigitsInNumbers),
            InputFilterMinMax(
                MyApplication.instance.minNumberInInputNumbers,
                MyApplication.instance.maxNumberInInputNumbers)
        )

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateInputs()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        nameText.addTextChangedListener(textWatcher)
        armorText.addTextChangedListener(textWatcher)

        validateInputs()

        enableAddButton(false) // Deshabilitar por defecto
        enableTrashButton(false) // Deshabilitar por defecto

        coroutineScope.launch {

            addBestiary()

            foldersWithCharacters = withContext(Dispatchers.IO) {
                repository.getFoldersWithCharacters()
            }

            val adapter = FoldersCharactersAdapter(mainActivity, foldersWithCharacters)
            expandableListView.setAdapter(adapter)
            expandableListView.setGroupIndicator(null)

            adapter.onPickImageRequested =
                { imageView, onImagePicked ->
                    mainActivity.pickAndPreviewImage(imageView) { uri ->
                        onImagePicked(uri)
                    }
                }
            // Configura el listener para cambios en la selección
            adapter.onSelectionChangedListener = { isAnySelected ->
                enableAddButton(isAnySelected)
                enableTrashButton(isAnySelected)
            }

            expandableListView.setOnGroupClickListener { parent, view, groupPosition, id ->
                // Verifica si el clic fue en el grupo y no en un elemento secundario
                if (view.findViewById<EditText>(R.id.editTextNameFolder) == null &&
                    view.findViewById<EditText>(R.id.editTextArmorClassFolder) == null &&
                    view.findViewById<EditText>(R.id.editTextLifeFolder) == null &&
                    view.findViewById<EditText>(R.id.spinnerFolders) == null) {
                    if (expandableListView.isGroupExpanded(groupPosition)) {
                        expandableListView.collapseGroup(groupPosition)
                    } else {
                        expandableListView.expandGroup(groupPosition)
                    }
                    true
                } else {
                    false
                }
            }

            // 1. Convertir el mapa de carpetas a una lista de FolderItem
            val folderItems = foldersWithCharacters.keys.map { it.name }.toMutableList()
            folderItems.sort()

            // 2. Crear un ArrayAdapter para el Spinner
            val adapterSpinner = ArrayAdapter(
                mainActivity,
                android.R.layout.simple_spinner_item,
                folderItems
            )
            adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // 3. Asignar el adaptador al Spinner
            spinnerFolders = popupView.findViewById(R.id.spinnerFolders)
            spinnerFolders.adapter = adapterSpinner

            buttonFolderClose.setOnClickListener {
                popupWindow.dismiss()
            }

            buttonFolderSendToFight.setOnClickListener {
                val selectedCharacters = mutableListOf<SavedCharacter>()

                for (itemPair in adapter.selectedItems) {
                    val groupPosition = itemPair.first
                    val childPosition = itemPair.second
                    val character = adapter.getChild(groupPosition, childPosition)

                    if(character != null) {
                        selectedCharacters.add(character)
                    }
                }

                if (selectedCharacters.isNotEmpty()) {

                    val putIntoString = PutIntoString(mainActivity)

                    val builder = AlertDialog.Builder(mainActivity)
                    builder.setTitle(R.string.confirm_action)
                    builder.setMessage(R.string.would_you_like_to_add_characters)
                    putIntoString.put(R.string.would_you_like_to_add_characters, selectedCharacters.size.toString())
                    builder.setMessage(putIntoString.put(R.string.send_to_fight_message, selectedCharacters.size.toString()))

                    // Botón Accept
                    builder.setPositiveButton(R.string.accept_buttons) { dialog, _ ->
                        // Añadir los personajes a la lista actual
                        selectedCharacters.forEach { character ->
                            mainActivity.addCharacterToActualList(
                                character.name, character.initiative,
                                character.armorClass, character.armorTouch, character.armorFlatFooted,
                                false, character.life, character.imageUri
                            )
                        }
                        mainActivity.saveCharacterList()
                        mainActivity.characterAdapter.notifyDataSetChanged()
                        Toast.makeText(mainActivity, putIntoString.put(R.string.send_to_fight_message, selectedCharacters.size.toString()), Toast.LENGTH_SHORT).show()
                        mainActivity.updateButtonStateByCharacterQuantity(mainActivity.buttonSort, GlobalVariables.minQuantityToSort)
                        dialog.dismiss() // Cerrar el diálogo
                        mainActivity.updateStartNextButton()
                    }

                    // Botón Cancel
                    builder.setNegativeButton(R.string.cancel_buttons) { dialog, _ ->
                        dialog.dismiss() // Cerrar el diálogo sin hacer nada
                    }

                    // Mostrar el AlertDialog
                    val dialog = builder.create()
                    dialog.show()

                } else {
                    Toast.makeText(mainActivity, R.string.no_character_selected, Toast.LENGTH_SHORT).show()
                }
            }


            buttonTrash.setOnClickListener {
                val inflater = LayoutInflater.from(mainActivity)
                val dialogView = inflater.inflate(R.layout.dialog_with_checkbox, null)
                val checkBoxExtraAction = dialogView.findViewById<CheckBox>(R.id.checkbox_extra_action)
                val isAnyGroupSelected = adapter.groupSelection.values.any { it }
                val isAnyCharacterSelected = adapter.selectedItems.any { it.second >= 0 }

                if (isAnyGroupSelected && isAnyCharacterSelected) {
                    checkBoxExtraAction.visibility = View.VISIBLE
                } else {
                    checkBoxExtraAction.visibility = View.GONE
                }

                if(isAnyGroupSelected && !isAnyCharacterSelected) {
                    checkBoxExtraAction.visibility = View.VISIBLE
                    checkBoxExtraAction.isChecked = true
                    checkBoxExtraAction.isEnabled = false
                }

                val builder = AlertDialog.Builder(mainActivity)
                builder.setTitle(R.string.confirm_action)
                builder.setView(dialogView)

                builder.setPositiveButton(R.string.accept_buttons) { dialog, _ ->
                    val selectedCharacters = mutableListOf<SavedCharacter>()
                    val selectedFolders = mutableSetOf<Folder>()

                    for (itemPair in adapter.selectedItems) {
                        val groupPosition = itemPair.first
                        val childPosition = itemPair.second

                        if (childPosition >= 0) {
                            val character = adapter.getChild(groupPosition, childPosition)
                            if (character != null) {
                                selectedCharacters.add(character)
                            }
                        } else if (childPosition == -1) {
                            val folder = adapter.getGroup(groupPosition) as Folder
                            selectedFolders.add(folder)
                        }
                    }

                    coroutineScope.launch(Dispatchers.IO) {
                        if (selectedCharacters.isNotEmpty()) {
                            repository.deleteCharactersByIds(selectedCharacters.map { it.id })
                        }

                        // Eliminar carpetas solo si el CheckBox está marcado y hay alguna carpeta seleccionada
                        if (checkBoxExtraAction.isChecked && isAnyGroupSelected) {
                            for ((groupPosition, isSelected) in adapter.groupSelection) {
                                if (isSelected) {
                                    val folder = adapter.getGroup(groupPosition) as Folder
                                    selectedFolders.add(folder)
                                }
                            }
                            if (selectedFolders.isNotEmpty()) {
                                repository.deleteFoldersByIds(selectedFolders.map { it.id })
                            }
                        }

                        val updatedFoldersWithCharacters = repository.getFoldersWithCharacters()

                        withContext(Dispatchers.Main) {
                            adapter.updateData(updatedFoldersWithCharacters)
                            adapter.notifyDataSetChanged()

                            val spinnerFolders: Spinner = popupView.findViewById(R.id.spinnerFolders)
                            val updatedFolderItems = updatedFoldersWithCharacters.keys.map { it.name }.sorted()
                            val adapterSpinner = ArrayAdapter(
                                mainActivity,
                                android.R.layout.simple_spinner_item,
                                updatedFolderItems
                            )
                            adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spinnerFolders.adapter = adapterSpinner

                            adapter.clearSelections()
                            enableTrashButton(false)
                            enableAddButton(false)

                            val totalDeletedItems = selectedCharacters.size + selectedFolders.size

                            val formattedMessage = PutIntoString(mainActivity).put(R.string.number_deleted_items, totalDeletedItems.toString())

                            Toast.makeText(mainActivity, formattedMessage, Toast.LENGTH_SHORT).show()

                            adapter.clearSelections()
                        }
                    }

                    dialog.dismiss()
                }

                builder.setNegativeButton(R.string.cancel_buttons) { dialog, _ ->
                    dialog.dismiss()
                }
                validateInputs()
                val dialog = builder.create()
                dialog.show()
            }

            buttonAdd.setOnClickListener {
                val name = nameText.text.toString()
                val armorClassText = armorText.text.toString()
                val lifeText = lifeText.text.toString()

                if (name.isNotEmpty()) {
                    val life = if (lifeText.isNotEmpty()) lifeText.toInt() else 0
                    val selectedFolder = spinnerFolders.selectedItem as String

                    val folderChosen: Folder? = foldersWithCharacters.keys.find { it.name == selectedFolder }

                    val armorClass = if (armorClassText.isNotEmpty()) armorClassText.toInt() else 0

                    coroutineScope.launch(Dispatchers.IO) {
                        val newCharacterId: Long = repository.insert(
                            SavedCharacter(
                                id = 0,
                                name = name,
                                armorClass = armorClass,
                                armorTouch = "",  // Reemplazar con campos reales
                                armorFlatFooted = "",  // Reemplazar con campos reales
                                life = life,
                                folderId = folderChosen?.id ?: 0,
                                initiative = 0
                            )
                        )

                        val updatedFoldersWithCharacters = repository.getFoldersWithCharacters()
                        withContext(Dispatchers.Main) {
                            adapter.updateData(updatedFoldersWithCharacters)
                            adapter.notifyDataSetChanged()

                            // Verificar que la carpeta seleccionada exista
                            if (folderChosen == null) {
                                Toast.makeText(mainActivity, R.string.folder_not_found, Toast.LENGTH_SHORT).show()
                                return@withContext
                            }

                            // Buscar la posición del grupo (carpeta) seleccionado
                            val groupPosition = adapter.getGroupPosition(folderChosen)
                            if (groupPosition == -1) {
                                Toast.makeText(mainActivity, R.string.folder_position_not_found, Toast.LENGTH_SHORT).show()
                                return@withContext
                            }

                            // Expandir la carpeta si no lo está
                            if (!expandableListView.isGroupExpanded(groupPosition)) {
                                expandableListView.expandGroup(groupPosition)
                            }

                            // Verificar que el nuevo personaje se haya agregado correctamente
                            val childPosition = adapter.getChildPosition(groupPosition, newCharacterId.toInt())
                            if (childPosition == -1) {
                                Toast.makeText(mainActivity, R.string.character_not_found_in_folder, Toast.LENGTH_SHORT).show()
                            } else {
                                // Desplazarse automáticamente a la ubicación del nuevo personaje
                                expandableListView.setSelectedChild(groupPosition, childPosition, true)
                            }

                            // Limpiar los campos de entrada
                            nameText.text.clear()
                            armorText.text.clear()
                            nameText.requestFocus()
                        }
                    }
                } else {
                    Toast.makeText(mainActivity, R.string.enter_name_armor_and_life_optional, Toast.LENGTH_SHORT).show()
                }
            }

            buttonFolderCreate.setOnClickListener {
                val dialogView = LayoutInflater.from(mainActivity).inflate(R.layout.popup_create_folder, null)
                val editTextInput = dialogView.findViewById<EditText>(R.id.editTextInput)
                val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancelCreateFolder)
                val buttonOk = dialogView.findViewById<Button>(R.id.buttonCreateFolder)

                // Inicialmente deshabilitar el botón
                buttonOk.isEnabled = false

                // Añadir un TextWatcher al EditText
                editTextInput.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
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

                val alertDialog = AlertDialog.Builder(mainActivity)
                    .setTitle(R.string.create_new_folder)
                    .setView(dialogView)
                    .create()

                buttonOk.setOnClickListener {
                    val inputText = editTextInput.text.toString()
                    if (inputText.isNotBlank()) {
                        coroutineScope.launch(Dispatchers.IO) {

                            val folderExists = repository.isFolderNameExists(inputText)

                            if (!folderExists) {
                                repository.insertFolder(Folder(0, inputText, 0))

                                // Actualizar foldersWithCharacters globalmente
                                foldersWithCharacters = repository.getFoldersWithCharacters()

                                withContext(Dispatchers.Main) {
                                    // Actualizar los datos del adaptador ExpandableListView
                                    adapter.updateData(foldersWithCharacters)
                                    adapter.notifyDataSetChanged()

                                    // Actualizar el adaptador del Spinner con las carpetas actualizadas
                                    val spinnerFolders: Spinner = popupView.findViewById(R.id.spinnerFolders)
                                    val updatedFolderItems = foldersWithCharacters.keys.map { it.name }.sorted()
                                    val adapterSpinner = ArrayAdapter(
                                        mainActivity,
                                        android.R.layout.simple_spinner_item,
                                        updatedFolderItems
                                    )
                                    adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    spinnerFolders.adapter = adapterSpinner

                                    // Mostrar mensaje de éxito
                                    Handler(Looper.getMainLooper()).post {
                                        Toast.makeText(mainActivity, R.string.new_folder_created, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(mainActivity, R.string.folder_name_in_use, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    alertDialog.dismiss()
                }


                buttonCancel.setOnClickListener {
                    alertDialog.dismiss()
                }

                alertDialog.show()
            }


        }


        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
    }

    fun validateInputs() {
        val isNameNotEmpty = nameText.text.toString().trim().isNotEmpty()
        buttonAdd.isEnabled = isNameNotEmpty && spinnerFolders.selectedItem != null
    }

    private fun enableTrashButton(isAnySelected: Boolean) {
        buttonTrash.isEnabled = isAnySelected
        val colorBackground: Int
        if (isAnySelected) {
            colorBackground = ContextCompat.getColor(mainActivity, R.color.darkRed)
            buttonTrash.setColorFilter(ContextCompat.getColor(mainActivity, R.color.white))
        } else {
            colorBackground = frameColor.disabledColor()
            frameColor.changeDisabledVectorColor(buttonTrash)
        }
        buttonTrash.backgroundTintList = ColorStateList.valueOf(colorBackground)
    }

    private fun enableAddButton (isAnySelected: Boolean){
        buttonFolderSendToFight.isEnabled = isAnySelected
    }

    // Cancelar las coroutines cuando la clase ya no es necesaria
    fun onDestroy() {
        coroutineScope.cancel()
    }

    suspend fun addBestiary(){

        if (!GlobalVariables.sharedPreferences.getBoolean("has_initialized_data", false)) {
            // Inserta la carpeta y el personaje
            repository.insertFolder(Folder(1, "Bestiary", 0))
            repository.insert(SavedCharacter(1,"Goblin", 16,"13","14",6,1, 0))
            repository.insert(SavedCharacter(2,"Troll", 16,"11","14",63,1, 0))
            repository.insert(SavedCharacter(3,"Hydra", 15,"9","14",47,1, 0))
            repository.insert(SavedCharacter(4,"Gelatinous Cube", 4,"4","4",50,1, 0))
            repository.insert(SavedCharacter(5,"Succubus", 20,"13","17",84,1, 0))
            repository.insert(SavedCharacter(6,"Intellect Devourer", 22,"17","16",84,1, 0))
            repository.insert(SavedCharacter(7,"Doppelganger", 16,"12","14",26,1, 0))
            repository.insert(SavedCharacter(8,"Drow", 15,"12","13",5,1, 0))
            repository.insert(SavedCharacter(9,"Ent", 21,"7","21",114,1, 0))
            repository.insert(SavedCharacter(10,"Skeleton", 16,"12","14",4,1, 0))

            // Actualiza el valor en SharedPreferences para que no se ejecute de nuevo
            with(GlobalVariables.sharedPreferences.edit()) {
                putBoolean("has_initialized_data", true)
                apply()
            }
        }
    }
}