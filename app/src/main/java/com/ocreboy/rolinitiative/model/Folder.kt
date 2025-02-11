package com.ocreboy.rolinitiative.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "folders")
@Parcelize
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var name: String,
    val folderId: Int

): Parcelable