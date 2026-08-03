package com.ocreboy.rolinitiative.popups

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import com.ocreboy.rolinitiative.utils.FrameColor
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.utils.PopupUtils

class PopupGreetings(private val context: Context) {

    @SuppressLint("MissingInflatedId", "InflateParams")
    fun showPopupWindow() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_greetings, null)
        val frameColor = FrameColor(context)
        val width = ViewGroup.LayoutParams.WRAP_CONTENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        popupView.setBackgroundResource(frameColor.getFrameColor())

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        PopupUtils.dimBehind(context, popupWindow)

    }
}
