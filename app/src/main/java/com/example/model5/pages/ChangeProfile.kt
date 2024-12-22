package com.example.model5.pages

import android.net.Uri
import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.model5.MenteeDetails
import com.example.model5.MenteeViewModel
import com.google.firebase.storage.FirebaseStorage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.draw.clip

@Composable
fun ChangeProfile(navController: NavController, menteeViewModel: MenteeViewModel, activity: Activity) {
    val menteeDetails by menteeViewModel.menteeDetails.collectAsState()

    var firstName by remember { mutableStateOf(menteeDetails.firstName) }
    var lastName by remember { mutableStateOf(menteeDetails.lastName) }
    var email by remember { mutableStateOf(menteeDetails.email) }
    var bio by remember { mutableStateOf(menteeDetails.bio) }
    var profileImageUri by remember { mutableStateOf(menteeDetails.profileImageUrl?.let { Uri.parse(it) }) } // Load existing image
    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                profileImageUri = it
                Log.d("ImagePicker", "Selected image URI: $profileImageUri")
            } ?: Log.e("ImagePicker", "No image selected")
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Change Mentee Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Image Upload Button
        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text("Upload Profile Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the selected or existing image in a circular shape
        Image(
            painter = rememberImagePainter(profileImageUri ?: menteeDetails.profileImageUrl),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape) // Clip image into a circular shape
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextFieldM(value = firstName, onValueChange = { firstName = it }, label = "First Name")
        Spacer(modifier = Modifier.height(8.dp))

        CustomTextFieldM(value = lastName, onValueChange = { lastName = it }, label = "Last Name")
        Spacer(modifier = Modifier.height(8.dp))

        CustomTextFieldM(value = email, onValueChange = { email = it }, label = "Email")
        Spacer(modifier = Modifier.height(8.dp))

        CustomTextFieldM(value = bio, onValueChange = { bio = it }, label = "Bio")
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            isLoading = true

            val updatedDetails = MenteeDetails(
                firstName = firstName,
                lastName = lastName,
                email = email,
                bio = bio,
                profileImageUrl = menteeDetails.profileImageUrl
            )

            if (profileImageUri != null) {
                uploadImageToFirebase(profileImageUri!!) { downloadUrl ->
                    if (downloadUrl != null) {
                        updatedDetails.profileImageUrl = downloadUrl.toString()
                    }
                    menteeViewModel.updateMenteeDetails(updatedDetails)
                    isLoading = false
                    navController.navigate("mentee_home")
                }
            } else {
                menteeViewModel.updateMenteeDetails(updatedDetails)
                isLoading = false
                navController.navigate("mentee_home")
            }
        }, enabled = !isLoading) {
            Text(text = if (isLoading) "Saving..." else "Save Profile")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun CustomTextFieldM(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    Column {
        Text(
            text = label,
            style = TextStyle(
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light
            ),
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Transparent)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) { innerTextField ->
                innerTextField()
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Gray)
        )
    }
}
