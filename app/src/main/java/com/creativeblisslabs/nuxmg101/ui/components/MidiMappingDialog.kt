package com.creativeblisslabs.nuxmg101.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.creativeblisslabs.nuxmg101.ui.theme.LimeGreenPrimary
import com.creativeblisslabs.nuxmg101.ui.theme.SkyBlueSecondary
import com.creativeblisslabs.nuxmg101.ui.theme.SurfaceContainerDefault
import com.creativeblisslabs.nuxmg101.ui.theme.SurfaceContainerHigh
import com.creativeblisslabs.nuxmg101.ui.theme.SurfaceContainerLow

data class MidiCcMapping(
    val id: Int,
    val name: String,
    val midiCc: Int,
    val range: String
)

val MG101_MIDI_MAPPINGS = listOf(
    MidiCcMapping(2, "CMP", 1, "1-2"),
    MidiCcMapping(3, "EFX", 2, "1-14"),
    MidiCcMapping(4, "AMP", 3, "1-25"),
    MidiCcMapping(5, "EQ", 4, "1-2"),
    MidiCcMapping(6, "NR", 5, "1-1"),
    MidiCcMapping(7, "MOD", 6, "1-14"),
    MidiCcMapping(8, "DLY", 7, "1-7"),
    MidiCcMapping(9, "RVB", 8, "1-5"),
    MidiCcMapping(10, "CAB", 9, "1-27"),
    MidiCcMapping(11, "P.L", 10, "NULL"),
    MidiCcMapping(12, "MIC", 11, "1-3"),
    MidiCcMapping(13, "NULL", 12, "NULL"),
    MidiCcMapping(14, "NULL", 13, "NULL"),
    MidiCcMapping(15, "CMP Knob 1", 14, "0-100"),
    MidiCcMapping(16, "CMP Knob 2", 15, "0-100"),
    MidiCcMapping(17, "CMP Knob 3", 16, "0-100"),
    MidiCcMapping(18, "CMP Knob 4", 17, "0-100"),
    MidiCcMapping(19, "EFX Knob 1", 18, "0-100"),
    MidiCcMapping(20, "EFX Knob 2", 19, "0-100"),
    MidiCcMapping(21, "EFX Knob 3", 20, "0-100"),
    MidiCcMapping(22, "EFX Knob 4", 21, "0-100"),
    MidiCcMapping(23, "EFX Knob 5", 22, "0-100"),
    MidiCcMapping(24, "EFX Knob 6", 23, "0-100"),
    MidiCcMapping(25, "AMP Knob 1", 24, "0-100"),
    MidiCcMapping(26, "AMP Knob 2", 25, "0-100"),
    MidiCcMapping(27, "AMP Knob 3", 26, "0-100"),
    MidiCcMapping(28, "AMP Knob 4", 27, "0-100"),
    MidiCcMapping(29, "AMP Knob 5", 28, "0-100"),
    MidiCcMapping(30, "AMP Knob 6", 29, "0-100"),
    MidiCcMapping(31, "AMP Knob 7", 30, "0-100"),
    MidiCcMapping(32, "AMP Knob 8", 31, "0-100"),
    MidiCcMapping(33, "EQ Knob 1", 32, "0-100"),
    MidiCcMapping(34, "EQ Knob 2", 33, "0-100"),
    MidiCcMapping(35, "EQ Knob 3", 34, "0-100"),
    MidiCcMapping(36, "EQ Knob 4", 35, "0-100"),
    MidiCcMapping(37, "EQ Knob 5", 36, "0-100"),
    MidiCcMapping(38, "EQ Knob 6", 37, "0-100"),
    MidiCcMapping(39, "EQ Knob 7", 38, "0-100"),
    MidiCcMapping(40, "EQ Knob 8", 39, "0-100"),
    MidiCcMapping(41, "EQ Knob 9", 40, "0-100"),
    MidiCcMapping(42, "EQ Knob 10", 41, "0-100"),
    MidiCcMapping(43, "EQ Knob 11", 42, "0-100"),
    MidiCcMapping(44, "EQ Knob 12", 43, "0-100"),
    MidiCcMapping(45, "NR Knob 1", 44, "0-100"),
    MidiCcMapping(46, "NR Knob 2", 45, "0-100"),
    MidiCcMapping(47, "NR Knob 3", 46, "0-100"),
    MidiCcMapping(48, "NR Knob 4", 47, "0-100"),
    MidiCcMapping(49, "MOD Knob 1", 48, "0-100"),
    MidiCcMapping(50, "MOD Knob 2", 49, "0-100"),
    MidiCcMapping(51, "MOD Knob 3", 50, "0-100"),
    MidiCcMapping(52, "MOD Knob 4", 51, "0-100"),
    MidiCcMapping(53, "MOD Knob 5", 52, "0-100"),
    MidiCcMapping(54, "MOD Knob 6", 53, "0-100"),
    MidiCcMapping(55, "DLY Knob 1", 54, "0-100"),
    MidiCcMapping(56, "DLY Knob 2", 55, "0-100"),
    MidiCcMapping(57, "DLY Knob 3", 56, "0-100"),
    MidiCcMapping(58, "DLY Knob 4", 57, "0-100"),
    MidiCcMapping(59, "DLY Knob 5", 58, "0-100"),
    MidiCcMapping(60, "DLY Knob 6", 59, "0-100"),
    MidiCcMapping(61, "DLY Knob 7", 60, "0-100"),
    MidiCcMapping(62, "DLY Knob 8", 61, "0-100"),
    MidiCcMapping(63, "RVB Knob 1", 62, "0-100"),
    MidiCcMapping(64, "RVB Knob 2", 63, "0-100"),
    MidiCcMapping(65, "RVB Knob 3", 64, "0-100"),
    MidiCcMapping(66, "RVB Knob 4", 65, "0-100"),
    MidiCcMapping(67, "CAB Knob 1", 66, "0-100"),
    MidiCcMapping(68, "CAB Knob 2", 67, "0-100"),
    MidiCcMapping(69, "CAB Knob 3", 68, "0-100"),
    MidiCcMapping(70, "CAB Knob 4", 69, "0-100"),
    MidiCcMapping(71, "CAB Knob 5", 70, "0-100"),
    MidiCcMapping(72, "CAB Knob 6", 71, "0-100"),
    MidiCcMapping(73, "Send", 72, "0-100"),
    MidiCcMapping(74, "Return", 73, "0-100"),
    MidiCcMapping(75, "Patch Min", 74, "0-100"),
    MidiCcMapping(76, "Patch Max", 75, "0-100"),
    MidiCcMapping(77, "Patch Level", 76, "0-100"),
    MidiCcMapping(78, "P.L Position", 77, "0-1"),
    MidiCcMapping(79, "Current Block", 78, "0-9"),
    MidiCcMapping(80, "Pedal", 79, "0-100"),
    MidiCcMapping(81, "Scene", 80, "0-2"),
    MidiCcMapping(82, "DRUM ENABLE", 81, "0-1"),
    MidiCcMapping(83, "DRUM TYPE", 82, "DYNAMIC"),
    MidiCcMapping(84, "DRUM LEVEL", 83, "0-100"),
    MidiCcMapping(85, "LOOP LEVEL", 84, "0-100"),
    MidiCcMapping(86, "LOOP STATE", 85, "DYNAMIC"),
    MidiCcMapping(87, "MICVOL", 86, "0-100"),
    MidiCcMapping(88, "MICTHRE", 87, "0-100"),
    MidiCcMapping(89, "MICDECAY", 88, "0-100"),
    MidiCcMapping(90, "AUXVOL", 89, "0-100"),
    MidiCcMapping(91, "UACIN", 90, "0-100"),
    MidiCcMapping(92, "UACOUT", 91, "0-100")
)

@Composable
fun MidiMappingDialog(
    midiLogs: List<String> = emptyList(),
    onSendCc: (ccNumber: Int, ccValue: Int) -> Unit = { _, _ -> },
    onSendPc: (pcNumber: Int) -> Unit = { _ -> },
    onDismissRequest: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Mapping Table, 1 = Live MIDI Controller
    var searchQuery by remember { mutableStateOf("") }
    var testCcNumber by remember { mutableStateOf("2") } // EFX default
    var testCcValue by remember { mutableStateOf(64f) }
    var testPcNumber by remember { mutableStateOf("0") }
    val localMidiLogs = remember { mutableStateListOf<String>() }
    val combinedLogs = remember(midiLogs, localMidiLogs.toList()) {
        (localMidiLogs.toList() + midiLogs).ifEmpty {
            listOf("[SYS] NUX MG-101 Live MIDI Console Ready")
        }
    }

    val filteredMappings = remember(searchQuery) {
        if (searchQuery.isBlank()) MG101_MIDI_MAPPINGS
        else MG101_MIDI_MAPPINGS.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.midiCc.toString().contains(searchQuery) ||
                    it.id.toString().contains(searchQuery)
        }
    }


    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(0.92f)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.background,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "MIDI MAPPING & COMMUNICATION",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Control Change (CC) & Program Change (PC) Table",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow)
                            .clickable { activeTab = 0 }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "CC MAPPING TABLE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (activeTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow)
                            .clickable { activeTab = 1 }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "LIVE MIDI TESTER",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (activeTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (activeTab == 0) {
                    // Search Bar for 92 CC Table
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search 92 CC Mappings (e.g. AMP, CC 2, EQ...)", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    // Mapping Table View
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Table Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ID",
                                    modifier = Modifier.weight(0.8f),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "PARAMETER",
                                    modifier = Modifier.weight(2f),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "MIDI CC",
                                    modifier = Modifier.weight(1.2f),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "RANGE",
                                    modifier = Modifier.weight(1.2f),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Table Data List
                            LazyColumn(
                                modifier = Modifier.weight(1f)
                            ) {
                                items(filteredMappings) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (item.id % 2 == 0) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow)
                                            .clickable {
                                                onSendCc(item.midiCc, 127)
                                                localMidiLogs.add(0, "[TX] CC ${item.midiCc} (${item.name}) -> 127")
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${item.id}",
                                            modifier = Modifier.weight(0.8f),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = item.name,
                                            modifier = Modifier.weight(2f),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${item.midiCc}",
                                            modifier = Modifier.weight(1.2f),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.range,
                                            modifier = Modifier.weight(1.2f),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Presets use Program Change: PC C0 00~06. For example, Preset 1 = PC C0 00",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                } else {
                    // Live MIDI Controller / Tester View
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "SEND CONTROL CHANGE (CC)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = testCcNumber,
                                        onValueChange = { testCcNumber = it },
                                        label = { Text("CC Number", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.width(110.dp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "VALUE: ${testCcValue.toInt()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = FontFamily.Monospace
                                        )

                                        Slider(
                                            value = testCcValue,
                                            onValueChange = { testCcValue = it },
                                            valueRange = 0f..127f,
                                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val ccNum = testCcNumber.toIntOrNull() ?: 2
                                            val ccVal = testCcValue.toInt()
                                            onSendCc(ccNum, ccVal)
                                            localMidiLogs.add(0, "[TX] CC $ccNum -> $ccVal")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                                    }
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "SEND PROGRAM CHANGE (PC)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = testPcNumber,
                                        onValueChange = { testPcNumber = it },
                                        label = { Text("Preset PC #", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.secondary)
                                    )

                                    Button(
                                        onClick = {
                                            val pcNum = testPcNumber.toIntOrNull() ?: 0
                                            onSendPc(pcNum)
                                            localMidiLogs.add(0, "[TX] PC Change -> $pcNum")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Text("LOAD PRESET", color = MaterialTheme.colorScheme.onSecondary)
                                    }
                                }
                            }
                        }


                        // Live Console Log
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "REALTIME MIDI COMM CONSOLE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )

                                    IconButton(
                                        onClick = { localMidiLogs.clear() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Clear Log",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(combinedLogs) { log ->
                                        Text(
                                            text = log,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (log.contains("[TX]")) MaterialTheme.colorScheme.primary else if (log.contains("[RX]")) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("OK", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
