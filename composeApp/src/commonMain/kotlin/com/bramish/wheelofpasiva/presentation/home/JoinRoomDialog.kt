package com.bramish.wheelofpasiva.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Dialog for joining a room with text input or QR code scanning.
 */
@Composable
fun JoinRoomDialog(
    viewModel: JoinRoomViewModel,
    nickname: String,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val roomId by viewModel.roomId.collectAsState()

    AlertDialog(
        onDismissRequest = {
            if (uiState !is JoinRoomUiState.Loading) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = "Join Room",
                style = MaterialTheme.typography.h6
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Enter the 6-digit room code or scan a QR code",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )

                // Room ID input
                OutlinedTextField(
                    value = roomId,
                    onValueChange = { viewModel.onRoomIdChange(it) },
                    label = { Text("Room Code") },
                    placeholder = { Text("123456") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is JoinRoomUiState.Loading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = uiState is JoinRoomUiState.Error,
                    trailingIcon = {
                        IconButton(
                            onClick = { viewModel.onOpenQrScanner() },
                            enabled = uiState !is JoinRoomUiState.Loading
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan QR Code"
                            )
                        }
                    }
                )

                // Error message (keep dialog open on error as per requirements)
                if (uiState is JoinRoomUiState.Error) {
                    Text(
                        text = (uiState as JoinRoomUiState.Error).message,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Loading indicator
                if (uiState is JoinRoomUiState.Loading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.onJoinRoom(nickname) },
                enabled = roomId.isNotBlank() &&
                         uiState !is JoinRoomUiState.Loading &&
                         roomId.length == 6
            ) {
                Text("Join")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = uiState !is JoinRoomUiState.Loading
            ) {
                Text("Cancel")
            }
        }
    )
}
