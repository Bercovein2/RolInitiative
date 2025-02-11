package com.ocreboy.rolinitiative.sounds

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ocreboy.rolinitiative.GlobalVariables
import com.ocreboy.rolinitiative.R
import java.io.File


class SoundAdapter(
    context: Context,
    val soundUris: MutableList<Uri?>,
    private val soundNames: MutableList<String>,
    private var selectedPosition: Int,
    private val onSoundSelected: (Uri?) -> Unit,
    private val onDeleteSound: (Int) -> Unit
) : ArrayAdapter<String>(context, R.layout.list_item_centered, soundNames) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_sound, parent, false)

        val soundNameTextView: TextView = view.findViewById(R.id.soundName)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)

        val soundName = soundNames[position]
        soundNameTextView.text = soundName

        if (position == selectedPosition) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.darkGray))
        } else {
            view.setBackgroundColor(Color.TRANSPARENT)
        }

        if (soundName == GlobalVariables.noSoundName) {
            deleteButton.visibility = View.GONE
        } else {
            deleteButton.visibility = View.VISIBLE
        }

        deleteButton.setOnClickListener {
            onDeleteSound(position)
            notifyDataSetChanged()
        }

        view.setOnClickListener {
            if (position < soundUris.size) {
                onSoundSelected(soundUris.getOrNull(position))
                if (selectedPosition != position) {
                    selectedPosition = position
                    notifyDataSetChanged()
                }
            } else {
                Log.e("SoundAdapter", "Posición inválida: $position, tamaño de soundUris: ${soundUris.size}")
            }
        }

        return view
    }

    fun deleteSoundFromActualList(uriToRemove: Uri) {
        val fileName = getFileNameFromUri(uriToRemove)
        Log.d("SoundAdapter", "Intentando eliminar: $fileName")

        if (fileName == null) {
            Log.e("SoundAdapter", "El archivo no tiene nombre o no se encontró el URI: $uriToRemove")
            return
        }

        val indexToRemove = soundNames.indexOf(fileName)
        if (indexToRemove == -1) {
            Log.e("SoundAdapter", "El nombre del archivo no se encontró en soundNames: $fileName")
        } else {
            soundNames.removeAt(indexToRemove)
            soundUris.removeAt(indexToRemove)
            Log.d("SoundAdapter", "Elemento eliminado en posición: $indexToRemove")
        }

        notifyDataSetChanged()
    }

    fun addSoundToActualList(newUri: Uri?) {
        if (newUri != null) {
            val fileName = getFileNameFromUri(newUri) ?: return

            Log.d("SoundAdapter", "Nuevo archivo agregado: $fileName")

            if (!soundUris.contains(newUri)) {
                soundUris.add(newUri)
                soundNames.add(fileName)

                Handler(Looper.getMainLooper()).post {
                    notifyDataSetChanged()
                }
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex).substringBeforeLast(".")
                }
            }
        }
        return fileName
    }
}

