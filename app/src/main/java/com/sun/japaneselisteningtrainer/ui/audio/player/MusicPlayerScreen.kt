package com.sun.japaneselisteningtrainer.ui.audio.player

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sun.japaneselisteningtrainer.TrainerTopAppBar
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.ui.audio.player.components.AudioProgressBar
import com.sun.japaneselisteningtrainer.ui.audio.player.components.EditAudioButton
import com.sun.japaneselisteningtrainer.ui.audio.player.components.LyricsBox
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
    onNavigationBack: () -> Unit,
    onEditAudio: () -> Unit,
) {
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
        PlayerContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            audio = Audio(1, "日本語"),
        )
    }
}

@Composable
fun PlayerContainer(
    modifier: Modifier = Modifier,
    audio: Audio
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
                    isPlaying = true
                )
            } else {
                LyricView(
                    audio = Audio(1, "日本語"),
                    modifier = Modifier.weight(1f)
                )
            }
            AudioTitle(
                modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_8)),
                title = "日本語"
            )
            AudioController(modifier = Modifier)
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
fun AudioController(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
    ) {
        AudioProgressBar(
            progress = 0.1f,
            currentPosition = 10000,
            duration = 200000,
            onSeek = { /* TODO: call onSeek */},
            onSeekFinished = { },
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(8.dp))
        TransportBar(
            isPlaying = true,
            isShuffleOn = true,
            isFavorite = true,
            onToggleShuffle = { },
            onPrevious = { },
            onPlayPause = { },
            onNext = { },
            onToggleFavorite = { }
        )
    }
}

@Composable
fun LyricView(
    modifier: Modifier = Modifier,
    audio: Audio
) {
    LyricsBox(
        lines = listOf("Ahayo", "Arigatougozaimasu"),
        currentLineIndex = 0,
        currentLineProgress = 1.0f,
        onSeekToLine = { /* map line -> time tại VM rồi call onSeek */ },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun AudioPlayerPreview() {
    JapaneseListeningTrainerTheme {
        MusicPlayerScreen(
            onEditAudio = {},
            modifier = Modifier,
            onNavigationBack = { },
        )
    }
}
