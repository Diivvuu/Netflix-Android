package com.example.myapplication.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.MainActivity
import com.example.myapplication.ui.components.NetflixHeader
import com.example.myapplication.ui.model.Profile
import com.example.myapplication.ui.viewmodel.ProfilesViewModel
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.launch

class ProfilesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)
        if (token.isNullOrBlank()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }



        setContent {
            val viewModel: ProfilesViewModel = viewModel()
            var shouldRefresh by remember { mutableStateOf(false) }

            val addProfileLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if(result.resultCode == Activity.RESULT_OK) {
                    shouldRefresh = true
                }
            }

            // ActivityResult launcher
            val editProfileLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if(result.resultCode == Activity.RESULT_OK) {
                    shouldRefresh = true   // Just trigger a recomposition
                }
            }

            // Pass the launcher to your ProfilesScreen
            ProfilesScreen(
                token = token,
                viewModel = viewModel,
                onAddProfile = {
                    addProfileLauncher.launch(Intent(this, AddProfileActivity::class.java))
                },
                onEditProfile = { profileId ->
                    val intent = Intent(this, EditProfileActivity::class.java)
                    intent.putExtra("profileId", profileId)
                    editProfileLauncher.launch(intent)
                },
                onLogout = {
                    prefs.edit().remove("auth_token").apply()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                shouldRefresh = shouldRefresh,
                onRefreshed = { shouldRefresh = false }
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfilesScreen(
    token: String,
    viewModel: ProfilesViewModel,
    onAddProfile : () -> Unit,
    onEditProfile: (String) -> Unit,
    onLogout: () -> Unit,
    shouldRefresh: Boolean,
    onRefreshed: () -> Unit
    ) {
    val profiles by viewModel.profiles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var profileToDelete by remember { mutableStateOf<Profile?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun onProfileSelected(profile : Profile) {

        scope.launch {
            try {
                val genreResponse = viewModel.fetchProfileGenres(token, profile.id);
                val hasMovieGenres = genreResponse?.movieGenreIds?.isNotEmpty() == true
                val hasTvGenres = genreResponse?.tvGenreIds?.isNotEmpty() == true
                Log.d("RESPONSE", hasMovieGenres.toString())
                Log.d("RESPONSE", hasTvGenres.toString())
                Log.d("RESPONSE", genreResponse.toString())
//                val context = LocalContext.current

                if(!hasMovieGenres && !hasTvGenres) {
                    val intent = Intent(context, GenreSelectionActivity::class.java)
                    intent.putExtra("profileId", profile.id)
                    context.startActivity(intent)
                }
                else {
                    val intent = Intent(context, NetflixMainActivity::class.java)
                    intent.putExtra("profileId", profile.id)
                    context.startActivity(intent)
                }
            }catch (e : Exception) {
//                Toast.makeText(FAiled, "", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onDeleteProfile(profile: Profile) {
        viewModel.deleteProfile(token, profile)
        showDeleteDialog = false
        profileToDelete = null
    }

    LaunchedEffect(Unit) { viewModel.fetchProfiles(token) }
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.fetchProfiles(token)
            onRefreshed()
        }
    }
    Scaffold(
        containerColor = Color(0xFF141414),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {onAddProfile()},
//                    addProfileLauncher.launch(Intent(context, AddProfileActivity::class.java))
//                    context.startActivity(Intent(context, AddProfileActivity::class.java))
//                    (context as? Activity)?.finish()
//                },
                containerColor = Color.Red,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Profile")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF141414)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NetflixHeader(
                showLogoutButton = true,
                onLogoutClick = onLogout
            )


            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Who's watching?",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $error", color = Color.Red)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        mainAxisSpacing = 32.dp,
                        crossAxisSpacing = 24.dp,
                        mainAxisAlignment = FlowMainAxisAlignment.Center
                    ) {
                        profiles.forEach { profile ->
                            Log.d("Profiles", profile.name)
                            Log.d("Profiles", profile.id)
                            Log.d("Profiles", profile.avatarUrl)
                            ProfileItem(
                                profile = profile,
                                onClick = { onProfileSelected(profile) },
                                onEditClick = {
                                    onEditProfile(profile.id)
                                },
                                onLongClick = {
                                    profileToDelete = profile
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }


            if (showDeleteDialog && profileToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        profileToDelete = null
                    },
                    title = { Text("Delete Profile") },
                    text = { Text("Are you sure you want to delete \"${profileToDelete?.name}\"?") },
                    confirmButton = {
                        TextButton(onClick = { profileToDelete?.let { onDeleteProfile(it) } }) {
                            Text("Delete", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            profileToDelete = null
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileItem(
    profile: Profile,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .size(140.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, // This is correct
            modifier = Modifier.fillMaxSize()
            ) {
            if (profile.avatarUrl.isNotBlank()) {
                Log.d(profile.avatarUrl, "RESPONSE_AVATAR")
                AsyncImage(
                    model = if (profile.avatarUrl.isNullOrBlank()) "https://picsum.photos/200" else profile.avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
//                        .background(
//                            brush = Brush.linearGradient(
//                                listOf(Color(0xFF24243e), Color(0xFF141E30))
//                            )
//                        )
//                    error = painterResource
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                ) {
                    Text(
                        text = profile.name.firstOrNull()?.uppercase() ?: "",
                        color = Color.White,
                        fontSize = 36.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = profile.name,
                color = Color.White,
                fontSize = 18.sp
            )
        }

        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Profile",
            tint = Color.White,
            modifier = Modifier
                .padding(4.dp)
                .size(20.dp)
                .clickable(onClick = onEditClick)
        )
    }
}

