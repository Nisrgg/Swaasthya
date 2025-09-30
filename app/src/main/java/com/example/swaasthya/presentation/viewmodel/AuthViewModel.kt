package com.example.swaasthya.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swaasthya.data.model.AuthResult
import com.example.swaasthya.data.model.AuthState
import com.example.swaasthya.data.model.User
import com.example.swaasthya.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            _authState.value = if (currentUser != null) {
                AuthState.Authenticated(currentUser)
            } else {
                AuthState.Unauthenticated
            }
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signInWithEmailAndPassword(email, password)
            _authState.value = if (result.isSuccess) {
                AuthState.Authenticated(result.user!!)
            } else {
                AuthState.Error(result.errorMessage ?: "Sign in failed")
            }
        }
    }
    
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.createUserWithEmailAndPassword(email, password)
            _authState.value = if (result.isSuccess) {
                AuthState.Authenticated(result.user!!)
            } else {
                AuthState.Error(result.errorMessage ?: "Sign up failed")
            }
        }
    }
    
    fun getGoogleSignInIntent(): Intent {
        return authRepository.getGoogleSignInIntent()
    }
    
    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.handleGoogleSignInResult(data)
            _authState.value = if (result.isSuccess) {
                AuthState.Authenticated(result.user!!)
            } else {
                AuthState.Error(result.errorMessage ?: "Google sign-in failed")
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }
}
