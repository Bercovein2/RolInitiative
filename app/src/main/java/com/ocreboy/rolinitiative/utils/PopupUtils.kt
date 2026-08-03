package com.ocreboy.rolinitiative.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.WindowManager
import android.widget.PopupWindow

object PopupUtils {

    fun dimBehind(context: Context, popupWindow: PopupWindow) {

        val activity = context.findActivity() ?: return

        try {

            val container = popupWindow.contentView.rootView

            val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val params = container.layoutParams as WindowManager.LayoutParams

            params.flags = params.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            params.dimAmount = 0.45f

            wm.updateViewLayout(container, params)

        } catch (e: Exception) {
            Log.w("PopupUtils", "No se pudo aplicar el fondo oscurecido", e)
        }
    }

    fun Context.findActivity(): Activity? =
        when (this) {
            is Activity -> this
            is ContextWrapper -> baseContext.findActivity()
            else -> null
        }
}