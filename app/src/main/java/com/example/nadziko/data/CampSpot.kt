package com.example.nadziko.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "camp_spots",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["createdBy"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CampSpot(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val locationName: String,
    val description: String,
    val accessTips: String,
    val packingTips: String,
    val rating: Float = 0f,
    val createdBy: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
