package com.ocreboy.rolinitiative

import android.content.SharedPreferences
import com.ocreboy.rolinitiative.language.LanguageManager

class GlobalVariables {
    companion object {
        val noSoundName = "<<No Sound>>"
        var addBestiary = true
        val maxLengthInputName = 20
        val minQuantityToStart = 2
        val minQuantityToClear = 0
        val minQuantityToSort = 2
        var turnInitial = 0
        var currentPosition = 0
        var initialPosition = -1
        val defaultTime = 0L
        val defaultSound = -1
        lateinit var sharedPreferences: SharedPreferences
        val defaultLanguage = "en"
        var dicePopupPrefs = "dice_popup_prefs"
        const val NOTE_KEY = "noteKey"
        const val MAX_CHAR_COUNT = 500
        val maxNumberMap = mapOf(
            "d2" to 2,
            "d4" to 4,
            "d6" to 6,
            "d8" to 8,
            "d10" to 10,
            "d12" to 12,
            "d20" to 20,
            "d100" to 100
        )
        val maxDicesIcons = mapOf(
            "d2" to R.drawable.d2,
            "d4" to R.drawable.d4,
            "d6" to R.drawable.d6,
            "d8" to R.drawable.d8,
            "d10" to R.drawable.d10,
            "d12" to R.drawable.d12,
            "d20" to R.drawable.d20,
            "d100" to R.drawable.d100
        )

        const val MAX_DICES_TO_THROW = 10
        lateinit var languageManager: LanguageManager

    }

    object GlobalVariables {
        lateinit var sharedPreferences: SharedPreferences
    }
}