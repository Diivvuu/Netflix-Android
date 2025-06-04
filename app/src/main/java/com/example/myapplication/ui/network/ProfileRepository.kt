package com.example.myapplication.ui.network

import android.content.Context
import android.util.Log
import com.example.myapplication.ui.model.GenreResponse
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

    suspend fun fetchProfileGenres(token: String, profileId: String): Result<GenreResponse> = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val url = "http://10.0.2.2:3000/api/profiles/$profileId/genres"
            Log.d("GENRE_DEBUG", "Request URL: $url")
            Log.d("GENRE_DEBUG", "Token: $token")
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .build()
            val response = client.newCall(request).execute()
            Log.d("GENRE_DEBUG", "Response code: ${response.code}")
            Log.d("GENRE_DEBUG", "Response headers: ${response.headers}")

            if(!response.isSuccessful) throw Exception("Error fetching genres: HTTP ${response.code}")

            val body = response.body?.string() ?: throw Exception("Empty response")
            Log.d("GENRE_DEBUG", "Response body: $body")

            val json = JSONObject(body)
            val movieGenreIds = json.optJSONArray("movieGenreIds")
            val tvGenreIds  = json.optJSONArray("tvGenreIds")

            val movieGenres = mutableListOf<String>()
            val tvGenres = mutableListOf<String>()

            if(movieGenreIds != null) {
                for (i in 0 until movieGenreIds.length()) {
                    movieGenres.add(movieGenreIds.getString(i))
                }
            }
            if(tvGenreIds != null) {
                for (i in 0 until  tvGenreIds.length()) {
                    tvGenres.add(tvGenreIds.getString(i))
                }
            }

            Log.d("GENRE_DEBUG", "Parsed movieGenres: $movieGenres")
            Log.d("GENRE_DEBUG", "Parsed tvGenres: $tvGenres")

            Result.success(GenreResponse(movieGenres, tvGenres))
        } catch (e : Exception) {
            Log.e("GENRE_DEBUG", "Failed to fetch profile genres", e)
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
