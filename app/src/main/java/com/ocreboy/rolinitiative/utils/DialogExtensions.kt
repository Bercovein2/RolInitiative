package com.ocreboy.rolinitiative.utils

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import coil.load
import com.ocreboy.rolinitiative.R

fun Context.showImageFullScreen(uri: String) {

    if (uri.isEmpty()) return

    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(R.layout.popup_image)

    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    //vista de imagen
    val imageView = dialog.findViewById<ImageView>(R.id.imagePopup)

    //boton para compartir
    val shareButton = dialog.findViewById<ImageView>(R.id.buttonShareImage)

    //cargar imagen
    imageView.load(uri) {

        crossfade(true) }

    // 🔹 Compartir imagen
    shareButton.setOnClickListener {
        shareImage(uri)
    }

    // 🔹 Cerrar al tocar la imagen
    imageView.setOnClickListener {
        dialog.dismiss()
    }

    dialog.show()
}

fun Context.shareImage(uriString: String) {

    val uri = android.net.Uri.parse(uriString)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    startActivity(
        Intent.createChooser(shareIntent, "Compartir imagen con")
    )
}

fun Context.isUriValid(uriString: String?): Boolean {
    if (uriString.isNullOrEmpty()) return false
    return try {
        contentResolver.openInputStream(Uri.parse(uriString))?.close()
        true
    } catch (e: Exception) {
        false
    }
}

