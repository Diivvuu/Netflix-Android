package com.example.myapplication.ui.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.example.myapplication.R
import com.example.myapplication.ui.model.VideoDetail
import com.example.myapplication.ui.viewmodel.VideoDetailViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import androidx.compose.ui.viewinterop.AndroidView
import coil.request.ImageRequest
import com.example.myapplication.MainActivity

class VideoDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val videoId = intent.getStringExtra("video_id") ?: return
        val videoType = intent.getStringExtra("video_type") ?: return
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = prefs.getString("auth_token", null) ?: return

        setContent {
            val viewModel: VideoDetailViewModel = viewModel()
            val videoDetail by viewModel.videoDetail.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val error by viewModel.error.collectAsState()
            val trailerUrl by viewModel.trailerUrl.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.loadVideoDetails(token, videoType, videoId)
            }

            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                when {
                    isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = Color.Red)
                    }
                    error != null -> Text("Error: $error", color = Color.Red)
                    videoDetail != null -> trailerUrl?.let {
                        VideoDetailContent(
                            detail = videoDetail!!,
                            type = videoType,
                            trailerUrl = it,
                            onBackClick = { finish() },
                            onLogoutClick = {
                                val editor = prefs.edit()
                                editor.remove("auth_token")
                                editor.apply()
                                val intent = Intent(this@VideoDetailActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoDetailContent(
    detail: VideoDetail,
    type: String,
    trailerUrl: String,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val trailerVideoId = remember(trailerUrl) {
        trailerUrl.substringAfter("v=")
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .background(Color.Black)
            .fillMaxSize()
    ) {
        // Netflix-style header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Back Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Netflix Logo
            Image(
                painter = painterResource(id = R.drawable.netflix_logo),
                contentDescription = "Netflix",
                modifier = Modifier
                    .height(32.dp)
                    .align(Alignment.Center)
            )

            // Logout Button
            IconButton(
                onClick = onLogoutClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = Color.White
                )
            }
        }

        // YouTube Player
        if (trailerVideoId.isNotEmpty()) {
            Box {
                AndroidView(
                    factory = { ctx ->
                        val view = YouTubePlayerView(ctx)
                        (ctx as? ComponentActivity)?.lifecycle?.addObserver(view)
                        view.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(player: YouTubePlayer) {
                                player.cueVideo(trailerVideoId, 0f)
                            }
                        })
                        view
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )

                IconButton(
                    onClick = {
                        val intent = Intent(context, YouTubeFullscreenActivity::class.java)
                        intent.putExtra("video_id", trailerVideoId)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color.White)
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            detail.title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            "⭐ ${detail.rating}  •  ${detail.genres.joinToString()}  •  📅 ${detail.releaseDate ?: "N/A"}",
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            detail.description,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (detail.cast.isNotEmpty()) {
            Text(
                "Cast",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                detail.cast.forEach { cast ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(context)
                                    .data(cast.profile)
                                    .crossfade(true)
                                    .transformations(RoundedCornersTransformation(8f))
                                    .scale(Scale.FILL)
                                    .build()
                            ),
                            contentDescription = cast.name,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            cast.name,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "as ${cast.character}",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (type == "tv" && detail.seasons.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Seasons",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            detail.seasons.forEach {
                Row(
                    modifier = Modifier
                        .padding(vertical = 6.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(it.poster),
                        contentDescription = it.name,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(it.name, color = Color.White)
                        Text("${it.episodeCount} episodes", color = Color.Gray)
                    }
                }
            }
        }
    }

    BackHandler {
        onBackClick()
    }
}
