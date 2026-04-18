package com.example.nadziko

import android.app.Application
import com.example.nadziko.data.AppDatabase
import com.example.nadziko.data.CampSpotRepository
import com.example.nadziko.data.RatingRepository
import com.example.nadziko.data.UserRepository

class NadzikoApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { CampSpotRepository(database.campSpotDao()) }
    val ratingRepository by lazy { RatingRepository(database.ratingDao()) }
    val userRepository by lazy { UserRepository(database.userDao()) }
}