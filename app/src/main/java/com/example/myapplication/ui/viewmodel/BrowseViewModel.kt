// BrowseViewModel.kt
package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.model.VideoItem
import com.example.myapplication.ui.network.BrowseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BrowseViewModel(application: Application) : AndroidViewModel(application) {
    private val _trending = MutableStateFlow<List<VideoItem>>(emptyList())
    val trending: StateFlow<List<VideoItem>> = _trending

    private val _recommended = MutableStateFlow<List<VideoItem>>(emptyList())
    val recommended: StateFlow<List<VideoItem>> = _recommended

    private val _recommended1 = MutableStateFlow<List<VideoItem>>(emptyList())
    val recommended1: StateFlow<List<VideoItem>> = _recommended1

    private val _topRatedTV = MutableStateFlow<List<VideoItem>>(emptyList())
    val topRatedTV: StateFlow<List<VideoItem>> = _topRatedTV

    private val _topRatedMovie = MutableStateFlow<List<VideoItem>>(emptyList())
    val topRatedMovie: StateFlow<List<VideoItem>> = _topRatedMovie

    private val _hero = MutableStateFlow<VideoItem?>(null)
    val hero: StateFlow<VideoItem?> = _hero


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchTrending(token: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = BrowseRepository.fetchTrending(token)
            _isLoading.value = false
            result.onSuccess { _trending.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun fetchRecommendedMovies(token: String, profileId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = BrowseRepository.fetchRecommendedMovies(token, profileId)
            _isLoading.value = false
            result.onSuccess { _recommended.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun fetchRecommendedTV(token: String, profileId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = BrowseRepository.fetchRecommendedTV(token, profileId)
            _isLoading.value = false
            result.onSuccess { _recommended1.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun fetchTopRatedTV(token: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = BrowseRepository.fetchTopRatedTV(token)
            _isLoading.value = false
            result.onSuccess { _topRatedTV.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun topRatedMovie(token: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = BrowseRepository.fetchTopRatedMovies(token)
            _isLoading.value = false
            result.onSuccess { _topRatedMovie.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun hero(token: String, profileId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = BrowseRepository.fetchHero(token, profileId)

            _isLoading.value = false
            result.onSuccess { heroItem ->
                _hero.value = heroItem
            }.onFailure {
                _error.value = it.message
            }
        }
    }



}
