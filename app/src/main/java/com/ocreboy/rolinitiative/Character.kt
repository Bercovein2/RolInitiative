package com.ocreboy.rolinitiative

import android.os.CountDownTimer
import android.os.Parcel
import android.os.Parcelable
import com.ocreboy.rolinitiative.model.SavedCharacter
import com.ocreboy.rolinitiative.sounds.SoundPlayer
import com.ocreboy.rolinitiative.utils.TimerUtils

data class Character(
    var name: String,
    var initiative: Int,
    var armorClass: Int,
    var armorTouch: String,
    var armorFlatFooted: String,
    var isSelected: Boolean = false,
    var life: Int,
    var isDead: Boolean = false,
    var hasActiveTimer: Boolean = false,
    var isPaused: Boolean = false,
    var timeLeftInSeconds: Int,
    var originalTimer: Int,
    var isTimerRunning: Boolean = false,
    var countDownTimer: CountDownTimer? = null,
    var timerSoundName: String? = GlobalVariables.noSoundName

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        false,
        false,
        false,
        0,
        0
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(initiative)
        parcel.writeInt(armorClass)
        parcel.writeString(armorTouch)
        parcel.writeString(armorFlatFooted)
        parcel.writeByte(if (isSelected) 1 else 0)
        parcel.writeInt(life)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Character> {
        override fun createFromParcel(parcel: Parcel): Character {
            return Character(parcel)
        }

        override fun newArray(size: Int): Array<Character?> {
            return arrayOfNulls(size)
        }
    }

    fun convertToSavedCharacter(folderId : Int) : SavedCharacter{
        return SavedCharacter(0, this.name, this.armorClass, this.armorTouch, this.armorFlatFooted,
            this.life, folderId, this.initiative)
    }

    fun startTimer(onTick: (Long) -> Unit, onFinish: () -> Unit) {
        countDownTimer?.cancel() // Cancelar cualquier temporizador anterior

        isPaused = false
        countDownTimer = object : CountDownTimer(timeLeftInSeconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                isTimerRunning = true
                timeLeftInSeconds = TimerUtils.getSecondsLeft(millisUntilFinished).toInt()
                onTick(millisUntilFinished)
            }

            override fun onFinish() {
                isPaused = false
                timeLeftInSeconds = 0
                onFinish()
                isTimerRunning = false
            }
        }.start()
    }

    fun stopTimer(){
        if(hasActiveTimer) {
            isPaused = false
            isTimerRunning = false
            countDownTimer?.cancel()
            timeLeftInSeconds = 0
            originalTimer = 0
        }
    }

    fun pauseTimer(){
        if(hasActiveTimer) {
            isPaused = true
            isTimerRunning = true
            countDownTimer?.cancel()
        }
    }
}