package com.example.nadziko.data

import kotlinx.coroutines.flow.Flow

class CampSpotRepository(
    private val campSpotDao: CampSpotDao
) {
    val allSpotsWithAuthors: Flow<Map<CampSpot, User>> = campSpotDao.getAllSpotsWithAuthors()

    fun getSpotWithAuthorById(id: Int): Flow<Map<CampSpot, User>> {
        return campSpotDao.getSpotWithAuthorById(id)
    }

    suspend fun addSpot(
        name: String,
        locationName: String,
        description: String,
        accessTips: String,
        packingTips: String,
        createdBy: Int,
        latitude: Double,
        longitude: Double
    ) {
        addSpotWithId(name, locationName, description, accessTips, packingTips, createdBy, latitude, longitude)
    }

    suspend fun addSpotWithId(
        name: String,
        locationName: String,
        description: String,
        accessTips: String,
        packingTips: String,
        createdBy: Int,
        latitude: Double,
        longitude: Double
    ): Long {
        val spot = CampSpot(
            name = name,
            locationName = locationName,
            description = description,
            accessTips = accessTips,
            packingTips = packingTips,
            rating = 0f,
            createdBy = createdBy,
            latitude = latitude,
            longitude = longitude,
        )

        return campSpotDao.insertSpot(spot)
    }

    suspend fun getSpotById(id: Int): CampSpot? {
        return campSpotDao.getSpotById(id)
    }

    suspend fun deleteSpotById(id: Int) {
        campSpotDao.deleteSpotById(id)
    }

    suspend fun updateSpot(spot: CampSpot) {
        campSpotDao.updateSpot(spot)
    }
}
