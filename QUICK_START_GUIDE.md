# 🚀 AUDIO SERVICE - QUICK START

## ⚡ **5-MINUTE SETUP**

### **1. Test Ngay:**
```kotlin
// 1. Run app
// 2. Tap "Add Test Audio" 
// 3. Tap audio item to play
// 4. Minimize app → Check notification
```

### **2. Tích Hợp Vào UI Khác:**
```kotlin
// Inject AudioServiceManager
class YourViewModel(
    private val audioServiceManager: AudioServiceManager
) : ViewModel() {
    
    fun playAudio(audio: Audio) {
        viewModelScope.launch {
            audioServiceManager.playAudio(audio)
        }
    }
}

// Observe states
@Composable
fun YourScreen() {
    val isPlaying by audioServiceManager.isPlaying.collectAsState()
    val currentAudio by audioServiceManager.currentAudio.collectAsState()
    
    // Your UI here
}
```

### **3. Key APIs:**
```kotlin
// Playback
audioServiceManager.playAudio(audio)
audioServiceManager.togglePlayPause()
audioServiceManager.nextTrack()

// Database  
audioServiceManager.loadAndPlayAudio(audioId)
audioServiceManager.toggleFavoriteStatus(audio)

// States
val isPlaying: StateFlow<Boolean>
val currentAudio: StateFlow<Audio?>
val currentPosition: StateFlow<Long>
```

---

## 🔧 **COMPONENTS OVERVIEW**

| Component | Chức Năng | Sử Dụng Khi |
|-----------|-----------|-------------|
| `AudioService` | Foreground service, ExoPlayer | Background playback |
| `AudioServiceManager` | UI interface, DI | UI cần control audio |
| `MusicPlayerViewModel` | Screen state management | Full player screen |
| `AudioNotificationManager` | Media notification | Service notification |

---

## 🎯 **INTEGRATION EXAMPLES**

### **Home Screen Mini Player:**
```kotlin
@Composable
fun HomeScreen() {
    val audioServiceManager = LocalAudioServiceManager.current
    val currentAudio by audioServiceManager.currentAudio.collectAsState()
    
    currentAudio?.let { audio ->
        MiniPlayer(
            audio = audio,
            onTap = { /* Navigate to full player */ }
        )
    }
}
```

### **Search Results:**
```kotlin
@Composable  
fun SearchResultItem(audio: Audio) {
    val audioServiceManager = LocalAudioServiceManager.current
    
    Row {
        Text(audio.title)
        IconButton(
            onClick = { 
                audioServiceManager.playAudio(audio) 
            }
        ) {
            Icon(Icons.Default.PlayArrow, "Play")
        }
    }
}
```

### **Playlist Screen:**
```kotlin
@Composable
fun PlaylistScreen(audios: List<Audio>) {
    val audioServiceManager = LocalAudioServiceManager.current
    
    Button(
        onClick = { 
            audioServiceManager.playPlaylist(audios) 
        }
    ) {
        Text("Play All")
    }
}
```

---

## ⚠️ **IMPORTANT NOTES**

### **Service Lifecycle:**
```kotlin
// ALWAYS bind/unbind properly
LaunchedEffect(Unit) {
    audioServiceManager.bindToService()
}

DisposableEffect(Unit) {
    onDispose {
        audioServiceManager.unbindFromService()
    }
}
```

### **Permissions:**
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### **Audio Files:**
```
app/src/main/res/raw/
├── test_audio.mp3    ✅ For testing
└── your_audio.mp3    ✅ Your audio files
```

---

## 🐛 **QUICK DEBUG**

### **Audio không phát:**
1. Check file trong `res/raw/`
2. Check `audio.filePath` match filename
3. Check ExoPlayer logs

### **Notification không hiện:**
1. Check notification permission
2. Check service foreground
3. Check notification channel

### **UI không update:**
1. Check service connected
2. Check StateFlow collection
3. Check ViewModel injection

---

**🎵 Ready to integrate! Check `AUDIO_SERVICE_GUIDE.md` for detailed docs.**
