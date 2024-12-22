package com.example.model5.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.model5.AuthViewModel
import com.example.model5.R
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.model5.getStoredRole

@Composable
fun SplashScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current


    LaunchedEffect(Unit) {
        delay(1000)
        val role = getStoredRole(context, authViewModel.getCurrentUserId())
        val destination = when (role) {
            "mentor" -> "mentor_home"
            "mentee" -> "mentee_home"
            else -> "auth"
        }
        navController.navigate(destination) {
            popUpTo("splash") { inclusive = true }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_icon),
            contentDescription = "Splash Image",
            modifier = Modifier.fillMaxSize().background(Color.Transparent),
            contentScale = ContentScale.Crop
        )
    }
}
