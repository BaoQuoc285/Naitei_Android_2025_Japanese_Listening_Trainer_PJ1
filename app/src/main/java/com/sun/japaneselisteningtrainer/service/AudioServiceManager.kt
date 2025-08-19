package com.sun.japaneselisteningtrainer.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Manager để UI tương tác với AudioService
 * Cung cấp easy API cho Compose UI
 */
class AudioServiceManager(
    private val context: Context,
    private val audioRepository: AudioRepository
) {
    
    companion object {
        private const val TAG = "AudioServiceManager"
    }
    
    private var audioService: AudioService? = null
    private var isBound = false
    private val managerScope = CoroutineScope(Dispatchers.Main + Job())
    private var flowCollectionJob: Job? = null
    
    // State flows để UI observe
    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _currentAudio = MutableStateFlow<Audio?>(null)
    val currentAudio: StateFlow<Audio?> = _currentAudio.asStateFlow()
    
    // Service connection
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Service connected")
            val binder = service as AudioService.AudioServiceBinder
            audioService = binder.getService()
            isBound = true
            _isServiceConnected.value = true
            
            // Start collecting flows from service
            startFlowCollection()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service disconnected")
            stopFlowCollection()
            audioService = null
            isBound = false
            _isServiceConnected.value = false
        }
    }
    
    /**
     * Bind to AudioService
     */
    fun bindToService() {
        if (!isBound) {
            val intent = Intent(context, AudioService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "Binding to service")
        }
    }
    
    /**
     * Unbind from AudioService
     */
    fun unbindFromService() {
        if (isBound) {
            stopFlowCollection()
            context.unbindService(serviceConnection)
            isBound = false
            _isServiceConnected.value = false
            Log.d(TAG, "Unbound from service")
        }
    }
    

    
    // ===== Audio Control Methods =====
    
    /**
     * Phát audio đơn lẻ
     */
    fun playAudio(audio: Audio) {
        audioService?.playAudio(audio)
    }
    
    /**
     * Phát playlist
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
     * Get current playback progress (0f to 1f)
     */
    fun getProgress(): Float {
        val duration = _duration.value
        val position = _currentPosition.value
        return if (duration > 0) {
            (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    
    /**
     * Start AudioService
     */
    fun startService() {
        val intent = Intent(context, AudioService::class.java)
        context.startService(intent)
    }
    
    /**
     * Stop AudioService
     */
    fun stopService() {
        val intent = Intent(context, AudioService::class.java).apply {
            action = AudioServiceConstants.ACTION_STOP
        }
        context.startService(intent)
    }
    
    // ===== Helper Methods =====
    
    /**
     * Start collecting flows from AudioService để sync states
     */
    private fun startFlowCollection() {
        audioService?.let { service ->
            flowCollectionJob?.cancel()
            flowCollectionJob = managerScope.launch {
                // Collect tất cả flows từ service để sync states
                launch {
                    service.isPlaying.collect { _isPlaying.value = it }
                }
                launch {
                    service.currentPosition.collect { _currentPosition.value = it }
                }
                launch {
                    service.duration.collect { _duration.value = it }
                }
                launch {
                    service.currentAudio.collect { _currentAudio.value = it }
                }
            }
            Log.d(TAG, "Started flow collection from service")
        }
    }
    
    /**
     * Stop collecting flows
     */
    private fun stopFlowCollection() {
        flowCollectionJob?.cancel()
        flowCollectionJob = null
        Log.d(TAG, "Stopped flow collection")
    }
    
    /**
     * Format milliseconds to MM:SS string
     */
    fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return "%02d:%02d".format(minutes, remainingSeconds)
    }
    
    /**
     * Check if service is playing
     */
    fun isCurrentlyPlaying(): Boolean {
        return _isPlaying.value
    }
    
    /**
     * Get current audio info
     */
    fun getCurrentAudio(): Audio? {
        return _currentAudio.value
    }
    
    /**
     * Load audio từ database và play
     */
    suspend fun loadAndPlayAudio(audioId: Int) {
        try {
            // Sử dụng first() thay vì collect để chỉ lấy first emission
            val audio = audioRepository.getAudioStream(audioId).first()
            audio?.let { playAudio(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading audio $audioId: ${e.message}")
        }
    }
    
    /**
     * Load playlist từ database và play
     */
    suspend fun loadAndPlayPlaylist(folderId: Int? = null, startAudioId: Int? = null) {
        try {
            // Sử dụng first() thay vì collect để chỉ lấy first emission
            val audioList = audioRepository.getAllAudioStream().first()
            val filteredList = if (folderId != null) {
                audioList.filter { it.folderId == folderId }
            } else {
                audioList
            }
            
            if (filteredList.isNotEmpty()) {
                val startIndex = startAudioId?.let { id ->
                    filteredList.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: 0
                } ?: 0
                
                playPlaylist(filteredList, startIndex)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading playlist: ${e.message}")
        }
    }
    
    /**
     * Update favorite status
     */
    suspend fun toggleFavoriteStatus(audio: Audio) {
        try {
            val updatedAudio = audio.copy(isFavorite = !audio.isFavorite)
            audioRepository.update(updatedAudio)
            _currentAudio.value = updatedAudio
        } catch (e: Exception) {
            Log.e(TAG, "Error updating favorite: ${e.message}")
        }
    }
    
    /**
     * Increment listen times
     */
    suspend fun incrementListenTimes(audio: Audio) {
        try {
            val updatedAudio = audio.copy(listenTimes = audio.listenTimes + 1)
            audioRepository.update(updatedAudio)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating listen times: ${e.message}")
        }
    }
    
    /**
     * Release resources
     */
    fun release() {
        stopFlowCollection()
        flowCollectionJob?.cancel()
        unbindFromService()
    }
}

/**
 * Extension functions cho dễ sử dụng
 */

/**
 * Tạo singleton instance cho toàn app
 */
object AudioServiceManagerSingleton {
    @Volatile
    private var INSTANCE: AudioServiceManager? = null
    
    fun getInstance(context: Context, audioRepository: AudioRepository): AudioServiceManager {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: AudioServiceManager(context.applicationContext, audioRepository).also { INSTANCE = it }
        }
    }
    
    /**
     * Helper để get instance với TrainerApplication
     */
    fun getInstance(context: Context): AudioServiceManager {
        val application = context.applicationContext as com.sun.japaneselisteningtrainer.TrainerApplication
        return getInstance(context, application.container.audioRepository)
    }
}
