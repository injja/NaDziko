package com.example.nadziko.data

import kotlinx.coroutines.flow.Flow

class RatingRepository(
    private val ratingDao: RatingDao
) {

    val allRatings: Flow<List<Rating>> = ratingDao.getAllRatings()

    fun getRatingsForSpot(spotId: Int): Flow<List<Rating>> {
        return ratingDao.getRatingsForSpot(spotId)
    }

    suspend fun getRatingById(id: Int): Rating? {
        return ratingDao.getRatingById(id)
    }

    suspend fun addRating(
        campSpotId: Int,
        userId: Int,
        rate: Int,
        comment: String
    ) {
        val rating = Rating(
            campSpotId = campSpotId,
            userId = userId,
            rate = rate,
            comment = comment.trim()
        )

        ratingDao.insertRating(rating)
    }

    suspend fun updateRating(rating: Rating) {
        ratingDao.updateRating(
            rating.copy(updatedAt = System.currentTimeMillis())
        )
    }

    suspend fun deleteRatingById(id: Int) {
        ratingDao.deleteRatingById(id)
    }

    fun getAverageRatingForSpot(spotId: Int): Flow<Float?> {
        return ratingDao.getAverageRatingForSpot(spotId)
    }

    suspend fun getRatingsForUser(userId: Int, spotId: Int): Rating? {
        return ratingDao.getRatingForSpotByUser(spotId, userId)
    }
}