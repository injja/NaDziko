package com.example.nadziko

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.nadziko.data.CampSpot
import com.example.nadziko.ui.CampSpotViewModel
import com.example.nadziko.ui.CampSpotViewModelFactory
import com.example.nadziko.ui.theme.NaDzikoTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

class MapActivity : ComponentActivity() {

    private val viewModel: CampSpotViewModel by viewModels {
        CampSpotViewModelFactory(
            (application as NadzikoApplication).repository,
            (application as NadzikoApplication).ratingRepository,
            (application as NadzikoApplication).spotImageRepository
        )
    }

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private var userLocation by mutableStateOf<LatLng?>(null)

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineGranted || coarseGranted) {
                loadUserLocation()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MapsInitializer.initialize(applicationContext)

        val initialSpotId = intent.getIntExtra("spot_id", -1)

        requestLocationPermissionIfNeeded()

        setContent {
            NaDzikoTheme {
                val spotsMap by viewModel.allSpotsWithAuthors.collectAsState(initial = emptyMap())
                val spots = spotsMap.keys.toList().filter { it.latitude != 0.0 && it.longitude != 0.0 }

                MapScreen(
                    spots = spots,
                    userLocation = userLocation,
                    initialSpotId = initialSpotId,
                    viewModel = viewModel,
                    onBackClick = { finish() },
                    onSpotClick = { spotId ->
                        val intent = Intent(this, CampSpotDetailsActivity::class.java)
                        intent.putExtra("spot_id", spotId)
                        startActivity(intent)
                    }
                )
            }
        }
    }

    private fun requestLocationPermissionIfNeeded() {
        val fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            loadUserLocation()
        } else {
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private fun loadUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                userLocation = LatLng(location.latitude, location.longitude)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    spots: List<CampSpot>,
    userLocation: LatLng?,
    initialSpotId: Int,
    viewModel: CampSpotViewModel,
    onBackClick: () -> Unit,
    onSpotClick: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val fallbackLocation = LatLng(52.2297, 21.0122)
    val defaultLocation = userLocation ?: fallbackLocation

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    val pagerState = rememberPagerState(pageCount = { spots.size })


    LaunchedEffect(spots) {
        if (spots.isNotEmpty() && initialSpotId != -1) {
            val index = spots.indexOfFirst { it.id == initialSpotId }
            if (index != -1) {
                pagerState.scrollToPage(index)

                val initialSpot = spots[index]
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(initialSpot.latitude, initialSpot.longitude), 14f
                )
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (spots.isNotEmpty()) {
            val selectedSpot = spots[pagerState.currentPage]
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(selectedSpot.latitude, selectedSpot.longitude),
                    14f
                ),
                durationMs = 600
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa miejscówek") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                userLocation?.let {
                    Marker(state = MarkerState(position = it), title = "Twoja lokalizacja")
                }

                spots.forEachIndexed { index, spot ->
                    Marker(
                        state = MarkerState(position = LatLng(spot.latitude, spot.longitude)),
                        title = spot.name,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                            false
                        }
                    )
                }
            }

            if (spots.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    pageSpacing = 16.dp
                ) { page ->
                    val spot = spots[page]
                    SpotMapCard(
                        spot = spot,
                        viewModel = viewModel,
                        onClick = { onSpotClick(spot.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SpotMapCard(spot: CampSpot, viewModel: CampSpotViewModel, onClick: () -> Unit) {
    val images by viewModel.getImagesForSpot(spot.id).collectAsState(initial = emptyList())
    val firstImageUri = images.firstOrNull()?.imageUri

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Color(0xFFEEEEEE))
            ) {
                if (firstImageUri != null) {
                    AsyncImage(
                        model = firstImageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(32.dp),
                        tint = Color.LightGray
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = spot.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = spot.locationName,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kliknij, aby otworzyć",
                    fontSize = 11.sp,
                    color = Color(0xFFD66A27),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}