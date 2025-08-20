package com.sun.japaneselisteningtrainer.ui.folder.components.FolderFormDialog

import androidx.lifecycle.ViewModel
import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.model.Folder

class FolderFormViewModel (private val folderRepository: FolderRepository) : ViewModel() {

}

data class FolderFormUiState(
    val title: String = "",
    val description: String = "",
    val isEntryValid: Boolean = false,
    val isTitleError: Boolean = false
)

fun FolderFormUiState.toFolder(): Folder = Folder(
    name = title,
    description = description
)

fun Folder.toFolderFormUiState(): FolderFormUiState = FolderFormUiState(
    title = name,
    description = description,
    isEntryValid = false,
    isTitleError = false
)


