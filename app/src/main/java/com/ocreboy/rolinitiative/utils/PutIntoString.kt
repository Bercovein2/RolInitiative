package com.ocreboy.rolinitiative.utils

import com.ocreboy.rolinitiative.MainActivity
import com.ocreboy.rolinitiative.R

class PutIntoString (private val mainActivity : MainActivity) {

    fun put(str:Int, toPut: String): String{
        val message = mainActivity.getString(str)
        return message.replace("%d", toPut, true)
    }

    fun put(str:String, toPut: String): String{
        return str.replace("%d", toPut, true)
    }
}