package com.example.nadziko.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "camp_spots")
data class CampSpot(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val locationName: String,
    val description: String,
    val accessTips: String,
    val packingTips: String,
    val rating: Float
)