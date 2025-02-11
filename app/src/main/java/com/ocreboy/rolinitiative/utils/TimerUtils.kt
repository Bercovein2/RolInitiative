package com.ocreboy.rolinitiative.utils

import android.annotation.SuppressLint
import com.ocreboy.rolinitiative.MyApplication
import com.ocreboy.rolinitiative.R

@SuppressLint("DefaultLocale")
class TimerUtils {
    companion object {

         fun formatTimerFull(seconds: Int): String {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
             return format02d(hours, minutes, secs)
        }

        fun formatTimerFull(seconds: Long): String {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            return format02d(hours.toInt(), minutes.toInt(), secs.toInt())
        }

        fun formatTimer60(time:Int): String {
            return String.format("%02d", time)
        }

        fun formatTimerFull(hours: Int, minutes: Int, seconds:Int): String {
            return format02d(hours, minutes, seconds)
        }

        private fun format02d(hours: Int, minutes: Int, seconds:Int): String {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        fun format02d(timerSeconds:Int):String{
            val hours = getHours(timerSeconds)
            val minutes = getMinutes(timerSeconds)
            val seconds = getSeconds(timerSeconds)
            return format02d(hours, minutes, seconds)
        }

        fun getHours(seconds:Int):Int{
            return seconds / 3600
        }
        fun getMinutes(seconds:Int):Int{
            return (seconds % 3600) / 60
        }
        fun getSeconds(seconds:Int):Int{
            return seconds % 60
        }

        fun getHours(seconds:Long):Int{
            return (seconds / 3600).toInt()
        }
        fun getMinutes(seconds:Long):Int{
            return ((seconds % 3600) / 60).toInt()
        }
        fun getSeconds(seconds:Long):Int{
            return (seconds % 60).toInt()
        }

        fun getSecondsFull(hours:Int, minutes:Int, seconds: Int):Int {
            return hours * 3600 + minutes * 60 + seconds
        }

        fun getSecondsLeft(millisUntilFinished: Long):Long{
            return millisUntilFinished / 1000
        }

        fun getZeroFormat() : String{
            return MyApplication.context.getString(R.string._00_00_00)
        }
    }
}