package com.example.nadziko.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CampSpotDao {

    @Query("""
        SELECT s.*, IFNULL(AVG(r.rate), 0.0) as rating, u.* 
        FROM camp_spots s
        JOIN users u ON s.createdBy = u.id
        LEFT JOIN ratings r ON s.id = r.campSpotId
        GROUP BY s.id
        ORDER BY s.id DESC
    """)
    fun getAllSpotsWithAuthors(): Flow<Map<CampSpot, User>>

    @Query("""
        SELECT s.*, IFNULL(AVG(r.rate), 0.0) as rating, u.*
        FROM camp_spots s
        JOIN users u ON s.createdBy = u.id
        LEFT JOIN ratings r ON s.id = r.campSpotId
        WHERE s.id = :id
        GROUP BY s.id
    """)
    fun getSpotWithAuthorById(id: Int): Flow<Map<CampSpot, User>>

    @Query("SELECT * FROM camp_spots WHERE id = :id")
    suspend fun getSpotById(id: Int): CampSpot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpot(spot: CampSpot)

    @Query("DELETE FROM camp_spots WHERE id = :id")
    suspend fun deleteSpotById(id: Int)

    @Update
    suspend fun updateSpot(spot: CampSpot)
}
