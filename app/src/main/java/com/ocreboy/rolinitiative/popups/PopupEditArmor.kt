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

class PopupEditArmor(private val context: Context) {

    lateinit var textCharacterName : TextView
    lateinit var editTextCharacterArmor : EditText
    lateinit var buttonIncrementArmor : Button
    lateinit var buttonReduceArmor : Button
    lateinit var buttonSave : Button
    lateinit var buttonCancel : Button

    lateinit var buttonClose: ImageButton

    @SuppressLint("InflateParams", "SetTextI18n", "MissingInflatedId")
    fun showPopupWindow(view: View, character: Character, mainActivity: MainActivity, position: Int) {
        // Inflate the popup layout
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_edit_armor, null)

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
        textCharacterName = popupView.findViewById(R.id.character_name_armor)
        textCharacterName.text = context.getString(R.string.armor_of) + " " + character.name

        editTextCharacterArmor = popupView.findViewById(R.id.edit_armor)
        editTextCharacterArmor.setText("${character.armorClass}")

        buttonIncrementArmor = popupView.findViewById(R.id.button_increase_armor)
        buttonReduceArmor = popupView.findViewById(R.id.button_decrease_armor)
        buttonSave = popupView.findViewById(R.id.button_save_armor)
        buttonCancel = popupView.findViewById(R.id.button_cancel_armor)

        editTextCharacterArmor.selectAllOnHold()

        editTextCharacterArmor.filters = Filters.numberBetweenZeroAndMax()

        // Configure buttons to update character's life
        buttonIncrementArmor.setOnClickListener {
            var armor: Int = editTextCharacterArmor.text.toString().toInt()
            if (armor < MyApplication.instance.maxNumberInInputNumbers) {
                armor += 1
                editTextCharacterArmor.setText("$armor")
            }
        }

        buttonReduceArmor.setOnClickListener {
            var armor: Int = editTextCharacterArmor.text.toString().toInt()
            if (armor > MyApplication.instance.minNumberInInputNumbers) {
                armor -= 1
                editTextCharacterArmor.setText("$armor")
            }
        }

        // Save button to update character details and save list
        buttonSave.setOnClickListener {
            if(editTextCharacterArmor.text.isEmpty()) {
                editTextCharacterArmor.setText("0")
            }
            character.armorClass = editTextCharacterArmor.text.toString().toInt()
            mainActivity.saveCharacter(character, position)
            popupWindow.dismiss()
            Toast.makeText(context, PutIntoString(mainActivity).put(R.string.armor_edited, character.name), Toast.LENGTH_SHORT).show()


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