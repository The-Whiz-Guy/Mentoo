package com.example.model5.pages

import com.example.model5.MentorDetails
import com.example.model5.MentorViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import android.net.Uri
import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import com.google.firebase.storage.FirebaseStorage


@Composable
fun EditProfile(navController: NavController, mentorViewModel: MentorViewModel, activity: Activity) {
    val mentorDetails by mentorViewModel.mentorDetails.collectAsState()

    // Local state for form fields
    var firstName by remember { mutableStateOf(mentorDetails.firstName) }
    var lastName by remember { mutableStateOf(mentorDetails.lastName) }
    var email by remember { mutableStateOf(mentorDetails.email) }
    var career by remember { mutableStateOf(mentorDetails.career.firstOrNull() ?: "") }
    var bio by remember { mutableStateOf(mentorDetails.bio) }
    var session by remember { mutableStateOf(mentorDetails.session) }
    var profileImageUri by remember { mutableStateOf(mentorDetails.profileImageUrl?.let { Uri.parse(it) }) } // Load existing image
    var isLoading by remember { mutableStateOf(false) } // Loading state

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                profileImageUri = it
                Log.d("ImagePicker", "Selected image URI: $profileImageUri")
            } ?: Log.e("ImagePicker", "No image selected")
        }
    )

    // Layout for editing the mentor profile
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Make the column scrollable
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Edit Mentor Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Image Upload Button
        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text("Upload Profile Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the selected or existing image in a circular shape
        Image(
            painter = rememberImagePainter(profileImageUri ?: mentorDetails.profileImageUrl),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape) // Clip image into a circular shape
                .background(Color.Gray),
            contentScale = ContentScale.Crop // This will crop the image and show the center portion
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(value = firstName, onValueChange = { firstName = it }, label = "First Name")
        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(value = lastName, onValueChange = { lastName = it }, label = "Last Name")
        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(value = email, onValueChange = { email = it }, label = "Email")
        Spacer(modifier = Modifier.height(8.dp))

        CustomDropdown(
            selectedCareer = career,
            onCareerSelected = { selectedCareer -> career = selectedCareer },
            careerOptions = listOf(
                "Software Developer", "Data Scientist", "Product Manager",
                "UI/UX Designer", "Business Analyst", "DevOps Engineer", "Cybersecurity Expert"
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(value = bio, onValueChange = { bio = it }, label = "Bio")
        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(value = session, onValueChange = { session = it }, label = "Session Details")
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            isLoading = true // Set loading state

            // Create updated details even if some fields are empty
            val updatedDetails = MentorDetails(
                firstName = firstName,
                lastName = lastName,
                email = email,
                career = listOf(career),
                bio = bio,
                session = session,
                profileImageUrl = mentorDetails.profileImageUrl // Keep existing URL if no new image
            )

            if (profileImageUri != null) {
                uploadImageToFirebase(profileImageUri!!) { downloadUrl ->
                    if (downloadUrl != null) {
                        // Update the profileImageUrl with the uploaded image URL
                        updatedDetails.profileImageUrl = downloadUrl.toString()
                    }
                    // Save mentor details regardless of upload success
                    mentorViewModel.updateMentorDetails(updatedDetails)
                    Log.d("EditProfile", "Mentor details updated successfully.")
                    isLoading = false // Reset loading state
                    navController.popBackStack() // Navigate back
                }
            } else {
                // Save mentor details without an image
                mentorViewModel.updateMentorDetails(updatedDetails)
                Log.d("EditProfile", "Mentor details updated without an image.")
                isLoading = false // Reset loading state
                navController.popBackStack() // Navigate back
            }
        }, enabled = !isLoading) {
            Text(text = if (isLoading) "Saving..." else "Save Profile")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}

fun uploadImageToFirebase(uri: Uri, onComplete: (Uri?) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference.child("mentor_images/${uri.lastPathSegment}")

    storageRef.putFile(uri).addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            onComplete(downloadUrl)
        }.addOnFailureListener { exception ->
            onComplete(null)
            Log.e("EditProfile", "Failed to get download URL", exception)
        }
    }.addOnFailureListener { exception ->
        onComplete(null)
        Log.e("EditProfile", "Failed to upload image", exception)
    }
}

@Composable
fun CustomDropdown(
    selectedCareer: String,
    onCareerSelected: (String) -> Unit,
    careerOptions: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        TextButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp)
        ) {
            Text(text = if (selectedCareer.isEmpty()) "Select a Career" else selectedCareer, color = Color.Black)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            careerOptions.forEach { career ->
                DropdownMenuItem(
                    onClick = {
                        onCareerSelected(career)
                        expanded = false
                    },
                    text = { Text(career) }
                )
            }
        }
    }
}

@Composable
fun CustomTextField(
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
                    .padding(vertical = 8.dp)  // Add some vertical padding
            ) { innerTextField ->
                innerTextField()
            }
        }
        Spacer(modifier = Modifier.height(2.dp)) // Spacer to separate the underline from the text field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Gray) // Underline color
        )
    }
}
