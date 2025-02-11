package com.ocreboy.rolinitiative.popups

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.ocreboy.rolinitiative.utils.FrameColor
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.utils.ClipboardUtils

class PopupSuggestions(private val context: Context) {

    @SuppressLint("MissingInflatedId", "InflateParams")
    fun showPopupWindow() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_suggestions, null)
        val frameColor = FrameColor(context)
        val width = ViewGroup.LayoutParams.WRAP_CONTENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        popupView.setBackgroundResource(frameColor.getFrameColor())

        // Referencia al TextView dentro del PopupWindow
        val emailTextView: TextView = popupView.findViewById(R.id.mail)
        val instagramTextView: TextView = popupView.findViewById(R.id.instagram)

        // Configura el OnClickListener para copiar el texto al portapapeles
        emailTextView.setOnClickListener {
            ClipboardUtils.copyTextToClipboard(context, emailTextView)
        }

        instagramTextView.setOnClickListener {
            val url = context.getString(R.string.instagramURL)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        }

        // Muestra el PopupWindow
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
    }
}
