package com.ocreboy.rolinitiative.popups

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import com.ocreboy.rolinitiative.Character
import com.ocreboy.rolinitiative.MainActivity
import com.ocreboy.rolinitiative.MyApplication
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.utils.Filters
import com.ocreboy.rolinitiative.utils.PutIntoString
import coil.load
import com.ocreboy.rolinitiative.utils.isUriValid
import com.ocreboy.rolinitiative.utils.showImageFullScreen

class PopupEdit(private val context: Context) {

    lateinit var editTextCharacterName: EditText
    lateinit var editTextCharacterInitiative: EditText
    lateinit var editTextCharacterArmorClass: EditText
    lateinit var editTextCharacterArmorTouch: EditText
    lateinit var editTextCharacterArmorFlatFooted: EditText
    lateinit var textViewCharacterLife: TextView
    lateinit var buttonIncrementLife: Button
    lateinit var buttonReduceLife: Button
    lateinit var buttonSave: Button
    lateinit var buttonCancel : Button
    lateinit var buttonClose: ImageButton

    lateinit var lifeImage: ImageView
    lateinit var armorImage: ImageView
    lateinit var initImage: ImageView

    lateinit var imageCharacterPreview: ImageView
    lateinit var buttonAddImage: ImageButton
    @SuppressLint("InflateParams")
    fun showPopupWindow(view: View, character: Character, mainActivity: MainActivity, position: Int) {
        // Inflate the popup layout
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_edit_character, null)

        popupView.setBackgroundResource(mainActivity.frameColor.getFrameColor())
        // Create the PopupWindow
        val popupWindow = PopupWindow(popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true)
        buttonClose = popupView.findViewById(R.id.buttonDiceClose)

        buttonClose.setOnClickListener {
            popupWindow.dismiss()
        }
        // Find and configure the views inside the popup
        editTextCharacterName = popupView.findViewById(R.id.editCharacterName)
        editTextCharacterInitiative = popupView.findViewById(R.id.editCharacterInitiative)
        editTextCharacterArmorClass = popupView.findViewById(R.id.editCharacterArmorClass)
        editTextCharacterArmorTouch = popupView.findViewById(R.id.editCharacterArmorTouch)
        editTextCharacterArmorFlatFooted = popupView.findViewById(R.id.editCharacterArmorFlatFooted)
        imageCharacterPreview = popupView.findViewById(R.id.imageCharacterPreview)

        buttonAddImage = popupView.findViewById(R.id.buttonAddImage)
        mainActivity.frameColor.changeVectorColorBlackWhite(buttonAddImage)

        textViewCharacterLife = popupView.findViewById(R.id.editCharacterLife)
        buttonIncrementLife = popupView.findViewById(R.id.buttonEditIncrementLife)
        buttonReduceLife = popupView.findViewById(R.id.buttonEditReduceLife)
        buttonSave = popupView.findViewById(R.id.buttonEditSave)
        buttonCancel = popupView.findViewById(R.id.buttonEditCancel)

        lifeImage = popupView.findViewById(R.id.charLifeEdit)
        mainActivity.frameColor.changeVectorColorBlackWhite(lifeImage)
        armorImage = popupView.findViewById(R.id.charArmorEdit)
        mainActivity.frameColor.changeVectorColorBlackWhite(armorImage)
        initImage = popupView.findViewById(R.id.charInitiativeEdit)
        mainActivity.frameColor.changeVectorColorBlackWhite(initImage)

        editTextCharacterName.setText(character.name)
        editTextCharacterInitiative.setText(character.initiative.toString())
        editTextCharacterArmorClass.setText(character.armorClass.toString())
        editTextCharacterArmorTouch.setText(character.armorTouch)
        editTextCharacterArmorFlatFooted.setText(character.armorFlatFooted)
        textViewCharacterLife.text = "${character.life}"

        if (!character.imageUri.isNullOrEmpty()) {
            if (context.isUriValid(character.imageUri)) {
                imageCharacterPreview.visibility = View.VISIBLE
                imageCharacterPreview.load(character.imageUri) {
                    crossfade(true)
                }
            } else {
            // 🔹 Si la URI ya no existe, la limpiamos
            character.imageUri = null
                imageCharacterPreview.visibility = View.GONE
            }
        } else {
            imageCharacterPreview.visibility = View.GONE
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateInputs()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        editTextCharacterName.addTextChangedListener(textWatcher)
        editTextCharacterInitiative.addTextChangedListener(textWatcher)
        editTextCharacterArmorClass.addTextChangedListener(textWatcher)
        editTextCharacterArmorTouch.addTextChangedListener(textWatcher)
        editTextCharacterArmorFlatFooted.addTextChangedListener(textWatcher)

        editTextCharacterName.selectAllOnHold()
        editTextCharacterInitiative.selectAllOnHold()
        editTextCharacterArmorClass.selectAllOnHold()
        editTextCharacterArmorTouch.selectAllOnHold()
        editTextCharacterArmorFlatFooted.selectAllOnHold()

        validateInputs()

        editTextCharacterName.filters = Filters.textNotEmptyToMax()
        editTextCharacterInitiative.filters = Filters.numberBetweenZeroAndMax()
        editTextCharacterArmorClass.filters = Filters.numberBetweenZeroAndMax()
        editTextCharacterArmorTouch.filters = Filters.numberBetweenZeroAndMax()
        editTextCharacterArmorFlatFooted.filters = Filters.numberBetweenZeroAndMax()

        // Configure buttons to update character's life
        buttonIncrementLife.setOnClickListener {
            var life: Int = textViewCharacterLife.text.toString().toInt()
            if (life < MyApplication.instance.maxNumberInInputNumbers) {
                life += 1
                character.life = life
                textViewCharacterLife.text = "${character.life}"
            }
        }

        buttonReduceLife.setOnClickListener {
            var life: Int = textViewCharacterLife.text.toString().toInt()
            if (life > MyApplication.instance.minNumberInInputNumbers) {
                life -= 1
                character.life = life
                textViewCharacterLife.text = "${character.life}"
            }
        }

        // Save button to update character details and save list
        buttonSave.setOnClickListener {
            character.name = editTextCharacterName.text.toString()
            character.initiative = editTextCharacterInitiative.text.toString().toInt()
            character.armorClass = editTextCharacterArmorClass.text.toString().toInt()
            character.armorTouch = editTextCharacterArmorTouch.text.toString()
            character.armorFlatFooted = editTextCharacterArmorFlatFooted.text.toString()
            mainActivity.saveCharacter(character, position)
            popupWindow.dismiss()
            Toast.makeText(context, PutIntoString(mainActivity).put(R.string.character_edited, character.name), Toast.LENGTH_SHORT).show()

        }

        // Cancel button to close the popup without saving
        buttonCancel.setOnClickListener {
            popupWindow.dismiss()
        }

        //AGRANDA LA IMAGEN
        imageCharacterPreview.setOnClickListener {
            character.imageUri?.let {
                context.showImageFullScreen(it)
            }
        }

        buttonAddImage.setOnClickListener {
            mainActivity.openImagePicker { uri ->
                if (uri != null) {
                    character.imageUri = uri.toString()

                    imageCharacterPreview.visibility = View.VISIBLE
                    imageCharacterPreview.load(uri) {
                        crossfade(true)
                    }

                    mainActivity.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }
        }

        // Show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    fun validateInputs() {
        val isNameNotEmpty = editTextCharacterName.text.toString().trim().isNotEmpty()
        val isNumberNotEmpty = editTextCharacterInitiative.text.toString().trim().isNotEmpty()
        buttonSave.isEnabled = isNameNotEmpty && isNumberNotEmpty
    }


    fun EditText.selectAllOnHold() {
        this.setOnLongClickListener {
            this.selectAll()
            true  // Indica que el evento fue consumido
        }
    }

}