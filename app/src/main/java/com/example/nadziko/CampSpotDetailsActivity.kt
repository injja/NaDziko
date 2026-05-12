package com.example.nadziko

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.nadziko.data.CampSpot
import com.example.nadziko.data.Folder
import com.example.nadziko.data.Rating
import com.example.nadziko.data.SpotImage
import com.example.nadziko.data.User
import com.example.nadziko.ui.CampSpotViewModel
import com.example.nadziko.ui.CampSpotViewModelFactory
import com.example.nadziko.ui.theme.NaDzikoTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

private val DarkGreen = Color(0xFF1E3524)
private val BrandOrange = Color(0xFFD66A27)
private val LightBg = Color(0xFFF6F6F6)

class CampSpotDetailsActivity : ComponentActivity() {
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

        val sessionManager = (application as NadzikoApplication).sessionManager
        val currentUserId = sessionManager.getUserId()
        val spotId = intent.getIntExtra("spot_id", -1)

        setContent {
            NaDzikoTheme {
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
                    spot = spot,
                    author = author,
                    images = images,
                    folders = folders,
                    savedInFolderIds = savedInFolderIds,
                    isSavedAtAll = isSavedAtAll,
                    ratings = ratingsMap,
                    averageRating = averageRating ?: 0f,
                    isCreator = spot?.createdBy == currentUserId,
                    onBack = { finish() },
                    onDelete = {
                        viewModel.deleteSpotById(spotId)
                        finish()
                    },
                    onEdit = {
                        val intent = Intent(this, AddCampSpotActivity::class.java)
                        intent.putExtra("spot_id", spotId)
                        startActivity(intent)
                    },
                    onAddRating = { rate, comment ->
                        viewModel.addRating(spotId, currentUserId, rate, comment)
                    },
                    onToggleFolder = { folderId, isChecked ->
                        viewModel.toggleSpotInFolder(folderId, spotId, isChecked)
                    }
                )
            }
        }
    }
}

@Composable
fun CampSpotDetailsScreen(
    spot: CampSpot?,
    author: User?,
    images: List<SpotImage>,
    folders: List<Folder>,
    savedInFolderIds: List<Int>,
    isSavedAtAll: Boolean,
    ratings: Map<Rating, User>,
    averageRating: Float,
    isCreator: Boolean,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onAddRating: (Int, String) -> Unit,
    onToggleFolder: (Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    var showFolderDialog by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddRatingDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń miejsce") },
            text = { Text("Czy na pewno chcesz usunąć to miejsce?") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) { Text("Usuń", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Anuluj", color = DarkGreen) }
            }
        )
    }

    if (showAddRatingDialog) {
        AddRatingDialog(
            onDismiss = { showAddRatingDialog = false },
            onConfirm = { rate, comment -> onAddRating(rate, comment); showAddRatingDialog = false }
        )
    }

    Scaffold { padding ->
        if (spot != null) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                // KOLAŻ Z PRZYCISKAMI
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    Row(Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(2f).fillMaxHeight().background(Color(0xFF4A6553)).clickable { selectedImageIndex = 0 }) {
                            if (images.isNotEmpty()) AsyncImage(model = images[0].imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else Icon(Icons.Default.Image, null, Modifier.align(Alignment.Center), Color.White.copy(0.5f))
                        }
                        Column(Modifier.weight(1f).fillMaxHeight()) {
                            Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF5C7965)).clickable { selectedImageIndex = 1 }) {
                                if (images.size > 1) AsyncImage(model = images[1].imageUri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                else Icon(Icons.Default.Image, null, Modifier.align(Alignment.Center), Color.White.copy(0.3f))
                            }
                            Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF6E8C77)).clickable { selectedImageIndex = 2 }) {
                                if (images.size > 2) AsyncImage(model = images[2].imageUri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                else Icon(Icons.Default.Image, null, Modifier.align(Alignment.Center), Color.White.copy(0.3f))
                            }
                        }
                    }

                    // Górne przyciski
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)) {
                            Icon(Icons.Filled.KeyboardArrowLeft, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        if (isCreator) {
                            Row {
                                IconButton(onClick = onEdit, modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)) {
                                    Icon(Icons.Filled.Edit, null, tint = Color.White)
                                }
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)) {
                                    Icon(Icons.Filled.Delete, null, tint = Color.White)
                                }
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth().offset(y = (-30).dp),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Text(spot.name, fontSize = 28.sp, fontWeight = FontWeight.Bold)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, null, tint = BrandOrange, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(text = "%.1f".format(averageRating), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = " • Dodane przez: ${author?.username ?: "Nieznany"}", fontSize = 14.sp, color = Color.Gray)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AmenityIcon(Icons.Outlined.WaterDrop, "Woda", spot.hasWaterAccess)
                            AmenityIcon(Icons.Outlined.LocalFireDepartment, "Ogień", spot.allowsFire)
                            AmenityIcon(Icons.Outlined.SignalCellularOff, "Brak LTE", !spot.hasLteCoverage)
                            AmenityIcon(Icons.Outlined.DirectionsCar, "4x4", spot.requires4x4)
                        }

                        Text("Opis: ${spot.description}", fontSize = 14.sp, color = Color.DarkGray)
                        Text("Jak dotrzeć: ${spot.accessTips}", fontSize = 14.sp, color = Color.DarkGray)
                        Text("Co zabrać: ${spot.packingTips}", fontSize = 14.sp, color = Color.DarkGray)

                        // Mapa z Intentem
                        val spotLatLng = LatLng(spot.latitude, spot.longitude)
                        Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).clickable {
                            val uri = Uri.parse("geo:${spot.latitude},${spot.longitude}?q=${spot.latitude},${spot.longitude}(${spot.name})")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") })
                        }) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(spotLatLng, 15f) },
                                uiSettings = MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = false)
                            ) { Marker(state = MarkerState(position = spotLatLng)) }
                        }

                        Button(
                            onClick = { showFolderDialog = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSavedAtAll) Color.LightGray else BrandOrange),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(if (isSavedAtAll) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null, tint = if (isSavedAtAll) BrandOrange else Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(if (isSavedAtAll) "Zarządzaj ulubionymi" else "Zapisz w ulubionych", fontWeight = FontWeight.Bold, color = if (isSavedAtAll) Color.Black else Color.White)
                        }

                        Divider(color = LightBg, thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))

                        // SEKCJA OCEN I OPINII
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Opinie (${ratings.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { showAddRatingDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                            ) {
                                Text("Dodaj opinię")
                            }
                        }

                        ratings.forEach { (rating, user) ->
                            RatingItem(rating, user)
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }

    if (showFolderDialog) {
        SelectFolderCheckboxesDialog(
            folders = folders,
            savedInFolderIds = savedInFolderIds,
            onDismiss = { showFolderDialog = false },
            onToggleFolder = onToggleFolder
        )
    }

    selectedImageIndex?.let { index ->
        PhotoGalleryViewer(images = images, initialIndex = index, onDismiss = { selectedImageIndex = null })
    }
}

@Composable
fun SelectFolderCheckboxesDialog(folders: List<Folder>, savedInFolderIds: List<Int>, onDismiss: () -> Unit, onToggleFolder: (Int, Boolean) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zapisz w folderach", fontWeight = FontWeight.Bold) },
        text = {
            if (folders.isEmpty()) Text("Najpierw utwórz folder w zakładce 'Zapisane'.")
            else LazyColumn {
                items(folders) { folder ->
                    val isChecked = savedInFolderIds.contains(folder.id)
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onToggleFolder(folder.id, !isChecked) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isChecked, onCheckedChange = { onToggleFolder(folder.id, it) }, colors = CheckboxDefaults.colors(checkedColor = BrandOrange))
                        Text(folder.name, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Gotowe", color = DarkGreen) } }
    )
}

@Composable
fun PhotoGalleryViewer(images: List<SpotImage>, initialIndex: Int, onDismiss: () -> Unit) {
    if (images.isEmpty() || initialIndex >= images.size) { onDismiss(); return }
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { images.size })
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(model = images[page].imageUri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 16.dp).background(Color.DarkGray.copy(0.5f), CircleShape)) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun AmenityIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = if (isActive) BrandOrange else Color.LightGray, modifier = Modifier.size(28.dp))
        Text(label, fontSize = 10.sp, color = if (isActive) Color.Black else Color.Gray)
    }
}

@Composable
fun RatingItem(rating: Rating, user: User) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = LightBg)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = user.username, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < rating.rate) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (index < rating.rate) Color(0xFFFFB300) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            if (rating.comment.isNotEmpty()) {
                Text(text = rating.comment, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp), color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun AddRatingDialog(onDismiss: () -> Unit, onConfirm: (Int, String) -> Unit) {
    var rate by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj opinię") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Twoja ocena:")
                Row {
                    repeat(5) { index ->
                        IconButton(onClick = { rate = index + 1 }) {
                            Icon(
                                imageVector = if (index < rate) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = null,
                                tint = if (index < rate) Color(0xFFFFB300) else Color.Gray
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Komentarz (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { Button(onClick = { onConfirm(rate, comment) }, colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)) { Text("Zapisz") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj", color = DarkGreen) } }
    )
}