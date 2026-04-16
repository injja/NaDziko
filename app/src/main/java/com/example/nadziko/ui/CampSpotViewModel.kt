package com.example.nadziko.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nadziko.data.CampSpot
import com.example.nadziko.data.CampSpotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CampSpotViewModel(
    private val repository: CampSpotRepository
) : ViewModel() {

    val allSpots: Flow<List<CampSpot>> = repository.allSpots

    fun addSpot(
        name: String,
        locationName: String,
        description: String,
        accessTips: String,
        packingTips: String
    ) {
        viewModelScope.launch {
            repository.addSpot(
                name = name,
                locationName = locationName,
                description = description,
                accessTips = accessTips,
                packingTips = packingTips
            )
        }
    }

    suspend fun getSpotById(id: Int): CampSpot? {
        return repository.getSpotById(id)
    }

    fun deleteSpotById(id: Int) {
        viewModelScope.launch {
            repository.deleteSpotById(id)
        }
    }
}

class CampSpotViewModelFactory(
    private val repository: CampSpotRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CampSpotViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CampSpotViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}