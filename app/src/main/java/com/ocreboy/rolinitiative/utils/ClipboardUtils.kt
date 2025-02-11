package com.ocreboy.rolinitiative.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.TextView
import android.widget.Toast
import com.ocreboy.rolinitiative.R

object ClipboardUtils {

    fun copyTextToClipboard(context: Context, textView: TextView) {
        // Obtén el servicio de portapapeles
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // Crea un ClipData con el texto del TextView
        val clip = ClipData.newPlainText("label", textView.text)
        // Copia el texto al portapapeles
        clipboard.setPrimaryClip(clip)

        // Muestra un mensaje de confirmación
        Toast.makeText(context, R.string.text_coppied_to_clipboard, Toast.LENGTH_SHORT).show()
    }
}