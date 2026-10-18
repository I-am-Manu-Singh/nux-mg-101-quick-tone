package com.creativeblisslabs.nuxmg101.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.creativeblisslabs.nuxmg101.data.model.MidiMappingEntry
import com.creativeblisslabs.nuxmg101.data.model.EffectSlotType
import com.creativeblisslabs.nuxmg101.data.repository.MidiMappingRepository
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativeblisslabs.nuxmg101.data.model.HardwareStatus
import com.creativeblisslabs.nuxmg101.data.model.Patch
import com.creativeblisslabs.nuxmg101.ui.components.MG101_MIDI_MAPPINGS
import com.creativeblisslabs.nuxmg101.ui.components.MidiCcMapping
import com.creativeblisslabs.nuxmg101.ui.components.TactileKnob

@Composable
fun HardwareControlsScreen(
    hardwareStatus: HardwareStatus,
    activePatch: Patch,
    effectChain: List<com.example.data.model.EffectBlock> = emptyList(),
    midiLogs: List<String> = emptyList(),
    midiMappingRepository: com.example.data.repository.MidiMappingRepository? = null,
    onSendCc: (ccNum: Int, ccVal: Int) -> Unit,
    onSendPc: (pcNum: Int) -> Unit,
    onToggleSlotActive: (EffectSlotType) -> Unit = {},
    onTogglePlPosition: () -> Unit = {},
    onPrevPatch: () -> Unit = {},
    onNextPatch: () -> Unit = {},
    onTapTempo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Hardware Front Panel, 1 = MIDI CC Map Table, 2 = Live MIDI Console

    // Panel controls state
    var expValue by remember { mutableFloatStateOf(75f) }
    var knob1Val by remember { mutableFloatStateOf(50f) }
    var knob2Val by remember { mutableFloatStateOf(50f) }
    var knob3Val by remember { mutableFloatStateOf(50f) }
    var currentTargetBlock by remember { mutableIntStateOf(2) } // 0..9 target block
    var isDrumActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Hardware",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(start = 12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

        // Tab Header
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
          
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Midi Mapping", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Live Console", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        when (selectedTab) {
            // 0 -> HardwareFrontPanelView(
            //     hardwareStatus = hardwareStatus,
            //     activePatch = activePatch,
            //     expValue = expValue,
            //     onExpChange = {
            //         expValue = it
            //         onSendCc(79, it.toInt().coerceIn(0, 100))
            //     },
            //     knob1 = knob1Val,
            //     onKnob1Change = {
            //         knob1Val = it
            //         onSendCc(24, it.toInt().coerceIn(0, 100))
            //     },
            //     knob2 = knob2Val,
            //     onKnob2Change = {
            //         knob2Val = it
            //         onSendCc(25, it.toInt().coerceIn(0, 100))
            //     },
            //     knob3 = knob3Val,
            //     onKnob3Change = {
            //         knob3Val = it
            //         onSendCc(26, it.toInt().coerceIn(0, 100))
            //     },
            //     onButtonPress = { buttonNum ->
            //         when (buttonNum) {
            //             1 -> { // 1: EDIT/EXIT -> Select/Focus Block CC 78
            //                 onSendCc(78, currentTargetBlock)
            //             }
            //             2 -> { // 2: SAVE -> Save/Store Patch CC 76
            //                 onSendCc(76, 50)
            //             }
            //             3 -> { // 3: MODE -> Switch Target Block
            //                 currentTargetBlock = (currentTargetBlock + 1) % 10
            //                 onSendCc(78, currentTargetBlock)
            //             }
            //             4 -> { // 4: JAM -> Toggle Drum Machine CC 81
            //                 isDrumActive = !isDrumActive
            //                 onSendCc(81, if (isDrumActive) 1 else 0)
            //             }
            //             5 -> onTapTempo() // 5: TAP -> Tap tempo
            //             6 -> onSendCc(79, 100) // 6: EXP -> Expression pedal target
            //             7 -> { // 7: LEFT -> Previous Target Block (CC 78)
            //                 currentTargetBlock = (currentTargetBlock - 1 + 10) % 10
            //                 onSendCc(78, currentTargetBlock)
            //             }
            //             8 -> { // 8: PAGE -> Toggle P.L position (CC 77: 0=Pre, 1=Post)
            //                 onTogglePlPosition()
            //             }
            //             9 -> { // 9: RIGHT -> Next Target Block (CC 78)
            //                 currentTargetBlock = (currentTargetBlock + 1) % 10
            //                 onSendCc(78, currentTargetBlock)
            //             }
            //             10 -> { // 10: SELECT Knob Push -> Toggle Current Block Active/Bypass
            //                 val slot = when (currentTargetBlock) {
            //                     0 -> EffectSlotType.GATE
            //                     1 -> EffectSlotType.COMP
            //                     2 -> EffectSlotType.EFX
            //                     3 -> EffectSlotType.AMP
            //                     4 -> EffectSlotType.CAB
            //                     5 -> EffectSlotType.EQ
            //                     6 -> EffectSlotType.MOD
            //                     7 -> EffectSlotType.DELAY
            //                     8 -> EffectSlotType.REVERB
            //                     9 -> EffectSlotType.PL
            //                     else -> EffectSlotType.EFX
            //                 }
            //                 onToggleSlotActive(slot)
            //             }
            //             12 -> onPrevPatch() // 12: DOWN / LOOP -> Patch Down
            //             13 -> onNextPatch() // 13: UP / DRUM -> Patch Up
            //         }
            //     }
            // )
            0 -> MidiCcTableView(
                midiMappingRepository = midiMappingRepository,
                onSendCc = onSendCc
            )
            1 -> LiveMidiConsoleView(
                midiLogs = midiLogs,
                onSendCc = onSendCc,
                onSendPc = onSendPc
            )
        }
    }
}

@Composable
fun HardwareFrontPanelView(
    hardwareStatus: HardwareStatus,
    activePatch: Patch,
    expValue: Float,
    onExpChange: (Float) -> Unit,
    knob1: Float,
    onKnob1Change: (Float) -> Unit,
    knob2: Float,
    onKnob2Change: (Float) -> Unit,
    knob3: Float,
    onKnob3Change: (Float) -> Unit,
    onButtonPress: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Physical NUX MG-101 Chassis Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Branding & LCD Screen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MG-101",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE65100))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("TS/AC-HD", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0D47A1))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("CORE IMAGE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Simulated 2.4" Color LCD
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activePatch.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "BPM ${hardwareStatus.currentBpm}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activePatch.slotNumber,
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                fontSize = 32.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text("VOL: 50", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text("EXP: AMP", color = Color(0xFFE65100), fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text("P.L: POST", color = Color(0xFF4CAF50), fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Top Function Buttons Grid (Numbered 1..9 from User Photo)
                Text(
                    text = "HARDWARE FUNCTION BUTTONS (1 ~ 9)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )

                val buttons = listOf(
                    Triple(1, "EDIT/EXIT", Icons.Default.Edit),
                    Triple(2, "SAVE", Icons.Default.Save),
                    Triple(3, "MODE", Icons.Default.Tune),
                    Triple(4, "JAM", Icons.Default.MusicNote),
                    Triple(5, "TAP", Icons.Default.Timer),
                    Triple(6, "EXP", Icons.Default.Build),
                    Triple(7, "LEFT", Icons.AutoMirrored.Filled.ArrowBack),
                    Triple(8, "PAGE", Icons.Default.SwapHoriz),
                    Triple(9, "RIGHT", Icons.AutoMirrored.Filled.ArrowForward)
                )

                val chunkedButtons = buttons.chunked(3)
                chunkedButtons.forEach { rowButtons ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowButtons.forEach { (num, name, icon) ->
                            OutlinedButton(
                                onClick = { onButtonPress(num) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$num",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = name,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Parameter Knobs (10 = SELECT, 11 = Knobs 1, 2, 3)
                Text(
                    text = "SELECT & PARAMETER ADJUSTMENT KNOBS (10, 11)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TactileKnob(
                        label = "10 SELECT",
                        value = 50f,
                        displayValue = "PUSH",
                        onValueChange = { onButtonPress(10) },
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                    TactileKnob(
                        label = "11 KNOB 1",
                        value = knob1,
                        displayValue = "${knob1.toInt()}",
                        onValueChange = onKnob1Change,
                        accentColor = Color(0xFF38BDF8)
                    )
                    TactileKnob(
                        label = "11 KNOB 2",
                        value = knob2,
                        displayValue = "${knob2.toInt()}",
                        onValueChange = onKnob2Change,
                        accentColor = Color(0xFF38BDF8)
                    )
                    TactileKnob(
                        label = "11 KNOB 3",
                        value = knob3,
                        displayValue = "${knob3.toInt()}",
                        onValueChange = onKnob3Change,
                        accentColor = Color(0xFF38BDF8)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Footswitches & Expression Pedal (12, 13, 14)
                Text(
                    text = "FOOTSWITCHES & EXPRESSION PEDAL (12, 13, 14)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Footswitch 12 (DOWN / LOOP)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(88.dp)
                            .clickable { onButtonPress(12) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("12 DOWN / LOOP", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("PATCH DOWN", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                        }
                    }

                    // Footswitch 13 (UP / DRUM)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(88.dp)
                            .clickable { onButtonPress(13) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("13 UP / DRUM", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("PATCH UP", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                        }
                    }
                }

                // 14 Expression Pedal Slider
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("14 EXPRESSION PEDAL (CC#79)", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("${expValue.toInt()} %", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = expValue,
                            onValueChange = onExpChange,
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MidiCcTableView(
    midiMappingRepository: com.example.data.repository.MidiMappingRepository? = null,
    onSendCc: (ccNum: Int, ccVal: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val allDbMappings by (midiMappingRepository?.allMappings ?: kotlinx.coroutines.flow.flowOf(com.example.data.repository.MidiMappingRepository.DEFAULT_MIDI_ENTRIES)).collectAsState(initial = com.example.data.repository.MidiMappingRepository.DEFAULT_MIDI_ENTRIES)

    var tableMode by remember { mutableIntStateOf(0) } // 0 = Pedal Models & Slots, 1 = Hardware CC Map (Knobs & Switches)
    val categories = listOf("ALL", "WAH", "CMP", "EFX", "AMP", "EQ", "GATE", "MOD", "DLY", "RVB", "IR", "P.L", "S/R", "VOL")
    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var editingEntry by remember { mutableStateOf<com.example.data.model.MidiMappingEntry?>(null) }
    var isImportDialogOpen by remember { mutableStateOf(false) }
    var csvImportText by remember { mutableStateOf("") }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    val filteredList = remember(allDbMappings, selectedCategory, searchQuery) {
        allDbMappings.filter { entry: MidiMappingEntry ->
            (selectedCategory == "ALL" || entry.category.equals(selectedCategory, ignoreCase = true)) &&
            (searchQuery.isBlank() ||
                entry.name.contains(searchQuery, ignoreCase = true) ||
                entry.category.contains(searchQuery, ignoreCase = true) ||
                entry.subcategory.contains(searchQuery, ignoreCase = true) ||
                entry.ccOn.toString().contains(searchQuery) ||
                entry.saveOn.toString().contains(searchQuery))
        }.sortedWith(compareBy<MidiMappingEntry> { it.category }.thenBy { it.displayOrder }.thenBy { it.id })
    }

    val filteredHardwareCc = remember(searchQuery) {
        if (searchQuery.isBlank()) MG101_MIDI_MAPPINGS
        else MG101_MIDI_MAPPINGS.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.midiCc.toString().contains(searchQuery) ||
            it.id.toString().contains(searchQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Mode Selector: Pedal Models vs Hardware CC Parameters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (tableMode == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(1.dp, if (tableMode == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                    .clickable { tableMode = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PEDAL MODELS & SLOTS (${filteredList.size})",
                    color = if (tableMode == 0) Color.Black else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (tableMode == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(1.dp, if (tableMode == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                    .clickable { tableMode = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "HARDWARE CC PARAMETERS (${filteredHardwareCc.size})",
                    color = if (tableMode == 1) Color.Black else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        // Actions Header: CSV Export, CSV Import, Reset Defaults
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PEDAL & MIDI MAPPING (${filteredList.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val csv = midiMappingRepository?.exportToCsv() ?: ""
                            exportSuccessMessage = "Exported ${allDbMappings.size} entries to CSV (Copied to Clip)"
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = "Export", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { isImportDialogOpen = true },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Filled.Upload, contentDescription = "Import", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            midiMappingRepository?.resetToDefaultData()
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        exportSuccessMessage?.let { msg ->
            Text(
                text = msg,
                color = Color(0xFF4CAF50),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by Name, Subcategory, CC#, Save Code...", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        if (tableMode == 0) {
            // Table Header for Pedal Models
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ORDER", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("CAT", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("NAME", modifier = Modifier.weight(1.8f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("CC ON", modifier = Modifier.weight(0.9f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("SAVE", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("ACTIONS", modifier = Modifier.weight(1.4f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End)
                }
            }

            // Table Rows for Pedal Models
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Re-order Move Up / Down
                            Row(modifier = Modifier.weight(0.7f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            coroutineScope.launch {
                                                midiMappingRepository?.reorderMappings(item.category, item, true)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("▲", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            coroutineScope.launch {
                                                midiMappingRepository?.reorderMappings(item.category, item, false)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("▼", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Text(item.category, modifier = Modifier.weight(0.7f), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Column(modifier = Modifier.weight(1.8f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                                Text("Sub: ${item.subcategory}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
                            }
                            Text("CC#${item.ccOn}", modifier = Modifier.weight(0.9f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("${item.saveOn}", modifier = Modifier.weight(0.8f), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontFamily = FontFamily.Monospace)

                            // Action Buttons: Send & Edit
                            Row(
                                modifier = Modifier.weight(1.4f),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { editingEntry = item },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Button(
                                    onClick = {
                                        val ccSlot = when (item.category.uppercase()) {
                                            "WAH" -> 0
                                            "CMP" -> 1
                                            "EFX" -> 2
                                            "AMP" -> 3
                                            "EQ" -> 4
                                            "GATE" -> 5
                                            "MOD" -> 6
                                            "DLY" -> 7
                                            "RVB" -> 8
                                            "IR" -> 9
                                            "P.L" -> 10
                                            "S/R" -> 10
                                            "VOL" -> 11
                                            else -> 2
                                        }
                                        onSendCc(ccSlot, item.ccOn)
                                    },
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("SEND", fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Table Header for Hardware CC Map
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ID", modifier = Modifier.weight(0.6f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("NAME", modifier = Modifier.weight(1.8f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("MIDI CC", modifier = Modifier.weight(1.0f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("RANGE", modifier = Modifier.weight(1.0f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("ACTION", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End)
                }
            }

            // Table Rows for Hardware CC Map
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredHardwareCc) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.id}", modifier = Modifier.weight(0.6f), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(item.name, modifier = Modifier.weight(1.8f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                            Text("CC #${item.midiCc}", modifier = Modifier.weight(1.0f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(item.range, modifier = Modifier.weight(1.0f), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Box(
                                modifier = Modifier.weight(0.8f),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Button(
                                    onClick = { onSendCc(item.midiCc, 64) },
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("SEND", fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Item Dialog
    editingEntry?.let { entry ->
        var editName by remember { mutableStateOf(entry.name) }
        var editSubcat by remember { mutableStateOf(entry.subcategory) }
        var editCcOn by remember { mutableStateOf(entry.ccOn.toString()) }
        var editSaveOn by remember { mutableStateOf(entry.saveOn.toString()) }

        androidx.compose.ui.window.Dialog(onDismissRequest = { editingEntry = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "EDIT PEDAL / MIDI ENTRY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Model Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editSubcat,
                        onValueChange = { editSubcat = it },
                        label = { Text("Subcategory") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editCcOn,
                            onValueChange = { editCcOn = it },
                            label = { Text("CC On Value") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editSaveOn,
                            onValueChange = { editSaveOn = it },
                            label = { Text("Save On Code") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { editingEntry = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val updated = entry.copy(
                                    name = editName,
                                    subcategory = editSubcat,
                                    ccOn = editCcOn.toIntOrNull() ?: entry.ccOn,
                                    saveOn = editSaveOn.toIntOrNull() ?: entry.saveOn
                                )
                                coroutineScope.launch {
                                    midiMappingRepository?.updateMapping(updated)
                                }
                                editingEntry = null
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    // CSV Import Dialog
    if (isImportDialogOpen) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { isImportDialogOpen = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "IMPORT MIDI & PEDALS CSV",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Paste CSV content (Category,Subcategory,Name,Save On,Save Off,CC On,CC Off,DisplayOrder)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = csvImportText,
                        onValueChange = { csvImportText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("Paste CSV here...") }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { isImportDialogOpen = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val count = midiMappingRepository?.importFromCsv(csvImportText) ?: 0
                                    exportSuccessMessage = "Successfully imported $count pedal entries"
                                    isImportDialogOpen = false
                                }
                            }
                        ) {
                            Text("Import CSV")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveMidiConsoleView(
    midiLogs: List<String> = emptyList(),
    onSendCc: (ccNum: Int, ccVal: Int) -> Unit,
    onSendPc: (pcNum: Int) -> Unit
) {
    var ccNumStr by remember { mutableStateOf("2") }
    var ccValStr by remember { mutableStateOf("64") }
    var pcNumStr by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Real-Time Live MIDI Activity Log Terminal
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE MIDI I/O ACTIVITY MONITOR (${midiLogs.size} EVENTS)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (midiLogs.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (midiLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO MIDI TRAFFIC YET.\nInteract with knobs, pedals, or send CC/PC below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        reverseLayout = true
                    ) {
                        items(midiLogs.reversed()) { log ->
                            val isTx = log.contains("TX", ignoreCase = true)
                            val isRx = log.contains("RX", ignoreCase = true)
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isTx) Color(0xFF38BDF8) else if (isRx) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Control Change Transmitter
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TRANSMIT CONTROL CHANGE (CC)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ccNumStr,
                        onValueChange = { ccNumStr = it },
                        label = { Text("CC Number", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ccValStr,
                        onValueChange = { ccValStr = it },
                        label = { Text("Value (0..127)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val ccNum = ccNumStr.toIntOrNull() ?: 2
                            val ccVal = ccValStr.toIntOrNull() ?: 0
                            onSendCc(ccNum, ccVal)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }

        // Program Change Transmitter
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TRANSMIT PROGRAM CHANGE (PC)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pcNumStr,
                        onValueChange = { pcNumStr = it },
                        label = { Text("Program Number (PC 0..71)", fontSize = 11.sp) },
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val pcNum = pcNumStr.toIntOrNull() ?: 0
                            onSendPc(pcNum)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("LOAD PC", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
