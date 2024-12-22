package com.example.model5.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model5.MenteeViewModel
import com.example.model5.MentorDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMentorPage(menteeViewModel: MenteeViewModel) {
    val menteeDetails by menteeViewModel.menteeDetails.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        MyMentorAppBar()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(menteeDetails.mentors) { mentorId ->
                MentorCard(mentorId, menteeViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMentorAppBar() {
    TopAppBar(
        title = {
            Text(
                text = "My Mentors",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp)
            )
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun MentorCard(mentorId: String, menteeViewModel: MenteeViewModel) {
    var mentorDetails by remember { mutableStateOf<MentorDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Collect mentor details when this card is first composed
    LaunchedEffect(mentorId) {
        menteeViewModel.getMentorById(mentorId).collect { mentor ->
            mentorDetails = mentor
            isLoading = false // Set loading to false once details are fetched
        }
    }

    if (isLoading) {
        LoadingCard()
    } else {
        mentorDetails?.let { details ->
            MentorCardContent(details) {
                menteeViewModel.removeMentorFromMentee(details.id)
            }
        }
    }
}

@Composable
fun LoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun MentorCardContent(mentor: MentorDetails, onRemoveMentor: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // Mentor's full name
            Text(
                text = "${mentor.firstName} ${mentor.lastName}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Mentor's career
            Text(
                text = mentor.career.firstOrNull().orEmpty(),
                fontSize = 18.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Remove button
            Button(
                onClick = onRemoveMentor,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = "Remove Mentor")
            }
        }
    }
}
