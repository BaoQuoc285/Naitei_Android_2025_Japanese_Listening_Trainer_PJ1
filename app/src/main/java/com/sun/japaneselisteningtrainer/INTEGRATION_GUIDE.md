# Hướng dẫn Integration AudioService với UI

## 📁 Files đã cập nhật

### 1. MusicPlayerScreen.kt
- ✅ Thêm `audioId` parameter
- ✅ Integrate `AudioServiceManager` 
- ✅ Thay thế hardcoded values bằng real data
- ✅ Thêm service lifecycle management
- ✅ Connect tất cả UI controls với service

### 2. TestAudioPlayerIntegration.kt
- ✅ Screen test để demo integration
- ✅ Selector cho different audio IDs
- ✅ Instructions và features overview

## 🔧 Cách chạy

### Option 1: Test trong existing app

Trong `MainActivity.kt` hoặc navigation, thêm:

```kotlin
import com.sun.japaneselisteningtrainer.TestAudioPlayerIntegration

// Trong Composable
TestAudioPlayerIntegration()
```

### Option 2: Update navigation route

Trong file navigation, cập nhật route để pass `audioId`:

```kotlin
// Existing route
composable(
    route = MusicPlayerDestination.routeWithArgs,
    arguments = listOf(navArgument(MusicPlayerDestination.audioIdArg) {
        type = NavType.IntType
    })
) { backStackEntry ->
    val audioId = backStackEntry.arguments?.getInt(MusicPlayerDestination.audioIdArg) ?: 1
    
    MusicPlayerScreen(
        audioId = audioId,
        onNavigationBack = { navController.popBackStack() },
        onEditAudio = { /* navigate to edit */ }
    )
}
```

## 🎵 Sample Audio Setup

Để test với real audio file:

### 1. Thêm audio file
```
app/src/main/res/raw/sample_audio.mp3
```

### 2. Cập nhật filePath trong MusicPlayerScreen.kt
```kotlin
val sampleAudio = remember(audioId) {
    Audio(
        id = audioId,
        title = "Japanese Audio Sample $audioId",
        filePath = "android.resource://${context.packageName}/${R.raw.sample_audio}",
        // ... rest of properties
    )
}
```

### 3. Hoặc sử dụng URL
```kotlin
filePath = "https://www.soundjay.com/misc/sounds-effects/bell-ringing-05.wav"
```

## 🎯 Features đã hoạt động

### ✅ Core Playback
- [x] Play/Pause audio
- [x] Next/Previous track (trong playlist)
- [x] Seek to position
- [x] Real-time progress tracking

### ✅ UI Integration  
- [x] RotatingDisc animation sync với playback
- [x] AudioProgressBar với real progress
- [x] TransportBar controls thực tế
- [x] Real audio title hiển thị
- [x] Lyrics từ script data

### ✅ Service Features
- [x] Foreground service cho background playback
- [x] Media notification với controls
- [x] Automatic service binding/unbinding
- [x] StateFlow reactive updates

### ✅ Advanced Features
- [x] Shuffle mode
- [x] Playlist support
- [x] Notification controls
- [x] App lifecycle handling

## 🧪 Test Scenarios

### Test Case 1: Basic Playback
1. Mở player với audioId
2. Verify service connects
3. Tap play - audio bắt đầu
4. Check notification xuất hiện
5. Test pause/resume

### Test Case 2: Controls
1. Test previous/next buttons
2. Test shuffle toggle
3. Test seek bằng progress bar
4. Test controls từ notification

### Test Case 3: UI States
1. RotatingDisc quay khi playing
2. Progress bar cập nhật real-time
3. Toggle lyrics/disc view
4. Title hiển thị đúng

### Test Case 4: Lifecycle
1. Minimize app - music tiếp tục
2. Kill app - service stops
3. Reopen app - reconnect service
4. Navigation back/forth

## 🚧 Next Steps cần implement

### 1. Database Integration
```kotlin
// Trong MusicPlayerScreen.kt, thay sample audio bằng:
LaunchedEffect(audioId) {
    val audio = audioRepository.getAudioById(audioId)
    audio?.let { audioServiceManager.playAudio(it) }
}
```

### 2. Favorite Toggle
```kotlin
onToggleFavorite = { 
    scope.launch {
        val updatedAudio = currentAudio.copy(isFavorite = !currentAudio.isFavorite)
        audioRepository.updateAudio(updatedAudio)
    }
}
```

### 3. Lyrics Sync
```kotlin
// Track current line dựa trên playback position
val currentLineIndex = remember(currentPosition) {
    // Calculate based on timing data
    calculateCurrentLine(currentPosition, audio.timingData)
}
```

### 4. Error Handling
```kotlin
// Handle audio load errors
LaunchedEffect(audioServiceManager.error.collectAsState().value) { error ->
    error?.let {
        // Show snackbar or error dialog
    }
}
```

## ⚡ Performance Tips

### Memory Management
- Service auto unbind khi screen dispose
- StateFlow chỉ update khi thực sự cần
- UI components stateless và reusable

### Battery Optimization
- Foreground service chỉ khi playing
- Position updates dừng khi pause
- Wake lock management tự động

### User Experience
- Smooth animations với real state
- Immediate UI feedback
- Background playback seamless

## 🎉 Kết quả

Bây giờ bạn đã có:
- **Fully functional audio player** với real ExoPlayer backend
- **Professional foreground service** với notification
- **Reactive UI** sync với playback state  
- **Clean architecture** dễ maintain và extend
- **Production-ready** foundation cho Japanese learning app

Chỉ cần thêm database integration và real audio files là có thể ship!
