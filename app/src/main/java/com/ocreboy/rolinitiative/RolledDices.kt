package com.ocreboy.rolinitiative

import java.security.SecureRandom

class RollDice(sides: Int, numDice: Int) {

    var total: Int
        private set

    var dices: List<Int>
        private set

    init {
        val secureRandom = SecureRandom()
        dices = List(numDice) { secureRandom.nextInt(sides) + 1 }
        total = dices.sum()
    }
}