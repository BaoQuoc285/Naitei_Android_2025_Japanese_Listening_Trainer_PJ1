package com.sun.japaneselisteningtrainer.ui.folder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.model.Folder
import kotlinx.coroutines.launch
import java.util.Stack

class FolderListViewModel(private val folderRepository: FolderRepository) : ViewModel() {


    var folderListUiState by mutableStateOf(FolderListUiState())

    init {
        viewModelScope.launch {
            folderRepository.getAllFolderStream().collect { it ->
                folderListUiState = folderListUiState.copy(
                    folderList = it
                )
            }
        }
    }

    fun updateUiState(newUiState: FolderListUiState) {
        folderListUiState = newUiState
    }

    suspend fun deleteFolder(folderId: Int = folderListUiState.selectedFolder.id) {
        folderRepository.delete(folderId)
        DeletedCache.pop()
    }

    fun openFolderMenu(folder: Folder) {
        folderListUiState = folderListUiState.copy(showMenu = true, selectedFolder = folder)
    }

    fun dismissMenu() {
        folderListUiState = folderListUiState.copy(showMenu = false)
    }

    fun deleteSelectedFolderFromUi() {
        DeletedCache.push(folderListUiState.folderList)
        folderListUiState = folderListUiState.copy(
            folderList = folderListUiState.folderList.filter { it.id != folderListUiState.selectedFolder.id }
        )
    }

    fun restoreFolder() {
        folderListUiState = folderListUiState.copy(
            folderList = DeletedCache.pop()
        )
    }

    fun showEditForm() {
        folderListUiState = folderListUiState.copy(showEditForm = true)
    }

    fun dismissEditForm() {
        folderListUiState = folderListUiState.copy(showEditForm = false)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class FolderListUiState(
    val folderList: List<Folder> = listOf(),
    val showMenu: Boolean = false,
    val selectedFolder: Folder = Folder(),
    val showEditForm: Boolean = false
)

private object DeletedCache {
    private var folderListStack: Stack<List<Folder>> = Stack()

    fun push(folderList: List<Folder>) {
        folderListStack.push(folderList)
    }

    fun pop(): List<Folder> {
        return folderListStack.pop()
    }

    fun reset() {
        folderListStack.clear()
    }
}
