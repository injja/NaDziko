package com.example.nadziko.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CampSpotDao {

    @Query("SELECT * FROM camp_spots ORDER BY id DESC")
    fun getAllSpots(): Flow<List<CampSpot>>

    @Query("SELECT * FROM camp_spots WHERE id = :id")
    suspend fun getSpotById(id: Int): CampSpot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpot(spot: CampSpot)

    @Query("DELETE FROM camp_spots WHERE id = :id")
    suspend fun deleteSpotById(id: Int)

    @Update
    suspend fun updateSpot(spot: CampSpot)
}