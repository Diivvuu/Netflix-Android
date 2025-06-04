package com.example.myapplication.ui.network

import retrofit2.http.*

interface GenreRepository {
    @GET("genres/movies")
    suspend fun getMovieGenres(@Header("Authorization") token: String): List<Genre>

    @GET("genres/tv")
    suspend fun getTvGenres(@Header("Authorization") token: String): List<Genre>

    @GET("profiles/{profileId}/genres")
    suspend fun getProfileGenres(
        @Header("Authorization") token: String,
        @Path("profileId") profileId: String
    ): ProfileGenreResponse

    @POST("profiles/{profileId}/movie-genres")
    suspend fun postMovieGenres(
        @Header("Authorization") token: String,
        @Path("profileId") profileId: String,
        @Body body: GenreIdsBody
    ): ApiResponse

    @POST("profiles/{profileId}/tv-genres")
    suspend fun postTvGenres(
        @Header("Authorization") token: String,
        @Path("profileId") profileId: String,
        @Body body: GenreIdsBody
    ): ApiResponse
}

data class GenreIdsBody(val genreIds: List<Int>)
data class ProfileGenreResponse(val movieGenreIds: List<Int>, val tvGenreIds: List<Int>)
data class ApiResponse(val message: String, val count: Int)
