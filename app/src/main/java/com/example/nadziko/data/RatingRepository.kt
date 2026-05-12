package com.example.nadziko.data

import kotlinx.coroutines.flow.Flow

class RatingRepository(
    private val ratingDao: RatingDao
) {

    val allRatings: Flow<List<Rating>> = ratingDao.getAllRatings()

    fun getRatingsWithAuthorsForSpot(spotId: Int): Flow<Map<Rating, User>> {
        return ratingDao.getRatingsWithAuthorsForSpot(spotId)
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
        addRatingWithId(campSpotId, userId, rate, comment)
    }

    suspend fun addRatingWithId(
        campSpotId: Int,
        userId: Int,
        rate: Int,
        comment: String
    ): Long {
        val rating = Rating(
            campSpotId = campSpotId,
            userId = userId,
            rate = rate,
            comment = comment.trim()
        )

        return ratingDao.insertRating(rating)
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

    suspend fun getRatingForSpotByUser(spotId: Int, userId: Int): Rating? {
        return ratingDao.getRatingForSpotByUser(spotId, userId)
    }
}
