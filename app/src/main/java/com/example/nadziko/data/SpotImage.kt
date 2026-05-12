package com.example.nadziko.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "spot_images",
    foreignKeys = [
        ForeignKey(
            entity = CampSpot::class,
            parentColumns = ["id"],
            childColumns = ["campSpotId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Rating::class,
            parentColumns = ["id"],
            childColumns = ["ratingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("campSpotId"), Index("ratingId")]
)
data class SpotImage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val campSpotId: Int,
    val ratingId: Int? = null, // Can be null if image is added to the spot directly
    val imageUri: String
)
