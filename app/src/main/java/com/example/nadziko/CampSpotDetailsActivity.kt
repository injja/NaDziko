package com.example.nadziko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nadziko.data.CampSpot
import com.example.nadziko.ui.CampSpotViewModel
import com.example.nadziko.ui.CampSpotViewModelFactory
import com.example.nadziko.ui.theme.NaDzikoTheme

class CampSpotDetailsActivity : ComponentActivity() {

    private val viewModel: CampSpotViewModel by viewModels {
        CampSpotViewModelFactory(
            (application as NadzikoApplication).repository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val spotId = intent.getIntExtra("spot_id", -1)

        setContent {
            NaDzikoTheme {
                var spot by remember { mutableStateOf<CampSpot?>(null) }
                var loaded by remember { mutableStateOf(false) }

                LaunchedEffect(spotId) {
                    spot = viewModel.getSpotById(spotId)
                    loaded = true
                }

                CampSpotDetailsScreen(
                    title = spot?.name ?: "Szczegóły miejsca",
                    locationName = spot?.locationName ?: "",
                    description = spot?.description ?: "",
                    accessTips = spot?.accessTips ?: "",
                    packingTips = spot?.packingTips ?: "",
                    rating = spot?.rating ?: 0f,
                    notFound = loaded && spot == null,
                    onBack = { finish() },
                    onDelete = {
                        viewModel.deleteSpotById(spotId)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampSpotDetailsScreen(
    title: String,
    locationName: String,
    description: String,
    accessTips: String,
    packingTips: String,
    rating: Float,
    notFound: Boolean,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń miejsce") },
            text = { Text("Czy na pewno chcesz usunąć to miejsce?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Anuluj")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Wstecz"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Usuń"
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
            if (notFound) {
                Text("Nie znaleziono miejsca.")
            } else {
                Text("Lokalizacja: $locationName")
                Text("Opis: $description")
                Text("Jak dotrzeć: $accessTips")
                Text("Co zabrać: $packingTips")
                Text("Ocena: %.1f".format(rating))
            }
        }
    }
}