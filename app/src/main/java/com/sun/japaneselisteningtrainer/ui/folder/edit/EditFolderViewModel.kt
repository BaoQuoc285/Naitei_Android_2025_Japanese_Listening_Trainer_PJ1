package com.sun.japaneselisteningtrainer.ui.folder.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.model.Folder
import com.sun.japaneselisteningtrainer.ui.folder.components.FolderFormDialog.DESCRIPTION_MAX_LENGTH
import com.sun.japaneselisteningtrainer.ui.folder.components.FolderFormDialog.FolderFormUiState
import com.sun.japaneselisteningtrainer.ui.folder.components.FolderFormDialog.toFolderFormUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditFolderViewModel(val folderRepository: FolderRepository) : ViewModel() {
    var uiState by mutableStateOf(FolderFormUiState())
    private var editingFolder: Folder = Folder()

    fun loadFolder(folder: Folder) {
        uiState = folder.toFolderFormUiState()
        editingFolder = folder
    }

    fun updateUiState(newUiState: FolderFormUiState) {
        viewModelScope.launch {
            uiState = newUiState.copy(
                isEntryValid = validateInput(newUiState),
                isTitleError = isTitleError(newUiState.title)
            )
        }
    }

    private suspend fun validateInput(uiState: FolderFormUiState): Boolean {
        return with(uiState) {
            this.validateTitleInput()
        }
    }

    private suspend fun FolderFormUiState.validateTitleInput(): Boolean {
        val title = this.title
        val des = this.description
        return when {
            title.isBlank() -> false
            des.length > DESCRIPTION_MAX_LENGTH -> false
            title == editingFolder.name && des == editingFolder.description -> false
            title != editingFolder.name && title.exists() -> false
            else -> true
        }
    }

    private suspend fun String.exists() = (folderRepository.getFolderStream(this).first() != null)

    private suspend fun isTitleError(title: String): Boolean =
        title != editingFolder.name && title.exists()

    suspend fun saveEditedFolder(
        folder: Folder = Folder(
            id = editingFolder.id,
            name = uiState.title,
            description = uiState.description
        )
    ) {
        folderRepository.update(folder)
    }
}

