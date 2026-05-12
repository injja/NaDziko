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
import androidx.compose.foundation.layout.height
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
class AddCampSpotActivity : ComponentActivity() {

    private val viewModel: CampSpotViewModel by viewModels {
        CampSpotViewModelFactory(
            (application as NadzikoApplication).repository,
            (application as NadzikoApplication).ratingRepository
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = (application as NadzikoApplication).sessionManager
        val userId = sessionManager.getUserId()

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
                        onSave = { name, locationName, description, accessTips, packingTips, lat, lng ->
                            if (isEditMode && existingSpot != null) {
                                viewModel.updateSpot(
                                    existingSpot!!.copy(
                                        name = name,
                                        locationName = locationName,
                                        description = description,
                                        accessTips = accessTips,
                                        packingTips = packingTips,
                                        latitude = lat,
                                        longitude = lng
                                    )
                                )
                            } else {
                                viewModel.addSpot(
                                    name = name,
                                    locationName = locationName,
                                    description = description,
                                    accessTips = accessTips,
                                    packingTips = packingTips,
                                    createdBy = userId,
                                    latitude = lat,
                                    longitude = lng
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
    onSave: (String, String, String, String, String, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var accessTips by remember { mutableStateOf("") }
    var packingTips by remember { mutableStateOf("") }
    
    // Default location (central Poland)
    var latitude by remember { mutableDoubleStateOf(52.0) }
    var longitude by remember { mutableDoubleStateOf(19.0) }

    var nameError by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), 6f)
    }

    LaunchedEffect(existingSpot) {
        if (existingSpot != null) {
            name = existingSpot.name
            locationName = existingSpot.locationName
            description = existingSpot.description
            accessTips = existingSpot.accessTips
            packingTips = existingSpot.packingTips
            latitude = existingSpot.latitude
            longitude = existingSpot.longitude
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), 13f)
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
                label = { Text("Lokalizacja (region/miasto)") },
                isError = locationError,
                modifier = Modifier.fillMaxWidth()
            )

            if (locationError) {
                Text("Podaj lokalizację")
            }

            val context = LocalContext.current

            val mapLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.let { data ->
                        latitude = data.getDoubleExtra("latitude", latitude)
                        longitude = data.getDoubleExtra("longitude", longitude)
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    val intent = Intent(context, FullScreenMapActivity::class.java).apply { 
                        putExtra("latitude", latitude)
                        putExtra("longitude", longitude)
                        putExtra("zoom", if (existingSpot != null) 13f else 6f)
                    }
                    mapLauncher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (latitude == 52.0 && longitude == 19.0 && existingSpot == null)
                        "Wybierz lokalizację na mapie"
                    else
                        "Lokalizacja: %.4f, %.4f".format(latitude, longitude)
                )
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
                        onSave(name, locationName, description, accessTips, packingTips, latitude, longitude)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "Zapisz zmiany" else "Zapisz")
            }
        }
    }
}
