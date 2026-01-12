package com.bramish.wheelofpasiva.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Sealed class representing different screens in the app.
 */
sealed class Screen {
    data object Home : Screen()
    data class Room(val roomId: String, val playerId: String) : Screen()
    data class Game(val roomId: String, val playerId: String) : Screen()
}

/**
 * Simple navigation manager using Compose state.
 * Replaces navigation-compose library to avoid iOS linkage issues.
 */
class SimpleNavigator {
    var currentScreen by mutableStateOf<Screen>(Screen.Home)
        private set

    fun navigateToHome() {
        currentScreen = Screen.Home
    }

    fun navigateToRoom(roomId: String, playerId: String) {
        currentScreen = Screen.Room(roomId, playerId)
    }

    fun navigateToGame(roomId: String, playerId: String) {
        currentScreen = Screen.Game(roomId, playerId)
    }

    fun navigateBack(): Boolean {
        return when (val screen = currentScreen) {
            is Screen.Home -> false // Can't go back from home
            is Screen.Room -> {
                currentScreen = Screen.Home
                true
            }
            is Screen.Game -> {
                currentScreen = Screen.Room(screen.roomId, screen.playerId)
                true
            }
        }
    }
}

@Composable
fun rememberSimpleNavigator(): SimpleNavigator {
    return remember { SimpleNavigator() }
}
