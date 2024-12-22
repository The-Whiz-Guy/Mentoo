package com.example.model5.pages

import com.example.model5.AuthViewModel
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.model5.R
import com.example.model5.getStoredRole
import com.example.model5.ui.theme.White
import com.example.model5.ui.theme.skyblue
import com.google.accompanist.pager.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

@Composable
fun AuthPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    val pagerState = rememberPagerState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(
                colors = listOf(skyblue, White))
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                count = 2,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> SignupSlide(navController, authViewModel)
                    1 -> LoginSlide(navController, authViewModel)
                }
            }
        }
    }
}

@Composable
fun SignupSlide(navController: NavController, authViewModel: AuthViewModel) {


    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.observeAsState()
    val context = LocalContext.current

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("798779967810-ko7m5lu96spj9r5vhmg31kt5rr0u12e3.apps.googleusercontent.com")
        .requestEmail()
        .build()

    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task, context, authViewModel)
        })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = description)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { authViewModel.signup(email, password) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        ) {
            Text(text = "Create Account")
        }

        Spacer(modifier = Modifier.height(8.dp))

        IconButton(
            onClick = {
                val signInIntent = googleSignInClient.signInIntent
                signInLauncher.launch(signInIntent)
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Sign in with Google",
                tint = Color.Unspecified
            )
        }

        LaunchedEffect(authState) {
            when (authState) {
                is AuthViewModel.AuthState.Authenticated -> {
                    val role = getStoredRole(context, authViewModel.getCurrentUserId())
                    if (role != null) {
                        navController.navigate(if (role == "mentor") "mentor_home" else "mentee_home") {
                            popUpTo("auth") { inclusive = true }
                        }
                    } else {
                        navController.navigate("choosing") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                }
                is AuthViewModel.AuthState.Error -> {
                    // Handle error
                }
                else -> Unit
            }
        }

    }
}

fun handleSignInResult(completedTask: Task<GoogleSignInAccount>, context: Context, authViewModel: AuthViewModel) {
    try {
        val account = completedTask.getResult(ApiException::class.java)
        account?.idToken?.let { idToken ->
            // Log the ID token for verification
            Log.d("Auth", "Google Sign-In successful, ID Token: $idToken")

            // Pass the ID token to sign in with Firebase
            authViewModel.signInWithGoogle(idToken)
        } ?: run {
            Toast.makeText(context, "Failed to get ID token", Toast.LENGTH_SHORT).show()
        }
    } catch (e: ApiException) {
        Toast.makeText(context, "Google Sign-In Failed: ${e.message}", Toast.LENGTH_LONG).show()
        Log.e("GoogleSignIn", "Google Sign-In Failed", e)
    }
}

@Composable
fun LoginSlide(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by authViewModel.authState.observeAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { authViewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        ) {
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        LaunchedEffect(authState) {
            when (authState) {
                is AuthViewModel.AuthState.Authenticated -> navController.navigate("choosing") {
                    popUpTo("auth") { inclusive = true }
                }
                is AuthViewModel.AuthState.Error -> Toast.makeText(
                    context,
                    (authState as AuthViewModel.AuthState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
                else -> Unit
            }
        }
    }
}


@Preview
@Composable
fun AuthPreview() {
    val navController = rememberNavController()
    AuthPage(modifier = Modifier, navController = navController, authViewModel = AuthViewModel())
}
