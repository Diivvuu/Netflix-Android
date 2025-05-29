package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.NetflixRed
import androidx.compose.foundation.isSystemInDarkTheme
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import com.example.myapplication.ui.components.NetflixHeader
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.widget.Toast
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onNavigateBack: () -> Unit,
    isSignIn: Boolean = true
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var currentMode by remember { mutableStateOf(if (isSignIn) "Sign In" else "Sign Up") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val iconColor = if (isDarkTheme) Color.White else Color.Gray

    val context = LocalContext.current
    val activity = context as Activity
    val coroutineScope = rememberCoroutineScope()

    // Google Sign In Setup
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("838302072689-0n4lgih5vo4eb159a5tkv0h3d8u98flr.apps.googleusercontent.com")
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(activity, gso)

    fun handleAuthWithEmail(email : String, password : String, isSignIn : Boolean, onSuccess : () -> Unit, onError  : (String) -> Unit){
        coroutineScope.launch{
            isLoading  = true
            errorMessage = null
            try {
                val client = OkHttpClient()
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val bodyString = if(isSignIn) {
                    """
                    {
                        "email": "${email.trim()}",
                        "password": "$password"
                    }
                    """
                } else {
                    """{
                        "email" : "${email.trim()}",
                        "password" : "$password",
                        "name" : "${username.trim()}"
                    }"""
                }
                val body = bodyString.trimIndent().toRequestBody(mediaType)
//                val body = """
//                    {
//                    "email" : "${email.trim()}",
//                    "password" : "$password
//                    }
//                """.trimIndent().toRequestBody(mediaType)

                val endpoint = if(isSignIn) "signin" else "signup"
                val request = Request.Builder().url("http://10.0.2.2:3000/api/auth/$endpoint").post(body).build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        coroutineScope.launch {
                            isLoading = false
                            onError("Network error: ${e.message}")
                            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }

                    override  fun onResponse(call : Call, response : Response) {
                        coroutineScope.launch{
                            isLoading = false
                            if(response.isSuccessful){
                                val responseBody = response.body?.string()
                                Log.d("BACKEND_RESPONSE", responseBody ?: "No Body")
                                Toast.makeText(context, if (isSignIn) "Sign in successful!" else "Account Created!", Toast.LENGTH_SHORT).show()
                                val token = try {
                                    JSONObject(responseBody ?: "").optString("token")
                                } catch (e : Exception){ "" }
                                val prefs = context.getSharedPreferences("app_refs", Context.MODE_PRIVATE)
                                prefs.edit().putString("auth_token", token).apply()
                                val intent = Intent(context, ProfilesActivity::class.java)
                                context.startActivity(intent)
                                activity.finish()
                                onSuccess()
                            }else {
                                onError("Server error : ${response.code}")
                                Toast.makeText(context, "Server error: ${response.code}", Toast.LENGTH_LONG).show()
                                Log.e("BACKEND_CALL", "Error: ${response.code}")
                            }
                        }
                    }
                })
            } catch (e : Exception){
                coroutineScope.launch {
                    isLoading = false
                    onError("Error: ${e.message}")
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = true
        errorMessage = null
        
        try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            Log.d("GOOGLE_ACCOUNT", "ID Token: $idToken")
            if (idToken != null) {
                coroutineScope.launch {
                    try {
                        // Send token to backend
                        val client = OkHttpClient()
                        val mediaType = "application/json; charset=utf-8".toMediaType()
                        val body = "{\"token\":\"$idToken\"}".toRequestBody(mediaType)
                    
                        val request = Request.Builder()
                        .url("http://10.0.2.2:3000/api/auth/google")
                            .post(body)
                            .build()
                    
                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                coroutineScope.launch {
                                    isLoading = false
                                    errorMessage = "Network error: ${e.message}"
                                    Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                                Log.e("BACKEND_CALL", "Failed: ${e.message}")
                            }
                    
                            override fun onResponse(call: Call, response: Response) {
                                coroutineScope.launch {
                                    isLoading = false
                                    if (response.isSuccessful) {
                                        val responseBody = response.body?.string()
                                        Log.d("BACKEND_RESPONSE", responseBody ?: "No body")
                                        Toast.makeText(context, "Successfully signed in!", Toast.LENGTH_SHORT).show()
                                        val token = try {
                                            JSONObject(responseBody ?: "").optString("token")
                                        } catch (e :Exception){""}

                                        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                        prefs.edit().putString("auth_token", token).apply()

                                        val intent = Intent(context, ProfilesActivity::class.java)
                                        context.startActivity(intent)
                                        activity.finish()
                                    } else {
                                        errorMessage = "Server error: ${response.code}"
                                        Toast.makeText(context, "Server error: ${response.code}", Toast.LENGTH_LONG).show()
                                        Log.e("BACKEND_CALL", "Error: ${response.code}")
                                    }
                                }
                            }
                        })
                    } catch (e: Exception) {
                        coroutineScope.launch {
                            isLoading = false
                            errorMessage = "Error: ${e.message}"
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                        Log.e("GOOGLE_SIGN_IN", "Error: ${e.message}")
                    }
                }
            } else {
                isLoading = false
                errorMessage = "Failed to get ID token"
                Toast.makeText(context, "Failed to get ID token", Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            isLoading = false
            errorMessage = "Sign-in failed: ${e.statusCode}"
            Toast.makeText(context, "Sign-in failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
            Log.e("GOOGLE_SIGN_IN", "Sign-in failed: ${e.statusCode}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Netflix Header
            NetflixHeader(
                title = currentMode,
                showBackButton = true,
                onBackClick = onNavigateBack,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Error Message
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            if(currentMode == "Sign Up") {
                OutlinedTextField(
                    value = username,
                    onValueChange = {username = it},
                    label = {Text("Name", color = textColor)},
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = iconColor)
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = NetflixRed,
                        focusedLabelColor = NetflixRed,
                        cursorColor = NetflixRed,
                        focusedTextColor = textColor,
                        unfocusedBorderColor = if(isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Gray
                    )
                )
            }
            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = textColor) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = iconColor)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = NetflixRed,
                    focusedLabelColor = NetflixRed,
                    cursorColor = NetflixRed,
                    focusedTextColor = textColor,
                    unfocusedBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Gray
                )
            )

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = textColor) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = iconColor)
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = iconColor
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = NetflixRed,
                    focusedLabelColor = NetflixRed,
                    cursorColor = NetflixRed,
                    focusedTextColor = textColor,
                    unfocusedBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Gray
                )
            )

            // Primary Button
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    if(email.isBlank() || password.isBlank()){
                        isLoading = false
                        errorMessage = "Email and password cannot be empty"
                        return@Button
                    }
                    handleAuthWithEmail(
                        email = email,
                        password = password,
                        isSignIn = currentMode == "Sign In",
                        onSuccess = {

                        },
                        onError = {msg ->
                            errorMessage = msg
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(currentMode, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider with OR
            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(
                    Modifier.weight(1f),
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.3f)
                )
                Text(
                    "  OR  ",
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Gray
                )
                Divider(
                    Modifier.weight(1f),
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google Button
            OutlinedButton(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    try {
                        Log.d("GOOGLE_SIGN_IN", "Launching Google Sign-In")
                        val signInIntent = googleSignInClient.signInIntent
                        launcher.launch(signInIntent)
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = "Failed to start Google Sign-In: ${e.message}"
                        Toast.makeText(context, "Failed to start Google Sign-In: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = textColor
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.3f))
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = textColor
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Icon",
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text("Continue with Google", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Toggle
            TextButton(onClick = {
                currentMode = if (currentMode == "Sign In") "Sign Up" else "Sign In"
            }) {
                Text(
                    text = if (currentMode == "Sign In") "Don't have an account? Sign Up"
                    else "Already have an account? Sign In",
                    fontSize = 14.sp,
                    color = NetflixRed
                )
            }
        }
    }
}

