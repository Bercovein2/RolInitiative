package com.ocreboy.rolinitiative.popups

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import com.ocreboy.rolinitiative.MainActivity
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.utils.FrameColor
import com.ocreboy.rolinitiative.utils.PopupUtils

class PopupCreateCampaign(
    private val activity: MainActivity,
    private val onCreateNew: () -> Unit,
    private val onUseCurrent: () -> Unit
) {

    private lateinit var popupWindow: PopupWindow

    private lateinit var buttonClose: ImageButton
    private lateinit var buttonCreateNew: Button
    private lateinit var buttonUseCurrent: Button
    private lateinit var textTitle: TextView
    private lateinit var textMessage: TextView

    private lateinit var frameColor: FrameColor

    fun show() {

        val popupView = LayoutInflater.from(activity)
            .inflate(R.layout.popup_create_campaign, null)

        val displayMetrics = activity.resources.displayMetrics

        frameColor = FrameColor(activity)

        popupWindow = PopupWindow(
            popupView,
            (displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupView.setBackgroundResource(frameColor.getFrameColor())
        popupWindow.elevation = 20f

        bindViews(popupView)

        setupViews()

        setupButtons()

        popupWindow.showAtLocation(
            activity.window.decorView,
            Gravity.CENTER,
            0,
            0
        )
        PopupUtils.dimBehind(activity, popupWindow)
    }


    private fun bindViews(view: View) {

        buttonClose = view.findViewById(R.id.buttonClose)

        buttonCreateNew = view.findViewById(R.id.buttonCreateNew)
        buttonUseCurrent = view.findViewById(R.id.buttonUseCurrent)

        textTitle = view.findViewById(R.id.textTitle)
        textMessage = view.findViewById(R.id.textMessage)
    }

    private fun setupViews() {

        frameColor.changeVectorColorDarkLightGray(buttonClose)

        frameColor.changeTextColor(textTitle)
        frameColor.changeTextColor(textMessage)
    }

    private fun setupButtons() {

        buttonClose.setOnClickListener {
            popupWindow.dismiss()
        }

        buttonCreateNew.setOnClickListener {

            popupWindow.dismiss()

            onCreateNew()
        }

        buttonUseCurrent.setOnClickListener {

            popupWindow.dismiss()

            onUseCurrent()
        }
    }
    
}