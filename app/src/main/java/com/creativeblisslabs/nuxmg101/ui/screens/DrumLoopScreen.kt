package com.creativeblisslabs.nuxmg101.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativeblisslabs.nuxmg101.data.model.HardwareStatus
import com.creativeblisslabs.nuxmg101.ui.components.TactileKnob

@Composable
fun DrumLoopScreen(
    hardwareStatus: HardwareStatus,
    modifier: Modifier = Modifier,
    onDrumSettingsChange: (ccNum: Int, ccVal: Int) -> Unit = { _, _ -> },
    onUpdatePrefs: (Boolean?, Float?, Float?, Float?, Float?, Float?, String?, Boolean?) -> Unit = { _, _, _, _, _, _, _, _ -> }
) {
    val scrollState = rememberScrollState()

    var isDrumPlaying by remember(hardwareStatus.drumEnabled) { mutableStateOf(hardwareStatus.drumEnabled) }
    var selectedPattern by remember { mutableStateOf("01 Standard") }
    var isPatternDropdownExpanded by remember { mutableStateOf(false) }
    var drumBpm by remember(hardwareStatus.currentBpm) { mutableIntStateOf(hardwareStatus.currentBpm) }

    var drumVol by remember(hardwareStatus.drumLevel) { mutableFloatStateOf(hardwareStatus.drumLevel) }

    var isMono by remember(hardwareStatus.drumRoutingMono) { mutableStateOf(hardwareStatus.drumRoutingMono) }
    var drumBass by remember(hardwareStatus.drumBass) { mutableFloatStateOf(hardwareStatus.drumBass) }
    var drumMid by remember(hardwareStatus.drumMid) { mutableFloatStateOf(hardwareStatus.drumMid) }
    var drumTreb by remember(hardwareStatus.drumTreble) { mutableFloatStateOf(hardwareStatus.drumTreble) }
    var isAutoRecord by remember { mutableStateOf(true) }
    var loopLevel by remember(hardwareStatus.loopLevel) { mutableFloatStateOf(hardwareStatus.loopLevel) }
    var looperState by remember(hardwareStatus.looperState) { mutableStateOf(hardwareStatus.looperState) } // REC, PLAY, STOP

    val patterns = listOf("01 Standard", "02 Rock", "03 Blues", "04 Metal", "05 Pop", "06 Funk")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            Text(
                text = "Drum & Looper",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )
        // DRUM Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "DRUM",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            Button(
                                onClick = { isPatternDropdownExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(selectedPattern, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            }
                            DropdownMenu(
                                expanded = isPatternDropdownExpanded,
                                onDismissRequest = { isPatternDropdownExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                patterns.forEach { pattern ->
                                    DropdownMenuItem(
                                        text = { Text(pattern, color = MaterialTheme.colorScheme.onSurface) },
                                        onClick = {
                                            selectedPattern = pattern
                                            isPatternDropdownExpanded = false
                                            onDrumSettingsChange(82, patterns.indexOf(pattern))
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                isDrumPlaying = !isDrumPlaying
                                onUpdatePrefs(isDrumPlaying, drumVol, drumBass, drumMid, drumTreb, loopLevel, looperState, isMono)
                                onDrumSettingsChange(81, if (isDrumPlaying) 1 else 0)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isDrumPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Play/Stop",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tempo row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "TEMPO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$drumBpm",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            drumBpm = (drumBpm + 5).let { if (it > 240) 60 else it }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "TAP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Knobs (Vol: CC 83, Bass/Mid/Treb EQ)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TactileKnob(
                        label = "VOL",
                        value = drumVol,
                        displayValue = "${drumVol.toInt()}",
                        onValueChange = { drumVol = it; onDrumSettingsChange(83, drumVol.toInt().coerceIn(0, 100)) },
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                    TactileKnob(
                        label = "BASS",
                        value = drumBass,
                        displayValue = "${drumBass.toInt()}",
                        onValueChange = { drumBass = it },
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                    TactileKnob(
                        label = "MIDDLE",
                        value = drumMid,
                        displayValue = "${drumMid.toInt()}",
                        onValueChange = { drumMid = it },
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                    TactileKnob(
                        label = "TREBLE",
                        value = drumTreb,
                        displayValue = "${drumTreb.toInt()}",
                        onValueChange = { drumTreb = it },
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // LOOP Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "LOOP",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        // Stereo/Mono Toggle
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (!isMono) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent)
                                    .clickable { isMono = false; onDrumSettingsChange(89, 0) }
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("STEREO", color = if (!isMono) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isMono) MaterialTheme.colorScheme.surface else Color.Transparent)
                                    .clickable { isMono = true; onDrumSettingsChange(89, 1) }
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("MONO", color = if (isMono) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Normal/Auto Record Toggle
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Transparent)
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("NORMAL RECORD", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("AUTO RECORD", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Level Slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LEVEL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Slider(
                        value = loopLevel,
                        onValueChange = { loopLevel = it; onDrumSettingsChange(84, loopLevel.toInt().coerceIn(0, 100)) },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.onSurface,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Loop Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // REC Button
                    Button(
                        onClick = {
                            val nextState = if (looperState == "REC") "PLAY" else "REC"
                            looperState = nextState
                            val ccVal = if (nextState == "REC") 1 else 2
                            onDrumSettingsChange(85, ccVal)
                            onUpdatePrefs(isDrumPlaying, drumVol, drumBass, drumMid, drumTreb, loopLevel, nextState, isMono)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (looperState == "REC") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(72.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = "Record",
                                modifier = Modifier.size(24.dp),
                                tint = if (looperState == "REC") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("REC", style = MaterialTheme.typography.labelSmall, color = if (looperState == "REC") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }

                    // STOP Button
                    Button(
                        onClick = {
                            looperState = "STOP"
                            onDrumSettingsChange(85, 0)
                            onUpdatePrefs(isDrumPlaying, drumVol, drumBass, drumMid, drumTreb, loopLevel, "STOP", isMono)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(72.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("STOP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }

                    // CLEAR Button
                    Button(
                        onClick = {
                            looperState = "STOP"
                            onDrumSettingsChange(85, 0)
                            onUpdatePrefs(isDrumPlaying, drumVol, drumBass, drumMid, drumTreb, loopLevel, "STOP", isMono)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(72.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("CLEAR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }

                    // UNDO Button
                    Button(
                        onClick = {
                            onDrumSettingsChange(85, 3) // DUB / UNDO layer
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(72.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Undo",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("UNDO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}