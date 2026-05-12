package com.example.nadziko

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nadziko.data.CampSpot
import com.example.nadziko.data.Rating
import com.example.nadziko.data.User
import com.example.nadziko.ui.CampSpotViewModel
import com.example.nadziko.ui.CampSpotViewModelFactory
import com.example.nadziko.ui.theme.NaDzikoTheme

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
                val ratingsMap by viewModel.getRatingsWithAuthors(spotId).collectAsState(initial = emptyMap())
                val averageRating by viewModel.getAverageRatingForSpot(spotId).collectAsState(initial = 0f)
                
                val spot = spotMap.keys.firstOrNull()
                val author = spotMap.values.firstOrNull()

                CampSpotDetailsScreen(
                    spot = spot,
                    author = author,
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
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampSpotDetailsScreen(
    spot: CampSpot?,
    author: User?,
    ratings: Map<Rating, User>,
    averageRating: Float,
    isCreator: Boolean,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onAddRating: (Int, String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddRatingDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń miejsce") },
            text = { Text("Czy na pewno chcesz usunąć to miejsce?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) { Text("Usuń") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Anuluj") }
            }
        )
    }

    if (showAddRatingDialog) {
        AddRatingDialog(
            onDismiss = { showAddRatingDialog = false },
            onConfirm = { rate, comment ->
                onAddRating(rate, comment)
                showAddRatingDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(spot?.name ?: "Szczegóły") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Wstecz")
                    }
                },
                actions = {
                    if (isCreator) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, "Usuń")
                        }
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, "Edytuj")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (spot != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Dodane przez: ${author?.username ?: "Nieznany"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Lokalizacja: ${spot.locationName}", style = MaterialTheme.typography.titleMedium)
                    Text("Opis: ${spot.description}")
                    Text("Jak dotrzeć: ${spot.accessTips}")
                    Text("Co zabrać: ${spot.packingTips}")
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Średnia ocena: %.1f ".format(averageRating), style = MaterialTheme.typography.titleLarge)
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFB300))
                    }

                    Divider()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Opinie (${ratings.size})", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = { showAddRatingDialog = true }) {
                            Text("Dodaj opinię")
                        }
                    }

                    ratings.forEach { (rating, user) ->
                        RatingItem(rating, user)
                    }
                }
            }
        }
    }
}

@Composable
fun RatingItem(rating: Rating, user: User) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < rating.rate) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (index < rating.rate) Color(0xFFFFB300) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            if (rating.comment.isNotEmpty()) {
                Text(
                    text = rating.comment,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
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
                                imageVector = if (index < rate) Icons.Filled.Star else Icons.Outlined.Star,
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
        confirmButton = {
            Button(onClick = { onConfirm(rate, comment) }) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}
