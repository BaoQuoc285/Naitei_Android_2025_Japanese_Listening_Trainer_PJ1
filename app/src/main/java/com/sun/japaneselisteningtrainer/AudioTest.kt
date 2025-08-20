package com.sun.japaneselisteningtrainer

import android.net.Uri
import android.util.Log
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
import com.sun.japaneselisteningtrainer.ui.AudioFilePickerWithPreview

@Composable
fun AudioTest() {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val container = (context.applicationContext as TrainerApplication).container
    val audioRepository = container.audioRepository
    
    var selectedAudioId by remember { mutableStateOf<Int?>(null) }
    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    val availableAudios by audioRepository.getAllAudioStream().collectAsState(initial = emptyList())
    
    // Request notification permission
    RequestNotificationPermission()
    
    if (hasNotificationPermission()) {
        if (selectedAudioId != null) {
            // Show MusicPlayerScreen with selected audio
            MusicPlayerScreen(
                audioId = selectedAudioId!!,
                onNavigationBack = { selectedAudioId = null },
                onEditAudio = { /* TODO */ }
            )
        } else {
            // Show audio list and controls
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎵 Audio Service Test",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Divider()
                
                // Section 1: Add test raw resource audio
                Text(
                    text = "1. Test Raw Resource Audio",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = {
                        scope.launch {
                            val testAudio = Audio(
                                title = "Test Raw Audio",
                                folderId = 1,
                                filePath = "test_audio", // Raw resource name
                                script = "これはテストオーディオです\n日本語の音声ファイルです\nExoPlayerで再生されます",
                                translate = "Đây là audio test\nFile âm thanh tiếng Nhật\nĐược phát bằng ExoPlayer",
                                isFavorite = false,
                                listenTimes = 0,
                                createdAt = System.currentTimeMillis()
                            )
                            
                            // Tạo URI cho raw resource test_audio
                            val sourceUri = Uri.parse("android.resource://${context.packageName}/raw/test_audio")
                            Log.d("AudioTest", "Adding raw test audio to database...")
                            val audioId = audioRepository.add(testAudio, sourceUri)
                            Log.d("AudioTest", "Raw test audio added with ID: $audioId")
                        }
                    }
                ) {
                    Text("Add Raw Test Audio")
                }
                
                Divider()
                
                // Section 2: Add real audio from storage
                Text(
                    text = "2. Add Real Audio from Storage",
                    style = MaterialTheme.typography.titleMedium
                )
                
                AudioFilePickerWithPreview(
                    selectedUri = selectedAudioUri,
                    onAudioSelected = { uri ->
                        selectedAudioUri = uri
                        Log.d("AudioTest", "Audio file selected: $uri")
                    },
                    onClearSelection = { selectedAudioUri = null }
                )
                
                if (selectedAudioUri != null) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val fileName = selectedAudioUri!!.lastPathSegment ?: "Unknown Audio"
                                    val realAudio = Audio(
                                        title = fileName,
                                        folderId = 1,
                                        filePath = "", // Will be set by repository
                                        script = "Real audio file from storage\n実際のオーディオファイル",
                                        translate = "File âm thanh thật từ storage\nĐược chọn từ máy của bạn",
                                        isFavorite = false,
                                        listenTimes = 0,
                                        createdAt = System.currentTimeMillis()
                                    )
                                    
                                    Log.d("AudioTest", "Adding real audio to database...")
                                    val audioId = audioRepository.add(realAudio, selectedAudioUri!!)
                                    Log.d("AudioTest", "Real audio added with ID: $audioId")
                                    
                                    // Clear selection after adding
                                    selectedAudioUri = null
                                } catch (e: Exception) {
                                    Log.e("AudioTest", "Failed to add real audio: ${e.message}", e)
                                }
                            }
                        }
                    ) {
                        Text("Add to Database")
                    }
                }
                
                Divider()
                
                // Section 3: Available audios list
                Text(
                    text = "3. Available Audios (${availableAudios.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (availableAudios.isEmpty()) {
                    Text(
                        text = "No audios available. Add some audio first!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    availableAudios.forEach { audio ->
                        Button(
                            onClick = { 
                                Log.d("AudioTest", "Selected audio: ${audio.title} with ID: ${audio.id}")
                                selectedAudioId = audio.id 
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("${audio.title} (ID: ${audio.id})")
                                Text(
                                    text = "Path: ${audio.filePath}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Cần quyền thông báo để sử dụng audio service")
        }
    }
}