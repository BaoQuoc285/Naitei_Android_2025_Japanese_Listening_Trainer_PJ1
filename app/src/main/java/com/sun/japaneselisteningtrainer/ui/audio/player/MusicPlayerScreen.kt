package com.sun.japaneselisteningtrainer.ui.audio.player

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.sun.japaneselisteningtrainer.TrainerTopAppBar
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.TrainerApplication
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.service.AudioServiceManager
import com.sun.japaneselisteningtrainer.ui.AppViewModelProvider
import kotlinx.coroutines.launch
import com.sun.japaneselisteningtrainer.ui.audio.player.components.AudioProgressBar
import com.sun.japaneselisteningtrainer.ui.audio.player.components.EditAudioButton
import com.sun.japaneselisteningtrainer.ui.audio.player.components.FavoriteToggleButton
import com.sun.japaneselisteningtrainer.ui.audio.player.components.LyricsBox
import com.sun.japaneselisteningtrainer.ui.audio.player.components.RandomToggleButton
import com.sun.japaneselisteningtrainer.ui.audio.player.components.RotatingDisc
import com.sun.japaneselisteningtrainer.ui.audio.player.components.TranscriptContainer
import com.sun.japaneselisteningtrainer.ui.audio.player.components.TransportBar
import com.sun.japaneselisteningtrainer.ui.navigation.NavigationDestination
import com.sun.japaneselisteningtrainer.ui.theme.JapaneseListeningTrainerTheme

object MusicPlayerDestination : NavigationDestination {
    override val route = "audio_player"
    override val titleRes = R.string.audio_player_title
    const val audioIdArg = "audioId"
    val routeWithArgs = "$route/{$audioIdArg}"

    fun createRoute(id: Int) = "$route/$id"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    modifier: Modifier = Modifier,
    audioId: Int = 1, // ID của audio cần phát
    onNavigationBack: () -> Unit,
    onEditAudio: () -> Unit,
    musicPlayerViewModel: MusicPlayerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scope = rememberCoroutineScope()

    // Observe ViewModel states
    val isServiceConnected by musicPlayerViewModel.isServiceConnected.collectAsState()
    val isPlaying by musicPlayerViewModel.isPlaying.collectAsState()
    val currentPosition by musicPlayerViewModel.currentPosition.collectAsState()
    val duration by musicPlayerViewModel.duration.collectAsState()
    val currentAudio by musicPlayerViewModel.currentAudioWithOverride.collectAsState()
    val isLoadingAudio by musicPlayerViewModel.isLoadingAudio.collectAsState()
    val audioLoadError by musicPlayerViewModel.audioLoadError.collectAsState()

    // Bind to service when screen opens
    LaunchedEffect(Unit) {
        musicPlayerViewModel.bindToService()
    }

    // Load audio from database when audioId changes
    LaunchedEffect(audioId, isServiceConnected) {
        if (isServiceConnected) {
            musicPlayerViewModel.loadAndPlayAudio(audioId)
        }
    }



    // Cleanup when screen closes
    DisposableEffect(Unit) {
        onDispose {
            musicPlayerViewModel.unbindFromService()
        }
    }
    Scaffold(
        topBar = {
            TrainerTopAppBar(
                title = stringResource(MusicPlayerDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigationBack,
                actions = {
                    EditAudioButton(onClick = onEditAudio)
                }
            )
        }
    ) { inner ->
        // Container nhận swipe để toggle transcript
        if (isLoadingAudio) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Đang tải audio...")
                }
            }
        } else if (audioLoadError != null) {
            // Error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = audioLoadError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            musicPlayerViewModel.loadAndPlayAudio(audioId)
                        }
                    ) {
                        Text("Thử lại")
                    }
                }
            }
        } else {
            // Success state với real data - sử dụng currentAudio nếu có
            val audio = currentAudio
            if (audio != null) {
                PlayerContainer(
                    musicPlayerViewModel = musicPlayerViewModel,
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    duration = duration,
                    currentAudio = audio,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner)
                )
            } else {
                // No audio found state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không tìm thấy audio với ID: $audioId",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerContainer(
    musicPlayerViewModel: MusicPlayerViewModel,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    currentAudio: Audio,
    modifier: Modifier = Modifier
) {
    var isTranscriptVisible by remember { mutableStateOf(false) }
    TranscriptContainer(
        visible = true,
        onToggle = { isTranscriptVisible = !isTranscriptVisible },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.dp_16))
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!isTranscriptVisible) {
                RotatingDiscBox(
                    modifier = Modifier
                        .padding(30.dp, 0.dp, 30.dp, 30.dp)
                        .fillMaxWidth(),
                    isPlaying = isPlaying
                )
            } else {
                LyricView(
                    audio = currentAudio,
                    modifier = Modifier.weight(1f)
                )
            }
            AudioTitle(
                modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_8)),
                title = currentAudio.title
            )
            AudioController(
                musicPlayerViewModel = musicPlayerViewModel,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                currentAudio = currentAudio,
                modifier = Modifier
            )
        }
    }
}

@Composable
fun RotatingDiscBox(
    modifier: Modifier,
    isPlaying: Boolean
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        RotatingDisc(
            imageRes = R.drawable.son_tung,
            isPlaying = isPlaying,
            size = 260.dp            // có thể 260–280dp cho đẹp
        )
    }
}

@Composable
fun AudioTitle(
    modifier: Modifier = Modifier,
    title: String,
) {
    Text(
        modifier = modifier,
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.displayMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun AudioController(
    musicPlayerViewModel: MusicPlayerViewModel,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    currentAudio: Audio,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
    ) {
        AudioProgressBar(
            progress = musicPlayerViewModel.getProgress(),
            currentPosition = currentPosition,
            duration = duration,
            onSeek = { progress ->
                val newPosition = (duration * progress).toLong()
                musicPlayerViewModel.seekTo(newPosition)
            },
            onSeekFinished = { },
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(8.dp))
        TransportBar(
            isPlaying = isPlaying,
            isShuffleOn = musicPlayerViewModel.isShuffleEnabled(),
            isFavorite = currentAudio.isFavorite,
            onToggleShuffle = { musicPlayerViewModel.toggleShuffle() },
            onPrevious = { musicPlayerViewModel.previousTrack() },
            onPlayPause = { musicPlayerViewModel.togglePlayPause() },
            onNext = { musicPlayerViewModel.nextTrack() },
            onToggleFavorite = {
                // Toggle favorite trong database
                musicPlayerViewModel.toggleFavoriteStatus(currentAudio)
            }
        )
    }
}

@Composable
fun LyricView(
    audio: Audio,
    modifier: Modifier = Modifier
) {
    // Split script thành lines
    val scriptLines = remember(audio.script) {
        audio.script.split("\n").filter { it.isNotBlank() }
    }

    LyricsBox(
        lines = scriptLines,
        currentLineIndex = 0, // TODO: Track current line based on playback position
        currentLineProgress = 0f, // TODO: Calculate line progress
        onSeekToLine = { lineIndex ->
            // TODO: Map line index to time position và seek
            // Tạm thời seek về đầu
            // audioServiceManager.seekTo(0L)
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun AudioPlayerPreview() {
    JapaneseListeningTrainerTheme {
        MusicPlayerScreen(
            audioId = 1,
            onEditAudio = {},
            modifier = Modifier,
            onNavigationBack = { },
        )
    }
}