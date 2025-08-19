package com.sun.japaneselisteningtrainer

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.ui.audio.player.MusicPlayerScreen
import com.sun.japaneselisteningtrainer.ui.permissions.RequestNotificationPermission
import com.sun.japaneselisteningtrainer.ui.permissions.hasNotificationPermission

@Composable
fun AudioTest() {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val appContainer = (context.applicationContext as TrainerApplication).container
    val audioRepository = appContainer.audioRepository
    
    var availableAudios by remember { mutableStateOf<List<Audio>>(emptyList()) }
    var selectedAudioId by remember { mutableStateOf<Int?>(null) }
    
    // Notification permission state
    val hasNotificationPerm = hasNotificationPermission()
    
    // Request notification permission
    RequestNotificationPermission { granted ->
        // Permission updated, UI sẽ recompose tự động
    }
    
    // Load audios
    LaunchedEffect(Unit) {
        audioRepository.getAllAudioStream().collect { audios ->
            availableAudios = audios
        }
    }
    
    if (selectedAudioId != null) {
        // Show player
        MusicPlayerScreen(
            audioId = selectedAudioId!!,
            onNavigationBack = { selectedAudioId = null },
            onEditAudio = { }
        )
    } else {
        // Show audio list
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎵 Audio Test",
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Add test audio if empty
            if (availableAudios.isEmpty()) {
                Button(
                    onClick = {
                        scope.launch {
                            val testAudio = Audio(
                                title = "Test Audio",
                                folderId = 1,
                                filePath = "test_audio",
                                script = "これはテストオーディオです\n日本語の音声ファイルです\nExoPlayerで再生されます",
                                translate = "Đây là audio test\nFile âm thanh tiếng Nhật\nĐược phát bằng ExoPlayer",
                                isFavorite = false,
                                listenTimes = 0,
                                createdAt = System.currentTimeMillis()
                            )
                            
                            // Tạo URI cho raw resource test_audio
                            val sourceUri = Uri.parse("android.resource://${context.packageName}/raw/test_audio")
                            audioRepository.add(testAudio, sourceUri)
                        }
                    }
                ) {
                    Text("Add Test Audio")
                }
            } else {
                // Show audio list
                availableAudios.forEach { audio ->
                    Button(
                        onClick = { selectedAudioId = audio.id.toInt() }
                    ) {
                        Text(audio.title)
                    }
                }
            }
        }
    }
}
