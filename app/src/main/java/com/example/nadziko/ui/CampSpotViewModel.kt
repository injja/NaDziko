package com.example.nadziko.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nadziko.data.CampSpot
import com.example.nadziko.data.CampSpotRepository
import com.example.nadziko.data.Folder
import com.example.nadziko.data.Rating
import com.example.nadziko.data.RatingRepository
import com.example.nadziko.data.SpotImage
import com.example.nadziko.data.SpotImageRepository
import com.example.nadziko.data.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CampSpotListItem(
    val spot: CampSpot,
    val author: User,
    val averageRating: Float?
)

class CampSpotViewModel(
    private val repository: CampSpotRepository,
    private val ratingRepository: RatingRepository,
    private val imageRepository: SpotImageRepository
) : ViewModel() {

    val allFolders: StateFlow<List<Folder>> = repository.folderDao.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedFolderId = MutableStateFlow<Int?>(null)

    val spotsInSelectedFolder: Flow<List<CampSpot>> = _selectedFolderId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.folderDao.getSpotsForFolder(id)
    }

    fun selectFolder(folderId: Int) {
        _selectedFolderId.value = folderId
    }

    fun addFolder(name: String) {
        viewModelScope.launch {
            repository.folderDao.insertFolder(Folder(name = name))
        }
    }

    fun saveSpotToFolder(folderId: Int, campSpotId: Int) {
        viewModelScope.launch {
            repository.saveSpotToFolder(folderId, campSpotId)
        }
    }

    fun isSpotSaved(spotId: Int): Flow<Boolean> {
        return repository.isSpotSaved(spotId)
    }

    fun removeSpotFromAllFolders(spotId: Int) {
        viewModelScope.launch {
            repository.removeSpotFromAllFolders(spotId)
        }
    }

    fun getFolderIdsForSpot(spotId: Int): Flow<List<Int>> {
        return repository.folderDao.getFolderIdsForSpot(spotId)
    }

    fun toggleSpotInFolder(folderId: Int, spotId: Int, shouldBeSaved: Boolean) {
        viewModelScope.launch {
            val crossRef = com.example.nadziko.data.SavedSpotCrossRef(folderId, spotId)
            if (shouldBeSaved) {
                repository.folderDao.saveSpotToFolder(crossRef)
            } else {
                repository.folderDao.deleteSavedSpot(crossRef)
            }
        }
    }

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
        createdBy: Int,
        latitude: Double,
        longitude: Double,
        imageUris: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val spotId = repository.addSpotWithId(
                name = name,
                locationName = locationName,
                description = description,
                accessTips = accessTips,
                packingTips = packingTips,
                createdBy = createdBy,
                latitude = latitude,
                longitude = longitude
            )

            if (imageUris.isNotEmpty()) {
                val images = imageUris.map { uri ->
                    SpotImage(campSpotId = spotId.toInt(), imageUri = uri)
                }
                imageRepository.insertImages(images)
            }
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

    fun addRating(campSpotId: Int, userId: Int, rate: Int, comment: String, imageUris: List<String> = emptyList()) {
        viewModelScope.launch {
            val ratingId = ratingRepository.addRatingWithId(campSpotId, userId, rate, comment)
            if (imageUris.isNotEmpty()) {
                val images = imageUris.map { uri ->
                    SpotImage(campSpotId = campSpotId, ratingId = ratingId.toInt(), imageUri = uri)
                }
                imageRepository.insertImages(images)
            }
        }
    }

    fun getAverageRatingForSpot(spotId: Int): Flow<Float?> {
        return ratingRepository.getAverageRatingForSpot(spotId)
    }

    fun getImagesForSpot(spotId: Int): Flow<List<SpotImage>> {
        return imageRepository.getImagesForSpot(spotId)
    }


}

class CampSpotViewModelFactory(
    private val repository: CampSpotRepository,
    private val ratingRepository: RatingRepository,
    private val imageRepository: SpotImageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CampSpotViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CampSpotViewModel(repository, ratingRepository, imageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}