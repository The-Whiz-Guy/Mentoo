package com.example.model5.pages

import com.example.model5.MentorDetails
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FieldValue
import com.maxkeppeler.sheets.calendar.models.CalendarSelection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationPage(
    mentorDetails: MentorDetails,
    navController: NavController,
    onComplete: (Boolean) -> Unit
) {
    var mobileNumber by remember { mutableStateOf("") }
    var governmentCertificateUri by remember { mutableStateOf<Uri?>(null) }
    var governmentCertificateFileName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf(mentorDetails.firstName) }
    var lastName by remember { mutableStateOf(mentorDetails.lastName) }
    var isSubmitted by remember { mutableStateOf(false) }
    var isVerified by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            governmentCertificateUri = uri
            governmentCertificateFileName = getFileName(context, uri)
        }
    }

    val calendarState = rememberUseCaseState()

    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val mentorId = auth.currentUser?.uid
        if (mentorId != null) {
            val db = FirebaseFirestore.getInstance()
            val mentorRef = db.collection("mentors").document(mentorId)
            mentorRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val status = document.getString("verification.status")
                    isSubmitted = status == "submitted"
                    isVerified = status == "success"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            isVerified -> {
                // Show verified message
                Text("YOUR ACCOUNT IS VERIFIED", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            isSubmitted -> {
                // Show confirmation text and erase button
                Text("Verification data submitted successfully!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        eraseVerificationData {
                            navController.navigate("verificationPage") {
                                popUpTo("VerificationPage") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Erase Data and Return")
                }
            }
            else -> {
                // Show form for verification data
                Text("Review your details", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    value = firstName,
                    onValueChange = {
                        firstName = it
                        errorMessage = null
                    },
                    label = "First Name"
                )

                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(
                    value = lastName,
                    onValueChange = {
                        lastName = it
                        errorMessage = null
                    },
                    label = "Last Name"
                )

                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(
                    value = mobileNumber,
                    onValueChange = {
                        mobileNumber = it
                        errorMessage = null
                    },
                    label = "Mobile Number +91 "
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        errorMessage = null
                    },
                    label = "Address"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { filePickerLauncher.launch("application/pdf") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Upload Government Certificate")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Display file name if available
                if (governmentCertificateFileName.isNotEmpty()) {
                    Text(text = "Selected file: $governmentCertificateFileName", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { calendarState.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pick Date of Birth")
                }

                selectedDate?.let {
                    Text(text = "Date of Birth: $it", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (mobileNumber.isNotEmpty() && isValidMobileNumber(mobileNumber) && governmentCertificateUri != null && address.isNotEmpty() && selectedDate != null) {
                            isUploading = true
                            uploadCertificate(governmentCertificateUri!!) { downloadUrl ->
                                if (downloadUrl != null) {
                                    val verificationData = VerificationData(
                                        firstName = firstName,
                                        lastName = lastName,
                                        mobileNumber = mobileNumber,
                                        governmentCertificateUri = downloadUrl,
                                        address = address,
                                        dob = selectedDate!!
                                    )
                                    submitVerificationData(verificationData) { success ->
                                        isUploading = false
                                        if (success) {
                                            isSubmitted = true
                                        } else {
                                            errorMessage = "Failed to submit verification data. Please try again."
                                        }
                                    }
                                } else {
                                    isUploading = false
                                    errorMessage = "Failed to upload the certificate. Please try again."
                                }
                            }
                        } else {
                            errorMessage = if (!isValidMobileNumber(mobileNumber)) {
                                "Invalid mobile number. Please enter a valid 10-digit number."
                            } else {
                                "Please complete all fields correctly"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Submit")
                    }
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }

    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(
            yearSelection = true,
            monthSelection = true,
            style = CalendarStyle.MONTH
        ),
        selection = CalendarSelection.Date { date ->
            selectedDate = date.toString()
        }
    )
}

fun eraseVerificationData(onComplete: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val mentorId = auth.currentUser?.uid

    if (mentorId != null) {
        val db = FirebaseFirestore.getInstance()
        val mentorRef = db.collection("mentors").document(mentorId)

        mentorRef.update(mapOf(
            "verification.firstName" to FieldValue.delete(),
            "verification.lastName" to FieldValue.delete(),
            "verification.mobileNumber" to FieldValue.delete(),
            "verification.governmentCertificate" to FieldValue.delete(),
            "verification.address" to FieldValue.delete(),
            "verification.dob" to FieldValue.delete(),
            "verification.status" to "pending"  // Reset status
        )).addOnSuccessListener {
            onComplete()
        }.addOnFailureListener { exception ->
            Log.e("VerificationPage", "Failed to erase verification data", exception)
            onComplete()
        }
    } else {
        Log.e("VerificationPage", "User not authenticated")
        onComplete()
    }
}

fun isValidMobileNumber(number: String): Boolean {
    val cleanedNumber = number.filter { it.isDigit() }
    return cleanedNumber.length == 10
}

data class VerificationData(
    val firstName: String,
    val lastName: String,
    val mobileNumber: String,
    val governmentCertificateUri: Uri,
    val address: String,
    val dob: String
)

fun uploadCertificate(uri: Uri, onComplete: (Uri?) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference.child("certificates/${uri.lastPathSegment}")

    storageRef.putFile(uri).addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            onComplete(downloadUrl)
        }.addOnFailureListener { exception ->
            onComplete(null)
            Log.e("VerificationPage", "Failed to get download URL", exception)
        }
    }.addOnFailureListener { exception ->
        onComplete(null)
        Log.e("VerificationPage", "Failed to upload certificate", exception)
    }
}

fun submitVerificationData(verificationData: VerificationData, onComplete: (Boolean) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val mentorId = auth.currentUser?.uid

    if (mentorId != null) {
        val db = FirebaseFirestore.getInstance()
        val mentorRef = db.collection("mentors").document(mentorId)

        mentorRef.update(mapOf(
            "verification.firstName" to verificationData.firstName,
            "verification.lastName" to verificationData.lastName,
            "verification.mobileNumber" to verificationData.mobileNumber,
            "verification.governmentCertificate" to verificationData.governmentCertificateUri.toString(),
            "verification.address" to verificationData.address,
            "verification.dob" to verificationData.dob,
            "verification.status" to "pending"  // Set status to pending
        )).addOnSuccessListener {
            onComplete(true)
        }.addOnFailureListener { exception ->
            Log.e("VerificationPage", "Failed to submit verification data", exception)
            onComplete(false)
        }
    } else {
        Log.e("VerificationPage", "User not authenticated")
        onComplete(false)
    }
}

fun getFileName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                return it.getString(nameIndex) ?: ""
            }
        }
    }
    return uri.path?.substringAfterLast("/") ?: ""
}

@Preview(showBackground = true)
@Composable
private fun VerificationPagePreview() {
    val sampleMentorDetails = MentorDetails(
        firstName = "",
        lastName = "",
        address = ""
    )

    VerificationPage(
        mentorDetails = sampleMentorDetails,
        navController = rememberNavController(),
        onComplete = { success ->

        }
    )
}
