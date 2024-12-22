package com.example.model5

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MentorDetails(
    var id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val career: List<String> = emptyList(),
    val bio: String = "",
    val address: String = "",
    val session: String = "",
    var profileImageUrl: String = ""
)


class MentorViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // StateFlow to hold a single mentor's details
    private val _mentorDetails = MutableStateFlow(MentorDetails())
    val mentorDetails: StateFlow<MentorDetails> = _mentorDetails

    // StateFlow to hold the list of all mentors
    private val _mentorList = MutableStateFlow<List<MentorDetails>>(emptyList())
    val mentorList: StateFlow<List<MentorDetails>> = _mentorList

    init {
        fetchMentorsFromFirestore()
    }

    private fun fetchMentorsFromFirestore() {
        viewModelScope.launch {
            db.collection("mentors")
                .whereEqualTo("verification.status", "success")
                .get()
                .addOnSuccessListener { result ->
                    val mentors = result.map { document ->
                        MentorDetails(
                            id = document.id, // Get the document ID
                            firstName = document.getString("firstName") ?: "",
                            lastName = document.getString("lastName") ?: "",
                            career = document.get("career") as? List<String> ?: emptyList(),
                            bio = document.getString("bio") ?: "",
                            session = document.getString("session") ?: "",
                            profileImageUrl = document.getString("profileImageUrl") ?: ""
                        )
                    }
                    Log.d("MentorViewModel", "Fetched mentors: $mentors")
                    _mentorList.value = mentors
                }
                .addOnFailureListener { exception ->
                    Log.e("MentorViewModel", "Error fetching mentors: ${exception.message}")
                }
        }
    }

    fun fetchMentorDetails() {
        val mentorId = auth.currentUser?.uid
        mentorId?.let {
            db.collection("mentors").document(it)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val mentor = document.toObject(MentorDetails::class.java)
                        mentor?.let { mentorData ->
                            _mentorDetails.value = mentorData
                        }

                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MentorViewModel", "Error fetching mentor details: ${exception.message}")
                }
        }
    }

    fun updateMentorDetails(newDetails: MentorDetails) {
        val mentorId = auth.currentUser?.uid
        mentorId?.let {
            db.collection("mentors").document(it)
                .set(newDetails)
                .addOnSuccessListener {
                    Log.d("MentorViewModel", "Mentor details updated successfully")
                    fetchMentorDetails()  // Fetch updated details
                }
                .addOnFailureListener { exception ->
                    Log.e("MentorViewModel", "Error updating mentor details: ${exception.message}")
                }
        }
    }

    fun updateMentorImage(imageUrl: String) {
        val mentorId = auth.currentUser?.uid
        mentorId?.let {
            db.collection("mentors").document(it)
                .update("profileImageUrl", imageUrl)
                .addOnSuccessListener {
                    Log.d("MentorViewModel", "Profile image URL updated: $imageUrl")
                    fetchMentorDetails()  // Reload mentor data with updated image
                }
                .addOnFailureListener { exception ->
                    Log.e("MentorViewModel", "Error updating profile image URL: ${exception.message}")
                }
        }
    }

    fun uploadImageToFirebase(uri: Uri, onUploadSuccess: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("mentor_images/${System.currentTimeMillis()}.jpg")
        Log.d("Firebase", "Uploading image to: ${storageRef.path}")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                Log.d("Firebase", "Image upload success.")
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    Log.d("Firebase", "Image uploaded successfully: $downloadUrl")
                    onUploadSuccess(downloadUrl.toString()) // This returns the URL as a String
                }.addOnFailureListener { downloadError ->
                    Log.e("Firebase", "Error getting download URL: ${downloadError.message}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Image upload failed: ${exception.message}")
            }
    }

    // Method to add a new mentor
    fun addMentor(mentorDetails: MentorDetails) {
        val mentorData = mentorDetails.copy(id = "") // Start with an empty ID
        db.collection("mentors")
            .add(mentorData)
            .addOnSuccessListener { documentReference ->
                // Update the mentor ID with the newly generated document ID
                mentorData.id = documentReference.id
                db.collection("mentors").document(documentReference.id)
                    .set(mentorData) // Save the mentor details with the generated ID
                    .addOnSuccessListener {
                        Log.d("MentorViewModel", "Mentor added successfully with ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("MentorViewModel", "Error adding mentor: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("MentorViewModel", "Error adding mentor: ${exception.message}")
            }
    }
}
