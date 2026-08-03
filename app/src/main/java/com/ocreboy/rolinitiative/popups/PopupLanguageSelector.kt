package com.ocreboy.rolinitiative.popups

import com.ocreboy.rolinitiative.R
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import android.widget.PopupWindow
import com.ocreboy.rolinitiative.GlobalVariables
import com.ocreboy.rolinitiative.MainActivity
import com.ocreboy.rolinitiative.utils.PopupUtils

class PopupLanguageSelector(private val context: MainActivity) {

    // Mostrar el popup
    fun show() {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.popup_language_selector, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Spinner y botones del popup
        val spinnerLanguages = popupView.findViewById<Spinner>(R.id.spinnerLanguages)
        val buttonConfirm = popupView.findViewById<Button>(R.id.buttonConfirm)
        val buttonCancel = popupView.findViewById<Button>(R.id.buttonCancel)

        val spanish = context.getString(R.string.spanish)
        val english = context.getString(R.string.english)
        val italian = context.getString(R.string.italian)

        // Lista de idiomas disponibles
        val languages = listOf(spanish, english, italian)
        val languageCodes = mapOf(spanish to "es", english to "en", italian to "it")

        // Adaptador para el Spinner
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, languages)
        spinnerLanguages.adapter = adapter

        // Obtener el idioma guardado en SharedPreferences
        val savedLanguage = GlobalVariables.sharedPreferences.getString("App_Language", GlobalVariables.defaultLanguage) ?: "en"

        // Encontrar el índice de la posición correspondiente al idioma guardado
        val selectedLanguage = when (savedLanguage) {
            "es" -> spanish
            "it" -> italian
            else -> english
        }

        // Establecer la selección del Spinner en el idioma guardado
        val selectedPosition = languages.indexOf(selectedLanguage)
        spinnerLanguages.setSelection(selectedPosition)

        // Mostrar el popup en el centro de la pantalla
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

        PopupUtils.dimBehind(context, popupWindow)


        // Acción del botón Confirmar
        buttonConfirm.setOnClickListener {
            val selectedLanguage = spinnerLanguages.selectedItem.toString()
            val languageCode = languageCodes[selectedLanguage] ?: "en"

            // Guardar la preferencia antes de cambiar el idioma
            GlobalVariables.languageManager.saveLanguagePreference(languageCode)

            // Aplicar el idioma
            GlobalVariables.languageManager.setAppLocale(languageCode, context)

            popupWindow.dismiss()

            // Reiniciar la actividad
            context.recreate()
        }

        // Acción del botón Cancelar
        buttonCancel.setOnClickListener {
            popupWindow.dismiss()
        }
    }
}

