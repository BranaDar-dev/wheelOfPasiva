package com.bramish.wheelofpasiva.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bramish.wheelofpasiva.domain.model.Player
import com.bramish.wheelofpasiva.domain.model.RoomError
import com.bramish.wheelofpasiva.domain.model.WheelSlice
import com.bramish.wheelofpasiva.domain.usecase.ObserveRoomUseCase
import com.bramish.wheelofpasiva.domain.usecase.SetSecretWordUseCase
import com.bramish.wheelofpasiva.domain.usecase.UpdateGameStateUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel for the Game screen.
 * Manages the wheel spinning logic, letter guessing, and scoring.
 */
class GameViewModel(
    private val observeRoomUseCase: ObserveRoomUseCase,
    private val updateGameStateUseCase: UpdateGameStateUseCase,
    private val setSecretWordUseCase: SetSecretWordUseCase,
    private val roomId: String,
    private val playerId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var currentPlayers: List<Player> = emptyList()
    private var currentTurnIndex: Int = 0
    private var hostId: String = ""
    private var hasShownSecretWordDialog: Boolean = false
    private var pendingPoints: Int = 0 // Points from current spin, added when letter is guessed

    init {
        observeRoom()
    }

    private fun observeRoom() {
        viewModelScope.launch {
            observeRoomUseCase(roomId)
                .catch { error ->
                    _uiState.value = GameUiState.Error(
                        error.message ?: "Failed to load game. Please try again."
                    )
                }
                .collect { result ->
                    result
                        .onSuccess { room ->
                            currentPlayers = room.players
                            currentTurnIndex = room.currentTurnIndex
                            hostId = room.hostId

                            // Game players are all players except the host (host doesn't play)
                            val gamePlayers = room.players.filter { it.id != room.hostId }

                            // Find current turn player from game players only
                            val currentTurnPlayer = if (gamePlayers.isNotEmpty()) {
                                val safeIndex = room.currentTurnIndex.coerceIn(0, gamePlayers.size - 1)
                                gamePlayers[safeIndex]
                            } else {
                                null
                            }

                            // Check if it's the current user's turn (host never has a turn)
                            val isHost = playerId == room.hostId
                            val isMyTurn = !isHost && currentTurnPlayer?.id == playerId

                            // Show secret word dialog if host and no word set yet
                            val shouldShowDialog = isHost &&
                                room.secretWord.isNullOrBlank() &&
                                !hasShownSecretWordDialog

                            if (shouldShowDialog) {
                                hasShownSecretWordDialog = true
                            }

                            // Convert revealed letters string to Set<Char>
                            val revealedLetters = room.revealedLetters.uppercase().toSet()

                            // Get last slice result
                            val lastSliceResult = room.lastSliceIndex?.let {
                                WheelSlice.getSliceAtIndex(it)
                            }

                            // Find winner name if game is over
                            val winnerName = room.winnerId?.let { wId ->
                                room.players.find { it.id == wId }?.nickname
                            }

                            // Determine if we should show guess input
                            // Show it when: it's my turn, not spinning, landed on points, and game not over
                            val showGuessInput = isMyTurn &&
                                !room.isSpinning &&
                                lastSliceResult is WheelSlice.Points &&
                                !room.isGameOver &&
                                !room.hasExtraTurn

                            _uiState.value = GameUiState.Playing(
                                roomId = roomId,
                                players = gamePlayers, // Only players who play (not host)
                                isSpinning = room.isSpinning,
                                currentTurnPlayer = currentTurnPlayer,
                                currentTurnIndex = room.currentTurnIndex,
                                isMyTurn = isMyTurn,
                                isHost = isHost,
                                secretWord = room.secretWord,
                                revealedLetters = revealedLetters,
                                playerScores = room.playerScores,
                                myScore = room.playerScores[playerId] ?: 0,
                                lastSliceIndex = room.lastSliceIndex,
                                lastSliceResult = lastSliceResult,
                                hasExtraTurn = room.hasExtraTurn,
                                showSecretWordDialog = shouldShowDialog,
                                showGuessInput = showGuessInput,
                                isGameOver = room.isGameOver,
                                winnerId = room.winnerId,
                                winnerName = winnerName
                            )

                            // Store pending points if it's a points slice
                            if (lastSliceResult is WheelSlice.Points && isMyTurn && !room.isSpinning) {
                                pendingPoints = lastSliceResult.value
                            }
                        }
                        .onFailure { error ->
                            val errorMessage = when (error) {
                                is RoomError.RoomNotFound -> "Room not found"
                                is RoomError.NetworkError -> "Connection error. Please check your internet."
                                else -> error.message ?: "Failed to load game"
                            }
                            _uiState.value = GameUiState.Error(errorMessage)
                        }
                }
        }
    }

    /**
     * Spins the wheel to get a random slice.
     * Only the current turn player can spin. Host cannot spin.
     */
    fun spinWheel() {
        val state = _uiState.value
        if (state !is GameUiState.Playing || state.isSpinning || state.players.isEmpty()) {
            return
        }

        // Host cannot spin - they only set the secret word
        if (state.isHost || !state.isMyTurn || state.isGameOver) {
            return
        }

        viewModelScope.launch {
            // Start spinning
            updateGameStateUseCase(
                roomId = roomId,
                isSpinning = true,
                lastSliceIndex = null,
                hasExtraTurn = false
            )

            // Simulate spinning animation delay
            delay(2000)

            // Get random slice index (0-7)
            val sliceIndex = Random.nextInt(8)
            val slice = WheelSlice.getSliceAtIndex(sliceIndex)

            // Handle the spin result
            when (slice) {
                is WheelSlice.Bankrupt -> {
                    // Clear player's score and end turn
                    val newScores = state.playerScores.toMutableMap()
                    newScores[playerId] = 0
                    val nextTurn = (state.currentTurnIndex + 1) % state.players.size

                    updateGameStateUseCase(
                        roomId = roomId,
                        isSpinning = false,
                        lastSliceIndex = sliceIndex,
                        playerScores = newScores,
                        nextTurnIndex = nextTurn,
                        hasExtraTurn = false
                    )
                }
                is WheelSlice.ExtraTurn -> {
                    // Player gets another spin
                    updateGameStateUseCase(
                        roomId = roomId,
                        isSpinning = false,
                        lastSliceIndex = sliceIndex,
                        hasExtraTurn = true
                    )
                }
                is WheelSlice.Points -> {
                    // Store the points value, will be added when letter is guessed
                    pendingPoints = slice.value
                    updateGameStateUseCase(
                        roomId = roomId,
                        isSpinning = false,
                        lastSliceIndex = sliceIndex,
                        hasExtraTurn = false
                    )
                }
            }
        }
    }

    /**
     * Guesses a letter.
     * If correct, adds pending points to score and reveals the letter.
     * If wrong, ends the turn. Host cannot guess.
     */
    fun guessLetter(letter: Char) {
        val state = _uiState.value
        if (state !is GameUiState.Playing || state.isHost || !state.isMyTurn || state.isGameOver) {
            return
        }

        val secretWord = state.secretWord?.uppercase() ?: return
        val guessedLetter = letter.uppercaseChar()

        // Check if letter already guessed
        if (guessedLetter in state.revealedLetters) {
            return
        }

        viewModelScope.launch {
            val letterInWord = secretWord.contains(guessedLetter)
            val newRevealedLetters = state.revealedLetters + guessedLetter
            val newRevealedString = newRevealedLetters.joinToString("")

            // After guessing one letter, turn always moves to next player
            val nextTurn = (state.currentTurnIndex + 1) % state.players.size

            if (letterInWord) {
                // Count occurrences and add points
                val occurrences = secretWord.count { it == guessedLetter }
                val pointsEarned = pendingPoints * occurrences
                val newScore = (state.playerScores[playerId] ?: 0) + pointsEarned
                val newScores = state.playerScores.toMutableMap()
                newScores[playerId] = newScore

                // Check if word is fully revealed
                val isWordComplete = secretWord.filter { !it.isWhitespace() }
                    .all { it in newRevealedLetters }

                if (isWordComplete) {
                    // Word guessed by revealing all letters - game over
                    updateGameStateUseCase(
                        roomId = roomId,
                        revealedLetters = newRevealedString,
                        playerScores = newScores,
                        isGameOver = true,
                        winnerId = playerId
                    )
                } else {
                    // Correct letter - add points and move to next turn
                    updateGameStateUseCase(
                        roomId = roomId,
                        revealedLetters = newRevealedString,
                        playerScores = newScores,
                        nextTurnIndex = nextTurn,
                        lastSliceIndex = null
                    )
                }
            } else {
                // Wrong letter - no points, move to next turn
                updateGameStateUseCase(
                    roomId = roomId,
                    revealedLetters = newRevealedString,
                    nextTurnIndex = nextTurn,
                    lastSliceIndex = null
                )
            }
            pendingPoints = 0
        }
    }

    /**
     * Guesses the entire word.
     * If correct, doubles the player's score and ends the game.
     * If wrong, ends the turn. Host cannot guess.
     */
    fun guessWord(word: String) {
        val state = _uiState.value
        if (state !is GameUiState.Playing || state.isHost || !state.isMyTurn || state.isGameOver) {
            return
        }

        val secretWord = state.secretWord?.uppercase() ?: return
        val guessedWord = word.uppercase().trim()

        viewModelScope.launch {
            if (guessedWord == secretWord) {
                // Correct! Double the score
                val currentScore = state.playerScores[playerId] ?: 0
                val doubledScore = currentScore * 2
                val newScores = state.playerScores.toMutableMap()
                newScores[playerId] = doubledScore

                // Reveal all letters
                val allLetters = secretWord.filter { !it.isWhitespace() }.toSet()
                val newRevealedString = (state.revealedLetters + allLetters).joinToString("")

                updateGameStateUseCase(
                    roomId = roomId,
                    revealedLetters = newRevealedString,
                    playerScores = newScores,
                    isGameOver = true,
                    winnerId = playerId,
                    lastSliceIndex = null
                )
            } else {
                // Wrong guess - end turn
                val nextTurn = (state.currentTurnIndex + 1) % state.players.size
                updateGameStateUseCase(
                    roomId = roomId,
                    nextTurnIndex = nextTurn,
                    lastSliceIndex = null
                )
            }
            pendingPoints = 0
        }
    }

    /**
     * Sets the secret word/phrase for the game.
     * Only the host can set the secret word.
     */
    fun setSecretWord(word: String) {
        val state = _uiState.value
        if (state !is GameUiState.Playing || !state.isHost) {
            return
        }

        viewModelScope.launch {
            setSecretWordUseCase(roomId, word.uppercase())
        }
    }

    /**
     * Dismisses the secret word dialog without setting a word.
     */
    fun dismissSecretWordDialog() {
        val state = _uiState.value
        if (state is GameUiState.Playing) {
            _uiState.value = state.copy(showSecretWordDialog = false)
        }
    }

    /**
     * Retries loading the game (used after errors).
     */
    fun retry() {
        _uiState.value = GameUiState.Loading
        observeRoom()
    }

    fun getRoomId(): String = roomId
}
