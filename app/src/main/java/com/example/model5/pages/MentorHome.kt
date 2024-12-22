package com.example.model5.pages

import com.example.model5.MentorDetails
import com.example.model5.MentorViewModel
import com.example.model5.AuthViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.model5.ui.theme.White
import com.example.model5.ui.theme.skyblue
import kotlinx.coroutines.launch
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model5.MenteeDetails
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

data class MenteeRequest(val name: String, val message: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorPage(navController: NavController, mentorViewModel: MentorViewModel, authViewModel: AuthViewModel) {
    val mentorDetails by mentorViewModel.mentorDetails.collectAsState()
    LaunchedEffect(Unit) {
        mentorViewModel.fetchMentorDetails()
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
            Box(modifier = Modifier.fillMaxHeight().width(drawerWidth)) {
                SlidingMenuContent(navController, authViewModel, mentorDetails)
            }
        },
        content = {
            Scaffold(
                topBar = {
                    SmallTopAppBar(
                        title = { Text("Mentor Home") },
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
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
                                    colors = listOf(skyblue, White)
                                )
                            )
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TabRow(
                                selectedTabIndex = pagerState.currentPage,
                                containerColor = skyblue,
                                contentColor = Color.Black
                            ) {
                                listOf("Profile", "Mentee Requests").forEachIndexed { index, title ->
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
                                    0 -> ProfileSlide(navController, mentorDetails)
                                    1 -> MenteeRequestsSlide()
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
fun ProfileSlide(navController: NavController, mentorDetails: MentorDetails) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // Padding for the entire screen
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f) // Fill available space
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
                                if (mentorDetails.profileImageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(mentorDetails.profileImageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile Image",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .background(Color.LightGray, shape = CircleShape),
                                        contentScale = ContentScale.Crop
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
                                fontSize = 12.sp,
                                color = Color.Black,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (!mentorDetails.session.isNullOrEmpty()) {
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

                            Button(
                                onClick = { navController.navigate("EditProfile") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White)
                            ) {
                                Text(text = "Edit Profile", fontSize = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Buttons at the bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // Padding for bottom area
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(listOf(
                        ButtonItem("Verify Your Account") {
                            navController.navigate("VerificationPage")
                        },
                        ButtonItem("My Mentees") {
                            /* Handle My Mentees */
                        },
                        ButtonItem("My Achievements") {
                            /* Handle My Achievements */
                        },
                        ButtonItem("Settings") {
                            /* Handle Settings */
                        }
                    )) { buttonItem ->
                        Button(
                            onClick = buttonItem.onClick,
                            modifier = Modifier
                                .size(120.dp) // Square shape
                                .clip(RectangleShape),
                            shape = RectangleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        ) {
                            Text(
                                text = buttonItem.text,
                                fontSize = 16.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}



data class ButtonItem(val text: String, val onClick: () -> Unit)

@Composable
fun MenteeRequestsSlide() {
    val requests = List(20) {
        MenteeRequest(
            name = "Mentee ${it + 1}",
            message = "This is a sample message from Mentee ${it + 1}."
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(requests) { request ->
            MenteeRequestItem(request)
        }
    }
}

@Composable
fun MenteeRequestItem(request: MenteeRequest) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = request.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = request.message, fontSize = 16.sp)
        }
    }
}

@Composable
fun SlidingMenuContent(navController: NavController, authViewModel: AuthViewModel, mentorDetails: MentorDetails) {
    val authState by authViewModel.authState.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (mentorDetails.profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(mentorDetails.profileImageUrl)
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

            Text(
                text = "Hi, ${mentorDetails.firstName}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

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
