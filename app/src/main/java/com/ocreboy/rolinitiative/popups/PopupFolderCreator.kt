package com.ocreboy.rolinitiative.popups

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.model.Folder
import com.ocreboy.rolinitiative.model.SavedCharacter
import com.ocreboy.rolinitiative.repository.CharacterRepository
import com.ocreboy.rolinitiative.utils.Filters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PopupFolderCreator {

    companion object {

        fun showCreateFolderDialog(
            context: Context,
            coroutineScope: CoroutineScope,
            repository: CharacterRepository,
            foldersWithCharacters: MutableMap<Folder, List<SavedCharacter>>,
            popupView: View,
            onFolderCreated: (String) -> Unit // Callback para devolver el nombre de la carpeta
        ) {
            coroutineScope.launch {
                val dialogView =
                    LayoutInflater.from(context).inflate(R.layout.popup_create_folder, null)
                val editTextInput = dialogView.findViewById<EditText>(R.id.editTextInput)
                val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancelCreateFolder)
                val buttonOk = dialogView.findViewById<Button>(R.id.buttonCreateFolder)

                // Deshabilitar el botón inicialmente
                buttonOk.isEnabled = false

                // Añadir TextWatcher para habilitar el botón solo si hay texto
                editTextInput.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        buttonOk.isEnabled = s?.isNotBlank() == true
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                // Limitar la longitud del input
                editTextInput.filters = Filters.textNotEmptyToMax()

                // Bloquear la tecla "Enter"
                editTextInput.setOnKeyListener { _, keyCode, _ ->
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
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

                                // Actualizar la lista de folders
                                val updatedFoldersWithCharacters = repository.getFoldersWithCharacters()
                                foldersWithCharacters.clear()
                                foldersWithCharacters.putAll(updatedFoldersWithCharacters)

                                withContext(Dispatchers.Main) {
                                    val updatedFolderItems = updatedFoldersWithCharacters.keys.map { it.name }.sorted()

                                    val spinnerFolders: Spinner = popupView.findViewById(R.id.spinnerFolders)
                                    val adapterSpinner = ArrayAdapter(
                                        context,
                                        android.R.layout.simple_spinner_item,
                                        updatedFolderItems
                                    )
                                    adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    spinnerFolders.adapter = adapterSpinner

                                    // Seleccionar la nueva carpeta
                                    val newFolderPosition = updatedFolderItems.indexOf(newFolder.name)
                                    spinnerFolders.setSelection(newFolderPosition)

                                    // Llamar al callback con el nombre de la nueva carpeta
                                    onFolderCreated(newFolder.name)

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

                // Cancelar el diálogo
                buttonCancel.setOnClickListener {
                    alertDialog.dismiss()
                }

                alertDialog.show()
            }
        }
    }
}
