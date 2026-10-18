package com.creativeblisslabs.nuxmg101.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.creativeblisslabs.nuxmg101.ui.theme.LimeGreenPrimary

import androidx.compose.material3.MaterialTheme

@Composable
fun WaveformView(
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        val points = listOf(
            0f to 0f, 0.05f to 0f, 0.08f to -0.1f, 0.1f to 0.1f, 0.12f to -0.3f, 0.15f to 0.4f,
            0.18f to -0.8f, 0.22f to 0.95f, 0.25f to -0.85f, 0.28f to 0.7f, 0.32f to -0.5f,
            0.35f to 0.35f, 0.4f to -0.25f, 0.45f to 0.15f, 0.5f to -0.1f, 0.55f to 0.08f,
            0.6f to -0.05f, 0.7f to 0.02f, 0.8f to -0.01f, 1.0f to 0f
        )

        val path = Path().apply {
            val firstX = points.first().first * width
            val firstY = centerY + (points.first().second * centerY * 0.9f)
            moveTo(firstX, firstY)

            for (i in 1 until points.size) {
                val pointX = points[i].first * width
                val pointY = centerY + (points[i].second * centerY * 0.9f)
                lineTo(pointX, pointY)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
