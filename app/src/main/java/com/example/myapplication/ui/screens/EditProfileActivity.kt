package com.example.myapplication.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.ui.viewmodel.EditProfileViewModel

private val NetflixRed = Color(0xFFE50914)
private val NetflixBlack = Color(0xFF141414)
private val NetflixDarkGray = Color(0xFF222222)
private val NetflixLightGray = Color(0xFF888888)

private val NetflixColorScheme = darkColorScheme(
    primary = NetflixRed,
    onPrimary = Color.White,
    background = NetflixBlack,
    onBackground = Color.White,
    surface = NetflixDarkGray,
    onSurface = Color.White,
    secondary = NetflixLightGray,
    onSecondary = Color.White
)

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val profileId = intent.getStringExtra("profileId") ?: ""
        setContent {
            MaterialTheme(colorScheme = NetflixColorScheme) {
                val viewModel: EditProfileViewModel = viewModel()
                EditProfileScreen(
                    profileId = profileId,
                    viewModel = viewModel,
                    onProfileUpdated = { finish() },
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profileId: String,
    viewModel: EditProfileViewModel,
    onProfileUpdated: () -> Unit,
    onBackPressed: () -> Unit
) {
    val name by viewModel.name.collectAsState()
    val avatarUrl by viewModel.avatarUrl.collectAsState()
    val isKid by viewModel.isKid.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadError by viewModel.uploadError.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current

    // Image picker launcher
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val token = prefs.getString("auth_token", null) ?: ""
            if (uri != null && token.isNotBlank()) {
                viewModel.uploadAvatar(context, uri, token)
            }
        }
    )

    // Fetch once on load
    LaunchedEffect(profileId) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null) ?: ""
        viewModel.loadProfile(context, profileId, token)
    }

    BackHandler(enabled = true) {
        onBackPressed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NetflixBlack),
            )
        },
        containerColor = NetflixBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NetflixBlack)
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(color = Color.White)
            } else {
                // Avatar Image + Change/Remove Buttons
                Box(contentAlignment = Alignment.Center) {
                    if (avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = "https://kndole-dev-safe.s3.amazonaws.com/$avatarUrl",
                            contentDescription = "Avatar",
                            modifier = Modifier.size(96.dp).clip(CircleShape)
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "No Avatar",
                            tint = NetflixLightGray,
                            modifier = Modifier.size(96.dp).clip(CircleShape)
                        )
                    }
                    if (isUploading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { pickImageLauncher.launch("image/*") },
                        enabled = !isUploading,
                        colors = ButtonDefaults.buttonColors(containerColor = NetflixRed)
                    ) { Text("Change Image", color = Color.White) }
                    if (avatarUrl.isNotBlank()) {
                        Button(
                            onClick = { viewModel.removeAvatar() },
                            enabled = !isUploading,
                            colors = ButtonDefaults.buttonColors(containerColor = NetflixDarkGray)
                        ) { Text("Remove", color = Color.White) }
                    }
                }
                if (uploadError != null) {
                    Text(uploadError!!, color = Color.Red, fontSize = 13.sp)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.setName(it) },
                    label = { Text("Name", color = NetflixLightGray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors1(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isKid,
                        onCheckedChange = { viewModel.setIsKid(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NetflixRed,
                            uncheckedColor = NetflixLightGray,
                            checkmarkColor = Color.White
                        )
                    )
                    Text("Kid profile?", color = Color.White)
                }

                if (error != null) {
                    Text(error!!, color = Color.Red, fontSize = 14.sp)
                }

                Button(
                    onClick = {
                        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        val token = prefs.getString("auth_token", null) ?: ""
                        viewModel.updateProfile(context, profileId, token, onProfileUpdated)
                    },
                    enabled = !isLoading && !isUploading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetflixRed,
                        contentColor = Color.White,
                        disabledContainerColor = NetflixDarkGray
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save Changes", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun textFieldColors1() = TextFieldDefaults.outlinedTextFieldColors(
    focusedTextColor = Color.White,
    cursorColor = NetflixRed,
    focusedBorderColor = NetflixRed,
    unfocusedBorderColor = NetflixLightGray,
    focusedPlaceholderColor = NetflixLightGray
)
