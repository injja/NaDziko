package com.example.nadziko

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.nadziko.ui.theme.NaDzikoTheme
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

        setContent {
            NaDzikoTheme {
                var latitude by remember { mutableDoubleStateOf(initialLat) }
                var longitude by remember { mutableDoubleStateOf(initialLng) }

                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), initialZoom)
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