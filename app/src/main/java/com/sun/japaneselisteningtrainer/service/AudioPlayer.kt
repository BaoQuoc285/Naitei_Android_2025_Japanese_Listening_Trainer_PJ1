package com.sun.japaneselisteningtrainer.service

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sun.japaneselisteningtrainer.data.model.Audio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioPlayer(private val context: Context) {
    
    companion object {
        private const val TAG = "AudioPlayer"
    }
    
    private val _exoPlayer = ExoPlayer.Builder(context).build()
    private val exoPlayer: ExoPlayer get() = _exoPlayer
    
    // State flows
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    private val _duration = MutableStateFlow(0L)

    private val _currentAudio = MutableStateFlow<Audio?>(null)

    private val _playbackState = MutableStateFlow(AudioServiceConstants.STATE_IDLE)

    // Callback interface
    interface AudioPlayerCallback {
        fun onPlaybackStateChanged(isPlaying: Boolean)
        fun onPositionChanged(position: Long, duration: Long)
        fun onAudioChanged(audio: Audio?)
        fun onError(error: String)
    }
    
    private var callback: AudioPlayerCallback? = null
    
    init {
        setupPlayerListener()
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState(playbackState)
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                callback?.onPlaybackStateChanged(isPlaying)
            }
            
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                updatePosition()
            }
            
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _playbackState.value = AudioServiceConstants.STATE_ERROR
                callback?.onError("Playback error: ${error.message}")
            }
        })
    }

    fun setCallback(callback: AudioPlayerCallback) {
        this.callback = callback
    }

    fun prepareAudio(audio: Audio) {
        try {
            Log.d(TAG, "Preparing audio: ${audio.title}, filePath: ${audio.filePath}")
            
            val uri = when {
                // Nếu filePath là URI (bắt đầu với file:// hoặc content://)
                audio.filePath.startsWith("file://") || audio.filePath.startsWith("content://") -> {
                    Log.d(TAG, "Using external file URI: ${audio.filePath}")
                    Uri.parse(audio.filePath)
                }
                // Nếu filePath là raw resource name
                else -> {
                    Log.d(TAG, "Using raw resource: ${audio.filePath}")
                    val resourceId = context.resources.getIdentifier(
                        audio.filePath, 
                        "raw", 
                        context.packageName
                    )
                    
                    if (resourceId == 0) {
                        throw Exception("Raw resource '${audio.filePath}' not found")
                    }
                    
                    Uri.parse("android.resource://${context.packageName}/$resourceId")
                }
            }
            
            Log.d(TAG, "Final URI for ExoPlayer: $uri")
            
            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            
            _currentAudio.value = audio
            callback?.onAudioChanged(audio)
            _playbackState.value = AudioServiceConstants.STATE_IDLE
            
            Log.d(TAG, "Audio prepared successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare audio: ${e.message}", e)
            _playbackState.value = AudioServiceConstants.STATE_ERROR
            callback?.onError("Không thể tải audio: ${e.message}")
        }
    }

    fun play() {
        exoPlayer.playWhenReady = true
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun stop() {
        exoPlayer.stop()
        _playbackState.value = AudioServiceConstants.STATE_STOPPED
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
        updatePosition()
    }

    fun isCurrentlyPlaying(): Boolean = exoPlayer.isPlaying

    fun getCurrentPosition(): Long = exoPlayer.currentPosition

    fun getDuration(): Long = exoPlayer.duration.takeIf { it > 0 } ?: 0L

    private fun updatePlaybackState(state: Int) {
        when (state) {
            Player.STATE_IDLE -> _playbackState.value = AudioServiceConstants.STATE_IDLE
            Player.STATE_BUFFERING -> {
                // Giữ nguyên state hiện tại khi buffering
            }
            Player.STATE_READY -> {
                _playbackState.value = if (exoPlayer.playWhenReady) {
                    AudioServiceConstants.STATE_PLAYING
                } else {
                    AudioServiceConstants.STATE_PAUSED
                }
                updatePosition()
            }
            Player.STATE_ENDED -> {
                _playbackState.value = AudioServiceConstants.STATE_STOPPED
                callback?.onPositionChanged(0L, getDuration())
            }
        }
    }

    private fun updatePosition() {
        val position = getCurrentPosition()
        val duration = getDuration()
        
        _currentPosition.value = position
        _duration.value = duration
        
        callback?.onPositionChanged(position, duration)
    }

    fun release() {
        exoPlayer.release()
        callback = null
    }
}
