package com.creativeblisslabs.nuxmg101.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.style.TextOverflow 
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.creativeblisslabs.nuxmg101.data.model.EffectBlock
import com.creativeblisslabs.nuxmg101.data.model.EffectSlotType
import com.creativeblisslabs.nuxmg101.data.model.HardwareStatus
import com.creativeblisslabs.nuxmg101.data.model.Patch
import com.creativeblisslabs.nuxmg101.ui.components.PedalGraphicView
import com.creativeblisslabs.nuxmg101.ui.components.ExpressionPedalGraphic
import com.creativeblisslabs.nuxmg101.ui.components.getPedalGraphicConfig
import com.creativeblisslabs.nuxmg101.ui.components.TactileKnob

@Composable
fun SignalDashboardScreen(
    hardwareStatus: HardwareStatus,
    activePatch: Patch,
    effectChain: List<EffectBlock>,
    selectedSlot: EffectSlotType,
    onSelectSlot: (EffectSlotType) -> Unit,
    onToggleSlotActive: (EffectSlotType) -> Unit,
    onToggleMute: () -> Unit,
    onTapTempo: () -> Unit,
    onPrevPatch: () -> Unit,
    onNextPatch: () -> Unit,
    onEditSlot: (EffectSlotType) -> Unit,
    onOpenModelChooser: (EffectSlotType) -> Unit,
    onGlobalParamChange: (ccNum: Int, value: Float) -> Unit = { _, _ -> },
    onUpdateHardwareLevels: (out: Float, phones: Float) -> Unit = { _, _ -> },
    onTogglePlPosition: () -> Unit = {},
    onToggleExpEngaged: () -> Unit = {},
    onSetExpTarget: (String) -> Unit = {},
    onSetExpValue: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var outLvl by remember(hardwareStatus.outputLevel) { mutableFloatStateOf(hardwareStatus.outputLevel) }
    var phonesLvl by remember(hardwareStatus.phonesLevel) { mutableFloatStateOf(hardwareStatus.phonesLevel) }

    val pedalRows = effectChain.chunked(5)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            Text(
                text = "Signal Chain",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )

        // Connected Device Card with MG-101 Illustration
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CONNECTED DEVICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = hardwareStatus.deviceName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (hardwareStatus.isWiredUsb) "USB-C OTG Live Sync" else "Bluetooth BLE Active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Detailed MG-101 Hardware Processor Illustration
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF18181B))
                        .border(1.5.dp, Color(0xFF3F3F46), RoundedCornerShape(8.dp))
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Brand and Badges
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("NUX", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White, fontFamily = FontFamily.Monospace)
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Box(modifier = Modifier.size(width = 8.dp, height = 4.dp).clip(RoundedCornerShape(1.dp)).background(Color(0xFFE65100)))
                                Box(modifier = Modifier.size(width = 8.dp, height = 4.dp).clip(RoundedCornerShape(1.dp)).background(Color(0xFF0D47A1)))
                            }
                        }
                        
                        // 2.4" Color Display Screen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF090D16))
                                .border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = activePatch.slotNumber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF38BDF8),
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = activePatch.name.take(6),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Knobs and Switches row
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF52525B)).border(0.5.dp, Color.White, CircleShape))
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF52525B)).border(0.5.dp, Color.White, CircleShape))
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF52525B)).border(0.5.dp, Color.White, CircleShape))
                            Box(modifier = Modifier.size(width = 14.dp, height = 8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF27272A)).border(0.5.dp, Color(0xFF71717A), RoundedCornerShape(2.dp)))
                        }
                    }
                }
            }
        }



        // Signal Chain Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "SIGNAL CHAIN",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = onTogglePlPosition,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Move P.L",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("MOVE P.L", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "ROUTING: SERIAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                pedalRows.forEach { rowPedals ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowPedals.forEachIndexed { index, block ->
                            val isSelected = block.slotType == selectedSlot
                            val pedalConfig = getPedalGraphicConfig(block.modelName, block.slotType)

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (block.active) {
                                        pedalConfig.primaryColor.copy(alpha = if (isSelected) 0.20f else 0.10f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLowest
                                    }
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) {
                                        pedalConfig.primaryColor
                                    } else if (block.active) {
                                        pedalConfig.primaryColor.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .width(68.dp)
                                    .height(138.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Upper Body: Badge & Graphic (Click to Select / Edit)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSelectSlot(block.slotType)
                                                onEditSlot(block.slotType)
                                            }
                                    ) {
                                    

                                        Box(
                                            modifier = Modifier
                                                .width(46.dp)
                                                .height(52.dp)
                                                .padding(vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            PedalGraphicView(
                                                modelName = block.modelName,
                                                slotType = block.slotType,
                                                isActive = block.active,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Text(
                                            text = block.modelName.ifBlank { block.slotType.displayName },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (block.active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 8.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
    Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (block.active) pedalConfig.primaryColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = block.slotType.shortCode,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (block.active) pedalConfig.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 9.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))
                                    // Lower Body: Stomp Footswitch / Power Toggle Button (Click to Toggle Power Only)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(22.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (block.active) pedalConfig.primaryColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                                            .border(1.dp, if (block.active) pedalConfig.primaryColor else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                            .clickable {
                                                onToggleSlotActive(block.slotType)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (block.active) "ON" else "OFF",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (block.active) pedalConfig.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.5.sp
                                        )
                                    }
                                }
                            }

                            // Connecting Audio Patch Cable Line
                            if (index < rowPedals.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(8.dp)
                                        .height(2.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Output, Phones & Expression Pedal Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Header with EXP Target Switcher & Engage Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LEVELS & EXP PEDAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    // EXP Target Selection Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("WAH", "EFX", "AMP", "MOD", "DLY", "RVB", "VOL").forEach { target ->
                            val isSelected = hardwareStatus.expTarget == target
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onSetExpTarget(target) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = target,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TactileKnob(
                        label = "OUTPUT",
                        value = outLvl,
                        displayValue = "${outLvl.toInt()} %",
                        onValueChange = {
                            outLvl = it
                            onGlobalParamChange(76, it) // CC 76 = Patch Level / Output
                            onUpdateHardwareLevels(outLvl, phonesLvl)
                        },
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    TactileKnob(
                        label = "PHONES",
                        value = phonesLvl,
                        displayValue = "${phonesLvl.toInt()} %",
                        onValueChange = {
                            phonesLvl = it
                            onGlobalParamChange(11, it) // CC 11 = VOL / Master Volume
                            onUpdateHardwareLevels(outLvl, phonesLvl)
                        },
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    // Expression Pedal Styled Exact Stomp/Treadle Widget
                    val expAccentColor = MaterialTheme.colorScheme.primary
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (hardwareStatus.expEngaged) expAccentColor.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        border = BorderStroke(
                            width = if (hardwareStatus.expEngaged) 2.dp else 1.dp,
                            color = if (hardwareStatus.expEngaged) expAccentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(68.dp)
                            .height(138.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Upper Body: Graphic (Click to cycle target)
                            Box(
                                modifier = Modifier
                                    .width(52.dp)
                                    .height(58.dp)
                                    .padding(vertical = 2.dp)
                                    .clickable {
                                        val targets = listOf("VOL", "WAH", "EFX", "MOD", "DLY", "RVB")
                                        val nextIdx = (targets.indexOf(hardwareStatus.expTarget) + 1) % targets.size
                                        onSetExpTarget(targets[nextIdx])
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                ExpressionPedalGraphic(
                                    targetName = "${hardwareStatus.expValue.toInt()}%",
                                    pedalValue = hardwareStatus.expValue,
                                    isEngaged = hardwareStatus.expEngaged,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Model / Position readout
                            Text(
                                text = "${hardwareStatus.expValue.toInt()} %",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hardwareStatus.expEngaged) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 8.sp,
                                textAlign = TextAlign.Center
                            )

                            // Target Slot Badge (e.g., EXP: WAH / EXP: VOL)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (hardwareStatus.expEngaged) expAccentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        val targets = listOf("VOL", "WAH", "EFX", "MOD", "DLY", "RVB")
                                        val nextIdx = (targets.indexOf(hardwareStatus.expTarget) + 1) % targets.size
                                        onSetExpTarget(targets[nextIdx])
                                    }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "EXP: ${hardwareStatus.expTarget}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (hardwareStatus.expEngaged) expAccentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 8.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Stomp Footswitch / Engage Toggle
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(22.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (hardwareStatus.expEngaged) expAccentColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, if (hardwareStatus.expEngaged) expAccentColor else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                    .clickable { onToggleExpEngaged() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (hardwareStatus.expEngaged) "ON" else "OFF",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (hardwareStatus.expEngaged) expAccentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "QUICK ACTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onToggleMute,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hardwareStatus.isMuted) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (hardwareStatus.isMuted) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(72.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.VolumeOff,
                                contentDescription = "Mute",
                                tint = if (hardwareStatus.isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (hardwareStatus.isMuted) "MUTED" else "MUTE",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (hardwareStatus.isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onClick = onTapTempo,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(72.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Tap Tempo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "TAP TEMPO",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${hardwareStatus.currentBpm} BPM",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}