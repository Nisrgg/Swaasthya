package com.example.swaasthya.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.swaasthya.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onNavigateToDoctors: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    val user = when (val state = authState) {
        is com.example.swaasthya.data.model.AuthState.Authenticated -> state.user
        else -> null
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏥 Swaasthya",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Your health companion is ready! 🎉",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                if (user != null) {
                    Text(
                        text = "Welcome, ${user.displayName ?: user.email}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Features Grid
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(getFeatureItems()) { feature ->
                FeatureCard(
                    title = feature.title,
                    description = feature.description,
                    icon = feature.icon,
                    onClick = { 
                        when (feature.title) {
                            "Find Doctors" -> onNavigateToDoctors()
                            "My Appointments" -> onNavigateToAppointments()
                            "Settings" -> onNavigateToProfile()
                            else -> { /* Handle other features */ }
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Sign Out Button
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out")
        }
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                onSignOut()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sign Out",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to sign out? You'll need to sign in again to access your account.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Sign Out")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class FeatureItem(
    val title: String,
    val description: String,
    val icon: ImageVector
)

fun getFeatureItems(): List<FeatureItem> {
    return listOf(
        FeatureItem(
            title = "Find Doctors",
            description = "Search and book appointments with specialists",
            icon = Icons.Default.Person
        ),
        FeatureItem(
            title = "My Appointments",
            description = "View and manage your upcoming appointments",
            icon = Icons.Default.Event
        ),
        FeatureItem(
            title = "Health Records",
            description = "Access your medical history and reports",
            icon = Icons.Default.Folder
        ),
        FeatureItem(
            title = "Emergency",
            description = "Quick access to emergency services",
            icon = Icons.Default.Emergency
        ),
        FeatureItem(
            title = "Chat Support",
            description = "Get help from our AI assistant",
            icon = Icons.Default.Chat
        ),
        FeatureItem(
            title = "Settings",
            description = "Manage your account and preferences",
            icon = Icons.Default.Settings
        )
    )
}
