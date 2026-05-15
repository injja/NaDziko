package com.example.nadziko

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.nadziko.ui.* // To importuje SavedScreen i Screen
import com.example.nadziko.ui.theme.NaDzikoTheme

private val DarkGreen = Color(0xFF1E3524)
private val BrandOrange = Color(0xFFD66A27)
private val LightBg = Color(0xFFF6F6F6)

class MainActivity : ComponentActivity() {

    private val viewModel: CampSpotViewModel by viewModels {
        CampSpotViewModelFactory(
            (application as NadzikoApplication).repository,
            (application as NadzikoApplication).ratingRepository,
            (application as NadzikoApplication).spotImageRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NaDzikoTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: CampSpotViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController, currentRoute)
        }
    ) { innerPadding ->
        val context = androidx.compose.ui.platform.LocalContext.current
        val application = context.applicationContext as NadzikoApplication
        val spots by viewModel.allSpotsWithAuthorsAndRatings.collectAsState(initial = emptyList())

        NavHost(
            navController = navController,
            startDestination = Screen.Search.route,
            modifier = Modifier
                .padding(innerPadding)
                .background(LightBg)
        ) {
            composable(Screen.Map.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Button(onClick = { context.startActivity(Intent(context, MapActivity::class.java)) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)) { Text("Otwórz Mapę") }
                }
            }

            composable(Screen.Search.route) {
                SpotsListScreen(spots, onAddClick = { context.startActivity(Intent(context, AddCampSpotActivity::class.java)) }) { spotId ->
                    navController.navigate("details/$spotId")
                }
            }

            composable(Screen.Saved.route) {
                SavedScreen(viewModel = viewModel) { spotId ->
                    navController.navigate("details/$spotId")
                }
            }

            composable(Screen.Discover.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ekran Odkrywaj (W budowie)", color = Color.Gray)
                }
            }

            composable(Screen.Profile.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Button(onClick = { context.startActivity(Intent(context, ProfileSettingsActivity::class.java)) },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)) { Text("Ustawienia Profilu") }
                }
            }

            composable("details/{spotId}") { backStackEntry ->
                val spotId = backStackEntry.arguments?.getString("spotId")?.toIntOrNull() ?: -1
                val spotMap by viewModel.getSpotWithAuthor(spotId).collectAsState(initial = emptyMap())
                val folders by viewModel.allFolders.collectAsState()
                val savedInFolderIds by viewModel.getFolderIdsForSpot(spotId).collectAsState(initial = emptyList())
                val images by viewModel.getImagesForSpot(spotId).collectAsState(initial = emptyList())
                val isSavedAtAll by viewModel.isSpotSaved(spotId).collectAsState(initial = false)
                val ratingsMap by viewModel.getRatingsWithAuthors(spotId).collectAsState(initial = emptyMap())
                val averageRating by viewModel.getAverageRatingForSpot(spotId).collectAsState(initial = 0f)

                val spot = spotMap.keys.firstOrNull()
                val author = spotMap.values.firstOrNull()

                CampSpotDetailsScreen(
                    spot = spot, author = author, images = images, folders = folders,
                    savedInFolderIds = savedInFolderIds, isSavedAtAll = isSavedAtAll,
                    ratings = ratingsMap, averageRating = averageRating ?: 0f,
                    isCreator = spot?.createdBy == application.sessionManager.getUserId(),
                    onBack = { navController.popBackStack() },
                    onDelete = { viewModel.deleteSpotById(spotId); navController.popBackStack() },
                    onEdit = {
                        val intent = Intent(context, AddCampSpotActivity::class.java).apply { putExtra("spot_id", spotId) }
                        context.startActivity(intent)
                    },
                    onAddRating = { rate, comment -> viewModel.addRating(spotId, application.sessionManager.getUserId(), rate, comment) },
                    onToggleFolder = { folderId, isChecked -> viewModel.toggleSpotInFolder(folderId, spotId, isChecked) }
                )
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
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onAddClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)) {
            Text("Dodaj nowe miejsce")
        }
        if (spots.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Brak miejsc. Dodaj pierwsze!") }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(spots) { item ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onSpotClick(item.spot.id) }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(item.spot.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(item.spot.locationName, style = MaterialTheme.typography.bodyMedium)
                            Text(item.spot.description, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                            Text(text = item.averageRating?.let { "Ocena: %.1f".format(it) } ?: "Brak ocen", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String?) {
    val items = listOf(Screen.Map, Screen.Search, Screen.Saved, Screen.Discover, Screen.Profile)

    var selectedTab by remember { mutableStateOf(Screen.Search.route) }

    if (currentRoute in items.map { it.route }) {
        selectedTab = currentRoute ?: Screen.Search.route
    }

    Surface(shadowElevation = 8.dp, color = Color.White) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            items.forEach { screen ->
                val isSelected = selectedTab == screen.route

                NavigationBarItem(
                    icon = { Icon(screen.icon, screen.title, modifier = Modifier.size(24.dp)) },
                    label = { Text(screen.title, fontSize = 11.sp) },
                    selected = isSelected,
                    alwaysShowLabel = true, // Możesz zmienić na false, jeśli 5 ikon z napisami nadal wydaje się zbyt gęsto
                    onClick = {
                        if (isSelected && currentRoute?.startsWith("details") == true) {
                            navController.popBackStack(screen.route, inclusive = false)
                        } else {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandOrange,
                        selectedTextColor = BrandOrange,
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}