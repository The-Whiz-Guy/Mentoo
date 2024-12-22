package com.example.model5

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.model5.ui.theme.Model5Theme
import android.view.WindowInsetsController
import android.view.WindowInsets
import androidx.annotation.RequiresApi
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val mentorViewModel: MentorViewModel by viewModels()
    private val menteeViewModel: MenteeViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        window.decorView.setBackgroundColor(getColor(R.color.teal_200)) // Set your background color

        // Set the activity to full screen
        window.decorView.windowInsetsController?.let { controller ->
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            Model5Theme {
                // Collect state within the composable function
                val mentorDetails by mentorViewModel.mentorDetails.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        mentorViewModel = mentorViewModel,
                        menteeViewModel = menteeViewModel,
                        mentorDetails = mentorDetails,)
                }
            }
        }
    }
}
