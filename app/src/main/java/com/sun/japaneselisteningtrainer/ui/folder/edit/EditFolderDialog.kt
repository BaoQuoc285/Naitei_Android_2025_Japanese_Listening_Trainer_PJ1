package com.sun.japaneselisteningtrainer.ui.folder.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.data.model.Folder
import com.sun.japaneselisteningtrainer.ui.AppViewModelProvider
import com.sun.japaneselisteningtrainer.ui.folder.components.FolderFormDialog.FolderFormDialog
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun EditFolderDialog(
    selected: Folder,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditFolderViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selected) {
        viewModel.loadFolder(selected)
    }

    FolderFormDialog(
        title = stringResource(R.string.edit_folder_title),
        uiState = viewModel.uiState,
        onValueChange = viewModel::updateUiState,
        onCancel = onCancel,
        onConfirm = {
            coroutineScope.launch {
                viewModel.saveEditedFolder()
                onConfirm()
            }
        },
        modifier = modifier,
    )
}
