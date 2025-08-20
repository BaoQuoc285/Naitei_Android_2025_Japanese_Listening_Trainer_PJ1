package com.sun.japaneselisteningtrainer.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import com.sun.japaneselisteningtrainer.R

@Composable
fun AudioFilePicker(
    onFileSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonText: String? = null
) {
    val context = LocalContext.current

    // Launcher mở file picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onFileSelected(it) }
    }

    Button(
        onClick = {
            // Mở picker chỉ cho audio
            launcher.launch("audio/*")
        },
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.AudioFile,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(buttonText ?: stringResource(R.string.select_audio_file))
    }
}

/**
 * Component với preview của file đã chọn
 */
@Composable
fun AudioFilePickerWithPreview(
    selectedUri: Uri?,
    onAudioSelected: (Uri) -> Unit,
    onClearSelection: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        AudioFilePicker(
            onFileSelected = onAudioSelected,
            enabled = enabled,
            buttonText = if (selectedUri == null) "Chọn File Audio" else "Đổi File Audio"
        )
        
        if (selectedUri != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AudioFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "File đã chọn:",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = selectedUri.lastPathSegment ?: "Unknown file",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    TextButton(onClick = onClearSelection) {
                        Text("Xóa")
                    }
                }
            }
        }
    }
}

