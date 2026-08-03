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
import com.ocreboy.rolinitiative.utils.PopupUtils
import com.ocreboy.rolinitiative.utils.PutIntoString

class PopupEditLife(private val context: Context) {

    lateinit var textCharacterName : TextView
    lateinit var editTextCharacterLife : EditText
    lateinit var buttonIncrementLife : Button
    lateinit var buttonReduceLife : Button
    lateinit var buttonSave : Button
    lateinit var buttonCancel : Button

    lateinit var buttonClose: ImageButton

    @SuppressLint("InflateParams", "SetTextI18n")
    fun showPopupWindow(view: View, character: Character, mainActivity: MainActivity, position: Int) {
        // Inflate the popup layout
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_edit_life, null)

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
        textCharacterName = popupView.findViewById(R.id.character_name_life)
        textCharacterName.text = context.getString(R.string.life_of) + " " + character.name

        editTextCharacterLife = popupView.findViewById(R.id.edit_life)
        editTextCharacterLife.setText("${character.life}")

        buttonIncrementLife = popupView.findViewById(R.id.button_increase_life)
        buttonReduceLife = popupView.findViewById(R.id.button_decrease_life)
        buttonSave = popupView.findViewById(R.id.button_save_life)
        buttonCancel = popupView.findViewById(R.id.button_cancel_life)

        editTextCharacterLife.selectAllOnHold()

        editTextCharacterLife.filters = Filters.numberBetweenZeroAndMax()

        // Configure buttons to update character's life
        buttonIncrementLife.setOnClickListener {
            var life: Int = editTextCharacterLife.text.toString().toInt()
            if (life < MyApplication.instance.maxNumberInInputNumbers) {
                life += 1
                editTextCharacterLife.setText("$life")
            }
        }

        buttonReduceLife.setOnClickListener {
            var life: Int = editTextCharacterLife.text.toString().toInt()
            if (life > MyApplication.instance.minNumberInInputNumbers) {
                life -= 1
                editTextCharacterLife.setText("$life")
            }
        }

        // Save button to update character details and save list
        buttonSave.setOnClickListener {
            if(editTextCharacterLife.text.isEmpty()) {
                editTextCharacterLife.setText("0")
            }
            character.life = editTextCharacterLife.text.toString().toInt()
            mainActivity.saveCharacter(character, position)
            popupWindow.dismiss()
            Toast.makeText(context, PutIntoString(mainActivity).put(R.string.life_edited, character.name), Toast.LENGTH_SHORT).show()

        }

        // Cancel button to close the popup without saving
        buttonCancel.setOnClickListener {
            popupWindow.dismiss()
        }

        // Show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        PopupUtils.dimBehind(context, popupWindow)

    }


    fun EditText.selectAllOnHold() {
        this.setOnLongClickListener {
            this.selectAll()
            true  // Indica que el evento fue consumido
        }
    }
}