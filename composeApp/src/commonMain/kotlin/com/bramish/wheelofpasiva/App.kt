package com.bramish.wheelofpasiva

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import com.bramish.wheelofpasiva.di.AppContainer
import com.bramish.wheelofpasiva.presentation.game.GameScreen
import com.bramish.wheelofpasiva.presentation.home.HomeScreen
import com.bramish.wheelofpasiva.presentation.navigation.Screen
import com.bramish.wheelofpasiva.presentation.navigation.rememberSimpleNavigator
import com.bramish.wheelofpasiva.presentation.room.RoomScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Main application composable.
 * Sets up the dependency injection container and simple navigation.
 * Uses state-based navigation instead of navigation-compose to avoid iOS linkage issues.
 */
@Composable
@Preview
fun App() {
    // Create the dependency injection container
    val appContainer = remember { AppContainer() }

    // Create the simple navigator (replaces NavController)
    val navigator = rememberSimpleNavigator()

    MaterialTheme {
        when (val screen = navigator.currentScreen) {
            is Screen.Home -> {
                HomeScreen(
                    viewModel = appContainer.provideHomeViewModel(),
                    joinRoomViewModel = appContainer.provideJoinRoomViewModel(),
                    onNavigateToRoom = { roomId ->
                        navigator.navigateToRoom(roomId)
                    }
                )
            }
            is Screen.Room -> {
                RoomScreen(
                    viewModel = appContainer.provideRoomViewModel(screen.roomId),
                    onNavigateBack = {
                        navigator.navigateBack()
                    },
                    onStartGame = {
                        navigator.navigateToGame(screen.roomId)
                    }
                )
            }
            is Screen.Game -> {
                GameScreen(
                    viewModel = appContainer.provideGameViewModel(screen.roomId),
                    onNavigateBack = {
                        navigator.navigateBack()
                    }
                )
            }
        }
    }
}