package com.sun.japaneselisteningtrainer.ui.audio.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.service.AudioServiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

/**
 * ViewModel cho MusicPlayerScreen với Dependency Injection
 */
class MusicPlayerViewModel(
    private val audioServiceManager: AudioServiceManager
) : ViewModel() {
    
    // Expose AudioServiceManager states
    val isServiceConnected: StateFlow<Boolean> = audioServiceManager.isServiceConnected
    val isPlaying: StateFlow<Boolean> = audioServiceManager.isPlaying
    val currentPosition: StateFlow<Long> = audioServiceManager.currentPosition
    val duration: StateFlow<Long> = audioServiceManager.duration
    val currentAudio: StateFlow<Audio?> = audioServiceManager.currentAudio
    
    // Loading states
    private val _isLoadingAudio = MutableStateFlow(false)
    val isLoadingAudio: StateFlow<Boolean> = _isLoadingAudio.asStateFlow()
    
    private val _audioLoadError = MutableStateFlow<String?>(null)
    val audioLoadError: StateFlow<String?> = _audioLoadError.asStateFlow()
    
    // Local UI state để track favorite changes
    private val _localFavoriteOverride = MutableStateFlow<Pair<Int, Boolean>?>(null)
    
    // Combined currentAudio với local favorite override
    val currentAudioWithOverride: StateFlow<Audio?> = combine(
        audioServiceManager.currentAudio,
        _localFavoriteOverride
    ) { serviceAudio, favoriteOverride ->
        if (serviceAudio != null && favoriteOverride != null && favoriteOverride.first == serviceAudio.id) {
            serviceAudio.copy(isFavorite = favoriteOverride.second)
        } else {
            serviceAudio
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null
    )
    
    /**
     * Bind to AudioService
     */
    fun bindToService() {
        audioServiceManager.bindToService()
    }
    
    /**
     * Unbind from AudioService
     */
    fun unbindFromService() {
        audioServiceManager.unbindFromService()
    }
    
    /**
     * Load and play audio by ID
     */
    fun loadAndPlayAudio(audioId: Int) {
        viewModelScope.launch {
            Log.d("MusicPlayerViewModel", "Starting to load audio with ID: $audioId")
            _isLoadingAudio.value = true
            _audioLoadError.value = null
            
            try {
                Log.d("MusicPlayerViewModel", "Calling audioServiceManager.loadAndPlayAudio")
                audioServiceManager.loadAndPlayAudio(audioId)
                Log.d("MusicPlayerViewModel", "Audio loaded successfully")
                _isLoadingAudio.value = false
            } catch (e: Exception) {
                Log.e("MusicPlayerViewModel", "Failed to load audio: ${e.message}", e)
                _audioLoadError.value = "Không thể load audio: ${e.message}"
                _isLoadingAudio.value = false
            }
        }
    }
    
    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        audioServiceManager.togglePlayPause()
    }
    
    /**
     * Seek to position
     */
    fun seekTo(position: Long) {
        audioServiceManager.seekTo(position)
    }
    
    /**
     * Next track
     */
    fun nextTrack() {
        audioServiceManager.nextTrack()
    }
    
    /**
     * Previous track
     */
    fun previousTrack() {
        audioServiceManager.previousTrack()
    }
    
    /**
     * Toggle shuffle
     */
    fun toggleShuffle() {
        audioServiceManager.toggleShuffle()
    }
    
    /**
     * Check if shuffle is enabled
     */
    fun isShuffleEnabled(): Boolean {
        return audioServiceManager.isShuffleEnabled()
    }
    
    /**
     * Get current playback progress
     */
    fun getProgress(): Float {
        return audioServiceManager.getProgress()
    }
    
    /**
     * Toggle favorite status
     */
    fun toggleFavoriteStatus(audio: Audio) {
        viewModelScope.launch {
            // Immediately update UI
            _localFavoriteOverride.value = Pair(audio.id, !audio.isFavorite)
            
            // Then update database
            audioServiceManager.toggleFavoriteStatus(audio)
        }
    }
    

    
    override fun onCleared() {
        super.onCleared()
        // Clean up when ViewModel is destroyed
        audioServiceManager.unbindFromService()
    }
}
