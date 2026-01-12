package com.bramish.wheelofpasiva.presentation.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bramish.wheelofpasiva.domain.model.Player
import kotlin.random.Random

/**
 * Game screen with the spinning wheel to select a player.
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            TopAppBar(
                title = { Text("Game - Room ${viewModel.getRoomId()}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
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
                        onReset = { viewModel.resetWheel() }
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
    onReset: () -> Unit
) {
    // Animation for wheel rotation
    var targetRotation by remember { mutableStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 2000),
        label = "wheel_rotation"
    )

    LaunchedEffect(state.isSpinning) {
        if (state.isSpinning) {
            targetRotation += 360f * 5 + Random.nextFloat() * 360f // 5 full rotations + random
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Players count
        Text(
            text = "Players: ${state.players.size}",
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Wheel visualization
        Box(
            modifier = Modifier
                .size(250.dp)
                .rotate(rotation),
            contentAlignment = Alignment.Center
        ) {
            // Outer circle
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = MaterialTheme.colors.primary.copy(alpha = 0.2f),
                elevation = 4.dp
            ) {}

            // Inner circle with player names
            Surface(
                modifier = Modifier.size(200.dp),
                shape = CircleShape,
                color = MaterialTheme.colors.primary.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (state.players.isEmpty()) {
                        Text(
                            text = "No players",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onPrimary
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            state.players.take(6).forEach { player ->
                                Text(
                                    text = player.nickname,
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface,
                                    maxLines = 1
                                )
                            }
                            if (state.players.size > 6) {
                                Text(
                                    text = "+${state.players.size - 6} more",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // Center dot
            Surface(
                modifier = Modifier.size(20.dp),
                shape = CircleShape,
                color = MaterialTheme.colors.secondary
            ) {}
        }

        // Pointer indicator
        Text(
            text = "▼",
            fontSize = 32.sp,
            color = MaterialTheme.colors.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Selected player result
        if (state.selectedPlayer != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 8.dp,
                backgroundColor = MaterialTheme.colors.secondary
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selected:",
                        style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onSecondary.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.selectedPlayer.nickname,
                        style = MaterialTheme.typography.h4,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Spin / Spin Again button
        Button(
            onClick = {
                if (state.selectedPlayer != null) {
                    onReset()
                }
                onSpin()
            },
            enabled = !state.isSpinning && state.players.isNotEmpty(),
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
                    text = if (state.selectedPlayer != null) "Spin Again" else "Spin the Wheel",
                    style = MaterialTheme.typography.button,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
