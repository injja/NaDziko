package com.example.nadziko.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nadziko.data.Rating
import com.example.nadziko.data.RatingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RatingViewModel(
    private val repository: RatingRepository
) : ViewModel() {

    val allRatings: Flow<List<Rating>> = repository.allRatings

    fun addRating(
        campSpotId: Int,
        userId: Int,
        rate: Int,
        comment: String
    ) {
        viewModelScope.launch {
            repository.addRating(
                campSpotId = campSpotId,
                userId = userId,
                rate = rate,
                comment = comment
            )
        }
    }

    suspend fun getRatingById(id: Int): Rating? {
        return repository.getRatingById(id)
    }

    fun deleteRatingById(id: Int) {
        viewModelScope.launch {
            repository.deleteRatingById(id)
        }
    }
    fun updateRating(rating: Rating) {
        viewModelScope.launch {
            repository.updateRating(rating)
        }
    }
}

class RatingViewModelFactory(
    private val repository: RatingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RatingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RatingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}