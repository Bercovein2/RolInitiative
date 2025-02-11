package com.ocreboy.rolinitiative.animations

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.widget.ImageButton

class ButtonAnimationHelper {

    @SuppressLint("ClickableViewAccessibility")
    fun applyScaleAnimation(imageButton: ImageButton) {
        val scaleDown = ObjectAnimator.ofFloat(imageButton, "scaleX", 1f)
        scaleDown.duration = 150
        val scaleDownY = ObjectAnimator.ofFloat(imageButton, "scaleY", 1f)
        scaleDownY.duration = 150

        val scaleUp = ObjectAnimator.ofFloat(imageButton, "scaleX", 1.3f)
        scaleUp.duration = 150
        val scaleUpY = ObjectAnimator.ofFloat(imageButton, "scaleY", 1.3f)
        scaleUpY.duration = 150

        imageButton.isFocusable = false
        imageButton.isClickable = true

        imageButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    scaleUp.start()
                    scaleUpY.start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                    scaleDown.start()
                    scaleDownY.start()
                    // Llamada a performClick para accesibilidad
                    imageButton.performClick()
                }
            }

            true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun applyScaleAnimationWithoutBackground(imageButton: ImageButton) {
        applyScaleAnimation(imageButton)
        imageButton.setBackgroundResource(android.R.color.transparent) // Fondo transparente para evitar el borde gris

    }



}