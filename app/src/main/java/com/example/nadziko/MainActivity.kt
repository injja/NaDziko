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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nadziko.data.CampSpot
import com.example.nadziko.ui.CampSpotViewModel
import com.example.nadziko.ui.CampSpotViewModelFactory
import com.example.nadziko.ui.theme.NaDzikoTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CampSpotViewModel by viewModels {
        CampSpotViewModelFactory(
            (application as NadzikoApplication).repository
        )
    }

    private val addSpotLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Nic nie trzeba ręcznie odświeżać, Flow zrobi to sam
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NaDzikoTheme {
                val spots by viewModel.allSpots.collectAsState(initial = emptyList())

                MainScreen(
                    spots = spots,
                    onAddClick = {
                        val intent = Intent(this, AddCampSpotActivity::class.java)
                        addSpotLauncher.launch(intent)
                    },
                    onSpotClick = { spotId ->
                        val intent = Intent(this, CampSpotDetailsActivity::class.java)
                        intent.putExtra("spot_id", spotId)
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
    spots: List<CampSpot>,
    onAddClick: () -> Unit,
    onSpotClick: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Miejscówki biwakowe") }
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

            if (spots.isEmpty()) {
                Text("Brak zapisanych miejsc. Dodaj pierwsze miejsce.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(spots) { spot ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSpotClick(spot.id) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = spot.name,
                                    style = MaterialTheme.typography.titleMedium
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
                                    text = "Ocena: %.1f".format(spot.rating),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}