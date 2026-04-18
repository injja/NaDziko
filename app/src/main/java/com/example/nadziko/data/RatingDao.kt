package com.example.nadziko.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RatingDao {

    @Query("SELECT * FROM ratings ORDER BY id DESC")
    fun getAllRatings(): Flow<List<Rating>>

    @Query("SELECT * FROM ratings WHERE id = :id")
    suspend fun getRatingById(id: Int): Rating?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: Rating)

    @Query("DELETE FROM ratings WHERE id = :id")
    suspend fun deleteRatingById(id: Int)

    @Update
    suspend fun updateRating(rating: Rating)

    @Query("SELECT * FROM ratings WHERE campSpotId = :spotId ORDER BY createdAt DESC")
    fun getRatingsForSpot(spotId: Int): Flow<List<Rating>>

    @Query("SELECT AVG(rate) FROM ratings WHERE campSpotId = :spotId")
    fun getAverageRatingForSpot(spotId: Int): Flow<Float?>

    @Query("SELECT * FROM ratings WHERE campSpotId = :spotId AND userId = :userId LIMIT 1")
    suspend fun getRatingForSpotByUser(spotId: Int, userId: Int): Rating?
}