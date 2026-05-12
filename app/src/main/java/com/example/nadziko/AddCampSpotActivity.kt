package com.example.nadziko

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nadziko.data.CampSpot
import com.example.nadziko.ui.CampSpotViewModel
import com.example.nadziko.ui.CampSpotViewModelFactory
import com.example.nadziko.ui.theme.NaDzikoTheme

class AddCampSpotActivity : ComponentActivity() {

    private val viewModel: CampSpotViewModel by viewModels {
        CampSpotViewModelFactory(
            (application as NadzikoApplication).repository,
            (application as NadzikoApplication).ratingRepository,
            (application as NadzikoApplication).spotImageRepository
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
                        onSave = { name, locationName, description, accessTips, packingTips, lat, lng, imageUris ->
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
                                    longitude = lng,
                                    imageUris = imageUris
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
    onSave: (String, String, String, String, String, Double, Double, List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var accessTips by remember { mutableStateOf("") }
    var packingTips by remember { mutableStateOf("") }
    
    var latitude by remember { mutableDoubleStateOf(52.0) }
    var longitude by remember { mutableDoubleStateOf(19.0) }

    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var nameError by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }

    LaunchedEffect(existingSpot) {
        if (existingSpot != null) {
            name = existingSpot.name
            locationName = existingSpot.locationName
            description = existingSpot.description
            accessTips = existingSpot.accessTips
            packingTips = existingSpot.packingTips
            latitude = existingSpot.latitude
            longitude = existingSpot.longitude
        }
    }

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            uris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            selectedImageUris = selectedImageUris + uris
        }
    )

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
                Text("Podaj nazwę miejsca", color = MaterialTheme.colorScheme.error)
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
                Text("Podaj lokalizację", color = MaterialTheme.colorScheme.error)
            }

            OutlinedButton(
                onClick = {
                    val intent = Intent(context, FullScreenMapActivity::class.java).apply { 
                        putExtra("latitude", latitude)
                        putExtra("longitude", longitude)
                        putExtra("zoom", if (existingSpot != null) 13f else 6f)
                        putExtra("has_existing_location", existingSpot != null || (latitude != 52.0 || longitude != 19.0))
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

            Text("Zdjęcia:", style = MaterialTheme.typography.titleSmall)
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().height(100.dp)
            ) {
                items(selectedImageUris) { uri ->
                    Card(
                        modifier = Modifier.size(100.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                item {
                    OutlinedIconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.size(100.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "Dodaj zdjęcie")
                    }
                }
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
                        onSave(
                            name, 
                            locationName, 
                            description, 
                            accessTips, 
                            packingTips, 
                            latitude, 
                            longitude, 
                            selectedImageUris.map { it.toString() }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "Zapisz zmiany" else "Zapisz")
            }
        }
    }
}
