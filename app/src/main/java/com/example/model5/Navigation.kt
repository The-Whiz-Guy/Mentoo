package com.example.model5

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.model5.pages.AuthPage
import com.example.model5.pages.ChangeProfile
import com.example.model5.pages.EditProfile
import com.example.model5.pages.MenteePage
import com.example.model5.pages.MentorPage
import com.example.model5.pages.MyMentorPage
import com.example.model5.pages.Role
import com.example.model5.pages.SkillSelectionPage
import com.example.model5.pages.SplashScreen
import com.example.model5.pages.VerificationPage

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    mentorViewModel: MentorViewModel,
    menteeViewModel: MenteeViewModel,
    mentorDetails: MentorDetails, ) {
    val navController = rememberNavController()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("splash") {
                SplashScreen(navController , authViewModel)
            }

            composable("auth") {
                AuthPage(modifier = Modifier.fillMaxSize(), navController, authViewModel)
            }

            composable("choosing") {
                Role(modifier = Modifier.fillMaxSize(), navController , authViewModel)
            }

            composable("mentor_home") {
                MentorPage(navController, mentorViewModel , authViewModel)
            }

            composable("mentee_home") {
                MenteePage(navController,menteeViewModel,mentorViewModel,authViewModel)
            }

            composable("EditProfile") {
                EditProfile(navController, mentorViewModel , activity = Activity())
            }

            composable("mymentee") {
                MyMentorPage(menteeViewModel)
            }


            composable("ChangeProfile") {
                ChangeProfile(navController, menteeViewModel, activity = Activity())
            }
            composable("VerificationPage") {
                VerificationPage(
                    mentorDetails = mentorDetails,
                    navController = navController,
                    onComplete = { success ->
                        if (success) {

                            navController.navigate("mentor_home") {

                                popUpTo("VerificationPage") { inclusive = true }
                            }
                        } else {

                        }
                    }
                )
            }

            composable("SkillSelection") {
                SkillSelectionPage(navController = navController, menteeViewModel = menteeViewModel)
            }

        }
    }
}