package com.bramish.wheelofpasiva.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest

/**
 * Home screen where users enter their nickname and choose to create or join a room.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    joinRoomViewModel: JoinRoomViewModel,
    onNavigateToRoom: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val nickname by viewModel.nickname.collectAsState()
    var showJoinDialog by remember { mutableStateOf(false) }

    // Handle navigation events from HomeViewModel
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is HomeViewModel.NavigationEvent.NavigateToRoom -> {
                    onNavigateToRoom(event.roomId)
                }
                is HomeViewModel.NavigationEvent.ShowJoinDialog -> {
                    showJoinDialog = true
                }
            }
        }
    }

    // Handle navigation events from JoinRoomViewModel
    LaunchedEffect(Unit) {
        joinRoomViewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is JoinRoomViewModel.NavigationEvent.NavigateToRoom -> {
                    showJoinDialog = false
                    joinRoomViewModel.reset()
                    onNavigateToRoom(event.roomId)
                }
                is JoinRoomViewModel.NavigationEvent.OpenQrScanner -> {
                    // TODO: Open QR scanner
                    // For now, this will be handled in the dialog
                }
            }
        }
    }

    // Show join room dialog
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
        topBar = {
            TopAppBar(
                title = { Text("Wheel of Pasiva") }
            )
        }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App title
                Text(
                    text = "Welcome!",
                    style = MaterialTheme.typography.h4,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Nickname input
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { viewModel.onNicknameChange(it) },
                    label = { Text("Enter your nickname") },
                    placeholder = { Text("Nickname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is HomeUiState.Loading
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Create Room button
                Button(
                    onClick = { viewModel.onCreateRoom() },
                    enabled = nickname.isNotBlank() && uiState !is HomeUiState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState is HomeUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colors.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Create Room")
                }

                // Join Room button
                Button(
                    onClick = { viewModel.onJoinRoom() },
                    enabled = nickname.isNotBlank() && uiState !is HomeUiState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Join Room")
                }

                // Error message
                if (uiState is HomeUiState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
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
