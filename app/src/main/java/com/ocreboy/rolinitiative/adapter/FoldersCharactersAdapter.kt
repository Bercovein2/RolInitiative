package com.ocreboy.rolinitiative.adapter

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.BaseExpandableListAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import coil.load
import com.ocreboy.rolinitiative.MainActivity
import com.ocreboy.rolinitiative.MyApplication
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.database.FolderDatabase
import com.ocreboy.rolinitiative.database.SavedCharacterDatabase
import com.ocreboy.rolinitiative.model.Folder
import com.ocreboy.rolinitiative.model.SavedCharacter
import com.ocreboy.rolinitiative.repository.CharacterRepository
import com.ocreboy.rolinitiative.utils.Filters
import com.ocreboy.rolinitiative.utils.FrameColor
import com.ocreboy.rolinitiative.utils.PutIntoString
import com.ocreboy.rolinitiative.utils.isUriValid
import com.ocreboy.rolinitiative.utils.showImageFullScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FoldersCharactersAdapter(
    private val context: MainActivity,
    private var foldersWithCharacters: Map<Folder, List<SavedCharacter>>

) : BaseExpandableListAdapter() {
    private val repository: CharacterRepository
    val selectedItems = mutableSetOf<Pair<Int, Int>>()
    val groupSelection = mutableMapOf<Int, Boolean>()
    val childSelection = mutableMapOf<Int, MutableList<Boolean>>()
    var onSelectionChangedListener: ((Boolean) -> Unit)? = null
    var onPickImageRequested: ((ImageView, (String?) -> Unit) -> Unit)? = null

    init {
        val dbCharacter = SavedCharacterDatabase.invoke(context)
        val dbFolder = FolderDatabase.invoke(context)
        repository = CharacterRepository(dbCharacter, dbFolder)
        foldersWithCharacters.toSortedMap(compareBy { it.name })
        for (i in foldersWithCharacters.keys.indices) {
            groupSelection[i] = false
            childSelection[i] = MutableList(foldersWithCharacters.values.elementAt(i).size) { false }
        }
    }

    fun updateData(newData: Map<Folder, List<SavedCharacter>>) {
        foldersWithCharacters = newData
        notifyDataSetChanged()
    }

    override fun getGroupCount(): Int {
        return foldersWithCharacters.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return foldersWithCharacters.values.elementAt(groupPosition).size
    }

    override fun getGroup(groupPosition: Int): Any {
        return foldersWithCharacters.keys.elementAt(groupPosition)
    }

    override fun getChild(groupPosition: Int, childPosition: Int): SavedCharacter? {

        val charactersList :List<SavedCharacter>
        if (foldersWithCharacters.values.size > groupPosition) {
            charactersList = foldersWithCharacters.values.elementAt(groupPosition)
        } else{
            charactersList = foldersWithCharacters.values.elementAt(0)
        }

        // Verificar si la lista de personajes no está vacía y si childPosition es válido
        if (charactersList.isNotEmpty() && childPosition >= 0 && childPosition < charactersList.size) {
            val character = charactersList[childPosition]
            return character
        } else {
            // Manejar el caso en que la lista esté vacía o childPosition no sea válido
            return null // O lanza una excepción, muestra un mensaje, etc.
        }

    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val folder = getGroup(groupPosition) as Folder
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.expandable_list_group, parent, false)
        val textView = view.findViewById<TextView>(R.id.groupTitle)
        val indicator = view.findViewById<ImageView>(R.id.indicator)
        val checkBox = view.findViewById<CheckBox>(R.id.checkBoxItemGroup)

        val editButton = view.findViewById<ImageView>(R.id.folderEdit)
        FrameColor(context).changeVectorColorBlackWhite(editButton)

        val characterCount = getChildrenCount(groupPosition)
        "${folder.name} (${characterCount})".also { textView.text = it }
        FrameColor(context).changeTextColor(textView)

        editButton.setOnClickListener {
            showEditFolderPopup(folder)
        }

        checkBox.isChecked = groupSelection[groupPosition] ?: false

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            groupSelection[groupPosition] = isChecked

            // Inicializar la lista de selección de hijos si no está inicializada
            if (childSelection[groupPosition] == null) {
                childSelection[groupPosition] = MutableList(characterCount) { false }
            }

            // Asegurarse de que la lista tiene el tamaño adecuado
            val childSelectionList = childSelection[groupPosition]!!
            if (childSelectionList.size != characterCount) {
                childSelection[groupPosition] = MutableList(characterCount) { isChecked }
            }

            for (childPosition in 0 until characterCount) {
                childSelectionList[childPosition] = isChecked
                val itemPair = Pair(groupPosition, childPosition)

                if (isChecked) {
                    selectedItems.add(itemPair)
                } else {
                    selectedItems.remove(itemPair)
                }
            }

            if (isChecked && characterCount == 0) {
                selectedItems.add(Pair(groupPosition, -1))
            } else if (!isChecked && characterCount == 0) {
                selectedItems.remove(Pair(groupPosition, -1))
            }

            onSelectionChangedListener?.invoke(selectedItems.isNotEmpty())
            notifyDataSetChanged()
        }

        indicator.setImageResource(
            if (isExpanded) R.drawable.ic_arrow_down else R.drawable.ic_arrow_right
        )
        indicator.setColorFilter(FrameColor(context).getIconColor())

        view.setOnClickListener {
            if (view.findViewById<EditText>(R.id.editTextNameFolder) == null &&
                view.findViewById<EditText>(R.id.editTextArmorClassFolder) == null &&
                view.findViewById<EditText>(R.id.editTextLifeFolder) == null &&
                view.findViewById<EditText>(R.id.spinnerFolders) == null &&
                view.findViewById<EditText>(R.id.buttonFolderAdd) == null) {
                val expandableListView = parent as ExpandableListView
                if (expandableListView.isGroupExpanded(groupPosition)) {
                    expandableListView.collapseGroup(groupPosition)
                } else {
                    expandableListView.expandGroup(groupPosition)
                }
            }
        }

        return view
    }


    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val character = getChild(groupPosition, childPosition) as SavedCharacter
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.expandable_list_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.childTitle)
        val armorView = view.findViewById<TextView>(R.id.childArmor)
        val lifeView = view.findViewById<TextView>(R.id.childLife)
        val initiativeView = view.findViewById<TextView>(R.id.childInitiative)
        val checkBox = view.findViewById<CheckBox>(R.id.checkBoxItem)
        val charArmor = view.findViewById<ImageView>(R.id.charArmor)
        val charLife = view.findViewById<ImageView>(R.id.charLife)
        val charInitiative = view.findViewById<ImageView>(R.id.charInitiative)

        val editButton = view.findViewById<ImageView>(R.id.charEdit)

        textView.text = character.name
        armorView.text = "${character.armorClass}"
        lifeView.text = "${character.life}"
        initiativeView.text = "${character.initiative}"

        FrameColor(context).changeTextColor(textView)
        FrameColor(context).changeTextColor(armorView)
        FrameColor(context).changeTextColor(lifeView)
        FrameColor(context).changeTextColor(initiativeView)

        FrameColor(context).changeVectorColorBlackWhite(charArmor)
        FrameColor(context).changeVectorColorBlackWhite(charLife)
        FrameColor(context).changeVectorColorBlackWhite(charInitiative)
        FrameColor(context).changeVectorColorBlackWhite(editButton)


        // Evitar que el listener se dispare mientras se reasigna el estado del checkbox
        checkBox.setOnCheckedChangeListener(null)

        // Restaurar el estado del checkbox desde el mapa childSelection
        val childSelectionList = childSelection[groupPosition]
        checkBox.isChecked = childSelectionList?.getOrNull(childPosition) ?: false

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            // Asegúrate de que childSelectionList tenga el tamaño adecuado
            if (childSelectionList == null || childPosition >= childSelectionList.size) {
                return@setOnCheckedChangeListener
            }
            childSelectionList[childPosition] = isChecked
            val itemPair = Pair(groupPosition, childPosition)

            if (isChecked) {
                selectedItems.add(itemPair)
            } else {
                selectedItems.remove(itemPair)
            }

            onSelectionChangedListener?.invoke(selectedItems.isNotEmpty())
        }

        editButton.setOnClickListener {
            val dialog = Dialog(context)
            val popupView = LayoutInflater.from(context).inflate(R.layout.popup_edit_character_in_folder, null)

            // Obtener los campos del popup
            val editTextName = popupView.findViewById<EditText>(R.id.editTextCharacterName)
            val editTextArmor = popupView.findViewById<EditText>(R.id.editTextCharacterArmor)
            val editTextArmorTouch = popupView.findViewById<EditText>(R.id.editCharacterArmorTouchFolder)
            val editTextArmorFlat = popupView.findViewById<EditText>(R.id.editCharacterArmorFlatFootedFolder)
            val editTextLife = popupView.findViewById<EditText>(R.id.editTextCharacterLife)
            val editTextInitiative = popupView.findViewById<EditText>(R.id.editTextCharacterInitiative)
            val buttonSave = popupView.findViewById<Button>(R.id.buttonSaveEditCharFolder)
            val buttonCancel = popupView.findViewById<Button>(R.id.buttonCancelEditCharFolder)
            val buttonClose = popupView.findViewById<ImageButton>(R.id.buttonCloseEditCharFolder)

            val buttonIncrementLife = popupView.findViewById<Button>(R.id.buttonEditIncrementLifeFolder)
            val buttonReduceLife = popupView.findViewById<Button>(R.id.buttonEditReduceLifeFolder)

            val buttonCamera = popupView.findViewById<ImageButton>(R.id.buttonAddImageFolder)
            FrameColor(context).changeVectorColorBlackWhite(buttonCamera)

            val imagePreview = popupView.findViewById<ImageView>(R.id.imageCharacterFolderPreview)
            // Configure buttons to update character's life
            buttonIncrementLife.setOnClickListener {
                var life: Int = editTextLife.text.toString().toInt()
                if (life < MyApplication.instance.maxNumberInInputNumbers) {
                    life += 1
                    character.life = life
                    editTextLife.setText("${character.life}")
                }
            }
            buttonCamera.setOnClickListener {
                onPickImageRequested?.invoke(imagePreview) { uri ->
                    character.imageUri = uri
                    imagePreview.visibility = View.VISIBLE
                }
            }
            buttonReduceLife.setOnClickListener {
                var life: Int
                life = if(editTextLife.text.isNotEmpty()) {
                    editTextLife.text.toString().toInt()
                } else {
                    0
                }
                if (life > MyApplication.instance.minNumberInInputNumbers) {
                    life -= 1
                    character.life = life
                    editTextLife.setText("${character.life}")
                }
            }

            // Prellenar con los datos del personaje
            editTextName.setText(character.name)
            editTextArmor.setText(character.armorClass.toString())
            editTextArmorTouch.setText(character.armorTouch)
            editTextArmorFlat.setText(character.armorFlatFooted)
            editTextLife.setText(character.life.toString())
            editTextInitiative.setText(character.initiative.toString())

            // limite de caracteres
            editTextName.filters = Filters.textNotEmptyToMax()
            editTextInitiative.filters = Filters.numberBetweenZeroAndMax()
            editTextArmor.filters = Filters.numberBetweenZeroAndMax()
            editTextLife.filters = Filters.numberBetweenZeroAndMax()
            editTextArmorTouch.filters = Filters.numberBetweenZeroAndMax()
            editTextArmorFlat.filters = Filters.numberBetweenZeroAndMax()

            //cargar imagen
            character.imageUri?.let {
                if (context.isUriValid(character.imageUri)) {
                    imagePreview.visibility = View.VISIBLE
                    imagePreview.load(it){
                        crossfade(true)
                    }
                } else {
                    // 🔹 Si la URI ya no existe, la limpiamos
                    character.imageUri = null
                    imagePreview.visibility = View.GONE
                }
            }
            // Acciones del botón cancelar
            buttonCancel.setOnClickListener {
                dialog.dismiss()
            }

            buttonClose.setOnClickListener {
                dialog.dismiss()
            }

            //AGRANDA LA IMAGEN
            imagePreview.setOnClickListener {
                character.imageUri?.let {
                    context.showImageFullScreen(it)
                }
            }


            // Acciones del botón guardar
            buttonSave.setOnClickListener {
                // Actualizar el personaje con los nuevos valores
                character.name = editTextName.text.toString()
                character.armorClass = editTextArmor.text.toString().toIntOrNull() ?: character.armorClass
                character.armorTouch = editTextArmorTouch.text.toString()
                character.armorFlatFooted = editTextArmorFlat.text.toString()
                character.life = editTextLife.text.toString().toIntOrNull() ?: character.life
                character.initiative = editTextInitiative.text.toString().toIntOrNull() ?: character.initiative

                // Actualizar en la base de datos
                // Debes implementar una función para actualizar el personaje en la base de datos
                updateCharacterInDatabase(character)

                // Actualizar la lista expandible
                notifyDataSetChanged()

                // Cerrar el popup
                dialog.dismiss()
                Toast.makeText(context, PutIntoString(context).put(R.string.character_edited, character.name), Toast.LENGTH_SHORT).show()
            }

            dialog.setContentView(popupView)
            dialog.show()

        }

        return view
    }



    fun updateCharacterInDatabase(character: SavedCharacter) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.update(character)
            } catch (e: Exception) {
                Log.e("DB_ERROR", "Error updating character: ${e.message}")
            }
        }
    }


    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    fun getGroupPosition(folder: Folder?): Int {
        return foldersWithCharacters.keys.indexOf(folder)
    }

    fun getChildren(groupPosition: Int): List<SavedCharacter> {
        val folder = getGroup(groupPosition) as Folder
        return foldersWithCharacters[folder] ?: emptyList()
    }

    fun getChildPosition(groupPosition: Int, characterId: Int): Int {
        val characters = getChildren(groupPosition)
        return characters.indexOfFirst { it.id == characterId }
    }

    fun clearSelections() {
        // Limpiar selecciones de carpetas (grupos)
        for (groupPosition in groupSelection.keys) {
            groupSelection[groupPosition] = false
        }

        // Limpiar selecciones de personajes (hijos)
        for (groupPosition in childSelection.keys) {
            val childSelectionList = childSelection[groupPosition]
            childSelectionList?.let {
                for (i in it.indices) {
                    it[i] = false
                }
            }
        }

        // Limpiar el conjunto de elementos seleccionados
        selectedItems.clear()

        // Notificar al adaptador que los datos han cambiado para actualizar la vista
        notifyDataSetChanged()
    }


    private fun showEditFolderPopup(folder: Folder) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.popup_edit_folder, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextNameFolder)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancelEditFolder)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSaveEditFolder)

        // Set current folder name in the EditText
        editTextName.setText(folder.name)

        val alertDialog = AlertDialog.Builder(context)
            .setTitle(R.string.editFolderName)
            .setView(dialogView)
            .create()

        // Deshabilitar el botón inicialmente
        buttonSave.isEnabled = false

        // Añadir TextWatcher para habilitar el botón solo si hay texto
        editTextName.addTextChangedListener(object : TextWatcher {
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
                buttonSave.isEnabled = s?.isNotBlank() == true
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Limitar la longitud del input
        editTextName.filters = Filters.textNotEmptyToMax()

        buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        buttonSave.setOnClickListener {
            val newName = editTextName.text.toString()
            if (newName.isNotBlank()) {
                // Update folder name in the database
                folder.name = newName
                updateFolderNameInDatabase(folder)
                notifyDataSetChanged() // Refresh the list after the change
                alertDialog.dismiss()
            } else {
                Toast.makeText(context, R.string.name_cannot_be_empty, Toast.LENGTH_SHORT).show()
            }
        }

        alertDialog.show()
    }

    private fun updateFolderNameInDatabase(folder: Folder) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateFolder(folder) // Assuming your repository has an updateFolder function
            withContext(Dispatchers.Main) {
                Toast.makeText(context, R.string.folder_updated, Toast.LENGTH_SHORT).show()
            }
        }
    }


}

