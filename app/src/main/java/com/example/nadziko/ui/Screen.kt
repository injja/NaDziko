package com.example.nadziko.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Map : Screen("map", "Mapa", Icons.Filled.Place)
    object Search : Screen("search", "Szukaj", Icons.Filled.Search)
    object Saved : Screen("saved", "Zapisane", Icons.Filled.Favorite)
    object Discover : Screen("discover", "Odkrywaj", Icons.Filled.List)
    object Profile : Screen("profile", "Profil", Icons.Filled.Person)
}