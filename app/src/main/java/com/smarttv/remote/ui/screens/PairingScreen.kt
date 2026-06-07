package com.smarttv.remote.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smarttv.remote.model.RemoteUiState

@Composable
fun PairingScreen(
    state: RemoteUiState,
    onPairingCodeChanged: (String) -> Unit,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Pair with TV",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter the code shown on your TV screen",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        if (state.connectedDevice != null) {
            Text(
                text = state.connectedDevice.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { newValue ->
                if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                    code = newValue
                }
            },
            label = { Text("Pairing Code") },
            placeholder = { Text("000000") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Go
            ),
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            enabled = !submitted
        )

        if (state.pairingError != null) {
            Text(
                text = state.pairingError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                submitted = true
                onPairingCodeChanged(code)
            },
            enabled = code.length == 6 && !submitted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pair")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "1. The TV should display a 6-digit code\n" +
                    "2. Enter the code above\n" +
                    "3. Tap Pair to connect",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}
