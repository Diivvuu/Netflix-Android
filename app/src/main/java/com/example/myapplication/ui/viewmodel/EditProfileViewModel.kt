package com.example.myapplication.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class EditProfileViewModel : ViewModel() {
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _avatarUrl = MutableStateFlow("")
    val avatarUrl: StateFlow<String> = _avatarUrl

    private val _isKid = MutableStateFlow(false)
    val isKid: StateFlow<Boolean> = _isKid

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadProfile(context: Context, profileId: String, token: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = OkHttpClient().newCall(
                        Request.Builder()
                            .url("http://10.0.2.2:3000/api/profiles/$profileId")
                            .addHeader("Authorization", "Bearer $token")
                            .get()
                            .build()
                    ).execute()
                    val bodyString = response.body?.string().orEmpty()
                    if (response.isSuccessful) {
                        val root = JSONObject(bodyString)
                        val obj = root.optJSONObject("profile") ?: JSONObject()
                        _name.value = obj.optString("name", "")
                        _avatarUrl.value = obj.optString("avatarUrl", "")
                        _isKid.value = obj.optBoolean("isKid", false)
                        _error.value = null
                    } else {
                        _error.value = "Error ${response.code}: $bodyString"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setName(value: String) { _name.value = value }
    fun setIsKid(value: Boolean) { _isKid.value = value }
    fun removeAvatar() { _avatarUrl.value = "" }

    fun uploadAvatar(context: Context, uri: Uri, token: String) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadError.value = null
            try {
                val result = withContext(Dispatchers.IO) {
                    val fileName = "avatar_${System.currentTimeMillis()}.jpg"
                    val fileType = "image/jpeg"
                    val requestJson = JSONObject().apply {
                        put("fileName", fileName)
                        put("fileType", fileType)
                        put("folder", "avatars")
                    }

                    val s3UrlRes = OkHttpClient().newCall(
                        Request.Builder()
                            .url("http://10.0.2.2:3000/api/upload/upload-url")
                            .addHeader("Authorization", "Bearer $token")
                            .post(requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                            .build()
                    ).execute()
                    val s3UrlJson = JSONObject(s3UrlRes.body?.string() ?: "")
                    val uploadUrl = s3UrlJson.getString("url")
                    val s3Key = s3UrlJson.getString("key")

                    // Read image bytes
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    if (bytes == null || bytes.isEmpty()) throw Exception("Failed to read image")

                    // Upload to S3
                    val putRes = OkHttpClient().newCall(
                        Request.Builder()
                            .url(uploadUrl)
                            .put(bytes.toRequestBody("image/jpeg".toMediaType()))
                            .build()
                    ).execute()

                    if (!putRes.isSuccessful) throw Exception("Failed to upload image")

                    s3Key // return S3 key (for backend storage)
                }

                _avatarUrl.value = result
            } catch (e: Exception) {
                _uploadError.value = "Image upload failed: ${e.message}"
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun updateProfile(context: Context, profileId: String, token: String, onSuccess: () -> Unit) {
        if (_name.value.isBlank()) {
            _error.value = "Profile name is required"
            return
        }
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val json = JSONObject().apply {
                        put("name", _name.value)
                        put("isKid", _isKid.value)
                        if (_avatarUrl.value.isNotBlank()) put("avatarUrl", _avatarUrl.value)
                    }
                    val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                    val response = OkHttpClient().newCall(
                        Request.Builder()
                            .url("http://10.0.2.2:3000/api/profiles/$profileId")
                            .addHeader("Authorization", "Bearer $token")
                            .put(body)
                            .build()
                    ).execute()
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        _error.value = "Error ${response.code}: ${response.body?.string().orEmpty()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
