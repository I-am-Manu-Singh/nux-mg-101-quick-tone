package com.creativeblisslabs.nuxmg101.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
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
import com.creativeblisslabs.nuxmg101.data.model.EffectBlock
import com.creativeblisslabs.nuxmg101.data.model.EffectSlotType

@Composable
fun SignalChainBar(
    effectChain: List<EffectBlock>,
    selectedSlot: EffectSlotType,
    onSelectSlot: (EffectSlotType) -> Unit,
    onToggleActive: (EffectSlotType) -> Unit,
    onLongClickSlot: ((EffectSlotType) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        // Multi-Row Grid for Mobile Visibility
        // Row 1: IN -> First 5 Blocks (GATE, COMP, EFX, AMP, CAB)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SignalEndpointBadge(label = "IN", modifier = Modifier.weight(0.7f))

            effectChain.take(5).forEach { block ->
                val isSelected = block.slotType == selectedSlot
                SignalBlockCard(
                    block = block,
                    isSelected = isSelected,
                    accentColor = when (block.slotType) {
                        EffectSlotType.EFX, EffectSlotType.AMP -> MaterialTheme.colorScheme.primary
                        EffectSlotType.DELAY, EffectSlotType.REVERB -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    onClick = { onSelectSlot(block.slotType) },
                    onLongClick = { onLongClickSlot?.invoke(block.slotType) ?: onSelectSlot(block.slotType) },
                    onToggleActive = { onToggleActive(block.slotType) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2: Remaining 4 Blocks (EQ, MOD, DLY, RVB) -> OUT
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            effectChain.drop(5).forEach { block ->
                val isSelected = block.slotType == selectedSlot
                SignalBlockCard(
                    block = block,
                    isSelected = isSelected,
                    accentColor = when (block.slotType) {
                        EffectSlotType.EFX, EffectSlotType.AMP -> MaterialTheme.colorScheme.primary
                        EffectSlotType.DELAY, EffectSlotType.REVERB -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    onClick = { onSelectSlot(block.slotType) },
                    onLongClick = { onLongClickSlot?.invoke(block.slotType) ?: onSelectSlot(block.slotType) },
                    onToggleActive = { onToggleActive(block.slotType) },
                    modifier = Modifier.weight(1f)
                )
            }

            SignalEndpointBadge(label = "OUT", modifier = Modifier.weight(0.7f))
        }
    }
}

@Composable
private fun SignalEndpointBadge(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SignalBlockCard(
    block: EffectBlock,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleActive: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerBg = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow
    val borderColor = if (isSelected) accentColor else if (block.active) accentColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(containerBg)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() }
            )
            .padding(4.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (block.active) accentColor else Color.Gray.copy(alpha = 0.4f))
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = block.slotType.shortCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (block.active) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onToggleActive() }
                        .padding(1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Toggle Power",
                        tint = if (block.active) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = block.modelName,
                style = MaterialTheme.typography.bodySmall,
                color = if (block.active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                maxLines = 2
            )
        }
    }
}

