package com.ocreboy.rolinitiative

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.ocreboy.rolinitiative.language.LanguageManager

class MyApplication : Application() {
    val maxCantDigitsInNumbers = 4
    val maxNumberInInputNumbers = maxNumberByDigits(maxCantDigitsInNumbers)
    val minNumberInInputNumbers = maxNumberInInputNumbers * -1

    @SuppressLint("StringFormatInvalid")
    override fun onCreate() {
        super.onCreate()
        instance = this

        getString(R.string.increaseReduceDescription, this.maxCantDigitsInNumbers)
        GlobalVariables.languageManager = LanguageManager(applicationContext)
        GlobalVariables.sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    }

    companion object {
        lateinit var instance: MyApplication
            private set
        // Exponer el contexto global de la aplicación
        val context
            get() = instance.applicationContext
    }

    private fun maxNumberByDigits(digits: Int): Int {
        // Verifica si el número de dígitos es menor o igual a 0, en cuyo caso devuelve 0
        if (digits <= 0) return 0

        // Genera un número que tenga tantos '9' como el número de dígitos especificados
        return "9".repeat(digits).toInt()
    }
}