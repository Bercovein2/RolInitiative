package com.ocreboy.rolinitiative.popups

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.Toast
import com.ocreboy.rolinitiative.utils.FrameColor
import com.ocreboy.rolinitiative.GlobalVariables
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.utils.PopupUtils

class PopupNotes (private val context: Context){

    lateinit var buttonClose: ImageButton

    @SuppressLint("SetTextI18n")
    fun showPopupWindow() {
        val builder = AlertDialog.Builder(context)
        val dialogLayout = LayoutInflater.from(context).inflate(R.layout.popup_notes_context, null)
        val editTextNotes = dialogLayout.findViewById<EditText>(R.id.editTextNotes)
        val characterCountTextView = dialogLayout.findViewById<TextView>(R.id.characterCount)

        buttonClose = dialogLayout.findViewById(R.id.buttonNotesClose)

        val frameColor = FrameColor(context)

        val savedNote = GlobalVariables.sharedPreferences.getString(GlobalVariables.NOTE_KEY, "")
        editTextNotes.setText(savedNote)

        characterCountTextView.text = "${savedNote?.length ?: 0}/${GlobalVariables.MAX_CHAR_COUNT}"

        editTextNotes.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val currentLength = s?.length ?: 0
                characterCountTextView.text = "$currentLength/${GlobalVariables.MAX_CHAR_COUNT}"

                if (currentLength > GlobalVariables.MAX_CHAR_COUNT) {
                    s?.delete(GlobalVariables.MAX_CHAR_COUNT, currentLength)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.save_buttons) { _, _ ->
            val note = editTextNotes.text.toString()
            val editor = GlobalVariables.sharedPreferences.edit()
            editor.putString(GlobalVariables.NOTE_KEY, note)
            editor.apply()
            Toast.makeText(context, R.string.note_saved, Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton(R.string.cancel_buttons) { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(frameColor.getFrameColor())


        buttonClose.setOnClickListener {
            dialog.dismiss()
        }
    }

}