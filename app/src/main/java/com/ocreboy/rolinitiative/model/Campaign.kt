package com.ocreboy.rolinitiative.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "campaigns")
@Parcelize
data class Campaign(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var name: String,
    var timer: String?
): Parcelable