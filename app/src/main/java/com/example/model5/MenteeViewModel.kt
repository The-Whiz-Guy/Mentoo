package com.example.model5

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Data class to store Mentee details
data class MenteeDetails(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val bio: String = "",
    val selectedSkills: List<String> = emptyList(),
    val mentors: List<String> = emptyList(), // List of mentor IDs
    var profileImageUrl: String = ""  // Add profileImageUrl field

)

class MenteeViewModel : ViewModel() {
    private val _menteeDetails = MutableStateFlow(MenteeDetails())
    val menteeDetails: StateFlow<MenteeDetails> = _menteeDetails

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        fetchMenteeDetails()
    }

    fun getMentorById(mentorId: String): StateFlow<MentorDetails?> {
        val mentorDetailsFlow = MutableStateFlow<MentorDetails?>(null)

        viewModelScope.launch {
            db.collection("mentors").document(mentorId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val mentorDetails = document.toObject(MentorDetails::class.java)
                        mentorDetailsFlow.value = mentorDetails
                    }
                }
                .addOnFailureListener {
                    // Handle error
                }
        }

        return mentorDetailsFlow
    }


    // Function to add a mentor to the mentee's list of mentors
    fun addMentorToMentee(mentorId: String) {
        viewModelScope.launch {
            val currentMentee = _menteeDetails.value

            // Add mentorId to the current list of mentors
            val updatedMentors = currentMentee.mentors + mentorId

            // Create an updated version of the mentee details with the new mentor list
            val updatedMentee = currentMentee.copy(mentors = updatedMentors)

            // Save the updated mentee details in Firestore
            saveMenteeDetails(updatedMentee)
        }
    }

    // Function to remove a mentor from the mentee's list of mentors
    fun removeMentorFromMentee(mentorId: String) {
        viewModelScope.launch {
            val currentMentee = _menteeDetails.value

            // Remove mentorId from the current list of mentors
            val updatedMentors = currentMentee.mentors - mentorId

            // Create an updated version of the mentee details with the updated mentor list
            val updatedMentee = currentMentee.copy(mentors = updatedMentors)

            // Save the updated mentee details in Firestore
            saveMenteeDetails(updatedMentee)
        }
    }

    // Function to fetch the mentee's details from Firestore
    fun fetchMenteeDetails() {
        val menteeId = auth.currentUser?.uid
        menteeId?.let {
            db.collection("mentees").document(it)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Convert Firestore document to MenteeDetails object
                        val mentee = document.toObject(MenteeDetails::class.java)
                        mentee?.let { menteeData ->
                            // Update local _menteeDetails state with fetched data
                            _menteeDetails.value = menteeData
                        }
                    }
                }
                .addOnFailureListener {
                    // Handle error (e.g., logging)
                }
        }
    }

    // Function to save updated mentee details in Firestore
    private fun saveMenteeDetails(updatedMentee: MenteeDetails) {
        val menteeId = auth.currentUser?.uid
        menteeId?.let {
            db.collection("mentees").document(it)
                .set(updatedMentee)
                .addOnSuccessListener {
                    // Refresh the local data after saving
                    fetchMenteeDetails()
                }
                .addOnFailureListener {
                    // Handle error (e.g., logging)
                }
        }
    }

    // Function to update mentee details (e.g., name, bio, etc.)`
    fun updateMenteeDetails(newDetails: MenteeDetails) {
        val menteeId = auth.currentUser?.uid
        menteeId?.let {
            db.collection("mentees").document(it)
                .set(newDetails)
                .addOnSuccessListener {
                    fetchMenteeDetails() // Refresh local data
                }
                .addOnFailureListener {
                    // Handle error (e.g., logging)
                }
        }
    }

    // Function to update mentee's skills in Firestore
    fun updateMenteeSkills(skills: List<String>) {
        val menteeId = auth.currentUser?.uid
        menteeId?.let {
            db.collection("mentees").document(it)
                .update("selectedSkills", skills)
                .addOnSuccessListener {
                    fetchMenteeDetails() // Refresh local data
                }
                .addOnFailureListener {
                    // Handle error (e.g., logging)
                }
        }
    }
}
