package com.sun.japaneselisteningtrainer.data.repository.local

import android.content.ContentValues
import android.net.Uri
import android.provider.BaseColumns
import android.util.Log
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository
import com.sun.japaneselisteningtrainer.data.storage.AudioFileStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

interface ChangeObserver {
    fun onChanged()
}

class DbChangeNotifier {
    private val observers = mutableListOf<ChangeObserver>()

    fun register(observer: ChangeObserver) {
        observers.add(observer)
    }

    fun remove(observer: ChangeObserver) {
        observers.remove(observer)
    }

    fun notifyChanged() {
        for (observer in observers) {
            observer.onChanged()
        }
    }
}

class LocalAudioRepository(dbHelper: JLTDbHelper, private val audioFileStorage: AudioFileStorage) : AudioRepository {
    private val db = dbHelper.writableDatabase
    private val notifier = DbChangeNotifier()

    /**
     * Add a new audio to the database with the given audio's source.
     */
    override suspend fun add(audio: Audio, source: Uri) : Int = withContext(Dispatchers.IO) {
        val filePath = audioFileStorage.save(source).toString()
        val savedAudio = audio.copy(filePath = filePath, createdAt = System.currentTimeMillis())
        val values = ContentValues().apply {
            put(JLTContract.Audio.COLUMN_FOLDER_ID, savedAudio.folderId)
            put(JLTContract.Audio.COLUMN_TITLE, savedAudio.title)
            put(JLTContract.Audio.COLUMN_FILE_PATH, savedAudio.filePath)
            put(JLTContract.Audio.COLUMN_SCRIPT, savedAudio.script)
            put(JLTContract.Audio.COLUMN_TRANSLATE, savedAudio.translate)
            put(JLTContract.Audio.COLUMN_IS_SUSPENDED, savedAudio.isSuspended)
            put(JLTContract.Audio.COLUMN_IS_FAVORITE, savedAudio.isFavorite)
            put(JLTContract.Audio.COLUMN_LISTEN_TIMES, savedAudio.listenTimes)
            put(JLTContract.Audio.COLUMN_CREATED_AT, savedAudio.createdAt)
        }
        val id = db.insert(JLTContract.Audio.TABLE_NAME, null, values)
        notifier.notifyChanged()
        return@withContext id.toInt()
    }

    override suspend fun delete(audio: Audio) = withContext(Dispatchers.IO) {
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(audio.id.toString())
        db.delete(JLTContract.Audio.TABLE_NAME, selection, selectionArgs)
        notifier.notifyChanged()
    }

    override suspend fun update(audio: Audio) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(JLTContract.Audio.COLUMN_FOLDER_ID, audio.folderId)
            put(JLTContract.Audio.COLUMN_TITLE, audio.title)
            put(JLTContract.Audio.COLUMN_FILE_PATH, audio.filePath)
            put(JLTContract.Audio.COLUMN_SCRIPT, audio.script)
            put(JLTContract.Audio.COLUMN_TRANSLATE, audio.translate)
            put(JLTContract.Audio.COLUMN_IS_SUSPENDED, audio.isSuspended)
            put(JLTContract.Audio.COLUMN_IS_FAVORITE, audio.isFavorite)
            put(JLTContract.Audio.COLUMN_LISTEN_TIMES, audio.listenTimes)
            put(JLTContract.Audio.COLUMN_CREATED_AT, audio.createdAt)
        }
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(audio.id.toString())
        db.update(JLTContract.Audio.TABLE_NAME, values, selection, selectionArgs)
        notifier.notifyChanged()
    }

    override fun getAllAudioStream(): Flow<List<Audio>> = callbackFlow {

        fun query(): List<Audio> {
            val audioList = mutableListOf<Audio>()
            val query = "SELECT * FROM ${JLTContract.Audio.TABLE_NAME}"
            val cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                    val folderId =
                        cursor.getInt(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_FOLDER_ID))
                    val title =
                        cursor.getString(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_TITLE))
                    val filePath =
                        cursor.getString(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_FILE_PATH))
                    val script =
                        cursor.getString(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_SCRIPT))
                    val translate =
                        cursor.getString(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_TRANSLATE))
                    val isSuspended =
                        (cursor.getInt(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_IS_SUSPENDED)) == 1)
                    val isFavorite =
                        (cursor.getInt(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_IS_FAVORITE)) == 1)
                    val listenTimes =
                        cursor.getInt(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_LISTEN_TIMES))
                    val createdAt =
                        cursor.getLong(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_CREATED_AT))
                    val audio = Audio(
                        id,
                        title,
                        folderId,
                        filePath,
                        script,
                        translate,
                        isSuspended,
                        isFavorite,
                        listenTimes,
                        createdAt
                    )
                    audioList.add(audio)
                } while (cursor.moveToNext())
            }
            cursor.close()
            return audioList
        }

        val observer = object : ChangeObserver {
            override fun onChanged() {
                try {
                    trySend(query()).isSuccess
                } catch (e: Exception) {
                    Log.v("MyTag", e.toString())
                }
            }
        }

        notifier.register(observer)
        trySend(query()).isSuccess

        // Hủy đăng ký khi Flow bị cancel
        awaitClose {
            notifier.remove(observer)
        }
    }

    override fun getAudioStream(id: Int): Flow<Audio?> = callbackFlow {
        fun query(): Audio? {
            val query = "SELECT * FROM ${JLTContract.Audio.TABLE_NAME} WHERE ${BaseColumns._ID} = ?"
            val cursor = db.rawQuery(query, arrayOf(id.toString()))
            if (cursor.moveToFirst()) {
                val folderId =
                    cursor.getInt(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_FOLDER_ID))
                val title =
                    cursor.getString(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_TITLE))
                val filePath =
                    cursor.getString(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_FILE_PATH))
                val script =
                    cursor.getString(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_SCRIPT))
                val translate =
                    cursor.getString(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_TRANSLATE))
                val isSuspended =
                    (cursor.getInt(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_IS_SUSPENDED)) == 1)
                val isFavorite =
                    (cursor.getInt(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_IS_FAVORITE)) == 1)
                val listenTimes =
                    cursor.getInt(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_LISTEN_TIMES))
                val createdAt =
                    cursor.getLong(cursor.getColumnIndexOrThrow(JLTContract.Audio.COLUMN_CREATED_AT))
                return Audio(
                    id,
                    title,
                    folderId,
                    filePath,
                    script,
                    translate,
                    isSuspended,
                    isFavorite,
                    listenTimes,
                    createdAt
                )
            }
            cursor.close()
            return null
        }

        val observer = object : ChangeObserver {
            override fun onChanged() {
                trySend(query()).isSuccess
            }
        }

        notifier.register(observer)
        trySend(query()).isSuccess

        awaitClose {
            notifier.remove(observer)
        }
    }
}
