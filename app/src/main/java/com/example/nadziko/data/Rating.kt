package com.example.nadziko.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ratings",
    foreignKeys = [
        ForeignKey(
            entity = CampSpot::class,
            parentColumns = ["id"],
            childColumns = ["campSpotId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("campSpotId"),
        Index("userId")
    ]
)
data class Rating(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val campSpotId: Int,
    val userId: Int,
    val rate: Int,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)