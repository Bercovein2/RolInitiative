package com.ocreboy.rolinitiative.model

import android.os.CountDownTimer
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ocreboy.rolinitiative.GlobalVariables
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Entity(tableName = "playing_characters")
@Parcelize
data class PlayingCharacter(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
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
    var timerSoundName: String? = GlobalVariables.noSoundName,
    var imageUri: String? = null,
    var order : Int,
    var campaignId : Long
): Parcelable