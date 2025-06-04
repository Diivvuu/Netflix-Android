// GenreSelectionViewModel.kt
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.model.Genre
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GenreSelectionViewModel(
    private val api: GenreApiService,
    private val profileAPi : ProfileApiService
) : ViewModel() {

    // UI state
    val movieGenres = MutableStateFlow<List<Genre>>(emptyList())
    val tvGenres = MutableStateFlow<List<Genre>>(emptyList())

    val selectedMovieGenres = MutableStateFlow<Set<String>>(emptySet())
    val selectedTvGenres = MutableStateFlow<Set<String>>(emptySet())

    val isLoading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    val successMessage = MutableStateFlow<String?>(null)

    fun loadGenres(token: String, profileId: String) {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            try {
                Log.d("GenreDebug", "Calling getMovieGenres")
                val movieList = api.getMovieGenres("Bearer $token")
                Log.d("GenreDebug", "Movie genres response: $movieList")

                Log.d("GenreDebug", "Calling getTvGenres")
                val tvList = api.getTvGenres("Bearer $token")
                Log.d("GenreDebug", "TV genres response: $tvList")

                movieGenres.value = movieList
                tvGenres.value = tvList

                Log.d("GenreDebug", "Calling getProfileGenres for profileId: $profileId")
                val userPrefs = api.getProfileGenres("Bearer $token", profileId)
                Log.d("GenreDebug", "User genre prefs: $userPrefs")

                selectedMovieGenres.value = userPrefs.movieGenreIds.toSet()
                selectedTvGenres.value = userPrefs.tvGenreIds.toSet()

                Log.d("GenreDebug", "Loaded all genres successfully")
            } catch (e: Exception) {
                Log.e("GenreDebug", "Failed to load genres: ${e.localizedMessage}", e)
                error.value = "Failed to load genres: ${e.localizedMessage}"
            }
            isLoading.value = false
        }
    }


    fun toggleMovieGenre(id: String) {
        selectedMovieGenres.value = selectedMovieGenres.value
            .let { if (id in it) it - id else it + id }
    }

    fun toggleTvGenre(id: String) {
        selectedTvGenres.value = selectedTvGenres.value
            .let { if (id in it) it - id else it + id }
    }

    fun submitGenres(token: String, profileId: String) {
        viewModelScope.launch {
            Log.d("called", token)
            Log.d("called", profileId)
                isLoading.value = true
            error.value = null
            try {
                Log.d("GenreSubmit", "Called submitGenres")
                Log.d("GenreSubmit", "Token: $token")
                Log.d("GenreSubmit", "ProfileId: $profileId")
                Log.d("GenreSubmit", "Movie genre IDs: ${selectedMovieGenres.value}")
                Log.d("GenreSubmit", "TV genre IDs: ${selectedTvGenres.value}")
                Log.d("GenreSubmit", "Endpoint for movie genres: /profiles/$profileId/movie-genres")
                Log.d("GenreSubmit", "Endpoint for tv genres: /profiles/$profileId/tv-genres")
                profileAPi.postMovieGenres("Bearer $token", profileId, GenreIdsBody(selectedMovieGenres.value.toList()))
                profileAPi.postTvGenres("Bearer $token", profileId, GenreIdsBody(selectedTvGenres.value.toList()))
                successMessage.value = "Preferences saved!"
            } catch (e: Exception) {
                error.value = "Failed to save: ${e.localizedMessage}"
            }
            isLoading.value = false
        }
    }
}
