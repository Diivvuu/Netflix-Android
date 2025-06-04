    package com.example.myapplication.ui.screens

    import android.app.Activity
    import android.content.Context
    import android.content.Intent
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
    import androidx.compose.material3.CheckboxDefaults.colors
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.text.input.ImeAction
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.lifecycle.viewmodel.compose.viewModel
    import coil.compose.AsyncImage
    import com.example.myapplication.ui.components.NetflixHeader
    import com.example.myapplication.ui.viewmodel.AddProfileViewModel
    import kotlinx.coroutines.launch
    import okhttp3.*
    import okhttp3.MediaType.Companion.toMediaType
    import okhttp3.RequestBody.Companion.toRequestBody
    import org.json.JSONObject
    import java.io.IOException

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

    class AddProfileActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                MaterialTheme(colorScheme = NetflixColorScheme) {
                    AddProfileScreen(
                        onProfileAdded = {
    //                        val intent = Intent(this, ProfilesActivity::class.java)
    //                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
    //                        startActivity(intent)
                            setResult(Activity.RESULT_OK)
                            finish()

                        },
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AddProfileScreen(
        viewModel: AddProfileViewModel = viewModel(),
        onProfileAdded: () -> Unit,
        onBackPressed: () -> Unit
    ) {
        var name by remember { mutableStateOf("") }
        var isKid by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        // State from ViewModel
        val avatarUrl by viewModel.avatarUrl.collectAsState()
        val isUploading by viewModel.isUploading.collectAsState()
        val uploadError by viewModel.uploadError.collectAsState()

        // Image picker launcher
        val pickImageLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri: Uri? ->
                if (uri != null) {
                    selectedImageUri = uri
                    // Get token from SharedPreferences
                    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    val token = prefs.getString("auth_token", null) ?: ""
                    viewModel.uploadAvatar(context, uri, token)
                }
            }
        )

        BackHandler(enabled = true) {
            onBackPressed()
        }

        fun addProfile() {
            if (name.isBlank()) {
                error = "Profile name is required"
                return
            }
            isLoading = true
            error = null

            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val token = prefs.getString("auth_token", null)
            if (token.isNullOrBlank()) {
                error = "Authentication required"
                isLoading = false
                return
            }

            val json = JSONObject().apply {
                put("name", name)
                if (avatarUrl.isNotBlank()) put("avatarUrl", avatarUrl)
                put("isKid", isKid)
            }
            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            OkHttpClient().newCall(
                Request.Builder()
                    .url("http://10.0.2.2:3000/api/profiles")
                    .addHeader("Authorization", "Bearer $token")
                    .post(body)
                    .build()
            ).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    scope.launch {
                        isLoading = false
                        error = "Network error: ${e.message}"
                    }
                }
                override fun onResponse(call: Call, response: Response) {
                    scope.launch {
                        isLoading = false
                        if (response.isSuccessful) {
                            onProfileAdded()
                        } else {
                            error = "Error ${response.code}: ${response.body?.string().orEmpty()}"
                        }
                    }
                }
            })
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NetflixBlack),
                    actions = {
                        NetflixHeader()
                    }
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
                Text("Add a new profile", color = Color.White, fontSize = 20.sp)

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", color = NetflixLightGray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                // Avatar picker and upload preview
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(NetflixDarkGray)
                        .clickable(enabled = !isUploading) { pickImageLauncher.launch("image/*") }
                ) {
                    when {
                        avatarUrl.isNotBlank() -> {
                            AsyncImage(
                                model = "https://kndole-dev-safe.s3.amazonaws.com/$avatarUrl", // or use signed URL
                                contentDescription = "Avatar",
                                modifier = Modifier.size(96.dp)
                            )
                        }
                        selectedImageUri != null -> {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Avatar (Selected)",
                                modifier = Modifier.size(96.dp)
                            )
                        }
                        else -> {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Upload Avatar",
                                tint = NetflixLightGray,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    if (isUploading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                Text("Tap to upload avatar", color = NetflixLightGray, fontSize = 13.sp)
                if (uploadError != null) {
                    Text(uploadError!!, color = Color.Red, fontSize = 13.sp)
                }

                // Kid profile checkbox
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isKid,
                        onCheckedChange = { isKid = it },
                        colors = colors(
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
                    onClick = { addProfile() },
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
                        Text("Add Profile", fontSize = 16.sp)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun textFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
        focusedTextColor = Color.White,
        cursorColor = NetflixRed,
        focusedBorderColor = NetflixRed,
        unfocusedBorderColor = NetflixLightGray,
        focusedPlaceholderColor = NetflixLightGray
    )
