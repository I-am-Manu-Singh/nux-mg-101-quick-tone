package com.creativeblisslabs.nuxmg101.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.roundToInt
import kotlin.math.sin

@SuppressLint("MissingPermission")
@Composable
fun TunerScreen(
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    var detectedNote by remember { mutableStateOf("---") }
    var noteOctave by remember { mutableStateOf("") }
    var centsOffset by remember { mutableFloatStateOf(0f) }
    var detectedFreq by remember { mutableFloatStateOf(0f) }
    var refPitch by remember { mutableIntStateOf(440) }
    var isListening by remember { mutableStateOf(true) }

    val noteNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    // Real-Time AudioRecord Pitch Detection Engine using Autocorrelation
    LaunchedEffect(hasMicPermission, isListening) {
        if (!hasMicPermission || !isListening) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            val sampleRate = 44100
            val bufferSize = (AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ) * 2).coerceAtLeast(4096)

            val audioRecord: AudioRecord? = try {
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
            } catch (e: Exception) {
                null
            }

            audioRecord?.let { recorder ->
                try {
                    recorder.startRecording()
                    val buffer = ShortArray(bufferSize)

                    while (isActive && isListening) {
                        val readCount = recorder.read(buffer, 0, buffer.size)
                        if (readCount > 512) {
                            var sum = 0.0
                            for (i in 0 until readCount) {
                                sum += buffer[i] * buffer[i]
                            }
                            val rms = kotlin.math.sqrt(sum / readCount)

                            if (rms > 200.0) {
                                val pitch = detectPitchAutocorrelation(buffer, readCount, sampleRate)
                                if (pitch in 55.0..1200.0) {
                                    val freq = pitch.toFloat()
                                    val noteNum = 69.0 + 12.0 * log2(pitch / refPitch.toDouble())
                                    val roundedNote = noteNum.roundToInt()
                                    val cents = ((noteNum - roundedNote) * 100.0).toFloat()

                                    val noteIndex = (roundedNote % 12 + 12) % 12
                                    val octave = (roundedNote / 12) - 1

                                    detectedFreq = freq
                                    centsOffset = cents.coerceIn(-50f, 50f)
                                    detectedNote = noteNames[noteIndex]
                                    noteOctave = octave.toString()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        recorder.stop()
                        recorder.release()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    val isInTune = detectedFreq > 0f && abs(centsOffset) <= 3f
    val statusColor by animateColorAsState(
        targetValue = if (isInTune) Color(0xFF4CAF50) else if (detectedFreq > 0f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tunerStatusColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Text(
            text = "Tuner",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif
        )

        // Top Permission / Listening Status Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!hasMicPermission) {
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("ENABLE MIC PERMISSION", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = if (isListening) "MIC LISTENING ACTIVE" else "MIC PAUSED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = { isListening = !isListening }) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Toggle Mic",
                        tint = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Large Note Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = detectedNote,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 80.sp
                )
                if (noteOctave.isNotEmpty()) {
                    Text(
                        text = noteOctave,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (detectedFreq == 0f) "PLUCK A STRING" else if (isInTune) "IN TUNE" else "${if (centsOffset > 0) "+" else ""}${centsOffset.toInt()} CENTS",
                style = MaterialTheme.typography.headlineSmall,
                color = statusColor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // --- ADVANCED ARC NEEDLE METER COMPONENT ---
        AdvancedTunerMeterCard(
            cents = centsOffset,
            isInTune = isInTune,
            modifier = Modifier.fillMaxWidth()
        )

        // Bento Grid & Mute / Ref Controls
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    BentoMetricItem(label = "FREQ", value = String.format("%.1f Hz", detectedFreq), color = MaterialTheme.colorScheme.primary)
                    BentoMetricItem(label = "REF", value = "$refPitch Hz", color = MaterialTheme.colorScheme.onSurface)
                    BentoMetricItem(label = "MODE", value = "CHROM", color = MaterialTheme.colorScheme.secondary)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "REF PITCH",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { refPitch = (refPitch - 1).coerceAtLeast(430) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "$refPitch Hz",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )

                            IconButton(
                                onClick = { refPitch = (refPitch + 1).coerceAtMost(450) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Mute Output",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )

                        Switch(
                            checked = isMuted,
                            onCheckedChange = { onToggleMute() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun AdvancedTunerMeterCard(
    cents: Float,
    isInTune: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedCents by animateFloatAsState(
        targetValue = cents.coerceIn(-50f, 50f),
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 220f),
        label = "AdvancedTunerNeedleSpring"
    )

    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val activeTint = if (isInTune) Color(0xFF4CAF50) else primaryColor
    
    // Capture theme colors safely outside the Canvas scope here:
    val canvasSurfaceColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        surfaceColor,
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                )
            )
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                val width = size.width
                val height = size.height
                val centerX = width / 2f
                val centerY = height + 105.dp.toPx()
                val radius = 160.dp.toPx()

                // 1. Background Arc Track
                drawArc(
                    color = borderColor.copy(alpha = 0.4f),
                    startAngle = 210f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )

                // 2. High-Precision Cent Ticks (-50 to +50)
                val totalTicks = 11
                for (i in 0 until totalTicks) {
                    val progress = i / (totalTicks - 1f)
                    val angleDeg = 210f + (progress * 120f)
                    val angleRad = Math.toRadians(angleDeg.toDouble())

                    val isCenter = i == 5
                    val isMajor = i % 2 == 0 || isCenter
                    val tickLength = if (isCenter) 14.dp.toPx() else if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                    val tickWidth = if (isCenter) 3.dp.toPx() else if (isMajor) 2.dp.toPx() else 1.dp.toPx()

                    val tickColor = when {
                        isCenter -> if (isInTune) Color(0xFF4CAF50) else primaryColor
                        else -> borderColor
                    }

                    val innerRadius = radius - 10.dp.toPx()
                    val outerRadius = innerRadius - tickLength

                    val startX = centerX + innerRadius * cos(angleRad).toFloat()
                    val startY = centerY + innerRadius * sin(angleRad).toFloat()
                    val endX = centerX + outerRadius * cos(angleRad).toFloat()
                    val endY = centerY + outerRadius * sin(angleRad).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = tickWidth,
                        cap = StrokeCap.Round
                    )
                }

                // 3. Realistic Needle Blade
                val normalizedCents = (animatedCents + 50f) / 100f
                val needleAngleDeg = 210f + (normalizedCents * 120f)
                val needleAngleRad = Math.toRadians(needleAngleDeg.toDouble())

                val needleLength = radius - 20.dp.toPx()
                val tipX = centerX + needleLength * cos(needleAngleRad).toFloat()
                val tipY = centerY + needleLength * sin(needleAngleRad).toFloat()

                val needleWidth = 2.5.dp.toPx()
                val perpRad = needleAngleRad + Math.PI / 2
                val baseLeftX = centerX + needleWidth * cos(perpRad).toFloat()
                val baseLeftY = centerY + needleWidth * sin(perpRad).toFloat()
                val baseRightX = centerX - needleWidth * cos(perpRad).toFloat()
                val baseRightY = centerY - needleWidth * sin(perpRad).toFloat()

                val needlePath = Path().apply {
                    moveTo(baseLeftX, baseLeftY)
                    lineTo(tipX, tipY)
                    lineTo(baseRightX, baseRightY)
                    close()
                }

                drawPath(
                    path = needlePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(activeTint, activeTint.copy(alpha = 0.5f))
                    )
                )

                // 4. Center Pivot Jewel (Using pre-captured canvasSurfaceColor)
                drawCircle(
                    color = canvasSurfaceColor,
                    radius = 5.5.dp.toPx(),
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = activeTint,
                    radius = 3.dp.toPx(),
                    center = Offset(centerX, centerY)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when {
                    isInTune -> "★ IN TUNE ★"
                    animatedCents < 0f -> "FLAT (${abs(animatedCents).toInt()}¢)"
                    else -> "SHARP (+${animatedCents.toInt()}¢)"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = activeTint,
                letterSpacing = 1.2.sp
            )
        }
    }
}

@Composable
private fun BentoMetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun detectPitchAutocorrelation(buffer: ShortArray, length: Int, sampleRate: Int): Double {
    val minPeriod = sampleRate / 1200
    val maxPeriod = sampleRate / 55

    var bestPeriod = -1
    var maxCorrelation = 0.0

    for (period in minPeriod..maxPeriod) {
        var correlation = 0.0
        val maxI = length - period
        for (i in 0 until maxI) {
            correlation += buffer[i].toDouble() * buffer[i + period].toDouble()
        }
        if (correlation > maxCorrelation) {
            maxCorrelation = correlation
            bestPeriod = period
        }
    }

    return if (bestPeriod > 0) sampleRate.toDouble() / bestPeriod.toDouble() else 0.0
}