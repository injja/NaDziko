package com.example.nadziko

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nadziko.data.CampSpot
import com.example.nadziko.ui.CampSpotViewModel
import com.example.nadziko.ui.CampSpotViewModelFactory
import com.example.nadziko.ui.theme.NaDzikoTheme

class AddCampSpotActivity : ComponentActivity() {

    private val viewModel: CampSpotViewModel by viewModels {
        CampSpotViewModelFactory(
            (application as NadzikoApplication).repository
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val spotId = intent.getIntExtra("spot_id", -1)
        val isEditMode = spotId != -1

        setContent {
            NaDzikoTheme {
                var existingSpot by remember { mutableStateOf<CampSpot?>(null) }
                var loaded by remember { mutableStateOf(!isEditMode) }

                LaunchedEffect(spotId) {
                    if (isEditMode) {
                        existingSpot = viewModel.getSpotById(spotId)
                    }
                    loaded = true
                }

                if (!loaded) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("Ładowanie...") },
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowBack,
                                            contentDescription = "Wstecz"
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
                                .padding(16.dp)
                        ) {
                            Text("Ładowanie danych...")
                        }
                    }
                } else {
                    AddCampSpotScreen(
                        isEditMode = isEditMode,
                        existingSpot = existingSpot,
                        onBack = { finish() },
                        onSave = { name, locationName, description, accessTips, packingTips ->
                            if (isEditMode && existingSpot != null) {
                                viewModel.updateSpot(
                                    existingSpot!!.copy(
                                        name = name,
                                        locationName = locationName,
                                        description = description,
                                        accessTips = accessTips,
                                        packingTips = packingTips
                                    )
                                )
                            } else {
                                viewModel.addSpot(
                                    name = name,
                                    locationName = locationName,
                                    description = description,
                                    accessTips = accessTips,
                                    packingTips = packingTips
                                )
                            }

                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCampSpotScreen(
    isEditMode: Boolean,
    existingSpot: CampSpot?,
    onBack: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var accessTips by remember { mutableStateOf("") }
    var packingTips by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }

    LaunchedEffect(existingSpot) {
        if (existingSpot != null) {
            name = existingSpot.name
            locationName = existingSpot.locationName
            description = existingSpot.description
            accessTips = existingSpot.accessTips
            packingTips = existingSpot.packingTips
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Edytuj miejsce" else "Dodaj miejsce")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Wstecz"
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Nazwa miejsca") },
                isError = nameError,
                modifier = Modifier.fillMaxWidth()
            )

            if (nameError) {
                Text("Podaj nazwę miejsca")
            }

            OutlinedTextField(
                value = locationName,
                onValueChange = {
                    locationName = it
                    locationError = false
                },
                label = { Text("Lokalizacja") },
                isError = locationError,
                modifier = Modifier.fillMaxWidth()
            )

            if (locationError) {
                Text("Podaj lokalizację")
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis miejsca") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = accessTips,
                onValueChange = { accessTips = it },
                label = { Text("Jak dotrzeć") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = packingTips,
                onValueChange = { packingTips = it },
                label = { Text("Co zabrać") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    nameError = name.isBlank()
                    locationError = locationName.isBlank()

                    if (!nameError && !locationError) {
                        onSave(name, locationName, description, accessTips, packingTips)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "Zapisz zmiany" else "Zapisz")
            }
        }
    }
}