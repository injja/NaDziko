package com.example.nadziko.data

import kotlinx.coroutines.flow.Flow

class CampSpotRepository(
    private val campSpotDao: CampSpotDao
) {
    val allSpots: Flow<List<CampSpot>> = campSpotDao.getAllSpots()

    suspend fun addSpot(
        name: String,
        locationName: String,
        description: String,
        accessTips: String,
        packingTips: String
    ) {
        val spot = CampSpot(
            name = name,
            locationName = locationName,
            description = description,
            accessTips = accessTips,
            packingTips = packingTips,
            rating = 0f
        )

        campSpotDao.insertSpot(spot)
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