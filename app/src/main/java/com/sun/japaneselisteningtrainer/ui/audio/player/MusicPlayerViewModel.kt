package com.sun.japaneselisteningtrainer.ui.audio.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.service.AudioServiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    private val _isLoadingAudio = MutableStateFlow(true)
    val isLoadingAudio: StateFlow<Boolean> = _isLoadingAudio.asStateFlow()
    
    private val _audioLoadError = MutableStateFlow<String?>(null)
    val audioLoadError: StateFlow<String?> = _audioLoadError.asStateFlow()
    
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
            _isLoadingAudio.value = true
            _audioLoadError.value = null
            
            try {
                audioServiceManager.loadAndPlayAudio(audioId)
                _isLoadingAudio.value = false
            } catch (e: Exception) {
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
            audioServiceManager.toggleFavoriteStatus(audio)
        }
    }
    
    /**
     * Increment listen times
     */
    fun incrementListenTimes(audio: Audio) {
        viewModelScope.launch {
            audioServiceManager.incrementListenTimes(audio)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up when ViewModel is destroyed
        audioServiceManager.unbindFromService()
    }
}
