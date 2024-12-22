package com.example.model5.pages

import com.example.model5.MentorDetails
import com.example.model5.MentorViewModel
import com.example.model5.AuthViewModel
import com.example.model5.MenteeDetails
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model5.MenteeViewModel
import com.example.model5.ui.theme.White
import com.example.model5.ui.theme.skyblue
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenteePage(
    navController: NavController,
    menteeViewModel: MenteeViewModel,
    mentorViewModel: MentorViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    val menteeDetails by menteeViewModel.menteeDetails.collectAsState()

    LaunchedEffect(Unit) {
        menteeViewModel.fetchMenteeDetails()
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val drawerWidth = screenWidth * 0.6f

    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(drawerWidth)
            ) {
                SlidingMenuContentM(navController, authViewModel,menteeDetails)
            }
        },
        content = {
            Scaffold(
                topBar = {
                    SmallTopAppBar(
                        title = { Text("Mentee Home") },
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = skyblue),
                    )
                },
                content = { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(White, skyblue)
                                )
                            )
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TabRow(
                                selectedTabIndex = pagerState.currentPage,
                                containerColor = skyblue,
                                contentColor = Color.Black
                            ) {
                                listOf("Profile", "View For Mentor").forEachIndexed { index, title ->
                                    Tab(
                                        text = { Text(title) },
                                        selected = pagerState.currentPage == index,
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        }
                                    )
                                }
                            }

                            HorizontalPager(
                                count = 2,
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                when (page) {
                                    0 -> ProfileSlideM(navController, menteeDetails)
                                    1 -> MentorListPage(
                                        navController = navController,
                                        mentorViewModel = mentorViewModel,
                                        menteeViewModel = menteeViewModel
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    )
}

@Composable
fun ProfileSlideM(navController: NavController, menteeDetails: MenteeDetails) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color.LightGray, shape = CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (menteeDetails.profileImageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(menteeDetails.profileImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray, shape = CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                "Photo",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "${menteeDetails.firstName} ${menteeDetails.lastName}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = menteeDetails.bio,
                        fontSize = 16.sp,
                        color = Color.Black,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { navController.navigate("ChangeProfile") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White)
                    ) {
                        Text(text = "Change Profile", fontSize = 18.sp)
                    }
                }
            }
        }

        // Additional buttons for skill selection, mentors, achievements, etc.
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { navController.navigate("SkillSelection") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text(text = "Skill Selection", fontSize = 16.sp)
                }

                Button(
                    onClick = { navController.navigate("mymentee") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text(text = "My Mentor", fontSize = 16.sp)
                }

                Button(
                    onClick = { /* Handle My Achievements */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text(text = "My Achievements", fontSize = 16.sp)
                }

                Button(
                    onClick = { /* Handle settings */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text(text = "Settings", fontSize = 16.sp)
                }
            }
        }
    }
}


@Composable
fun MentorListPage(
    mentorViewModel: MentorViewModel = viewModel(),
    menteeViewModel: MenteeViewModel = viewModel(),
    navController: NavController
) {
    val mentorList by mentorViewModel.mentorList.collectAsState(initial = emptyList())
    val menteeDetails by menteeViewModel.menteeDetails.collectAsState()

    // Function to handle adding mentor to mentee's profile
    val onAddToMyMentor: (String) -> Unit = { mentorId ->
        menteeViewModel.addMentorToMentee(mentorId)
    }

    // Function to handle "Know More"
    val onKnowMore: (String) -> Unit = { mentorId ->
        navController.navigate("mentorDetails/$mentorId")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (mentorList.isEmpty()) {
            Text(
                text = "No mentors available",
                fontSize = 18.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(mentorList) { mentorDetails ->
                    MentorCard(
                        mentorDetails = mentorDetails,
                        onAddToMyMentor = onAddToMyMentor,
                        onKnowMore = onKnowMore,
                        isAdded = menteeDetails.mentors.contains(mentorDetails.id)
                    )
                }
            }
        }
    }
}

@Composable
fun MentorCard(
    mentorDetails: MentorDetails,
    onAddToMyMentor: (String) -> Unit,
    onKnowMore: (String) -> Unit,
    isAdded: Boolean
) {
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
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mentor's profile image or placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.LightGray, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (mentorDetails.profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = mentorDetails.profileImageUrl,
                        contentDescription = "Mentor's Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "Photo",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "${mentorDetails.firstName} ${mentorDetails.lastName}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = mentorDetails.career.firstOrNull().orEmpty(),
                fontSize = 18.sp,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = mentorDetails.bio.ifEmpty { "No bio provided" },
                fontSize = 16.sp,
                color = Color.Black,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (mentorDetails.session.isNotEmpty()) {
                Text(
                    text = "Meet link: ${mentorDetails.session}",
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { /* Handle session link click */ }
                        .padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { onKnowMore(mentorDetails.id) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(text = "Know More")
                }

                Button(
                    onClick = {
                        if (!isAdded) {
                            // Call the function to add the mentor ID to the mentee's Firestore details
                            onAddToMyMentor(mentorDetails.id)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),  // Adjust the height for better spacing
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF87CEEB), contentColor = Color.White), // Use a color for sky blue
                    contentPadding = PaddingValues(horizontal = 16.dp)  // Add padding for better text alignment
                ) {
                    Text(
                        text = if (isAdded) "This mentor is added to my mentor list" else "Make My Mentor",
                        maxLines = 1,  // Ensure the text is in one line
                        overflow = TextOverflow.Ellipsis,  // Handle text overflow
                        fontSize = 16.sp,  // Adjust font size for better fit
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
@Composable
fun SlidingMenuContentM(navController: NavController, authViewModel: AuthViewModel, menteeDetails: MenteeDetails) {
    val authState by authViewModel.authState.observeAsState()

    // UI layout starts here
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween  // Align content to top and buttons to the bottom
    ) {
        // Top section: Greeting and profile picture
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (menteeDetails.profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(menteeDetails.profileImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Mentee's Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        "Photo",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Greeting text with mentee's name
            Text(
                text = "Hi, ${menteeDetails.firstName}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        // Bottom section: Buttons aligned at the bottom
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { /* Handle share action */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Text(text = "Share", fontSize = 16.sp)
            }

            Button(
                onClick = { /* Handle about us action */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Text(text = "About Us", fontSize = 16.sp)
            }

            Button(
                onClick = {
                    authViewModel.signOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Text(text = "Logout", fontSize = 16.sp)
            }
        }

        if (authState is AuthViewModel.AuthState.Unauthenticated) {
            LaunchedEffect(Unit) {
                navController.navigate("auth") {
                    popUpTo("choosing") { inclusive = true }
                }
            }
        }
    }
}
