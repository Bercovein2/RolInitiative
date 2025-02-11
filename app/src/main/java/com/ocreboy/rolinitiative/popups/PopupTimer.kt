package com.ocreboy.rolinitiative.popups

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.ocreboy.rolinitiative.MainActivity
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.utils.PutIntoString

class PopupTimer(
    private val context: MainActivity,
    private val onTimerSet: (hours: Int, minutes: Int, seconds: Int) -> Unit
) {
    @SuppressLint("DefaultLocale", "SetTextI18n")
    fun show() {
        val popupView: View = LayoutInflater.from(context).inflate(R.layout.popup_edit_timer, null)

        // Obtener referencias a los campos de texto
        val hoursEditText: EditText = popupView.findViewById(R.id.editHours)
        val minutesEditText: EditText = popupView.findViewById(R.id.editMinutes)
        val secondsEditText: EditText = popupView.findViewById(R.id.editSeconds)

        // Configurar los valores iniciales
        hoursEditText.setText(String.format("%02d", context.timerHelper.getHours()))
        minutesEditText.setText(String.format("%02d", context.timerHelper.getMinutes()))
        secondsEditText.setText(String.format("%02d", context.timerHelper.getSeconds()))

        // Limitar a 2 caracteres en cada campo
        hoursEditText.filters = arrayOf(InputFilter.LengthFilter(2))
        minutesEditText.filters = arrayOf(InputFilter.LengthFilter(2))
        secondsEditText.filters = arrayOf(InputFilter.LengthFilter(2))

        // Seleccionar todo el texto al hacer clic
        hoursEditText.setOnClickListener {
            hoursEditText.selectAll()
            hoursEditText.requestFocus()
        }
        minutesEditText.setOnClickListener {
            minutesEditText.selectAll()
            minutesEditText.requestFocus()
        }
        secondsEditText.setOnClickListener {
            secondsEditText.selectAll()
            secondsEditText.requestFocus()
        }

        // Crear el AlertDialog sin botones automáticos
        val dialog = AlertDialog.Builder(context)
            .setView(popupView)
            .create()

        // Configurar el botón "OK"
        popupView.findViewById<Button>(R.id.buttonOK).setOnClickListener {
            val hours = hoursEditText.text.toString().toIntOrNull() ?: 0
            val minutes = minutesEditText.text.toString().toIntOrNull() ?: 0
            val seconds = secondsEditText.text.toString().toIntOrNull() ?: 0

            if (hours >= 0 || minutes >= 0 || seconds >= 0) {
                onTimerSet(hours, minutes, seconds)
                val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                Toast.makeText(context, PutIntoString(context).put(R.string.timer_set_to, timeFormatted), Toast.LENGTH_SHORT).show()

                context.saveTimerOnMemory()
                dialog.dismiss() // Cerrar el diálogo
            } else {
                Toast.makeText(context, R.string.invalid_time, Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar el botón "Cancel"
        popupView.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            dialog.dismiss() // Cerrar el diálogo sin realizar ninguna acción
        }

        // Configurar el botón "Clean"
        popupView.findViewById<Button>(R.id.buttonClean).setOnClickListener {
            clearTimer(hoursEditText, minutesEditText, secondsEditText) // Restablecer los valores
        }

        // Mostrar el diálogo
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun clearTimer(
        hoursEditText: EditText,
        minutesEditText: EditText,
        secondsEditText: EditText
    ) {
        hoursEditText.setText("00")
        minutesEditText.setText("00")
        secondsEditText.setText("00")
    }
}

