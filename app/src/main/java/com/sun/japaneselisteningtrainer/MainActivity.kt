package com.sun.japaneselisteningtrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sun.japaneselisteningtrainer.ui.theme.JapaneseListeningTrainerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Bind to AudioService khi app khởi động
        val appContainer = (application as TrainerApplication).container
        appContainer.audioServiceManager.bindToService()
        
        setContent {
            JapaneseListeningTrainerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TrainerApp() // Main app with navigation
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Unbind service khi app thực sự tắt
        if (isFinishing) {
            val appContainer = (application as TrainerApplication).container
            appContainer.audioServiceManager.unbindFromService()
        }
    }
}

@Preview
@Composable
fun TrainerAppPreview() {
    JapaneseListeningTrainerTheme {
        TrainerApp()
    }
}
