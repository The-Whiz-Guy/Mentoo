package com.example.model5

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init { checkAuthState() }

    private fun checkAuthState() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and Password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Login Failed")
                }
            }
    }

    fun signup(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and Password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Sign Up Failed")
                }
            }
    }
    fun signInWithGoogle(idToken: String) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Log Firebase authentication success
                    Log.d("Auth", "Firebase Authentication successful")

                    _authState.value = AuthState.Authenticated
                } else {
                    // Log the error if authentication fails
                    Log.e("Auth", "Firebase Authentication failed: ${task.exception?.message}")
                    _authState.value = AuthState.Error(task.exception?.message ?: "Sign In Failed")
                }
            }
    }

        private val _currentUserId = MutableLiveData<String?>()
        val currentUserId: LiveData<String?> get() = _currentUserId

        init {
            // Initialize the user ID (Replace this logic with your actual authentication service)
            _currentUserId.value = getCurrentFirebaseUserId()
        }

        fun getCurrentUserId(): String? {
            return _currentUserId.value
        }

        private fun getCurrentFirebaseUserId(): String? {
            // Logic to fetch the current user ID from Firebase (or other authentication service)
            val currentUser = FirebaseAuth.getInstance().currentUser
            return currentUser?.uid
        }



    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    sealed class AuthState {
        data object Authenticated : AuthState()
        data object Unauthenticated : AuthState()
        data object Loading : AuthState()
        data class Error(val message: String) : AuthState()
    }
}

















