package com.example.swaasthya.presentation.screen.doctors

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.swaasthya.data.model.Doctor
import com.example.swaasthya.presentation.viewmodel.DoctorsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorsScreen(
    onNavigateBack: () -> Unit,
    onDoctorClick: (Doctor) -> Unit,
    onSignOut: () -> Unit,
    viewModel: DoctorsViewModel = hiltViewModel()
) {
    val doctorsState by viewModel.doctorsState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSpecialization by remember { mutableStateOf("") }
    
    // Load more when reaching the end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty()) {
                    val lastVisibleItem = visibleItems.last()
                    if (lastVisibleItem.index >= visibleItems.size - 3) {
                        viewModel.loadMoreDoctors()
                    }
                }
            }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Find Doctors") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { showSearchDialog = true }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
                IconButton(onClick = { viewModel.refreshDoctors() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
                IconButton(onClick = onSignOut) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                }
            }
        )
        
        // Content
        when (val state = doctorsState) {
            is com.example.swaasthya.presentation.viewmodel.DoctorsState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading doctors...")
                    }
                }
            }
            
            is com.example.swaasthya.presentation.viewmodel.DoctorsState.Success -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.response.doctors) { doctor ->
                        DoctorCard(
                            doctor = doctor,
                            onClick = { onDoctorClick(doctor) }
                        )
                    }
                    
                    if (state.response.pagination.has_next) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
            
            is com.example.swaasthya.presentation.viewmodel.DoctorsState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshDoctors() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
    
    // Search Dialog
    if (showSearchDialog) {
        SearchDialog(
            onDismiss = { showSearchDialog = false },
            onSearch = { query, specialization ->
                if (query.isNotEmpty() || specialization.isNotEmpty()) {
                    viewModel.searchDoctors(query, specialization)
                }
                showSearchDialog = false
            },
            searchQuery = searchQuery,
            onQueryChange = { searchQuery = it },
            selectedSpecialization = selectedSpecialization,
            onSpecializationChange = { selectedSpecialization = it }
        )
    }
}

@Composable
fun DoctorCard(
    doctor: Doctor,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doctor.user?.name ?: "Dr. Unknown",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = doctor.specialization,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Doctor",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Work,
                    contentDescription = "Experience",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${doctor.experience} years experience",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (doctor.phone.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = "Phone",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = doctor.phone,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDialog(
    onDismiss: () -> Unit,
    onSearch: (String, String) -> Unit,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    selectedSpecialization: String,
    onSpecializationChange: (String) -> Unit
) {
    val specializations = listOf(
        "Cardiology", "Dermatology", "Neurology", "Orthopedics", 
        "Pediatrics", "Psychiatry", "Radiology", "Surgery"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Search Doctors") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    label = { Text("Search by name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedSpecialization,
                        onValueChange = onSpecializationChange,
                        readOnly = true,
                        label = { Text("Specialization") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        specializations.forEach { specialization ->
                            DropdownMenuItem(
                                text = { Text(specialization) },
                                onClick = {
                                    onSpecializationChange(specialization)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSearch(searchQuery, selectedSpecialization) }) {
                Text("Search")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
