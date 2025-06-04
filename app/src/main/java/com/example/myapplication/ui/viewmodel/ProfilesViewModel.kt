package com.example.myapplication.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.model.GenreResponse
import com.example.myapplication.ui.model.Profile
import com.example.myapplication.ui.network.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ProfilesViewModel(application: Application) : AndroidViewModel(application) {

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchProfiles(token: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = ProfileRepository.fetchProfiles(token)
            _isLoading.value = false
            result.onSuccess { profilesList ->
                // --- LOG ALL PROFILES HERE ---
                Log.d("Profiles", "Fetched ${profilesList.size} profiles:")
                profilesList.forEachIndexed { idx, profile ->
                    Log.d("Profiles", "[$idx] Name: ${profile.name}, Id: ${profile.id}, AvatarUrl: ${profile.avatarUrl}")
                }
                // ----------------------------
                _profiles.value = profilesList
            }.onFailure {
                _error.value = it.message
            }
        }
    }

    suspend fun fetchProfileGenres(token: String, profileId: String): GenreResponse? {
        _isLoading.value = true
        val result = ProfileRepository.fetchProfileGenres(token, profileId)
        _isLoading.value = false
        return result.getOrNull()
    }


    fun deleteProfile(token: String, profile: Profile) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = ProfileRepository.deleteProfile(token, profile.id)
            _isLoading.value = false
            if (result.isSuccess) {
                _profiles.value = _profiles.value.filterNot { it.id == profile.id }
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }
}
