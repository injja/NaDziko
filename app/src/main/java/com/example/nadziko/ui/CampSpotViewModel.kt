package com.example.nadziko.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nadziko.data.CampSpot
import com.example.nadziko.data.CampSpotRepository
import com.example.nadziko.data.Rating
import com.example.nadziko.data.RatingRepository
import com.example.nadziko.data.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class CampSpotListItem(
    val spot: CampSpot,
    val author: User,
    val averageRating: Float?
)

class CampSpotViewModel(
    private val repository: CampSpotRepository,
    private val ratingRepository: RatingRepository
) : ViewModel() {

    val allSpotsWithAuthors: Flow<Map<CampSpot, User>> = repository.allSpotsWithAuthors

    val allSpotsWithAuthorsAndRatings: Flow<List<CampSpotListItem>> =
        allSpotsWithAuthors.flatMapLatest { spotsMap ->
            if (spotsMap.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    spotsMap.map { (spot, user) ->
                        ratingRepository.getAverageRatingForSpot(spot.id)
                            .map { averageRating ->
                                CampSpotListItem(
                                    spot = spot,
                                    author = user,
                                    averageRating = averageRating
                                )
                            }
                    }
                ) { items ->
                    items.toList()
                }
            }
        }

    fun addSpot(
        name: String,
        locationName: String,
        description: String,
        accessTips: String,
        packingTips: String,
        createdBy: Int
    ) {
        viewModelScope.launch {
            repository.addSpot(
                name = name,
                locationName = locationName,
                description = description,
                accessTips = accessTips,
                packingTips = packingTips,
                createdBy = createdBy
            )
        }
    }

    suspend fun getSpotById(id: Int): CampSpot? {
        return repository.getSpotById(id)
    }

    fun getSpotWithAuthor(id: Int): Flow<Map<CampSpot, User>> {
        return repository.getSpotWithAuthorById(id)
    }

    fun deleteSpotById(id: Int) {
        viewModelScope.launch {
            repository.deleteSpotById(id)
        }
    }

    fun updateSpot(spot: CampSpot) {
        viewModelScope.launch {
            repository.updateSpot(spot)
        }
    }

    fun getRatingsWithAuthors(spotId: Int): Flow<Map<Rating, User>> {
        return ratingRepository.getRatingsWithAuthorsForSpot(spotId)
    }

    fun addRating(campSpotId: Int, userId: Int, rate: Int, comment: String) {
        viewModelScope.launch {
            ratingRepository.addRating(campSpotId, userId, rate, comment)
        }
    }

    fun getAverageRatingForSpot(spotId: Int): Flow<Float?> {
        return ratingRepository.getAverageRatingForSpot(spotId)
    }
}

class CampSpotViewModelFactory(
    private val repository: CampSpotRepository,
    private val ratingRepository: RatingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CampSpotViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CampSpotViewModel(repository, ratingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}