package com.ocreboy.rolinitiative.sounds

import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import com.ocreboy.rolinitiative.MainActivity
import com.ocreboy.rolinitiative.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.os.Environment
import androidx.annotation.RequiresApi


class SoundManager(private val context: MainActivity) {

    private val folderName = "TimerSounds"

    init {
        // No necesitas crear la carpeta manualmente, MediaStore gestiona las rutas.
        Log.d("SoundManager", "SoundManager inicializado")
    }

    companion object {
        const val REQUEST_DELETE_PERMISSION = 1001  // Puedes usar cualquier número que no se repita en otras solicitudes
    }

    // Mantener openFilePicker como está
    fun openFilePicker(callback: (Uri?) -> Unit) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
        }
        (context as MainActivity).filePickerCallbackUri = callback
        context.filePickerLauncher.launch(intent)
    }

    // Obtener la lista de archivos de audio desde MediaStore
    fun getAudioFiles(): List<Uri> {
        val audioList = mutableListOf<Uri>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME
        )

        val selection = "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("Music/$folderName/%")

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                audioList.add(contentUri)
            }
        }

        return audioList
    }

    // Guardar un archivo en MediaStore
    fun saveFileToExternalStorage(uri: Uri) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = getFileNameFromUri(uri) ?: "unknown_sound.mp3"

        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/$folderName")
        }

        val resolver = context.contentResolver
        val newUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

        newUri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            Toast.makeText(context, R.string.new_sound_added, Toast.LENGTH_SHORT).show()
            Log.d("SoundManager", "Archivo guardado en MediaStore: $fileName")
        } ?: run {
            Log.e("SoundManager", "Error al guardar el archivo en MediaStore")
        }

        (context as MainActivity).filePickerCallbackUri?.invoke(newUri)
        (context as MainActivity).filePickerCallbackUri = null
    }

    // Obtener el nombre del archivo seleccionado
    fun getFileNameFromUri(uri: Uri?): String {
        var name = ""
        if(uri != null) {
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            name = it.getString(nameIndex)
                        }
                    }
                }
            } else if (uri.scheme == "file") {
                name = File(uri.path!!).name
            }
            name = name.substringBeforeLast(".")
        }
        return name
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun deleteSoundFile(activity: Activity, uri: Uri) {
        try {
            val rowsDeleted = context.contentResolver.delete(uri, null, null)
            if (rowsDeleted > 0) {
                Log.d("SoundManager", "Archivo eliminado: $uri")
            } else {
                Log.e("SoundManager", "No se pudo eliminar el archivo: $uri")
            }
        } catch (e: SecurityException) {
            val intentSender = MediaStore.createDeleteRequest(context.contentResolver, listOf(uri)).intentSender
            activity.startIntentSenderForResult(intentSender, REQUEST_DELETE_PERMISSION, null, 0, 0, 0, null)
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun copySoundsToMediaStore() {
        val sounds = listOf(
            R.raw.fire_blow,
            R.raw.cloc_timer,
            R.raw.clown_horn,
            R.raw.kitchen_timer
        )

        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        sounds.forEach { soundResId ->
            val fileName = context.resources.getResourceEntryName(soundResId) + ".aac"

            // Verificar si el archivo ya existe en MediaStore
            if (isFileInMediaStore(fileName)) {
                Log.d("SoundManager", "El archivo ya existe en MediaStore: $fileName")
                return@forEach
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/aac")
                put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/$folderName")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }

            val uri = context.contentResolver.insert(collection, contentValues)

            uri?.let {
                try {
                    val inputStream = context.resources.openRawResource(soundResId)
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    context.contentResolver.update(it, contentValues, null, null)

                    Log.d("SoundManager", "Archivo copiado a MediaStore: $fileName")
                } catch (e: Exception) {
                    Log.e("SoundManager", "Error al copiar el archivo $fileName a MediaStore", e)
                }
            } ?: run {
                Log.e("SoundManager", "No se pudo obtener URI para $fileName")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun isFileInMediaStore(fileName: String): Boolean {
        val projection = arrayOf(MediaStore.Audio.Media.DISPLAY_NAME)
        val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)

        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val cursor = context.contentResolver.query(collection, projection, selection, selectionArgs, null)

        cursor?.use {
            return it.moveToFirst()  // Si el cursor devuelve algún resultado, el archivo existe
        }

        return false
    }


    fun getFileByName(fileName: String): Uri? {
        val folderUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) // Usa MediaStore para acceder a los archivos externos

        // Definimos el filtro de búsqueda en la carpeta deseada y el nombre del archivo
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("$fileName%") // Usamos un LIKE para la búsqueda parcial (ignorando mayúsculas/minúsculas)

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME
        )

        // Realizamos la consulta
        val cursor = context.contentResolver.query(
            folderUri,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(MediaStore.MediaColumns._ID)
                val fileUri = ContentUris.withAppendedId(folderUri, it.getLong(idIndex)) // Construye el Uri final del archivo

                return fileUri // Retorna el Uri del archivo encontrado
            }
        }

        return null // Si no se encuentra el archivo, retornamos null
    }



}
