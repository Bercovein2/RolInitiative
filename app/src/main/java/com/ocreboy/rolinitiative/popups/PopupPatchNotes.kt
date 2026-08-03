package com.ocreboy.rolinitiative.popups

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.ocreboy.rolinitiative.utils.AppVersion
import com.ocreboy.rolinitiative.utils.FrameColor
import com.ocreboy.rolinitiative.GlobalVariables
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.utils.PopupUtils

class PopupPatchNotes(private val context: Context) {

    fun evaluateUpdate(){
        val userVersion = (GlobalVariables.sharedPreferences.getString("ACTUAL_VERSION", "") ?: "")
        val actualVersion = AppVersion(context).getAppVersionName()

        Log.d("MainActivity", "User version: $userVersion, Actual version: $actualVersion")

        if (userVersion.isEmpty() || actualVersion != userVersion) {
        //if (true) {
            this.showPopupWindow()
        }
    }

    @SuppressLint("MissingInflatedId", "InflateParams")
    fun showPopupWindow() {
        try {
            val frameColor = FrameColor(context)
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.popup_patch_notes, null)

            val textViewTitle = popupView.findViewById<TextView>(R.id.textViewTitle)

            val color = frameColor.getIconColor()

            textViewTitle.compoundDrawablesRelative.forEachIndexed { index, drawable ->
                drawable?.mutate()?.let {
                    DrawableCompat.setTint(it, color)
                    textViewTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        if (index == 0) it else textViewTitle.compoundDrawablesRelative[0],
                        if (index == 1) it else textViewTitle.compoundDrawablesRelative[1],
                        if (index == 2) it else textViewTitle.compoundDrawablesRelative[2],
                        if (index == 3) it else textViewTitle.compoundDrawablesRelative[3]
                    )
                }
            }
            textViewTitle.invalidate()

            val width = ViewGroup.LayoutParams.WRAP_CONTENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            val focusable = true
            val popupWindow = PopupWindow(popupView, width, height, focusable)

            popupView.setBackgroundResource(frameColor.getFrameColor()) // Ajusta según tu necesidad

            // Muestra el PopupWindow solo si el contexto es una actividad que no se está finalizando
            if (context is AppCompatActivity && !context.isFinishing) {
                val rootView = context.window.decorView.rootView
                popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0)
                PopupUtils.dimBehind(context, popupWindow)

                GlobalVariables.sharedPreferences.edit().putString("ACTUAL_VERSION", AppVersion(context).getAppVersionName()).apply()
                Log.d("PopupPatchNotes", "Popup window shown successfully")
            } else {
                Log.e("PopupPatchNotes", "Context is not valid or is finishing")
            }
        } catch (e: Exception) {
            Log.e("PopupPatchNotes", "Error showing popup window", e)
        }
    }
}
