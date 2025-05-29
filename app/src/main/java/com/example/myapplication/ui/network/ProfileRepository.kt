package com.example.myapplication.ui.network

import android.content.Context
import com.example.myapplication.ui.model.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object ProfileRepository {
    suspend fun fetchProfiles(token: String): Result<List<Profile>> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("http://10.0.2.2:3000/api/profiles")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        try {
            val response = OkHttpClient().newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string().orEmpty()
                val arr = JSONObject(body).optJSONArray("profiles") ?: JSONArray()
                val list = (0 until arr.length()).map {
                    val obj = arr.getJSONObject(it)
                    Profile(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        avatarUrl = obj.optString("avatarUrl")
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProfile(token: String, id: String): Result<Unit> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("http://10.0.2.2:3000/api/profiles/$id")
            .addHeader("Authorization", "Bearer $token")
            .delete()
            .build()
        try {
            val response = OkHttpClient().newCall(request).execute()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
