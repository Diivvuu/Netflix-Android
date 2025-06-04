import com.example.myapplication.ui.model.Genre
import retrofit2.Response
import retrofit2.http.*

interface GenreApiService {
    @GET("genres/movies")
    suspend fun getMovieGenres(@Header("Authorization") token: String): List<Genre>

    @GET("genres/tv")
    suspend fun getTvGenres(@Header("Authorization") token: String): List<Genre>

    @GET("profiles/{profileId}/genres")
    suspend fun getProfileGenres(
        @Header("Authorization") token: String,
        @Path("profileId") profileId: String
    ): ProfileGenreResponse
}

interface ProfileApiService {
    @POST("profiles/{profileId}/movie-genres")
    suspend fun postMovieGenres(
        @Header("Authorization") token: String,
        @Path("profileId") profileId: String,
        @Body body: GenreIdsBody
    ): Response<Unit>

    @POST("profiles/{profileId}/tv-genres")
    suspend fun postTvGenres(
        @Header("Authorization") token: String,
        @Path("profileId") profileId: String,
        @Body body: GenreIdsBody
    ): Response<Unit>
}

data class GenreIdsBody(val genreIds: List<String>)
data class ProfileGenreResponse(val movieGenreIds: List<String>, val tvGenreIds: List<String>)
data class ApiResponse(val message: String, val count: Int)
