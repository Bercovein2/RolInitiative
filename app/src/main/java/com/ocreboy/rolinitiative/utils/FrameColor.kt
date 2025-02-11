package com.ocreboy.rolinitiative.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.ocreboy.rolinitiative.GlobalVariables
import com.ocreboy.rolinitiative.R

class FrameColor (private val context: Context) {

    fun getFrameColor(): Int {
        return if (isDarkThemeOn()) {
            R.drawable.popup_background_dark_mode
        } else {
            R.drawable.popup_background_light_mode
        }
    }

    fun isDarkThemeOn() : Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    @SuppressWarnings
    fun changeVectorColorDarkLightGray(imageView: ImageView) {
        val drawable: Drawable? = imageView.drawable
        val colorRes: Int = if (this.isDarkThemeOn()) {
            R.color.darkGray
        } else {
            R.color.lightGray
        }
        drawable?.mutate()?.setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.SRC_IN)
    }

    @SuppressWarnings
    fun changeVectorColorBlackWhite(imageView: ImageView) {
        val drawable: Drawable? = imageView.drawable
        val colorRes: Int = getThemeColorForIcon()
        drawable?.mutate()?.setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.SRC_IN)
    }

    @SuppressWarnings
    fun changeDisabledVectorColor(imageView: ImageView) {
        val drawable: Drawable? = imageView.drawable
        val colorRes: Int = if (this.isDarkThemeOn()) {
            R.color.disabledDarkGrayVector
        } else {
            R.color.disabledLightGrayVector
        }
        drawable?.mutate()?.setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.SRC_IN)
    }

    fun disabledColor() : Int {
        if (this.isDarkThemeOn()) {
            return ContextCompat.getColor(context, R.color.disabledDarkGray)
        } else {
            return ContextCompat.getColor(context, R.color.disabledLightGray)
        }
    }

    fun changeTextColor(textView: TextView) {
        if (this.isDarkThemeOn()) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            textView.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
    }

    fun getThemeColorForIcon(): Int {
        if (this.isDarkThemeOn()) {
            return R.color.white
        } else {
            return R.color.black
        }
    }

    fun getIconColor(): Int{
        return ContextCompat.getColor(context, this.getThemeColorForIcon())
    }

    fun changeThemeToogleIcon(item : MenuItem) {
        val isNightMode = GlobalVariables.sharedPreferences.getBoolean("NIGHT_MODE", false)
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            item.setIcon(R.drawable.ic_button_light_mode)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            item.setIcon(R.drawable.ic_button_dark_mode)
        }
    }


}