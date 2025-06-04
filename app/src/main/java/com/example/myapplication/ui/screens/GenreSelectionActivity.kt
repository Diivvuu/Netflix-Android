package com.example.myapplication.ui.screens;

import GenreApiService
import GenreSelectionViewModel
import ProfileApiService
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.model.Genre
import com.example.myapplication.ui.theme.NetflixCloneTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.accompanist.flowlayout.FlowRow


class GenreSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get from Intent
        val profileId = intent.getStringExtra("profileId") ?: return
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = prefs.getString("auth_token", null) ?: return

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api  = retrofit.create(GenreApiService::class.java)

        val profileretrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val profileApi = profileretrofit.create(ProfileApiService::class.java)

        // Replace with DI/Hilt in production
//        val api = /* build your Retrofit GenreApiService here */
        val viewModel = GenreSelectionViewModel(api, profileApi)

        setContent {
            NetflixCloneTheme  {
                var shouldFinish by remember { mutableStateOf(false) }
                if (shouldFinish) {
                    val intent = Intent(this, NetflixMainActivity::class.java)
                    intent.putExtra("profileId", profileId)
                    startActivity(intent)
                    finish()
                }
                GenreSelectionScreen(
                    profileId = profileId,
                    token = token,
                    viewModel = viewModel,
                    onBack = { finish() },
                    onPreferencesSaved = { shouldFinish = true }
                )
            }
        }


    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreSelectionScreen(
    profileId: String,
    token: String,
    viewModel: GenreSelectionViewModel,
    onBack: () -> Unit,
    onPreferencesSaved: () -> Unit
) {
    val movieGenres by viewModel.movieGenres.collectAsState()
    val tvGenres by viewModel.tvGenres.collectAsState()
    val selectedMovieGenres by viewModel.selectedMovieGenres.collectAsState()
    val selectedTvGenres by viewModel.selectedTvGenres.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // POLLING: Keep fetching genres every 2 seconds until data is loaded
    LaunchedEffect(Unit) {
//        while (movieGenres.isEmpty() || tvGenres.isEmpty()) {
            viewModel.loadGenres(token, profileId)
//            kotlinx.coroutines.delay(2000)
//        }
    }

    LaunchedEffect(successMessage) {
        if (!successMessage.isNullOrBlank()) {
            kotlinx.coroutines.delay(600)
            onPreferencesSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Your Genres") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                if (movieGenres.isEmpty() || tvGenres.isEmpty()) {
                    // Always loading until genres are loaded
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    // DO NOT show error, just render content when data is available
                    successMessage?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Movie Genres", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    GenreRow(
                        genres = movieGenres,
                        selected = selectedMovieGenres,
                        onToggle = { viewModel.toggleMovieGenre(it) }
                    )
                    Spacer(Modifier.height(24.dp))
                    Text("TV Genres", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    GenreRow(
                        genres = tvGenres,
                        selected = selectedTvGenres,
                        onToggle = { viewModel.toggleTvGenre(it) }
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { viewModel.submitGenres(token, profileId) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Save Preferences")
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenreRow(
    genres: List<Genre>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        mainAxisSpacing = 12.dp,
        crossAxisSpacing = 12.dp
    ) {
        genres.forEach { genre ->
            val isSelected = genre.id in selected
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(genre.id) },
                label = {
                    Text(
                        genre.name,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    containerColor = Color.Transparent,
                    labelColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier
                    .height(38.dp)
            )
        }
    }
}


