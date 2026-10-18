package com.creativeblisslabs.nuxmg101.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.creativeblisslabs.nuxmg101.ui.theme.StatusError

@Composable
fun TunerNeedleView(
    centsOffset: Float, // -50 to +50
    isInTune: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedAngle by animateFloatAsState(
        targetValue = (centsOffset / 50f) * 45f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "tunerNeedleRotation"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val activeColor = if (isInTune) primaryColor else StatusError

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2f, height)
            val radius = height * 0.9f

            // Base Arc Track
            drawArc(
                color = trackColor,
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Center In-Tune Tick
            drawLine(
                color = primaryColor,
                start = Offset(center.x, center.y - radius - 10.dp.toPx()),
                end = Offset(center.x, center.y - radius + 10.dp.toPx()),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Outer Tick Marks (-50, -25, +25, +50)
            val angles = listOf(-45f, -22.5f, 22.5f, 45f)
            angles.forEach { angleDeg ->
                val rad = Math.toRadians(angleDeg.toDouble() - 90.0)
                val startX = center.x + ((radius - 8.dp.toPx()) * kotlin.math.cos(rad)).toFloat()
                val startY = center.y + ((radius - 8.dp.toPx()) * kotlin.math.sin(rad)).toFloat()
                val endX = center.x + ((radius + 8.dp.toPx()) * kotlin.math.cos(rad)).toFloat()
                val endY = center.y + ((radius + 8.dp.toPx()) * kotlin.math.sin(rad)).toFloat()

                drawLine(
                    color = StatusError.copy(alpha = 0.6f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Rotating Needle
            val needleRad = Math.toRadians(animatedAngle.toDouble() - 90.0)
            val needleLength = radius * 0.95f
            val needleEndX = center.x + (needleLength * kotlin.math.cos(needleRad)).toFloat()
            val needleEndY = center.y + (needleLength * kotlin.math.sin(needleRad)).toFloat()

            drawLine(
                color = activeColor,
                start = center,
                end = Offset(needleEndX, needleEndY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Pivot Circle
            drawCircle(
                color = activeColor,
                radius = 8.dp.toPx(),
                center = center
            )
        }
    }
}
