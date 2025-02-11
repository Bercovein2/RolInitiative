package com.ocreboy.rolinitiative.sounds

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

class SoundPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    // Cambié el parámetro para recibir Uri en lugar de File
    fun setSound(uri: Uri?) {
        release() // Detener cualquier reproducción previa

        if (uri == null) {
            Log.w("SoundPlayer", "Se recibió un Uri nulo, no se reproducirá sonido")
            return
        }

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(context, uri)  // Usar Uri en lugar de File
                prepare()
            } catch (e: Exception) {
                Log.e("SoundPlayer", "Error al preparar el archivo de sonido", e)
            }
        }
    }

    fun playSound() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.prepare()
            }
            it.start()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
