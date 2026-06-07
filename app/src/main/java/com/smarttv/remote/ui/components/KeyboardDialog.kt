package com.smarttv.remote.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun KeyboardDialog(
    onDismiss: () -> Unit,
    onSendText: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Keyboard Input")
        },
        text = {
            Column {
                Text(
                    text = "Enter text to send to your TV",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Text") },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onSendText(text)
                        text = ""
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
