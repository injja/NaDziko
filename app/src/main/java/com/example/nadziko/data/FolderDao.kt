package com.example.nadziko.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders")
    fun getAllFolders(): Flow<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveSpotToFolder(crossRef: SavedSpotCrossRef)

    @Delete
    suspend fun deleteSavedSpot(crossRef: SavedSpotCrossRef)

    // Tego brakowało - metoda usuwająca ze wszystkich folderów:
    @Query("DELETE FROM saved_spots WHERE campSpotId = :spotId")
    suspend fun removeSpotFromAllFolders(spotId: Int)

    // Pobiera listę ID folderów, w których jest dana miejscówka
    @Query("SELECT folderId FROM saved_spots WHERE campSpotId = :spotId")
    fun getFolderIdsForSpot(spotId: Int): Flow<List<Int>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_spots WHERE campSpotId = :spotId)")
    fun isSpotSaved(spotId: Int): Flow<Boolean>

    @Transaction
    @Query("""
        SELECT camp_spots.* FROM camp_spots 
        INNER JOIN saved_spots ON camp_spots.id = saved_spots.campSpotId 
        WHERE saved_spots.folderId = :folderId
    """)
    fun getSpotsForFolder(folderId: Int): Flow<List<CampSpot>>
}