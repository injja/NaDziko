package com.example.nadziko.data

import kotlinx.coroutines.flow.Flow

class SpotImageRepository(private val spotImageDao: SpotImageDao) {
    fun getImagesForSpot(campSpotId: Int): Flow<List<SpotImage>> {
        return spotImageDao.getImagesForSpot(campSpotId)
    }

    suspend fun insertImages(images: List<SpotImage>) {
        spotImageDao.insertImages(images)
    }

    suspend fun insertImage(image: SpotImage) {
        spotImageDao.insertImage(image)
    }
}
