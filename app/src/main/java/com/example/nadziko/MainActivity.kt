package com.example.nadziko

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nadziko.ui.CampSpotListItem
import com.example.nadziko.ui.CampSpotViewModel
import com.example.nadziko.ui.CampSpotViewModelFactory
import com.example.nadziko.ui.theme.NaDzikoTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CampSpotViewModel by viewModels {
        CampSpotViewModelFactory(
            (application as NadzikoApplication).repository,
            (application as NadzikoApplication).ratingRepository
        )
    }

    private val addSpotLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Flow will update automatically
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NaDzikoTheme {
                val spots by viewModel.allSpotsWithAuthorsAndRatings.collectAsState(
                    initial = emptyList()
                )

                MainScreen(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    spots: List<CampSpotListItem>,
    onAddClick: () -> Unit,
    onMapClick: () -> Unit,
    onSpotClick: (Int) -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Miejscówki biwakowe") },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profil"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dodaj nowe miejsce")
            }
            Button(
                onClick = onMapClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pokaż mapę miejscówek")
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

                                Text(
                                    text = spot.locationName,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = spot.description,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    text = averageRating?.let {
                                        "Ocena: %.1f".format(it)
                                    } ?: "Brak ocen",
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
}