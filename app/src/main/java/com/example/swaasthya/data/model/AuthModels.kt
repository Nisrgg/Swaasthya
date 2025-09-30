package com.example.swaasthya.data.model

data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
)

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

data class AuthResult(
    val isSuccess: Boolean,
    val user: User? = null,
    val errorMessage: String? = null
)
