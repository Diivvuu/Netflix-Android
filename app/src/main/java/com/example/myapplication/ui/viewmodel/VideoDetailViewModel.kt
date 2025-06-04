package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.model.VideoDetail
import com.example.myapplication.ui.network.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoDetailViewModel : ViewModel() {
    private val _videoDetail = MutableStateFlow<VideoDetail?>(null)
    val videoDetail = _videoDetail.asStateFlow()

    private val _trailerUrl = MutableStateFlow<String?>(null)
    val trailerUrl: StateFlow<String?> = _trailerUrl

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadVideoDetails(token: String, type: String, id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true // ✅ FIX: use _isLoading
                val detail = VideoRepository.fetchVideoDetails(token, type, id)
                _videoDetail.value = detail
                _trailerUrl.value = VideoRepository.fetchTrailerUrl(token, type, id)
            } catch (e: Exception) {
                _error.value = e.message // ✅ FIX: use _error
            } finally {
                _isLoading.value = false // ✅ FIX: use _isLoading
            }
        }
    }
}
