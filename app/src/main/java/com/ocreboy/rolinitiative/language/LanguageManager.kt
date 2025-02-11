package com.ocreboy.rolinitiative.language

import android.content.Context
import com.ocreboy.rolinitiative.GlobalVariables
import com.ocreboy.rolinitiative.MainActivity
import java.util.Locale

class LanguageManager(private val context: Context) {

    fun setAppLocale(language: String, context: Context) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)

        // Actualiza la configuración del sistema
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    fun saveLanguagePreference(language: String) {
        val currentLanguage = GlobalVariables.sharedPreferences.getString("App_Language", null)
        if (currentLanguage != language) {
            GlobalVariables.sharedPreferences.edit().putString("App_Language", language).apply()
        }
    }

    fun loadSavedLanguage(context: Context) {
        // Obtener el idioma guardado
        val language = GlobalVariables.sharedPreferences.getString("App_Language", GlobalVariables.defaultLanguage) ?: GlobalVariables.defaultLanguage
        // Aplicar el idioma al contexto recibido
        setAppLocale(language, context)
    }
}
