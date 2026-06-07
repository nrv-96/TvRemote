package com.smarttv.remote.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.smarttv.remote.protocol.KeyCodes
import com.smarttv.remote.ui.theme.DPadCenterColor
import com.smarttv.remote.ui.theme.DPadDownColor
import com.smarttv.remote.ui.theme.DPadLeftColor
import com.smarttv.remote.ui.theme.DPadRightColor
import com.smarttv.remote.ui.theme.DPadUpColor
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DPad(
    onDirectionPressed: (Int) -> Unit,
    onCenterPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeDirection by remember { mutableStateOf(-1) }
    var isCenterPressed by remember { mutableStateOf(false) }

    val upColor by animateColorAsState(
        targetValue = if (activeDirection == KeyCodes.DPAD_UP) DPadUpColor else Color.White,
        label = "upColor"
    )
    val downColor by animateColorAsState(
        targetValue = if (activeDirection == KeyCodes.DPAD_DOWN) DPadDownColor else Color.White,
        label = "downColor"
    )
    val leftColor by animateColorAsState(
        targetValue = if (activeDirection == KeyCodes.DPAD_LEFT) DPadLeftColor else Color.White,
        label = "leftColor"
    )
    val rightColor by animateColorAsState(
        targetValue = if (activeDirection == KeyCodes.DPAD_RIGHT) DPadRightColor else Color.White,
        label = "rightColor"
    )
    val centerColor by animateColorAsState(
        targetValue = if (isCenterPressed) Color.LightGray else DPadCenterColor,
        label = "centerColor"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth(0.7f)
            .aspectRatio(1f)
            .padding(8.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                    val maxRadius = size.width / 2f
                    val centerRadius = size.width / 5f

                    if (distance <= centerRadius) {
                        isCenterPressed = true
                        onCenterPressed()
                        return@detectTapGestures
                    }

                    if (distance <= maxRadius) {
                        val angle = atan2(dy, dx) * 180f / PI.toFloat()
                        val normalizedAngle = if (angle < 0) angle + 360f else angle

                        val direction = when {
                            normalizedAngle in 45f..135f -> KeyCodes.DPAD_DOWN
                            normalizedAngle in 135f..225f -> KeyCodes.DPAD_LEFT
                            normalizedAngle in 225f..315f -> KeyCodes.DPAD_UP
                            else -> KeyCodes.DPAD_RIGHT
                        }
                        activeDirection = direction
                        onDirectionPressed(direction)
                    }
                }
            }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.width / 2f
        val innerRadius = size.width / 5f

        drawUpArrow(center, outerRadius, innerRadius, upColor)
        drawDownArrow(center, outerRadius, innerRadius, downColor)
        drawLeftArrow(center, outerRadius, innerRadius, leftColor)
        drawRightArrow(center, outerRadius, innerRadius, rightColor)

        drawCircle(
            color = centerColor,
            radius = innerRadius,
            center = center
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.3f),
            radius = innerRadius * 0.3f,
            center = center
        )
    }
}

private fun DrawScope.drawUpArrow(
    center: Offset, outerRadius: Float, innerRadius: Float, color: Color
) {
    val startAngle = 225f
    val sweepAngle = 90f
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
        size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
        style = Fill
    )
    drawArc(
        color = Color.Black.copy(alpha = 0.2f),
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
        size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
        style = Stroke(width = 1f)
    )

    val arrowAngle = 270f * PI.toFloat() / 180f
    val arrowLength = (outerRadius - innerRadius) * 0.4f
    val midRadius = (outerRadius + innerRadius) / 2f
    val tipX = center.x + midRadius * cos(arrowAngle)
    val tipY = center.y + midRadius * sin(arrowAngle)
    val tip = Offset(tipX, tipY)

    val arrowOffset = arrowLength * 0.3f
    val left = Offset(tipX - arrowOffset, tipY + arrowOffset)
    val right = Offset(tipX + arrowOffset, tipY + arrowOffset)

    val trianglePath = androidx.compose.ui.graphics.Path().apply {
        moveTo(tip.x, tip.y + arrowLength * 0.5f)
        lineTo(left.x, left.y)
        lineTo(right.x, right.y)
        close()
    }
    drawPath(trianglePath, Color.White.copy(alpha = 0.8f))
}

private fun DrawScope.drawDownArrow(
    center: Offset, outerRadius: Float, innerRadius: Float, color: Color
) {
    val startAngle = 45f
    val sweepAngle = 90f
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
        size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
        style = Fill
    )
    drawArc(
        color = Color.Black.copy(alpha = 0.2f),
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
        size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
        style = Stroke(width = 1f)
    )

    val arrowAngle = 90f * PI.toFloat() / 180f
    val arrowLength = (outerRadius - innerRadius) * 0.4f
    val midRadius = (outerRadius + innerRadius) / 2f
    val tipX = center.x + midRadius * cos(arrowAngle)
    val tipY = center.y + midRadius * sin(arrowAngle)

    val arrowOffset = arrowLength * 0.3f
    val left = Offset(tipX - arrowOffset, tipY - arrowOffset)
    val right = Offset(tipX + arrowOffset, tipY - arrowOffset)

    val trianglePath = androidx.compose.ui.graphics.Path().apply {
        moveTo(tip.x, tip.y - arrowLength * 0.5f)
        lineTo(left.x, left.y)
        lineTo(right.x, right.y)
        close()
    }
    drawPath(trianglePath, Color.White.copy(alpha = 0.8f))
}

private fun DrawScope.drawLeftArrow(
    center: Offset, outerRadius: Float, innerRadius: Float, color: Color
) {
    val startAngle = 135f
    val sweepAngle = 90f
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
        size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
        style = Fill
    )
    drawArc(
        color = Color.Black.copy(alpha = 0.2f),
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
        size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
        style = Stroke(width = 1f)
    )

    val arrowAngle = 180f * PI.toFloat() / 180f
    val arrowLength = (outerRadius - innerRadius) * 0.4f
    val midRadius = (outerRadius + innerRadius) / 2f
    val tipX = center.x + midRadius * cos(arrowAngle)
    val tipY = center.y + midRadius * sin(arrowAngle)

    val arrowOffset = arrowLength * 0.3f
    val top = Offset(tipX + arrowOffset, tipY - arrowOffset)
    val bottom = Offset(tipX + arrowOffset, tipY + arrowOffset)

    val trianglePath = androidx.compose.ui.graphics.Path().apply {
        moveTo(tip.x - arrowLength * 0.5f, tip.y)
        lineTo(top.x, top.y)
        lineTo(bottom.x, bottom.y)
        close()
    }
    drawPath(trianglePath, Color.White.copy(alpha = 0.8f))
}

private fun DrawScope.drawRightArrow(
    center: Offset, outerRadius: Float, innerRadius: Float, color: Color
) {
    val startAngle = 315f
    val sweepAngle = 90f
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
        size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
        style = Fill
    )
    drawArc(
        color = Color.Black.copy(alpha = 0.2f),
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
        size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
        style = Stroke(width = 1f)
    )

    val arrowAngle = 0f
    val arrowLength = (outerRadius - innerRadius) * 0.4f
    val midRadius = (outerRadius + innerRadius) / 2f
    val tipX = center.x + midRadius * cos(arrowAngle)
    val tipY = center.y + midRadius * sin(arrowAngle)

    val arrowOffset = arrowLength * 0.3f
    val top = Offset(tipX - arrowOffset, tipY - arrowOffset)
    val bottom = Offset(tipX - arrowOffset, tipY + arrowOffset)

    val trianglePath = androidx.compose.ui.graphics.Path().apply {
        moveTo(tip.x + arrowLength * 0.5f, tip.y)
        lineTo(top.x, top.y)
        lineTo(bottom.x, bottom.y)
        close()
    }
    drawPath(trianglePath, Color.White.copy(alpha = 0.8f))
}
