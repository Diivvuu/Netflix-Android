package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.myapplication.ui.screens.AuthScreen
import com.example.myapplication.ui.screens.OnboardingScreen
import com.example.myapplication.ui.theme.NetflixCloneTheme
import com.example.myapplication.ui.screens.SplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NetflixCloneTheme  {
                var currentScreen by remember { mutableStateOf("splash") }
                var isSignIn by remember { mutableStateOf(true) }

                when (currentScreen) {
                    "splash" -> SplashScreen {
                        currentScreen = "onboarding"
                    }
                    "onboarding" -> {
                        OnboardingScreen(
                            onNavigateToSignIn = {
                                isSignIn = true
                                currentScreen = "auth"
                            },
                            onNavigateToSignUp = {
                                isSignIn = false
                                currentScreen = "auth"
                            }
                        )
                    }
                    "auth" -> {
                        AuthScreen(
                            isSignIn = isSignIn,
                            onNavigateBack = {
                                currentScreen = "onboarding"
                            }
                        )
                    }
                }
            }
        }
    }
}
