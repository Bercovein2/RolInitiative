package com.ocreboy.rolinitiative.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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

    val imageView = dialog.findViewById<ImageView>(R.id.imagePopup)

    imageView.load(uri) {
        crossfade(true)
    }

    imageView.setOnClickListener {
        dialog.dismiss()
    }

    dialog.show()
}
