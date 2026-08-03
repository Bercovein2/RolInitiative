package com.ocreboy.rolinitiative.filters

import android.text.InputFilter
import android.text.Spanned

// Define una clase de filtro personalizada para limitar la cantidad de dígitos
class InputFilterMinMax(private val min: Int, private val max: Int) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            // Construir el nuevo valor resultante de la entrada actual
            val input = (dest?.subSequence(0, dstart).toString() + source + dest?.subSequence(dend, dest.length))

            // Permitir que solo "-" sea ingresado como el primer carácter
            if (input == "-" && min < 0) {
                return null
            }

            // Convertir el valor a entero
            val inputInt = input.toInt()
            // Verificar si el valor está dentro del rango permitido
            if (isInRange(min, max, inputInt)) {
                return null
            }
        } catch (nfe: NumberFormatException) {
            // No hacer nada si hay una excepción
        }
        // Devolver una cadena vacía para rechazar la entrada
        return ""
    }

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}