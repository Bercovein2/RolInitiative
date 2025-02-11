package com.ocreboy.rolinitiative.popups

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import com.ocreboy.rolinitiative.Character
import com.ocreboy.rolinitiative.MainActivity
import com.ocreboy.rolinitiative.MyApplication
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.utils.Filters
import com.ocreboy.rolinitiative.utils.PutIntoString

class PopupEditInitiative(private val context: Context) {

    lateinit var textCharacterName : TextView
    lateinit var editTextCharacterInitiative : EditText
    lateinit var buttonIncrementInitiative : Button
    lateinit var buttonReduceInitiative : Button
    lateinit var buttonSave : Button
    lateinit var buttonCancel : Button

    lateinit var buttonClose: ImageButton

    @SuppressLint("InflateParams", "SetTextI18n", "MissingInflatedId")
    fun showPopupWindow(view: View, character: Character, mainActivity: MainActivity, position: Int) {
        // Inflate the popup layout
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_edit_initiative, null)

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
        textCharacterName = popupView.findViewById(R.id.character_name_initiative)
        textCharacterName.text = context.getString(R.string.initiative_of) + " " + character.name

        editTextCharacterInitiative = popupView.findViewById(R.id.edit_initiative)
        editTextCharacterInitiative.setText("${character.initiative}")

        buttonIncrementInitiative = popupView.findViewById(R.id.button_increase_initiative)
        buttonReduceInitiative = popupView.findViewById(R.id.button_decrease_initiative)
        buttonSave = popupView.findViewById(R.id.button_save_initiative)
        buttonCancel = popupView.findViewById(R.id.button_cancel_initiative)

        editTextCharacterInitiative.selectAllOnHold()

        editTextCharacterInitiative.filters = Filters.numberBetweenZeroAndMax()

        // Configure buttons to update character's life
        buttonIncrementInitiative.setOnClickListener {
            var initiative: Int = editTextCharacterInitiative.text.toString().toInt()
            if (initiative < MyApplication.instance.maxNumberInInputNumbers) {
                initiative += 1
                editTextCharacterInitiative.setText("$initiative")
            }
        }

        buttonReduceInitiative.setOnClickListener {
            var initiative: Int = editTextCharacterInitiative.text.toString().toInt()
            if (initiative > MyApplication.instance.minNumberInInputNumbers) {
                initiative -= 1
                editTextCharacterInitiative.setText("$initiative")
            }
        }

        // Save button to update character details and save list
        buttonSave.setOnClickListener {
            if(editTextCharacterInitiative.text.isEmpty()) {
                editTextCharacterInitiative.setText("0")
            }
            character.initiative = editTextCharacterInitiative.text.toString().toInt()
            mainActivity.saveCharacter(character, position)
            popupWindow.dismiss()
            Toast.makeText(context, PutIntoString(mainActivity).put(R.string.initiative_edited, character.name), Toast.LENGTH_SHORT).show()

        }

        // Cancel button to close the popup without saving
        buttonCancel.setOnClickListener {
            popupWindow.dismiss()
        }

        // Show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }


    fun EditText.selectAllOnHold() {
        this.setOnLongClickListener {
            this.selectAll()
            true  // Indica que el evento fue consumido
        }
    }
}