package com.example.myapplication.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.ComponentActivity.MODE_PRIVATE
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.MainActivity
import com.example.myapplication.ui.components.NetflixHeader
import com.example.myapplication.ui.theme.NetflixCloneTheme

class NetflixMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val profileId = intent.getStringExtra("profileId") ?: "abc123"
        setContent {
            NetflixCloneTheme (darkTheme = true){
                NetflixMainScreen(profileId)
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetflixMainScreen(profileId: String) {
    val tabs = listOf(
        BottomNavItem("Browse", Icons.Default.Home, { BrowseScreen(profileId) }),
        BottomNavItem("Watchlist", Icons.Default.Bookmark, { WatchlistScreen() }),
        BottomNavItem("Settings", Icons.Default.Settings, { SettingsScreen() })
    )
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val prefs = context.getSharedPreferences("app_prefs", 0)


    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            NetflixHeader(
                showBackButton = false,
                showLogoutButton = true,
                onLogoutClick = {
                    // TODO: Add logout logic or dialog
                    prefs.edit().remove("auth_token").apply()
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    activity?.finish()
                }
            )
        },
        bottomBar = {
            NavigationBar(
                tonalElevation = 4.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                tabs[selectedTab].screen()
            }
        }
    )
}

@Composable
fun WatchlistScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Your Watchlist", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun SettingsScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Settings / Switch Profile", style = MaterialTheme.typography.headlineMedium)
    }
}
