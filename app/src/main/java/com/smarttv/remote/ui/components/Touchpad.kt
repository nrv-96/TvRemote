package com.smarttv.remote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun Touchpad(
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val bgColor = if (isPressed)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onTap()
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isPressed = true },
                    onDragEnd = { isPressed = false },
                    onDragCancel = { isPressed = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val threshold = 50f
                        if (kotlin.math.abs(dragAmount.x) > kotlin.math.abs(dragAmount.y)) {
                            if (dragAmount.x > threshold) {
                                onSwipeRight()
                            } else if (dragAmount.x < -threshold) {
                                onSwipeLeft()
                            }
                        } else {
                            if (dragAmount.y > threshold) {
                                onSwipeDown()
                            } else if (dragAmount.y < -threshold) {
                                onSwipeUp()
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Touch & Swipe",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
