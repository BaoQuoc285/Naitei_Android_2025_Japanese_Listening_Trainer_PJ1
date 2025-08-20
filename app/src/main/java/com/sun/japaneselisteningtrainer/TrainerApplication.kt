package com.sun.japaneselisteningtrainer

import android.app.Application
import com.sun.japaneselisteningtrainer.data.AppContainer
import com.sun.japaneselisteningtrainer.data.AppDataContainer
import com.sun.japaneselisteningtrainer.data.MockAppDataContainer

class TrainerApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }

}
