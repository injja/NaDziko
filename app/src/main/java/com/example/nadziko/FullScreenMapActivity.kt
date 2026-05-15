package com.example.nadziko

import android.Manifest
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.nadziko.ui.theme.NaDzikoTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

class FullScreenMapActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialLat = intent.getDoubleExtra("latitude", 52.0)
        val initialLng = intent.getDoubleExtra("longitude", 19.0)
        val initialZoom = intent.getFloatExtra("zoom", 6f)

        val hasExistingLocation = intent.getBooleanExtra("has_existing_location", false)

        setContent {
            NaDzikoTheme {
                val context = LocalContext.current
                var latitude by remember { mutableDoubleStateOf(initialLat) }
                var longitude by remember { mutableDoubleStateOf(initialLng) }
                var zoom by remember { mutableFloatStateOf(initialZoom) }

                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), zoom)
                }


                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                            || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (granted) {
                        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                            .addOnSuccessListener { location ->
                                if (location != null) {
                                    latitude = location.latitude
                                    longitude = location.longitude
                                    zoom = 14f
                                    cameraPositionState.position =
                                        CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), zoom)
                                }
                            }
                    }
                }

                LaunchedEffect(Unit) {
                    if (!hasExistingLocation) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Wybierz lokalizację") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = "Wstecz")
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    val result = android.content.Intent().apply {
                                        putExtra("latitude", latitude)
                                        putExtra("longitude", longitude)
                                    }
                                    setResult(Activity.RESULT_OK, result)
                                    finish()
                                }) {
                                    Icon(Icons.Filled.Check, contentDescription = "Zatwierdź")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            onMapClick = { latLng ->
                                latitude = latLng.latitude
                                longitude = latLng.longitude
                            }
                        ) {
                            Marker(
                                state = MarkerState(position = LatLng(latitude, longitude)),
                                title = "Wybrana lokalizacja"
                            )
                        }
                    }
                }
            }
        }
    }
}