package com.creativeblisslabs.nuxmg101.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativeblisslabs.nuxmg101.ui.theme.LimeGreenPrimary
import com.creativeblisslabs.nuxmg101.ui.theme.SkyBlueSecondary
import com.creativeblisslabs.nuxmg101.ui.theme.SurfaceBright
import com.creativeblisslabs.nuxmg101.ui.theme.SurfaceContainerHigh
import com.creativeblisslabs.nuxmg101.ui.theme.SurfaceContainerLow

@Composable
fun TactileKnob(
    label: String,
    value: Float, // 0.0f to 100.0f
    displayValue: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    accentColor: Color = LimeGreenPrimary,
    defaultValue: Float = 50f
) {
    var dragAccumulator by remember(value) { mutableFloatStateOf(value) }
    val animatedAngle by animateFloatAsState(
        targetValue = ((value / 100f) * 270f) - 135f,
        label = "knobRotation"
    )

    val trackBgColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val knobGradientStart = Color(0xFF4A4A4A)
    val knobGradientEnd = Color(0xFF1E1E1E)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            onValueChange(defaultValue)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = -dragAmount.y * 0.75f + dragAmount.x * 0.75f
                        dragAccumulator = (dragAccumulator + delta).coerceIn(0f, 100f)
                        onValueChange(dragAccumulator)
                    }
                }
        ) {
            // Outer circular progress arc around the knob
            Canvas(modifier = Modifier.size(size)) {
                val strokeWidth = 3.dp.toPx()
                val radius = (size.toPx() - strokeWidth - 8.dp.toPx()) / 2f
                val centerOffset = Offset(size.toPx() / 2f, size.toPx() / 2f)
                val arcTopLeft = Offset(centerOffset.x - radius, centerOffset.y - radius)
                val arcSize = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)

                // Background track arc (135° to 405° = 270° sweep)
                drawArc(
                    color = trackBgColor,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Value Progress Arc
                val sweep = (value / 100f) * 270f
                if (sweep > 0f) {
                    drawArc(
                        color = accentColor,
                        startAngle = 135f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth + 1.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Metal Knob Body (Squircle Shape)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size - 14.dp)
                    .shadow(8.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                knobGradientStart,
                                knobGradientEnd
                            )
                        )
                    )
                    .border(1.dp, Color(0xFF444444), RoundedCornerShape(14.dp))
            ) {
                if (label.uppercase() == "VOL" || label.uppercase() == "PATCH LEVEL") {
                    Text(
                        text = "VOL",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
                    )
                }
                // Pointer indicator line
                Canvas(modifier = Modifier.size(size - 16.dp)) {
                    val radius = (size.toPx() - 16.dp.toPx()) / 2f
                    val center = Offset(radius, radius)
                    val pointerLength = radius * 0.4f
                    val angleRad = Math.toRadians(animatedAngle.toDouble() - 90.0)

                    val startX = center.x + (radius * 0.4f * kotlin.math.cos(angleRad)).toFloat()
                    val startY = center.y + (radius * 0.4f * kotlin.math.sin(angleRad)).toFloat()
                    val endX = center.x + (radius * 0.8f * kotlin.math.cos(angleRad)).toFloat()
                    val endY = center.y + (radius * 0.8f * kotlin.math.sin(angleRad)).toFloat()

                    drawLine(
                        color = Color.White,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (label.uppercase() == "VOL") "DRUM" else label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
