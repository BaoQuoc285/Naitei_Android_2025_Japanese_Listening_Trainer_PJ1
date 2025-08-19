package com.sun.japaneselisteningtrainer.data

import android.content.Context
import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.repository.mock.MockFolderRepository
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository
import com.sun.japaneselisteningtrainer.data.repository.local.JLTDbHelper
import com.sun.japaneselisteningtrainer.data.repository.local.LocalAudioRepository
import com.sun.japaneselisteningtrainer.data.repository.local.LocalFolderRepository
import com.sun.japaneselisteningtrainer.data.repository.mock.MockAudioRepository
import com.sun.japaneselisteningtrainer.data.storage.AudioFileStorage
import com.sun.japaneselisteningtrainer.data.storage.ExternalAudioFileStorage
import com.sun.japaneselisteningtrainer.service.AudioServiceManager


/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val audioRepository: AudioRepository
    val folderRepository: FolderRepository
    val audioServiceManager: AudioServiceManager
}

/**
 * [AppContainer] implementation that provides instance of mock repository
 */
class MockAppDataContainer(private val context: Context) : AppContainer {
    override val audioRepository: AudioRepository = MockAudioRepository()
    override val folderRepository: FolderRepository = MockFolderRepository()
    
    override val audioServiceManager: AudioServiceManager by lazy {
        AudioServiceManager(context, audioRepository)
    }
}

class AppDataContainer(private val context: Context) : AppContainer {
    private val dbHelper = JLTDbHelper(context)
    private val audioFileStorage: AudioFileStorage = ExternalAudioFileStorage(context)

    override val audioRepository: AudioRepository by lazy {
        LocalAudioRepository(dbHelper, audioFileStorage)
    }

    override val folderRepository: FolderRepository by lazy {
        LocalFolderRepository(dbHelper)
    }
    
    override val audioServiceManager: AudioServiceManager by lazy {
        AudioServiceManager(context, audioRepository)
    }
}
