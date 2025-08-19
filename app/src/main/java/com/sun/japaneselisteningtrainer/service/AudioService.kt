package com.sun.japaneselisteningtrainer.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import com.sun.japaneselisteningtrainer.data.model.Audio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground Service để phát audio
 * Sử dụng ExoPlayer và hiển thị notification
 */
class AudioService : Service(), AudioPlayer.AudioPlayerCallback {
    
    companion object {
        private const val TAG = "AudioService"
    }
    
    // Binder cho client kết nối
    inner class AudioServiceBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }
    
    private val binder = AudioServiceBinder()
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var notificationManager: AudioNotificationManager
    
    // Coroutine scope cho service
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var positionUpdateJob: Job? = null
    
    // State flows để UI có thể observe
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L) 
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _currentAudio = MutableStateFlow<Audio?>(null)
    val currentAudio: StateFlow<Audio?> = _currentAudio.asStateFlow()
    
    // Playlist management
    private var playlist: List<Audio> = emptyList()
    private var currentIndex = 0
    private var isShuffleEnabled = false
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AudioService created")
        
        // Khởi tạo components
        audioPlayer = AudioPlayer(this).apply {
            setCallback(this@AudioService)
        }
        notificationManager = AudioNotificationManager(this)
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")
        
        when (intent?.action) {
            AudioServiceConstants.ACTION_PLAY -> handlePlay()
            AudioServiceConstants.ACTION_PAUSE -> handlePause()
            AudioServiceConstants.ACTION_NEXT -> handleNext()
            AudioServiceConstants.ACTION_PREVIOUS -> handlePrevious()
            AudioServiceConstants.ACTION_STOP -> handleStop()
            AudioServiceConstants.ACTION_SEEK -> {
                val position = intent.getLongExtra(AudioServiceConstants.EXTRA_SEEK_POSITION, 0L)
                handleSeek(position)
            }
        }
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "AudioService destroyed")
        
        // Cleanup
        stopPositionUpdates()
        audioPlayer.release()
        notificationManager.cancelNotification()
        serviceScope.coroutineContext[Job]?.cancel()
    }
    
    // ===== AudioPlayer.AudioPlayerCallback Implementation =====
    
    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
        
        if (isPlaying) {
            startPositionUpdates()
            startForegroundService()
        } else {
            stopPositionUpdates()
            // Giữ foreground service nhưng update notification
            // ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
        }
        
        updateNotification()
    }
    
    override fun onPositionChanged(position: Long, duration: Long) {
        _currentPosition.value = position
        _duration.value = duration
        
        // Có thể update notification với progress nếu muốn
    }
    
    override fun onAudioChanged(audio: Audio?) {
        _currentAudio.value = audio
        
        // Force start foreground service khi có audio
        if (audio != null) {
            startForegroundService()
        }
        
        updateNotification()
    }
    
    override fun onError(error: String) {
        Log.e(TAG, "Audio error: $error")
        // Có thể show toast hoặc notification error
    }
    
    // ===== Public API Methods =====
    
    /**
     * Phát audio đơn lẻ
     */
    fun playAudio(audio: Audio) {
        audioPlayer.prepareAudio(audio)
        audioPlayer.play()
        
        // Cập nhật playlist với audio đơn lẻ
        playlist = listOf(audio)
        currentIndex = 0
    }
    
    /**
     * Phát playlist
     */
    fun playPlaylist(audioList: List<Audio>, startIndex: Int = 0) {
        if (audioList.isEmpty()) return
        
        playlist = audioList
        currentIndex = startIndex.coerceIn(0, audioList.size - 1)
        
        audioPlayer.prepareAudio(playlist[currentIndex])
        audioPlayer.play()
    }
    
    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        if (audioPlayer.isCurrentlyPlaying()) {
            audioPlayer.pause()
        } else {
            audioPlayer.play()
        }
    }
    
    /**
     * Seek to position
     */
    fun seekTo(position: Long) {
        audioPlayer.seekTo(position)
    }
    
    /**
     * Next track
     */
    fun nextTrack() {
        if (playlist.isNotEmpty()) {
            currentIndex = if (isShuffleEnabled) {
                playlist.indices.random()
            } else {
                (currentIndex + 1) % playlist.size
            }
            
            audioPlayer.prepareAudio(playlist[currentIndex])
            audioPlayer.play()
        }
    }
    
    /**
     * Previous track
     */
    fun previousTrack() {
        if (playlist.isNotEmpty()) {
            currentIndex = if (currentIndex > 0) {
                currentIndex - 1
            } else {
                playlist.size - 1
            }
            
            audioPlayer.prepareAudio(playlist[currentIndex])
            audioPlayer.play()
        }
    }
    
    /**
     * Toggle shuffle
     */
    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
    }
    
    /**
     * Check if shuffle is enabled
     */
    fun isShuffleEnabled(): Boolean = isShuffleEnabled
    
    // ===== Private Helper Methods =====
    
    private fun handlePlay() {
        audioPlayer.play()
    }
    
    private fun handlePause() {
        audioPlayer.pause()
    }
    
    private fun handleNext() {
        nextTrack()
    }
    
    private fun handlePrevious() {
        previousTrack()
    }
    
    private fun handleStop() {
        audioPlayer.stop()
        stopSelf()
    }
    
    private fun handleSeek(position: Long) {
        audioPlayer.seekTo(position)
    }
    
    /**
     * Bắt đầu foreground service với notification
     */
    private fun startForegroundService() {
        val currentAudio = _currentAudio.value ?: run {
            Log.w(TAG, "Cannot start foreground service: no current audio")
            return
        }
        
        Log.d(TAG, "Starting foreground service for audio: ${currentAudio.title}")
        
        val notification = notificationManager.createNotification(
            audio = currentAudio,
            isPlaying = _isPlaying.value,
            position = _currentPosition.value,
            duration = _duration.value
        )
        
        try {
            startForeground(AudioServiceConstants.NOTIFICATION_ID, notification)
            Log.d(TAG, "Foreground service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service: ${e.message}")
        }
    }
    
    /**
     * Cập nhật notification
     */
    private fun updateNotification() {
        val currentAudio = _currentAudio.value ?: return
        
        val notification = notificationManager.createNotification(
            audio = currentAudio,
            isPlaying = _isPlaying.value,
            position = _currentPosition.value,
            duration = _duration.value
        )
        
        // Update notification thông qua notification manager
        notificationManager.updateNotification(notification)
    }
    
    /**
     * Bắt đầu cập nhật position định kỳ
     */
    private fun startPositionUpdates() {
        stopPositionUpdates()
        
        positionUpdateJob = serviceScope.launch {
            while (isActive && audioPlayer.isCurrentlyPlaying()) {
                val position = audioPlayer.getCurrentPosition()
                val duration = audioPlayer.getDuration()
                
                _currentPosition.value = position
                _duration.value = duration
                
                delay(1000) // Cập nhật mỗi giây
            }
        }
    }
    
    /**
     * Dừng cập nhật position
     */
    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
}
