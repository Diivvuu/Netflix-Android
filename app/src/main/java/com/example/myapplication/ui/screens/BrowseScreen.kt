package com.example.myapplication.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.ui.viewmodel.BrowseViewModel
import com.example.myapplication.ui.model.VideoItem

@Composable
fun BrowseScreen(profileId: String, viewModel: BrowseViewModel = viewModel()) {
    val trending by viewModel.trending.collectAsState()
    val recommended by viewModel.recommended.collectAsState()
    val recommended1 by viewModel.recommended1.collectAsState()
    val topRatedTV by viewModel.topRatedTV.collectAsState()
    val topRatedMovie by viewModel.topRatedMovie.collectAsState()
    val hero by viewModel.hero.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val token = prefs.getString("auth_token", null)

    LaunchedEffect(Unit) {
        if (token != null) {
            viewModel.fetchRecommendedMovies(token, profileId)
            viewModel.fetchRecommendedTV(token, profileId)
            viewModel.fetchTrending(token)
            viewModel.fetchTopRatedTV(token)
//        viewModel.fetchTopRatedTV(token)
            viewModel.topRatedMovie(token)
            viewModel.hero(token, profileId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
    ) {
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else if (error != null) {
            Text(
                text = "Error: $error",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            HeroBanner(hero = hero) // 👈 Add this
            Spacer(modifier = Modifier.height(12.dp))
            Section("Trending Now", trending)
            Section("Recommended Movies", recommended)
            Section("Recommended TV Shows", recommended1)
            Section("Top Rated TV Shows", topRatedTV)
            Section("Top Rated Movie", topRatedMovie)
        }
    }

}
@Composable
fun HeroBanner(hero: VideoItem?) {
    if (hero == null) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(hero.posterUrl),
            contentDescription = hero.title,
            modifier = Modifier.fillMaxSize()
        )

        // Dark overlay gradient from top and bottom for cinematic look
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0x80000000), // top fade
                        0.4f to Color.Transparent,
                        0.9f to Color(0xFF000000)  // bottom fade
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 24.dp)
        ) {
            Text(
                text = hero.title,
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                Button(
                    onClick = { /* Play action */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("▶ Play", color = Color.Black)
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = { /* Info action */ },
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("ℹ More Info")
                }
            }
        }
    }
}

@Composable
fun Section(title: String, items: List<VideoItem>) {
    val context = LocalContext.current

    Text(
        text = title,
        color = Color.White,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 8.dp)
    ) {
        items(items) { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(140.dp)
                    .padding(end = 10.dp)
                    .clickable {
                        val intent = Intent(context, VideoDetailActivity::class.java).apply {
                            putExtra("video_id", item.id)
                            putExtra("video_title", item.title)
                            putExtra("video_type", item.media_type) // e.g., "movie" or "tv"
                        }
                        context.startActivity(intent)
                    }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(item.posterUrl),
                    contentDescription = item.title,
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.title,
                    color = Color.LightGray,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

