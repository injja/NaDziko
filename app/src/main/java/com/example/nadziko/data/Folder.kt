package com.example.nadziko.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(
    tableName = "saved_spots",
    primaryKeys = ["folderId", "campSpotId"]
)
data class SavedSpotCrossRef(
    val folderId: Int,
    val campSpotId: Int
)