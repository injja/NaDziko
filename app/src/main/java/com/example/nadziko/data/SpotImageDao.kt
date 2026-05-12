package com.example.nadziko.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpotImageDao {
    @Query("SELECT * FROM spot_images WHERE campSpotId = :campSpotId")
    fun getImagesForSpot(campSpotId: Int): Flow<List<SpotImage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: SpotImage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<SpotImage>)

    @Query("DELETE FROM spot_images WHERE id = :id")
    suspend fun deleteImageById(id: Int)
}
