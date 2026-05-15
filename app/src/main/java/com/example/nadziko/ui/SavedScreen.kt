package com.example.nadziko.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nadziko.data.CampSpot

val DarkGreen = Color(0xFF1E3524)
val BrandOrange = Color(0xFFD66A27)
val LightBg = Color(0xFFF6F6F6)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    viewModel: CampSpotViewModel,
    onSpotClick: (Int) -> Unit
) {
    val folders by viewModel.allFolders.collectAsState()
    val spots by viewModel.spotsInSelectedFolder.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(folders) {
        if (folders.isNotEmpty() && selectedTabIndex < folders.size) {
            viewModel.selectFolder(folders[selectedTabIndex].id)
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(DarkGreen)) {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DarkGreen,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = BrandOrange
                    ),
                    title = { Text("Zapisane", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { /* Powrót */ }) {
                            Icon(Icons.Default.KeyboardArrowLeft, null, modifier = Modifier.size(32.dp))
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) { Icon(Icons.Default.Star, null) }
                    }
                )

                Text(
                    text = "Moje foldery:",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Medium
                )

                if (folders.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = DarkGreen,
                        contentColor = Color.White,
                        edgePadding = 16.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = BrandOrange,
                                height = 3.dp
                            )
                        },
                        divider = {}
                    ) {
                        folders.forEachIndexed { index, folder ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = {
                                    selectedTabIndex = index
                                    viewModel.selectFolder(folder.id)
                                },
                                text = {
                                    Text(
                                        folder.name,
                                        color = if (selectedTabIndex == index) BrandOrange else Color.LightGray,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(50)
            ) {
                Text("Nowy Folder", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Add, null)
            }
        }
    ) { paddingValues ->
        if (showAddDialog) {
            AddFolderDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name ->
                    if (name.isNotBlank()) viewModel.addFolder(name)
                    showAddDialog = false
                }
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().background(LightBg).padding(paddingValues)
        ) {
            items(spots) { spot ->
                SavedSpotCard(
                    spot = spot,
                    viewModel = viewModel,
                    onClick = { onSpotClick(spot.id) }
                )
            }
        }
    }
}

@Composable
fun SavedSpotCard(
    spot: CampSpot,
    viewModel: CampSpotViewModel,
    onClick: () -> Unit
) {
    val images by viewModel.getImagesForSpot(spot.id).collectAsState(initial = emptyList())

    val averageRating by viewModel.getAverageRatingForSpot(spot.id).collectAsState(initial = null)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp).background(Color(0xFFE0E0E0))) {
                if (images.isNotEmpty()) {
                    AsyncImage(
                        model = images.first().imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, null, modifier = Modifier.align(Alignment.Center), tint = Color.Gray)
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                Text(spot.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black, maxLines = 1)
                Text(spot.locationName, fontSize = 12.sp, color = Color.Gray, maxLines = 1)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp).fillMaxWidth()
                ) {
                    Icon(Icons.Default.Star, null, tint = BrandOrange, modifier = Modifier.size(14.dp))

                    Text(
                        text = " " + (averageRating?.let { String.format("%.1f", it) } ?: "brak"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(4.dp), color = LightBg) {
                        Icon(Icons.Default.Place, null, tint = DarkGreen, modifier = Modifier.padding(4.dp).size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddFolderDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowy folder") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nazwa folderu") },
                singleLine = true
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(name) }) { Text("Dodaj") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}