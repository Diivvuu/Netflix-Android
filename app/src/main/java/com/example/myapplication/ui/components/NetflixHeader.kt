// NetflixHeader.kt
package com.example.myapplication.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun NetflixHeader(
    title: String? = null,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    showLogoutButton: Boolean = true,
    onLogoutClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.White

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Back Button
        if (showBackButton && onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }
        }

        // Netflix Logo
        Image(
            painter = painterResource(id = R.drawable.netflix_logo),
            contentDescription = "Netflix",
            modifier = Modifier
                .height(32.dp)
                .align(Alignment.Center)
        )

        // Optional Title
        if (title != null) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = if (showLogoutButton) 48.dp else 0.dp)
            )
        }

        // Logout Button (top right)
        if (showLogoutButton && onLogoutClick != null) {
            IconButton(
                onClick = onLogoutClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = textColor
                )
            }
        }
    }
}
