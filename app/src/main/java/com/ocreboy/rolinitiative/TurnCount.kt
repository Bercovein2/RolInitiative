package com.ocreboy.rolinitiative

import android.annotation.SuppressLint
import android.widget.TextView

class TurnCount () {

    @SuppressLint("SetTextI18n")
    fun updateTurnCount(textTurn: TextView) {
        var actualRound : Int = textTurn.text.toString().toInt()
        actualRound++
        textTurn.text = "$actualRound"
        GlobalVariables.sharedPreferences.edit().putString("TURN_COUNT", textTurn.text.toString()).apply()

    }

    @SuppressLint("SetTextI18n")
    fun resetTurnCount(textTurn: TextView) {
        textTurn.text = "${GlobalVariables.turnInitial}"
        GlobalVariables.sharedPreferences.edit().putString("TURN_COUNT", textTurn.text.toString()).apply()
    }

}