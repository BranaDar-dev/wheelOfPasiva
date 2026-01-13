package com.bramish.wheelofpasiva.presentation.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bramish.wheelofpasiva.domain.model.WheelSlice
import com.bramish.wheelofpasiva.domain.model.WheelSlice.Companion.displayText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Wheel slice colors
private val sliceColors = listOf(
    Color(0xFF673AB7), // Deep Purple
    Color(0xFF3F51B5), // Indigo
    Color(0xFFFFC107), // Amber (Gold)
    Color(0xFFB71C1C), // Bankrupt - Crimson
    Color(0xFF673AB7), // Deep Purple
    Color(0xFF3F51B5), // Indigo
    Color(0xFFFFC107), // Amber (Gold)
    Color(0xFF9C27B0)  // Extra Turn - Purple Accent
)

/**
 * Game screen with the Wheel of Fortune style gameplay.
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Game - Room ${viewModel.getRoomId()}") }, // Reverted
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colors.onBackground
                        )
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is GameUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is GameUiState.Playing -> {
                    GameContent(
                        state = state,
                        onSpin = { viewModel.spinWheel() },
                        onSetSecretWord = { word, language -> viewModel.setSecretWord(word, language) },
                        onDismissSecretWordDialog = { viewModel.dismissSecretWordDialog() },
                        onGuessLetter = { letter -> viewModel.guessLetter(letter) },
                        onGuessWord = { word -> viewModel.guessWord(word) }
                    )
                }

                is GameUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colors.error,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.body1
                            )
                            Button(onClick = { viewModel.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameContent(
    state: GameUiState.Playing,
    onSpin: () -> Unit,
    onSetSecretWord: (String, com.bramish.wheelofpasiva.domain.model.Language) -> Unit,
    onDismissSecretWordDialog: () -> Unit,
    onGuessLetter: (Char) -> Unit,
    onGuessWord: (String) -> Unit
) {
    // Animation for wheel rotation
    var targetRotation by remember { mutableStateOf(0f) }
    var lastProcessedSlice by remember { mutableStateOf<Int?>(null) }
    val rotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 2000),
        label = "wheel_rotation"
    )

    // When we get a new slice result, calculate rotation to land on it
    LaunchedEffect(state.lastSliceIndex) {
        if (state.lastSliceIndex != null && state.lastSliceIndex != lastProcessedSlice) {
            lastProcessedSlice = state.lastSliceIndex
            val sliceIndex = state.lastSliceIndex

            // Each slice is 45 degrees
            // Slice 0's center is at 22.5 degrees from top
            // To center slice N under the arrow (at top), we need to rotate by:
            // -(N * 45 + 22.5) degrees
            val targetAngle = -(sliceIndex * 45f + 22.5f)

            // Normalize target angle to 0-360 range
            val normalizedTarget = ((targetAngle % 360f) + 360f) % 360f

            // Current rotation normalized
            val currentNormalized = ((targetRotation % 360f) + 360f) % 360f

            // Calculate adjustment needed
            var adjustment = normalizedTarget - currentNormalized
            if (adjustment > 180f) adjustment -= 360f
            if (adjustment < -180f) adjustment += 360f

            // Add 5 full rotations for spinning effect, plus the adjustment
            targetRotation = targetRotation + 5 * 360f + adjustment
        }
    }

    // Secret word dialog for host
    if (state.showSecretWordDialog) {
        SecretWordDialog(
            onConfirm = onSetSecretWord,
            onDismiss = onDismissSecretWordDialog
        )
    }

    // Game over dialog
    if (state.isGameOver) {
        GameOverDialog(
            winnerName = state.winnerName ?: "Unknown",
            winnerScore = state.winnerId?.let { state.playerScores[it] } ?: 0
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Word display
        if (!state.secretWord.isNullOrBlank()) {
            WordDisplay(
                displayWord = state.getDisplayWord(),
                isHost = state.isHost
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Players scores row
        PlayersScoreRow(
            players = state.players,
            playerScores = state.playerScores,
            currentTurnPlayerId = state.currentTurnPlayer?.id
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Current turn indicator
        TurnIndicator(
            isMyTurn = state.isMyTurn,
            currentTurnPlayerName = state.currentTurnPlayer?.nickname ?: "...",
            hasExtraTurn = state.hasExtraTurn
        )

        Spacer(modifier = Modifier.height(12.dp))


        // Pointer (arrow) above the wheel
        Text(
            text = "\u25BC",
            fontSize = 32.sp,
            color = MaterialTheme.colors.secondary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Wheel
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            WheelCanvas(
                rotation = rotation,
                lastSliceIndex = state.lastSliceIndex,
                modifier = Modifier.fillMaxSize()
            )
        }

        // (Result is now highlighted on the wheel itself)

        // Letter keyboard - always visible to show progress
        if (!state.secretWord.isNullOrBlank() && !state.isGameOver) {
            // Show message if letters are disabled (haven't spun yet)
            if (state.isMyTurn && !state.showGuessInput && !state.isSpinning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp,
                    backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Spin the wheel first to select a letter",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            LetterKeyboard(
                revealedLetters = state.revealedLetters,
                secretWord = state.secretWord,
                enabled = state.showGuessInput,
                language = state.language,
                onLetterClick = onGuessLetter
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Word guess button (only when it's my turn and landed on points)
        if (state.showGuessInput) {
            WordGuessArea(onGuessWord = onGuessWord)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Spin button, host message, or waiting message
        if (state.isHost && !state.isGameOver) {
            // Host view - they don't play, just watch
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp,
                backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "You are the host",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Watching ${state.currentTurnPlayer?.nickname ?: "..."} play...",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else if (state.isMyTurn && !state.isGameOver) {
            // Can spin if: not currently spinning, have players, and secret word is set
            // Note: lastSliceIndex may belong to a previous player's turn, so we don't use it
            val canSpin = !state.isSpinning &&
                state.players.isNotEmpty() &&
                !state.secretWord.isNullOrBlank()

            Button(
                onClick = onSpin,
                enabled = canSpin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary
                )
            ) {
                if (state.isSpinning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colors.onPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Spinning...",
                        style = MaterialTheme.typography.button,
                        fontSize = 18.sp
                    )
                } else {
                    Text(
                        text = if (state.hasExtraTurn) "Spin Again (Extra Turn!)" else "Spin the Wheel",
                        style = MaterialTheme.typography.button,
                        fontSize = 18.sp
                    )
                }
            }
        } else if (!state.isGameOver) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp,
                backgroundColor = MaterialTheme.colors.surface
            ) {
                Text(
                    text = "Waiting for ${state.currentTurnPlayer?.nickname ?: "..."} to play...",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Slice labels: 100, 200, 300, Bankrupt, 100, 200, 300, Extra Turn
private val sliceLabels = listOf("100", "200", "300", "\u2620", "100", "200", "300", "\u21BB")

/**
 * Wheel with 8 colored slices and labels.
 */
@Composable
private fun WheelCanvas(
    rotation: Float,
    lastSliceIndex: Int?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Canvas for drawing slices
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation }
        ) {
            val sweepAngle = 360f / 8
            val radius = size.minDimension / 2

            // Draw slices, highlight selected
            for (i in 0 until 8) {
                val startAngle = i * sweepAngle - 90f // Start from top
                val isSelected = lastSliceIndex == i
                drawArc(
                    color = if (isSelected) sliceColors[i].copy(alpha = 1f) else sliceColors[i].copy(alpha = 0.6f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
                // If selected, draw a border
                if (isSelected) {
                    drawArc(
                        color = Color.Yellow,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                    )
                }
            }

            // Draw slice borders
            for (i in 0 until 8) {
                val angleDegrees = i * sweepAngle - 90f
                val angle = angleDegrees * PI / 180.0
                val endX = center.x + radius * cos(angle).toFloat()
                val endY = center.y + radius * sin(angle).toFloat()
                drawLine(
                    color = Color.White,
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 2f
                )
            }

            // Draw center circle
            drawCircle(
                color = Color.White,
                radius = radius * 0.15f,
                center = center
            )
        }

        // Labels overlay (rotates with wheel)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation },
            contentAlignment = Alignment.Center
        ) {
            val sweepAngle = 360f / 8
            for (i in 0 until 8) {
                // Calculate position for label (65% from center)
                val midAngleDegrees = i * sweepAngle + sweepAngle / 2 - 90f
                val midAngle = midAngleDegrees * PI / 180.0

                // Indices 2 and 6 are Amber (Light), so use Black text. Others are Dark, use White.
                val textColor = if (i == 2 || i == 6) Color.Black else Color.White
                val isSelected = lastSliceIndex == i

                // If selected, show the label at the top (under the arrow)
                if (isSelected) {
                    Text(
                        text = sliceLabels[i],
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Yellow,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 12.dp)
                    )
                }

                SliceLabel(
                    text = sliceLabels[i],
                    angleDegrees = midAngleDegrees,
                    isSpecial = i == 3 || i == 7, // Bankrupt or Extra Turn
                    textColor = if (isSelected) Color.Yellow else textColor,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                    fontSize = if (isSelected) 24.sp else if (i == 3 || i == 7) 20.sp else 14.sp
                )
            }
        }
    }
}

/**
 * Label for a wheel slice, positioned at the correct angle.
 */
@Composable
private fun BoxScope.SliceLabel(
    text: String,
    angleDegrees: Float,
    isSpecial: Boolean,
    textColor: Color,
    fontWeight: FontWeight = FontWeight.Bold,
    fontSize: androidx.compose.ui.unit.TextUnit = if (isSpecial) 20.sp else 14.sp
) {
    val angle = angleDegrees * PI / 180.0
    // Position at 65% of radius from center
    val offsetFraction = 0.32f

    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = textColor,
        modifier = Modifier
            .align(Alignment.Center)
            .offset(
                x = (offsetFraction * 220 * cos(angle)).dp,
                y = (offsetFraction * 220 * sin(angle)).dp
            )
            .graphicsLayer { rotationZ = angleDegrees + 90f }
    )
}

/**
 * Displays the word with revealed and hidden letters.
 */
@Composable
private fun WordDisplay(
    displayWord: String,
    isHost: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isHost) "Secret Word (only you can see):" else "Guess the word:",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = displayWord,
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = if (isHost) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.onSurface
                }
            )
        }
    }
}

/**
 * Row showing all players with their scores.
 */
@Composable
private fun PlayersScoreRow(
    players: List<com.bramish.wheelofpasiva.domain.model.Player>,
    playerScores: Map<String, Int>,
    currentTurnPlayerId: String?
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(players) { player ->
            val isCurrentTurn = player.id == currentTurnPlayerId
            val score = playerScores[player.id] ?: 0

            Card(
                elevation = if (isCurrentTurn) 8.dp else 2.dp,
                backgroundColor = if (isCurrentTurn) {
                    MaterialTheme.colors.primary.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colors.surface
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = player.nickname,
                        style = MaterialTheme.typography.body2,
                        fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$score pts",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Current turn indicator.
 */
@Composable
private fun TurnIndicator(
    isMyTurn: Boolean,
    currentTurnPlayerName: String,
    hasExtraTurn: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = if (isMyTurn) {
            MaterialTheme.colors.primary.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colors.surface
        }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isMyTurn) "Your Turn!" else "Current Turn:",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentTurnPlayerName,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = if (isMyTurn) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.onSurface
                }
            )
            if (hasExtraTurn) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Extra Turn!",
                    style = MaterialTheme.typography.caption,
                    color = Color(0xFF9C27B0),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Displays the result of the spin.
 */
@Composable
private fun SpinResultDisplay(slice: WheelSlice) {
    val (text, color) = when (slice) {
        is WheelSlice.Points -> "+${slice.value} points!" to Color(0xFF4CAF50)
        is WheelSlice.Bankrupt -> "BANKRUPT! Lost all points!" to Color(0xFFF44336)
        is WheelSlice.ExtraTurn -> "EXTRA TURN! Spin again!" to Color(0xFF9C27B0)
    }

    Card(
        elevation = 4.dp,
        backgroundColor = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * Visual keyboard showing all letters A-Z.
 * Guessed letters are marked with color based on whether they're in the word.
 */
@Composable
private fun LetterKeyboard(
    revealedLetters: Set<Char>,
    secretWord: String,
    enabled: Boolean,
    language: com.bramish.wheelofpasiva.domain.model.Language,
    onLetterClick: (Char) -> Unit
) {
    val alphabet = language.getAlphabet()
    val secretWordUpper = secretWord.uppercase()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Split alphabet into rows based on language
        val rows = when (language) {
            com.bramish.wheelofpasiva.domain.model.Language.ENGLISH -> {
                // Split into 3 rows
                listOf(
                    alphabet.take(9),      // A-I
                    alphabet.drop(9).take(9), // J-R
                    alphabet.drop(18)      // S-Z
                )
            }
            com.bramish.wheelofpasiva.domain.model.Language.HEBREW -> {
                // Split Hebrew alphabet (22 letters) into 3 rows
                listOf(
                    alphabet.take(8),      // First 8 letters
                    alphabet.drop(8).take(7),  // Next 7 letters
                    alphabet.drop(15)      // Last 7 letters
                )
            }
        }

        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                row.forEach { letter ->
                    val isGuessed = letter in revealedLetters
                    val isInWord = secretWordUpper.contains(letter)

                    LetterButton(
                        letter = letter,
                        isGuessed = isGuessed,
                        isInWord = isInWord,
                        enabled = enabled && !isGuessed,
                        onClick = { onLetterClick(letter) }
                    )
                }
            }
        }
    }
}

/**
 * Single letter button in the keyboard.
 */
@Composable
private fun LetterButton(
    letter: Char,
    isGuessed: Boolean,
    isInWord: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isGuessed && isInWord -> Color(0xFF4CAF50) // Green - correct
        isGuessed && !isInWord -> Color(0xFFE0E0E0) // Gray - wrong
        !enabled -> MaterialTheme.colors.surface.copy(alpha = 0.3f) // Dimmed when disabled
        else -> MaterialTheme.colors.surface
    }
    val textColor = when {
        isGuessed && isInWord -> Color.White
        isGuessed && !isInWord -> Color.Gray
        enabled -> MaterialTheme.colors.onSurface
        else -> MaterialTheme.colors.onSurface.copy(alpha = 0.3f) // More dimmed when disabled
    }

    Surface(
        modifier = Modifier
            .size(32.dp),
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor,
        elevation = if (isGuessed) 0.dp else if (enabled) 2.dp else 0.dp
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = backgroundColor,
                disabledBackgroundColor = backgroundColor
            ),
            elevation = null,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = letter.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

/**
 * Area for guessing the whole word.
 */
@Composable
private fun WordGuessArea(onGuessWord: (String) -> Unit) {
    var wordInput by remember { mutableStateOf("") }
    var showInput by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!showInput) {
            TextButton(onClick = { showInput = true }) {
                Text(
                    text = "Or guess the whole word (doubles score!)",
                    color = MaterialTheme.colors.primary
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = wordInput,
                    onValueChange = { wordInput = it.uppercase() },
                    label = { Text("Guess the word") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (wordInput.isNotBlank()) {
                                onGuessWord(wordInput)
                                wordInput = ""
                                showInput = false
                            }
                        }
                    )
                )

                Button(
                    onClick = {
                        if (wordInput.isNotBlank()) {
                            onGuessWord(wordInput)
                            wordInput = ""
                            showInput = false
                        }
                    },
                    enabled = wordInput.isNotBlank()
                ) {
                    Text("Guess")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(onClick = { showInput = false; wordInput = "" }) {
                Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

/**
 * Dialog for the host to enter the secret word/phrase.
 */
@Composable
private fun SecretWordDialog(
    onConfirm: (String, com.bramish.wheelofpasiva.domain.model.Language) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf(com.bramish.wheelofpasiva.domain.model.Language.ENGLISH) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter Secret Word",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter a word or phrase for players to guess",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Language Toggle
                Card(
                    elevation = 2.dp,
                    backgroundColor = MaterialTheme.colors.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Language:",
                            style = MaterialTheme.typography.body2,
                            fontWeight = FontWeight.Medium
                        )

                        // English Button
                        Button(
                            onClick = { selectedLanguage = com.bramish.wheelofpasiva.domain.model.Language.ENGLISH },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (selectedLanguage == com.bramish.wheelofpasiva.domain.model.Language.ENGLISH) {
                                    MaterialTheme.colors.primary
                                } else {
                                    MaterialTheme.colors.surface
                                }
                            ),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "English",
                                color = if (selectedLanguage == com.bramish.wheelofpasiva.domain.model.Language.ENGLISH) {
                                    Color.White
                                } else {
                                    MaterialTheme.colors.onSurface
                                }
                            )
                        }

                        // Hebrew Button
                        Button(
                            onClick = { selectedLanguage = com.bramish.wheelofpasiva.domain.model.Language.HEBREW },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (selectedLanguage == com.bramish.wheelofpasiva.domain.model.Language.HEBREW) {
                                    MaterialTheme.colors.primary
                                } else {
                                    MaterialTheme.colors.surface
                                }
                            ),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "עברית",
                                color = if (selectedLanguage == com.bramish.wheelofpasiva.domain.model.Language.HEBREW) {
                                    Color.White
                                } else {
                                    MaterialTheme.colors.onSurface
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Word or phrase") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Skip")
                    }

                    Button(
                        onClick = { if (text.isNotBlank()) onConfirm(text, selectedLanguage) },
                        enabled = text.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

/**
 * Dialog shown when the game is over.
 */
@Composable
private fun GameOverDialog(
    winnerName: String,
    winnerScore: Int
) {
    Dialog(onDismissRequest = { }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Game Over!",
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Winner:",
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = winnerName,
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$winnerScore points",
                    style = MaterialTheme.typography.h6,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
