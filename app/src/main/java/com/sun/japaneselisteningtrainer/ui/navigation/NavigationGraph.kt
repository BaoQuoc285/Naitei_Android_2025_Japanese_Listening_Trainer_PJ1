/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.japaneselisteningtrainer.ui.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sun.japaneselisteningtrainer.ui.audio.entry.AudioEntryDestination
import com.sun.japaneselisteningtrainer.ui.audio.entry.AudioEntryScreen
import com.sun.japaneselisteningtrainer.ui.audio.player.MusicPlayerDestination
import com.sun.japaneselisteningtrainer.ui.audio.player.MusicPlayerScreen
import com.sun.japaneselisteningtrainer.ui.folder.FolderListDestination
import com.sun.japaneselisteningtrainer.ui.folder.FolderListScreen
import com.sun.japaneselisteningtrainer.ui.home.HomeDestination
import com.sun.japaneselisteningtrainer.ui.home.HomeScreen
import com.sun.japaneselisteningtrainer.ui.navigation.NavItem.Add
import com.sun.japaneselisteningtrainer.ui.navigation.NavItem.Companion.items
import com.sun.japaneselisteningtrainer.ui.navigation.NavItem.Folder
import com.sun.japaneselisteningtrainer.ui.navigation.NavItem.Home
import com.sun.japaneselisteningtrainer.ui.theme.JapaneseListeningTrainerTheme

sealed class NavItem(
    val icon: ImageVector,
    val des: NavigationDestination?
) {
    object Home : NavItem(Icons.Default.Home, HomeDestination)
    object Folder : NavItem(Icons.Default.Menu, FolderListDestination)
    object Add : NavItem(Icons.Default.Add, AudioEntryDestination)
    object Search : NavItem(Icons.Default.Search, null)
    object Profile : NavItem(Icons.Default.Person, null)

    companion object {
        val items by lazy { listOf(Home, Folder, Add, Search, Profile) }
    }
}


/**
 * Provides Navigation graph for the application.
 */
@Composable
fun TrainerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateToAudioEntry = { navController.navigate(AudioEntryDestination.route) },
                navigateToMusicPlayer = { audioId ->
                    navController.navigate(MusicPlayerDestination.createRoute(audioId))
                },
                navigationBar = {
                    TrainerNavigationBar(
                        selectedItem = Home,
                        navController = navController
                    )
                }
            )
        }
        composable(route = AudioEntryDestination.route) {
            AudioEntryScreen(
                navigateBack = {
                    navController.popBackStack()
                },
                onNavigationUp = {
                    navController.navigateUp()
                }
            )
        }
        composable(route = FolderListDestination.route) {
            FolderListScreen(
                navigateBar = {
                    TrainerNavigationBar(
                        selectedItem = Folder,
                        navController = navController
                    )
                }
            )
        }
        composable(
            route = MusicPlayerDestination.routeWithArgs,
            arguments = listOf(navArgument(MusicPlayerDestination.audioIdArg) { type = NavType.IntType })
        ) { backStackEntry ->
            val audioId = backStackEntry.arguments?.getInt(MusicPlayerDestination.audioIdArg) ?: 1
            MusicPlayerScreen(
                audioId = audioId,
                onNavigationBack = { navController.popBackStack() },
                onEditAudio = { /* TODO: navigate to edit screen */ }
            )
        }
    }
}


@Composable
fun TrainerNavigationBar(selectedItem: NavItem, navController: NavHostController) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (item in items) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(4.dp)
                ) {
                    TrainerNavigationBarItem(
                        icon = item.icon,
                        onClick = {
                            item.des?.route?.let { navController.navigate(it) }
                        },
                        isSelected = when (item) {
                            Add -> true
                            selectedItem -> true
                            else -> false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TrainerNavigationBarItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: () -> Unit,
    isSelected: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp) // icon to hơn mặc định
            )
        }
    }
}

@Preview
@Composable
fun TrainerNavigationBarPreview() {
    JapaneseListeningTrainerTheme {
        val navController = rememberNavController()
        TrainerNavigationBar(
            selectedItem = NavItem.Home,
            navController = navController,
        )
    }
}