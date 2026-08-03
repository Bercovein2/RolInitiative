package com.ocreboy.rolinitiative.popups

import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import com.ocreboy.rolinitiative.MainActivity
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.utils.PopupUtils

fun PopupHideAllTimers(context: MainActivity, onConfirm: () -> Unit) {

    if(!context.characterAdapter.areThereAnyCharacterWithTimer()) {
        Toast.makeText(context, R.string.no_character_timer, Toast.LENGTH_SHORT).show()
        return
    }

    // Inflar el diseño del popup
    val inflater = LayoutInflater.from(context)
    val popupView = inflater.inflate(R.layout.popup_hide_timers_confirmation, null)

    popupView.setBackgroundResource(context.frameColor.getFrameColor())

    // Crear el PopupWindow
    val popupWindow = PopupWindow(popupView, WRAP_CONTENT, WRAP_CONTENT, true)

    // Configurar los botones del popup
    val btnOk = popupView.findViewById<Button>(R.id.buttonOk)
    val btnCancel = popupView.findViewById<Button>(R.id.buttonCancel)
    val textMessage = popupView.findViewById<TextView>(R.id.textMessage)

    // Configurar el mensaje del popup
    textMessage.text = context.getString(R.string.are_you_sure)

    // Configurar el texto de los botones
    btnOk.text = context.getString(R.string.ok)
    btnCancel.text = context.getString(R.string.cancel) // Asegúrate de que el texto del botón Cancel esté bien asignado

    // Acción al presionar el botón OK
    btnOk.setOnClickListener {
        onConfirm() // Ejecutar la acción de confirmación
        popupWindow.dismiss() // Cerrar el popup
    }

    // Acción al presionar el botón Cancel
    btnCancel.setOnClickListener {
        popupWindow.dismiss() // Solo cerrar el popup
    }

    // Mostrar el PopupWindow
    popupWindow.showAtLocation(popupView, android.view.Gravity.CENTER, 0, 0)
    PopupUtils.dimBehind(context, popupWindow)

}