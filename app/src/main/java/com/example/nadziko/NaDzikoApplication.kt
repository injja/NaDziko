package com.example.nadziko

import android.app.Application
import com.example.nadziko.data.AppDatabase
import com.example.nadziko.data.CampSpotRepository

class NadzikoApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { CampSpotRepository(database.campSpotDao()) }
}