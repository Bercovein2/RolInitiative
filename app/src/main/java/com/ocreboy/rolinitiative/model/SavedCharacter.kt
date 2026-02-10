package com.ocreboy.rolinitiative.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "characters")
@Parcelize
data class SavedCharacter(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var name: String,
    var armorClass: Int,
    var armorTouch: String,
    var armorFlatFooted: String,
    var life: Int,
    val folderId: Int,
    var initiative: Int,
    var imageUri: String? = null
): Parcelable