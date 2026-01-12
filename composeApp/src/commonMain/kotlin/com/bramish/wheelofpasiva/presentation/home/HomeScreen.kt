package com.bramish.wheelofpasiva.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest

/**
 * Home screen where users enter their nickname and choose to create or join a room.
 * Redesigned with Dark "Fortune" Theme.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    joinRoomViewModel: JoinRoomViewModel,
    onNavigateToRoom: (roomId: String, playerId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val nickname by viewModel.nickname.collectAsState()
    var showJoinDialog by remember { mutableStateOf(false) }

    // Navigation Events
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is HomeViewModel.NavigationEvent.NavigateToRoom -> {
                    onNavigateToRoom(event.roomId, event.playerId)
                }
                is HomeViewModel.NavigationEvent.ShowJoinDialog -> {
                    showJoinDialog = true
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        joinRoomViewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is JoinRoomViewModel.NavigationEvent.NavigateToRoom -> {
                    showJoinDialog = false
                    joinRoomViewModel.reset()
                    onNavigateToRoom(event.roomId, event.playerId)
                }
                is JoinRoomViewModel.NavigationEvent.OpenQrScanner -> {
                    // TODO: Open QR scanner
                }
            }
        }
    }

    if (showJoinDialog) {
        JoinRoomDialog(
            viewModel = joinRoomViewModel,
            nickname = nickname,
            onDismiss = {
                showJoinDialog = false
                joinRoomViewModel.reset()
            }
        )
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Wheel of Pasiva", // Kept as App Title
                    style = MaterialTheme.typography.h3,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary
                )

                Text(
                    text = "Welcome!", // Reverted from "Enter the circle of fate"
                    style = MaterialTheme.typography.h5, // Adjusted style slightly for subtitle look
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Input Card
                Card(
                    elevation = 8.dp,
                    shape = MaterialTheme.shapes.medium,
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { viewModel.onNicknameChange(it) },
                            label = { Text("Enter your nickname") }, // Reverted
                            placeholder = { Text("Nickname") }, // Reverted
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState !is HomeUiState.Loading,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colors.secondary,
                                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                                textColor = MaterialTheme.colors.onSurface
                            ),
                            shape = CircleShape // Pill shaped input
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action Buttons
                        Button(
                            onClick = { viewModel.onCreateRoom() },
                            enabled = nickname.isNotBlank() && uiState !is HomeUiState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary,
                                disabledBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.3f)
                            )
                        ) {
                            if (uiState is HomeUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colors.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Text("Create Room") // Reverted
                        }

                        Button(
                            onClick = { viewModel.onJoinRoom() },
                            enabled = nickname.isNotBlank() && uiState !is HomeUiState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                backgroundColor = Color.Transparent,
                                contentColor = MaterialTheme.colors.primary
                            )
                        ) {
                            Text("Join Room") // Reverted
                        }
                    }
                }

                // Error Display
                if (uiState is HomeUiState.Error) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = (uiState as HomeUiState.Error).message,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
