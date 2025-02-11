package com.ocreboy.rolinitiative.utils

import android.text.InputFilter
import android.text.Spanned

class NonEmptyInputFilter : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        // Si la nueva entrada resultante es vacía, rechazar la entrada
        val result = (dest?.subSequence(0, dstart).toString() + source + dest?.subSequence(dend, dest.length))
        return if (result.isBlank()) "" else null
    }
}
