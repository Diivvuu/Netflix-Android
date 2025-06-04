package com.example.myapplication.ui.network

import android.util.Log
import com.example.myapplication.ui.model.CastMember
import com.example.myapplication.ui.model.Season
import com.example.myapplication.ui.model.VideoDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

object VideoRepository {
    suspend fun fetchVideoDetails(
        token: String,
        type: String,
        id: String
    ): VideoDetail = withContext(Dispatchers.IO) {
        val url = "http://10.0.2.2:3000/api/browse/details/$type/$id"
        Log.d("url", url)
        Log.d("tattaid", id)
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed: ${response.code}")
            val jsonStr = response.body?.string() ?: throw Exception("Empty body")
            val obj = JSONObject(jsonStr)

            val castArray = obj.optJSONArray("cast") ?: JSONArray()
            val cast = List(castArray.length()) { i ->
                val c = castArray.getJSONObject(i)
                CastMember(
                    name = c.optString("name"),
                    character = c.optString("character"),
                    profile = c.optString("profile", null)
                )
            }

            val seasonsArray = obj.optJSONArray("seasons") ?: JSONArray()
            val seasons = List(seasonsArray.length()) { i ->
                val s = seasonsArray.getJSONObject(i)
                Season(
                    seasonNumber = s.optInt("seasonNumber"),
                    episodeCount = s.optInt("episodeCount"),
                    name = s.optString("name"),
                    poster = s.optString("poster", null)
                )
            }

            return@withContext VideoDetail(
                id = obj.optString("id"),
                type = obj.optString("type"),
                title = obj.optString("title"),
                description = obj.optString("description"),
                posterUrl = obj.optString("posterUrl"),
                backdropUrl = obj.optString("backdropUrl"),
                releaseDate = obj.optString("releaseDate"),
                genres = obj.optJSONArray("genres")?.let { genresArray ->
                    List(genresArray.length()) { j -> genresArray.getString(j) }
                } ?: emptyList(),
                rating = obj.optDouble("rating", 0.0),
                cast = cast,
                seasons = seasons
            )
        }
    }

    suspend fun fetchTrailerUrl(token: String, type: String, id: String): String? = withContext(Dispatchers.IO) {
        val url = "http://10.0.2.2:3000/api/video/details/$type/$id/trailer"
        Log.d("TrailerDebug", "▶️ Trailer fetch started")
        Log.d("TrailerDebug", "➡️ URL: $url")
        Log.d("TrailerDebug", "➡️ Type: $type, ID: $id")
        Log.d("TrailerDebug", "➡️ Token: Bearer $token")

        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .build()

            client.newCall(request).execute().use { response ->
                Log.d("TrailerDebug", "⬅️ Response code: ${response.code}")

                val body = response.body?.string()
                Log.d("TrailerDebug", "⬅️ Response body: $body")

                if (!response.isSuccessful || body == null) {
                    Log.e("TrailerDebug", "❌ Failed to fetch trailer")
                    return@withContext null
                }

                val obj = JSONObject(body)
                val trailerUrl = obj.optString("trailerUrl", null)
                Log.d("TrailerDebug", "✅ Trailer URL: $trailerUrl")

                return@withContext trailerUrl
            }
        } catch (e: Exception) {
            Log.e("TrailerDebug", "❌ Exception occurred: ${e.message}", e)
            return@withContext null
        }
    }


}
