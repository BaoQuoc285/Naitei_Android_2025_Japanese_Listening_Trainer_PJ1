package com.sun.japaneselisteningtrainer.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.seconds

/**
 * Manager class để tương tác với AudioService
 * Cung cấp API dễ sử dụng cho UI components
 * Integrated với AudioRepository cho database operations
 */
class AudioServiceManager(
    private val context: Context,
    private val audioRepository: AudioRepository
) : ServiceConnection {

    companion object {
        private const val TAG = "AudioServiceManager"
    }

    private var audioService: AudioService? = null
    private var managerScope: CoroutineScope? = null
    private var lastPlayedAudioId: Int? = null // Track last played audio để tránh duplicate increment

    // Service connection state
    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    // Playback states - synced from AudioService
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentAudio = MutableStateFlow<Audio?>(null)
    val currentAudio: StateFlow<Audio?> = _currentAudio.asStateFlow()

    /**
     * Bind to AudioService
     */
    fun bindToService() {
        Log.d(TAG, "Binding to AudioService")
        val intent = Intent(context, AudioService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    /**
     * Unbind from AudioService
     */
    fun unbindFromService() {
        Log.d(TAG, "Unbinding from AudioService")
        stopFlowCollection()
        try {
            context.unbindService(this)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Service was not bound: ${e.message}")
        }
        audioService = null
        _isServiceConnected.value = false
    }

    /**
     * Start flow collection from AudioService
     */
    private fun startFlowCollection(service: AudioService) {
        managerScope = CoroutineScope(Job() + Dispatchers.Main)
        managerScope?.launch {
            launch {
                service.isPlaying.collectLatest { _isPlaying.value = it }
            }
            launch {
                service.currentPosition.collectLatest { _currentPosition.value = it }
            }
            launch {
                service.duration.collectLatest { _duration.value = it }
            }
            launch {
                service.currentAudio.collectLatest { _currentAudio.value = it }
            }
        }
    }

    /**
     * Stop flow collection
     */
    private fun stopFlowCollection() {
        managerScope?.cancel()
        managerScope = null
    }

    // ServiceConnection callbacks
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.d(TAG, "Service connected")
        val binder = service as AudioService.AudioServiceBinder
        audioService = binder.getService()
        _isServiceConnected.value = true
        
        // Start collecting states from service
        audioService?.let { startFlowCollection(it) }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.d(TAG, "Service disconnected")
        stopFlowCollection()
        audioService = null
        _isServiceConnected.value = false
    }

    // ============ PLAYBACK CONTROL METHODS ============

    /**
     * Play single audio
     */
    fun playAudio(audio: Audio) {
        audioService?.playAudio(audio)
    }

    /**
     * Play playlist
     */
    fun playPlaylist(audioList: List<Audio>, startIndex: Int = 0) {
        audioService?.playPlaylist(audioList, startIndex)
    }

    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        audioService?.togglePlayPause()
    }

    /**
     * Seek to position
     */
    fun seekTo(position: Long) {
        audioService?.seekTo(position)
    }

    /**
     * Next track
     */
    fun nextTrack() {
        audioService?.nextTrack()
    }

    /**
     * Previous track
     */
    fun previousTrack() {
        audioService?.previousTrack()
    }

    /**
     * Toggle shuffle
     */
    fun toggleShuffle() {
        audioService?.toggleShuffle()
    }

    /**
     * Check if shuffle is enabled
     */
    fun isShuffleEnabled(): Boolean {
        return audioService?.isShuffleEnabled() ?: false
    }

    /**
     * Get current progress (0.0 to 1.0)
     */
    fun getProgress(): Float {
        val dur = _duration.value
        val pos = _currentPosition.value
        return if (dur > 0) pos.toFloat() / dur else 0f
    }

    // ============ DATABASE OPERATIONS ============

    /**
     * Load audio from database and play
     */
    suspend fun loadAndPlayAudio(audioId: Int, forceReload: Boolean = false) {
        try {
            Log.d(TAG, "Loading audio with ID: $audioId")
            
            // Add timeout để tránh wait forever
            val audio = withTimeoutOrNull(5.seconds) {
                Log.d(TAG, "Querying database for audio ID: $audioId")
                audioRepository.getAudioStream(audioId).first()
            }
            
            Log.d(TAG, "Database query result: ${audio?.title ?: "null"}")
            
            if (audio != null) {
                Log.d(TAG, "Playing audio: ${audio.title}")
                
                // Load toàn bộ playlist để hỗ trợ next/previous
                val allAudios = withTimeoutOrNull(5.seconds) {
                    audioRepository.getAllAudioStream().first()
                } ?: emptyList()
                
                if (allAudios.isNotEmpty()) {
                    val startIndex = allAudios.indexOfFirst { it.id == audioId }.takeIf { it >= 0 } ?: 0
                    playPlaylist(allAudios, startIndex)
                } else {
                    // Fallback: chỉ phát audio đơn lẻ nếu không có playlist
                    playAudio(audio)
                }
                
                // Increment listen times chỉ khi là audio mới hoặc force reload
                if (forceReload || lastPlayedAudioId != audio.id) {
                    incrementListenTimes(audio)
                    lastPlayedAudioId = audio.id
                }
            } else {
                throw Exception("Audio with ID $audioId not found in database")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Database query timeout for audio ID: $audioId")
            throw Exception("Database query timeout")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load audio: ${e.message}")
            throw e
        }
    }



    /**
     * Toggle favorite status and update database
     */
    suspend fun toggleFavoriteStatus(audio: Audio) {
        try {
            val updatedAudio = audio.copy(isFavorite = !audio.isFavorite)
            audioRepository.update(updatedAudio)
            
            // Update currentAudio state để UI sync
            if (_currentAudio.value?.id == audio.id) {
                _currentAudio.value = updatedAudio
            }
            
            Log.d(TAG, "Toggled favorite for: ${audio.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle favorite: ${e.message}")
            throw e
        }
    }

    /**
     * Increment listen times and update database
     */
    suspend fun incrementListenTimes(audio: Audio) {
        try {
            val updatedAudio = audio.copy(listenTimes = audio.listenTimes + 1)
            audioRepository.update(updatedAudio)
            Log.d(TAG, "Incremented listen times for: ${audio.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to increment listen times: ${e.message}")
            throw e
        }
    }


}