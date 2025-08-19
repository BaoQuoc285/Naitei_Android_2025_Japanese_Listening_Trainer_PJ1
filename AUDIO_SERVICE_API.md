# 🎵 AUDIO SERVICE - API REFERENCE

## 📋 **AudioServiceManager API**

### **Constructor**
```kotlin
class AudioServiceManager(
    private val context: Context,
    private val audioRepository: AudioRepository
)
```

---

## 🎮 **PLAYBACK CONTROL**

### **Basic Playback**
```kotlin
// Play single audio
fun playAudio(audio: Audio)

// Play playlist from specific index
fun playPlaylist(audioList: List<Audio>, startIndex: Int = 0)

// Toggle play/pause state
fun togglePlayPause()

// Stop playback
fun stopService()
```

### **Navigation Controls**
```kotlin
// Next track in playlist
fun nextTrack()

// Previous track in playlist  
fun previousTrack()

// Toggle shuffle mode on/off
fun toggleShuffle()

// Check if shuffle is enabled
fun isShuffleEnabled(): Boolean
```

### **Seek Controls**
```kotlin
// Seek to specific position (milliseconds)
fun seekTo(position: Long)

// Get current progress (0.0 to 1.0)
fun getProgress(): Float
```

---

## 💾 **DATABASE OPERATIONS**

### **Load & Play**
```kotlin
// Load audio from database by ID and play
suspend fun loadAndPlayAudio(audioId: Int)

// Load and play playlist from folder
suspend fun loadAndPlayPlaylist(
    folderId: Int? = null, 
    startAudioId: Int? = null
)
```

### **User Actions**
```kotlin
// Toggle favorite status and save to database
suspend fun toggleFavoriteStatus(audio: Audio)

// Increment listen times counter
suspend fun incrementListenTimes(audio: Audio)
```

---

## 📊 **STATE OBSERVATION**

### **Playback States**
```kotlin
// Service connection status
val isServiceConnected: StateFlow<Boolean>

// Playing/paused state
val isPlaying: StateFlow<Boolean>

// Current playback position in milliseconds
val currentPosition: StateFlow<Long>

// Total audio duration in milliseconds
val duration: StateFlow<Long>

// Currently loaded audio information
val currentAudio: StateFlow<Audio?>
```

### **Usage Example**
```kotlin
@Composable
fun AudioPlayerScreen() {
    val audioServiceManager = LocalAudioServiceManager.current
    
    // Observe states
    val isPlaying by audioServiceManager.isPlaying.collectAsState()
    val currentPosition by audioServiceManager.currentPosition.collectAsState()
    val duration by audioServiceManager.duration.collectAsState()
    val currentAudio by audioServiceManager.currentAudio.collectAsState()
    
    // Use states in UI
    PlayButton(
        isPlaying = isPlaying,
        onClick = { audioServiceManager.togglePlayPause() }
    )
    
    ProgressBar(
        progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
        onSeek = { progress -> 
            audioServiceManager.seekTo((duration * progress).toLong())
        }
    )
}
```

---

## 🔧 **SERVICE MANAGEMENT**

### **Lifecycle**
```kotlin
// Bind to AudioService
fun bindToService()

// Unbind from AudioService  
fun unbindFromService()

// Start service explicitly
fun startService()

// Release all resources
fun release()
```

### **Best Practices**
```kotlin
@Composable
fun YourScreen() {
    val audioServiceManager = remember { AudioServiceManagerSingleton.getInstance(context) }
    
    // Bind when screen opens
    LaunchedEffect(Unit) {
        audioServiceManager.bindToService()
    }
    
    // Unbind when screen closes
    DisposableEffect(Unit) {
        onDispose {
            audioServiceManager.unbindFromService()
        }
    }
}
```

---

## 🛠️ **UTILITY METHODS**

### **Information Getters**
```kotlin
// Check if currently playing
fun isCurrentlyPlaying(): Boolean

// Get current audio info
fun getCurrentAudio(): Audio?

// Format milliseconds to MM:SS
fun formatTime(milliseconds: Long): String
```

### **Usage Examples**
```kotlin
// Format time display
val currentTimeText = audioServiceManager.formatTime(currentPosition)
val durationText = audioServiceManager.formatTime(duration)

// Check playing state
if (audioServiceManager.isCurrentlyPlaying()) {
    // Show pause icon
} else {
    // Show play icon
}
```

---

## 🎯 **MusicPlayerViewModel API**

### **Constructor**
```kotlin
class MusicPlayerViewModel(
    private val audioServiceManager: AudioServiceManager
) : ViewModel()
```

### **Exposed States**
```kotlin
// All AudioServiceManager states
val isServiceConnected: StateFlow<Boolean>
val isPlaying: StateFlow<Boolean>
val currentPosition: StateFlow<Long>
val duration: StateFlow<Long>
val currentAudio: StateFlow<Audio?>

// Loading states
val isLoadingAudio: StateFlow<Boolean>
val audioLoadError: StateFlow<String?>
```

### **Methods**
```kotlin
// Service management
fun bindToService()
fun unbindFromService()

// Audio loading
fun loadAndPlayAudio(audioId: Int)

// Playback control (delegates to AudioServiceManager)
fun togglePlayPause()
fun seekTo(position: Long)
fun nextTrack()
fun previousTrack()
fun toggleShuffle()
fun getProgress(): Float
fun isShuffleEnabled(): Boolean

// Database operations
fun toggleFavoriteStatus(audio: Audio)
fun incrementListenTimes(audio: Audio)
```

---

## 🔗 **DEPENDENCY INJECTION**

### **AppContainer Integration**
```kotlin
// AppContainer.kt
interface AppContainer {
    val audioServiceManager: AudioServiceManager
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val audioServiceManager: AudioServiceManager by lazy {
        AudioServiceManager(context, audioRepository)
    }
}
```

### **ViewModel Factory**
```kotlin
// AppViewModelProvider.kt
initializer {
    MusicPlayerViewModel(
        audioServiceManager = trainerApplication().container.audioServiceManager
    )
}
```

### **Usage in Composable**
```kotlin
@Composable
fun MusicPlayerScreen(
    musicPlayerViewModel: MusicPlayerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // ViewModel automatically gets AudioServiceManager injected
}
```

---

## 🎵 **AUDIO MODEL**

### **Audio Data Class**
```kotlin
data class Audio(
    val id: Int = 0,
    val title: String = "",
    val folderId: Int = 0,
    val filePath: String = "",           // File name in res/raw/ (without extension)
    val script: String = "",             // Japanese transcript
    val translate: String = "",          // Vietnamese translation
    val isSuspended: Boolean = false,
    val isFavorite: Boolean = false,     // Favorite status
    val listenTimes: Int = 0,           // Play count
    val createdAt: Long = 0             // Creation timestamp
)
```

### **Audio Repository Interface**
```kotlin
interface AudioRepository {
    suspend fun add(audio: Audio, source: Uri): Int
    suspend fun delete(audio: Audio)
    suspend fun update(audio: Audio)
    fun getAllAudioStream(): Flow<List<Audio>>
    fun getAudioStream(id: Int): Flow<Audio?>
}
```

---

## 🚨 **ERROR HANDLING**

### **Common Exceptions**
```kotlin
// Audio loading errors
try {
    audioServiceManager.loadAndPlayAudio(audioId)
} catch (e: Exception) {
    // Handle: File not found, network error, etc.
    Log.e("AudioError", "Failed to load audio: ${e.message}")
}

// Database errors  
try {
    audioServiceManager.toggleFavoriteStatus(audio)
} catch (e: Exception) {
    // Handle: Database write error
    Log.e("DatabaseError", "Failed to update favorite: ${e.message}")
}
```

### **State Validation**
```kotlin
// Check service connection before operations
if (audioServiceManager.isServiceConnected.value) {
    audioServiceManager.playAudio(audio)
} else {
    Log.w("AudioService", "Service not connected yet")
}

// Check audio validity
currentAudio?.let { audio ->
    audioServiceManager.toggleFavoriteStatus(audio)
} ?: run {
    Log.w("AudioService", "No current audio to toggle favorite")
}
```

---

## 📱 **CONSTANTS**

### **AudioServiceConstants**
```kotlin
object AudioServiceConstants {
    // Service Actions
    const val ACTION_PLAY = "com.sun.japaneselisteningtrainer.ACTION_PLAY"
    const val ACTION_PAUSE = "com.sun.japaneselisteningtrainer.ACTION_PAUSE"
    const val ACTION_NEXT = "com.sun.japaneselisteningtrainer.ACTION_NEXT"
    const val ACTION_PREVIOUS = "com.sun.japaneselisteningtrainer.ACTION_PREVIOUS"
    const val ACTION_STOP = "com.sun.japaneselisteningtrainer.ACTION_STOP"
    
    // Notification
    const val NOTIFICATION_CHANNEL_ID = "audio_playback_channel"
    const val NOTIFICATION_ID = 1001
    
    // Playback States
    const val STATE_IDLE = 0
    const val STATE_PLAYING = 1
    const val STATE_PAUSED = 2
    const val STATE_STOPPED = 3
    const val STATE_ERROR = 4
}
```

---

## 🔄 **MIGRATION GUIDE**

### **From Singleton to DI**
```kotlin
// ❌ Old way (Singleton)
val audioServiceManager = AudioServiceManagerSingleton.getInstance(context)

// ✅ New way (DI)
@Composable
fun YourScreen(
    viewModel: YourViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // AudioServiceManager injected into ViewModel
}
```

### **Update ViewModels**
```kotlin
// Add AudioServiceManager to ViewModel constructor
class YourViewModel(
    private val audioServiceManager: AudioServiceManager
) : ViewModel() {
    // Use audioServiceManager here
}

// Register in AppViewModelProvider
initializer {
    YourViewModel(
        audioServiceManager = trainerApplication().container.audioServiceManager
    )
}
```

---

**📚 For complete guide, see `AUDIO_SERVICE_GUIDE.md`**  
**🚀 For quick start, see `QUICK_START_GUIDE.md`**
