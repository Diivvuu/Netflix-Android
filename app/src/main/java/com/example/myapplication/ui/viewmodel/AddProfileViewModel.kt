package com.example.myapplication.ui.viewmodel
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AddProfileViewModel : ViewModel() {
    private val _avatarUrl = MutableStateFlow("")
    val avatarUrl: StateFlow<String> = _avatarUrl

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError

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
//                    Log.d("UPLOAD_DEBUG", "Status: ${s3UrlRes.code} | Body: $responseBodyString")
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

    fun clearUploadState() {
        _avatarUrl.value = ""
        _isUploading.value = false
        _uploadError.value = null
    }
}
