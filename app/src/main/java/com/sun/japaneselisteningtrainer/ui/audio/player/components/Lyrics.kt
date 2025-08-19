package com.sun.japaneselisteningtrainer.ui.audio.player.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.max

/* ---------- 1) Lyric line: theo theme, không hard-code màu ---------- */
@Composable
private fun LyricLine(
    text: String,
    isCurrent: Boolean,
    lineProgress: Float,  // 0f..1f cho dòng hiện tại
) {
    val cs = MaterialTheme.colorScheme
    val base = cs.onSurface.copy(alpha = 0.70f)     // chữ thường
    val highlight = cs.onSurface                    // chữ đang hát

    val annotated = remember(text, isCurrent, lineProgress) {
        if (!isCurrent || text.isBlank()) {
            buildAnnotatedString { withStyle(SpanStyle(color = base)) { append(text) } }
        } else {
            val p = lineProgress.coerceIn(0f, 1f)
            val cut = (text.length * p).toInt().coerceIn(0, text.length)
            buildAnnotatedString {
                withStyle(SpanStyle(color = highlight, fontWeight = FontWeight.SemiBold)) {
                    append(text.substring(0, cut))
                }
                if (cut < text.length) {
                    withStyle(SpanStyle(color = base)) { append(text.substring(cut)) }
                }
            }
        }
    }

    Text(
        text = annotated,
        fontSize = 16.sp,
        lineHeight = 22.sp
    )
}
/* ---------- 2) LyricsBox: container dùng surfaceVariant theo theme ---------- */
@Composable
fun LyricsBox(
    lines: List<String>,
    currentLineIndex: Int,
    currentLineProgress: Float,
    onSeekToLine: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Tự cuộn: đưa dòng hiện tại gần giữa viewport
    LaunchedEffect(currentLineIndex) {
        val target = max(currentLineIndex - 3, 0)
        listState.animateScrollToItem(target)
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(lines) { index, line ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noRippleClickable { onSeekToLine(index) }
                ) {
                    LyricLine(
                        text = line,
                        isCurrent = (index == currentLineIndex),
                        lineProgress = if (index == currentLineIndex) currentLineProgress else 0f
                    )
                }
            }
        }
    }
}

/* ---------- 3) Helpers giữ nguyên ---------- */
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    this.clickable(
        interactionSource = interaction,
        indication = null,
        onClick = onClick
    )
}

/* Container nhận swipe để toggle transcript (không dính màu) */
@Composable
fun TranscriptContainer(
    visible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val thresholdPx = 40f
    Box(
        modifier = modifier.pointerInput(thresholdPx) {
            var toggleTriggered = false
            var totalDx = 0f
            detectHorizontalDragGestures(
                onDragStart = {
                    toggleTriggered = false
                    totalDx = 0f
                },
                onHorizontalDrag = { _, dragAmount ->
                    if (!toggleTriggered) {
                        totalDx += dragAmount
                        if (abs(totalDx) >= thresholdPx) {
                            toggleTriggered = true
                            onToggle()
                        }
                    }
                },
                onDragEnd = { /* no-op */ },
                onDragCancel = { /* no-op */ }
            )
        }
    ) {
        if (visible) content()
    }
}
