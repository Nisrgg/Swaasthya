package com.example.swaasthya

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.swaasthya.data.model.AuthState
import com.example.swaasthya.presentation.screen.HomeScreen
import com.example.swaasthya.presentation.screen.auth.LoginScreen
import com.example.swaasthya.presentation.screen.auth.SignUpScreen
import com.example.swaasthya.presentation.screen.doctors.DoctorsScreen
import com.example.swaasthya.presentation.viewmodel.AuthViewModel
import com.example.swaasthya.ui.theme.SwaasthyaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private var googleSignInResult by mutableStateOf<Intent?>(null)
    
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        googleSignInResult = result.data
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SwaasthyaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SwaasthyaApp(
                        modifier = Modifier.padding(innerPadding),
                        onGoogleSignIn = { intent ->
                            googleSignInLauncher.launch(intent)
                        },
                        googleSignInResult = googleSignInResult,
                        onGoogleSignInResultConsumed = {
                            googleSignInResult = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SwaasthyaApp(
    modifier: Modifier = Modifier,
    onGoogleSignIn: (Intent) -> Unit,
    googleSignInResult: Intent?,
    onGoogleSignInResultConsumed: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    
    // Handle Google Sign-In result
    LaunchedEffect(googleSignInResult) {
        googleSignInResult?.let { result ->
            viewModel.handleGoogleSignInResult(result)
            onGoogleSignInResultConsumed()
        }
    }
    
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is AuthState.Unauthenticated -> {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
            else -> { /* Loading state */ }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate("home")
                },
                onNavigateToSignUp = {
                    navController.navigate("signup")
                },
                onGoogleSignIn = onGoogleSignIn
            )
        }
        
        composable("signup") {
            SignUpScreen(
                onNavigateToHome = {
                    navController.navigate("home")
                },
                onNavigateToLogin = {
                    navController.navigate("login")
                },
                onGoogleSignIn = onGoogleSignIn
            )
        }
        
        composable("home") {
            HomeScreen(
                onSignOut = {
                    viewModel.signOut()
                },
                onNavigateToDoctors = {
                    navController.navigate("doctors")
                },
                onNavigateToAppointments = {
                    // TODO: Implement appointments screen
                },
                onNavigateToProfile = {
                    // TODO: Implement profile screen
                }
            )
        }
        
        composable("doctors") {
            DoctorsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onDoctorClick = { doctor ->
                    // TODO: Implement doctor details screen
                },
                onSignOut = {
                    viewModel.signOut()
                }
            )
        }
    }
}