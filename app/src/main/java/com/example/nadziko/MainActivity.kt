package com.example.nadziko

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.nadziko.ui.CampSpotListItem
import com.example.nadziko.ui.CampSpotViewModel
import com.example.nadziko.ui.CampSpotViewModelFactory
import com.example.nadziko.ui.SavedScreen
import com.example.nadziko.ui.Screen
import com.example.nadziko.ui.theme.NaDzikoTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CampSpotViewModel by viewModels {
        CampSpotViewModelFactory(
            (application as NadzikoApplication).repository,
            (application as NadzikoApplication).ratingRepository,
            (application as NadzikoApplication).spotImageRepository
        )
    }

    private val addSpotLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NaDzikoTheme {
                val spots by viewModel.allSpotsWithAuthorsAndRatings.collectAsState(initial = emptyList())

                MainAppScreen(
                    viewModel = viewModel,
                    spots = spots,
                    onAddClick = {
                        val intent = Intent(this, AddCampSpotActivity::class.java)
                        addSpotLauncher.launch(intent)
                    },
                    onMapClick = {
                        val intent = Intent(this, MapActivity::class.java)
                        startActivity(intent)
                    },
                    onSpotClick = { spotId ->
                        val intent = Intent(this, CampSpotDetailsActivity::class.java)
                        intent.putExtra("spot_id", spotId)
                        startActivity(intent)
                    },
                    onProfileClick = {
                        val intent = Intent(this, ProfileSettingsActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: CampSpotViewModel,
    spots: List<CampSpotListItem>,
    onAddClick: () -> Unit,
    onMapClick: () -> Unit,
    onSpotClick: (Int) -> Unit,
    onProfileClick: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Map, Screen.Search, Screen.Saved, Screen.Discover, Screen.Profile)

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Search.route, // Startujemy od zakładki z listą
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Map.route) {
                // Tymczasowy przycisk otwierający Twoje stare MapActivity
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Button(onClick = onMapClick) { Text("Otwórz Pełną Mapę") }
                }
            }
            composable(Screen.Search.route) {
                SpotsListScreen(spots, onAddClick, onSpotClick)
            }
            composable(Screen.Saved.route) {
                SavedScreen(viewModel = viewModel, onSpotClick = onSpotClick)
            }
            composable(Screen.Discover.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ekran Odkrywaj (W budowie)")
                }
            }
            composable(Screen.Profile.route) {
                // Tymczasowy przycisk otwierający stare ProfileSettingsActivity
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Button(onClick = onProfileClick) { Text("Przejdź do profilu") }
                }
            }
        }
    }
}

@Composable
fun SpotsListScreen(
    spots: List<CampSpotListItem>,
    onAddClick: () -> Unit,
    onSpotClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onAddClick, modifier = Modifier.fillMaxWidth()) {
            Text("Dodaj nowe miejsce")
        }

        if (spots.isEmpty()) {
            Text("Brak zapisanych miejsc. Dodaj pierwsze miejsce.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(spots) { item ->
                    val spot = item.spot
                    val averageRating = item.averageRating

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSpotClick(spot.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = spot.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = spot.locationName, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = spot.description,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = averageRating?.let { "Ocena: %.1f".format(it) } ?: "Brak ocen",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}