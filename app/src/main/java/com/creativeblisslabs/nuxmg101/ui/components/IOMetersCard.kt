package com.creativeblisslabs.nuxmg101.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativeblisslabs.nuxmg101.ui.theme.LimeGreenDim
import com.creativeblisslabs.nuxmg101.ui.theme.StatusError
import com.creativeblisslabs.nuxmg101.ui.theme.SunsetOrangeTertiary
import com.creativeblisslabs.nuxmg101.ui.theme.SurfaceContainerDefault
import com.creativeblisslabs.nuxmg101.ui.theme.SurfaceContainerLowest
import com.creativeblisslabs.nuxmg101.ui.theme.SurfaceContainerHighest

@Composable
fun IOMetersCard(
    inputDb: Float,
    outputDb: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Equalizer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 6.dp)
            )

            Text(
                text = "I/O METERS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Meter
        MeterBarRow(label = "INPUT", dbValue = inputDb)

        Spacer(modifier = Modifier.height(10.dp))

        // Output Meter
        MeterBarRow(label = "OUTPUT", dbValue = outputDb)
    }
}

@Composable
private fun MeterBarRow(
    label: String,
    dbValue: Float
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )

            Text(
                text = "${dbValue.toInt()} dB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 10 Segments Bar
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(4.dp))
                .padding(2.dp)
        ) {
            val segmentThresholds = listOf(-36f, -30f, -24f, -18f, -12f, -6f, -3f, 0f, 2f, 4f)
            segmentThresholds.forEachIndexed { index, thresh ->
                val isActive = dbValue >= thresh
                val segmentColor = when {
                    index >= 8 -> StatusError
                    index >= 6 -> SunsetOrangeTertiary
                    else -> LimeGreenDim
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (isActive) segmentColor else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}
