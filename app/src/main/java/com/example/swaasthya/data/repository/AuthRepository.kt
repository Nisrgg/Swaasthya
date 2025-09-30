package com.example.swaasthya.data.repository

import android.content.Context
import com.example.swaasthya.data.model.AuthResult
import com.example.swaasthya.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val context: Context,
    private val googleWebClientId: String
) {

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(googleWebClientId) // Injected from ConfigModule
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
    
    fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.let { firebaseUser ->
            User(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString()
            )
        }
    }
    
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user?.let { firebaseUser ->
                User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString()
                )
            }
            AuthResult(isSuccess = true, user = user)
        } catch (e: Exception) {
            AuthResult(isSuccess = false, errorMessage = e.message)
        }
    }
    
    suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user?.let { firebaseUser ->
                User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString()
                )
            }
            AuthResult(isSuccess = true, user = user)
        } catch (e: Exception) {
            AuthResult(isSuccess = false, errorMessage = e.message)
        }
    }
    
    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user?.let { firebaseUser ->
                User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString()
                )
            }
            AuthResult(isSuccess = true, user = user)
        } catch (e: Exception) {
            AuthResult(isSuccess = false, errorMessage = e.message)
        }
    }
    
    fun getGoogleSignInIntent() = googleSignInClient.signInIntent
    
    suspend fun handleGoogleSignInResult(data: android.content.Intent?): AuthResult {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                signInWithGoogle(idToken)
            } ?: AuthResult(isSuccess = false, errorMessage = "Google sign-in failed")
        } catch (e: ApiException) {
            AuthResult(isSuccess = false, errorMessage = "Google sign-in failed: ${e.message}")
        }
    }
    
    suspend fun signOut(): AuthResult {
        return try {
            firebaseAuth.signOut()
            googleSignInClient.signOut().await()
            AuthResult(isSuccess = true)
        } catch (e: Exception) {
            AuthResult(isSuccess = false, errorMessage = e.message)
        }
    }
}
