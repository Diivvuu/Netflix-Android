// BrowseRepository.kt
package com.example.myapplication.ui.network

import android.util.Log
import com.example.myapplication.ui.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object BrowseRepository {
    suspend fun fetchTrending(token: String): Result<List<VideoItem>> = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("http://10.0.2.2:3000/api/browse/trending")
                .addHeader("Authorization", "Bearer $token")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

            val jsonString = response.body?.string() ?: throw Exception("Empty response")
            val json = JSONObject(jsonString)
            val results = json.optJSONArray("results")

            val videoList = mutableListOf<VideoItem>()
            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                videoList.add(
                    VideoItem(
                        id = item.optString("id"),
                        title = item.optString("title", item.optString("name")), // support movie & tv
                        posterUrl = "https://image.tmdb.org/t/p/w500${item.optString("poster_path")}",
                        media_type = item.optString("media_type", item.optString("media_type"))
                    )
                )
            }
            Result.success(videoList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchRecommendedMovies(token: String, profileId: String): Result<List<VideoItem>> = withContext(Dispatchers.IO) {
            val TAG = "RECOMMENDED_API"
            try {
                val client = OkHttpClient()
                val url = "http://10.0.2.2:3000/api/browse/movie/$profileId"

                Log.d(TAG, "Request URL: $profileId")
                Log.d(TAG, "Request URL: $url")
                Log.d(TAG, "Authorization Token: $token")

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                val response = client.newCall(request).execute()

                Log.d(TAG, "Response Code: ${response.code}")
                Log.d(TAG, "Response Message: ${response.message}")
                Log.d(TAG, "Response Headers: ${response.headers}")

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    Log.e(TAG, "Error Body: $errorBody")
                    throw Exception("HTTP ${response.code}: $errorBody")
                }

                val body = response.body?.string() ?: throw Exception("Empty response")
                Log.d(TAG, "Response Body: $body")

                val json = JSONObject(body).optJSONArray("results") ?: return@withContext Result.success(emptyList())

                val items = List(json.length()) { i ->
                    val obj = json.getJSONObject(i)
                    VideoItem(
                        id = obj.optString("id"),
                        title = obj.optString("title", obj.optString("name")),
                        posterUrl = "https://image.tmdb.org/t/p/w500${obj.optString("poster_path")}",
                        media_type = "movie"
                    )
                }

                Result.success(items)
            } catch (e: Exception) {
                Log.e(TAG, "Exception occurred: ${e.message}", e)
                Result.failure(e)
            }
        }

    suspend fun fetchRecommendedTV(token: String, profileId: String): Result<List<VideoItem>> = withContext(Dispatchers.IO) {
        val TAG = "RECOMMENDED_API"
        try {
            val client = OkHttpClient()
            val url = "http://10.0.2.2:3000/api/browse/tv/$profileId"

            Log.d(TAG, "Request URL: $profileId")
            Log.d(TAG, "Request URL: $url")
            Log.d(TAG, "Authorization Token: $token")

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .build()

            val response = client.newCall(request).execute()

            Log.d(TAG, "Response Code: ${response.code}")
            Log.d(TAG, "Response Message: ${response.message}")
            Log.d(TAG, "Response Headers: ${response.headers}")

            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                Log.e(TAG, "Error Body: $errorBody")
                throw Exception("HTTP ${response.code}: $errorBody")
            }

            val body = response.body?.string() ?: throw Exception("Empty response")
            Log.d(TAG, "Response Body: $body")

            val json = JSONObject(body).optJSONArray("results") ?: return@withContext Result.success(emptyList())

            val items = List(json.length()) { i ->
                val obj = json.getJSONObject(i)
                VideoItem(
                    id = obj.optString("id"),
                    title = obj.optString("title", obj.optString("name")),
                    posterUrl = "https://image.tmdb.org/t/p/w500${obj.optString("poster_path")}",
                    media_type = "tv"

                )
            }

            Result.success(items)
        } catch (e: Exception) {
            Log.e(TAG, "Exception occurred: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchTopRatedTV(token: String, ): Result<List<VideoItem>> = withContext(Dispatchers.IO) {
        val TAG = "RECOMMENDED_API"
        try {
            val client = OkHttpClient()
            val url = "http://10.0.2.2:3000/api/browse/top-rated/tv"

            Log.d(TAG, "Request URL: $url")
            Log.d(TAG, "Authorization Token: $token")

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .build()

            val response = client.newCall(request).execute()

            Log.d(TAG, "Response Code: ${response.code}")
            Log.d(TAG, "Response Message: ${response.message}")
            Log.d(TAG, "Response Headers: ${response.headers}")

            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                Log.e(TAG, "Error Body: $errorBody")
                throw Exception("HTTP ${response.code}: $errorBody")
            }

            val body = response.body?.string() ?: throw Exception("Empty response")
            Log.d(TAG, "Response Body: $body")

            val json = JSONObject(body).optJSONArray("results") ?: return@withContext Result.success(emptyList())

            val items = List(json.length()) { i ->
                val obj = json.getJSONObject(i)
                VideoItem(
                    id = obj.optString("id"),
                    title = obj.optString("title", obj.optString("name")),
                    posterUrl = "https://image.tmdb.org/t/p/w500${obj.optString("poster_path")}",
                    media_type = "tv"
                )
            }

            Result.success(items)
        } catch (e: Exception) {
            Log.e(TAG, "Exception occurred: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchTopRatedMovies(token: String): Result<List<VideoItem>> = withContext(Dispatchers.IO) {
        val TAG = "RECOMMENDED_API"
        try {
            val client = OkHttpClient()
            val url = "http://10.0.2.2:3000/api/browse/top-rated/movie"

            Log.d(TAG, "Request URL: $url")
            Log.d(TAG, "Authorization Token: $token")

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .build()

            val response = client.newCall(request).execute()

            Log.d(TAG, "Response Code: ${response.code}")
            Log.d(TAG, "Response Message: ${response.message}")
            Log.d(TAG, "Response Headers: ${response.headers}")

            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                Log.e(TAG, "Error Body: $errorBody")
                throw Exception("HTTP ${response.code}: $errorBody")
            }

            val body = response.body?.string() ?: throw Exception("Empty response")
            Log.d(TAG, "Response Body: $body")

            val json = JSONObject(body).optJSONArray("results") ?: return@withContext Result.success(emptyList())

            val items = List(json.length()) { i ->
                val obj = json.getJSONObject(i)
                VideoItem(
                    id = obj.optString("id"),
                    title = obj.optString("title", obj.optString("name")),
                    posterUrl = "https://image.tmdb.org/t/p/w500${obj.optString("poster_path")}",
                    media_type = "movie"

                )
            }

            Result.success(items)
        } catch (e: Exception) {
            Log.e(TAG, "Exception occurred: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchHero(token: String, profileId: String): Result<VideoItem> = withContext(Dispatchers.IO) {
        val TAG = "HERO_API"
        try {
            val client = OkHttpClient()
            val url = "http://10.0.2.2:3000/api/browse/hero/$profileId"

            Log.d(TAG, "Request URL: $url")
            Log.d(TAG, "Authorization Token: $token")

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .build()

            val response = client.newCall(request).execute()

            Log.d(TAG, "Response Code: ${response.code}")
            Log.d(TAG, "Response Message: ${response.message}")
            Log.d(TAG, "Response Headers: ${response.headers}")

            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                Log.e(TAG, "Error Body: $errorBody")
                throw Exception("HTTP ${response.code}: $errorBody")
            }

            val body = response.body?.string() ?: throw Exception("Empty response")
            Log.d(TAG, "Response Body: $body")

            val obj = JSONObject(body)
            val videoItem = VideoItem(
                id = obj.optString("id"),
                title = obj.optString("title", obj.optString("name")),
                posterUrl = "https://image.tmdb.org/t/p/w500${obj.optString("poster_path")}",
                media_type = obj.optString("media_type", obj.optString("media_type"))
            )

            Result.success(videoItem)

        } catch (e: Exception) {
            Log.e(TAG, "Exception occurred: ${e.message}", e)
            Result.failure(e)
        }
    }

}




