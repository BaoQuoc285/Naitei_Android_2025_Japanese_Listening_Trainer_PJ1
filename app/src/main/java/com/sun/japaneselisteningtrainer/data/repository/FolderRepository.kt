package com.sun.japaneselisteningtrainer.data.folder

import com.sun.japaneselisteningtrainer.data.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    suspend fun add(folder: Folder)
    suspend fun delete(folderId: Int)
    suspend fun update(folder: Folder)
    fun getAllFolderStream(): Flow<List<Folder>>
    fun getFolderStream(id: Int): Flow<Folder?>
    fun getFolderStream(title: String): Flow<Folder?>
}
