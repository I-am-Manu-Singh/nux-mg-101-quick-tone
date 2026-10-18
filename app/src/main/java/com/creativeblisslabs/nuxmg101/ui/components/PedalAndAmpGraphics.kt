package com.creativeblisslabs.nuxmg101.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativeblisslabs.nuxmg101.data.model.EffectSlotType

/**
 * Visual styling configuration for pedals and amps matching the MG-101 Hardware Chart
 */
data class PedalGraphicConfig(
    val primaryColor: Color,
    val secondaryColor: Color,
    val faceplateColor: Color,
    val label: String,
    val knobCount: Int = 2,
    val isRoundFuzz: Boolean = false,
    val isAmp: Boolean = false,
    val isCab: Boolean = false,
    val isEq: Boolean = false
)

fun getPedalGraphicConfig(modelName: String, slotType: EffectSlotType): PedalGraphicConfig {
    val clean = modelName.trim().uppercase()
    return when (slotType) {
        EffectSlotType.PL -> PedalGraphicConfig(
            primaryColor = Color(0xFF90CAF9),
            secondaryColor = Color(0xFF42A5F5),
            faceplateColor = Color(0xFF1E88E5),
            label = "P.L",
            knobCount = 1
        )
        EffectSlotType.GATE -> PedalGraphicConfig(
            primaryColor = Color(0xFF4CAF50), // Green (matching NG on processor)
            secondaryColor = Color(0xFF2E7D32),
            faceplateColor = Color(0xFF1B5E20),
            label = "NG",
            knobCount = 2
        )
        EffectSlotType.COMP -> when (clean) {
            "ROSE COMP" -> PedalGraphicConfig(
                primaryColor = Color(0xFFFFEB3B), // Yellow (matching CMP on processor)
                secondaryColor = Color(0xFFFBC02D),
                faceplateColor = Color(0xFFF57F17),
                label = "ROSE",
                knobCount = 2
            )
            else -> PedalGraphicConfig(
                primaryColor = Color(0xFFFFEB3B), // Yellow (matching CMP on processor)
                secondaryColor = Color(0xFFFBC02D),
                faceplateColor = Color(0xFFF57F17),
                label = "CMP",
                knobCount = 2
            )
        }
        EffectSlotType.EFX -> when (clean) {
            "DIST+" -> PedalGraphicConfig(Color(0xFFFBC02D), Color(0xFFF57F17), Color(0xFFFBC02D), "DIST +", 2)
            "RC BST" -> PedalGraphicConfig(Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFFFFFFFF), "RC BST", 4)
            "AC BST" -> PedalGraphicConfig(Color(0xFFFBC02D), Color(0xFFF57F17), Color(0xFFFFF59D), "AC BST", 4)
            "DIST UNO" -> PedalGraphicConfig(Color(0xFFEF6C00), Color(0xFFE65100), Color(0xFFFF9800), "DIST ONE", 3)
            "T SCREAMER" -> PedalGraphicConfig(Color(0xFF2E7D32), Color(0xFF1B5E20), Color(0xFF388E3C), "TS", 3)
            "BLUES DRIVE" -> PedalGraphicConfig(Color(0xFF1565C0), Color(0xFF0D47A1), Color(0xFF1976D2), "BLUES", 3)
            "MORNING DRIVE" -> PedalGraphicConfig(Color(0xFFFFF9C4), Color(0xFFFFF59D), Color(0xFFFFFFFF), "MORNING", 3)
            "MODERN DIST" -> PedalGraphicConfig(Color(0xFF212121), Color(0xFF000000), Color(0xFF37474F), "KAT", 3)
            "RED DIST" -> PedalGraphicConfig(Color(0xFFC62828), Color(0xFFB71C1C), Color(0xFFD32F2F), "RED DIST", 3)
            "KATANA" -> PedalGraphicConfig(Color(0xFFFFFFFF), Color(0xFFECEFF1), Color(0xFFFFFFFF), "KATANA", 1)
            "CRUNCH" -> PedalGraphicConfig(Color(0xFFD84315), Color(0xFFBF360C), Color(0xFFE64A19), "CRUNCH", 3)
            "TOUCH WAH" -> PedalGraphicConfig(Color(0xFF7E57C2), Color(0xFF5E35B1), Color(0xFF9575CD), "T WAH", 3)
            "RED FUZZ" -> PedalGraphicConfig(Color(0xFFD32F2F), Color(0xFFB71C1C), Color(0xFFD32F2F), "FUZZ", 2, isRoundFuzz = true)
            "MUF DIST" -> PedalGraphicConfig(Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFFB0BEC5), "MUFF", 3)
            else -> PedalGraphicConfig(Color(0xFFEF6C00), Color(0xFFE65100), Color(0xFFFF9800), "EFX", 3)
        }
        EffectSlotType.AMP -> {
            val (pri, sec, face) = when (clean) {
                "JAZZ CLEAN" -> Triple(Color(0xFF37474F), Color(0xFF263238), Color(0xFF546E7A))
                "DELUXE RVB" -> Triple(Color(0xFF212121), Color(0xFF000000), Color(0xFF37474F))
                "BASS MATE", "TWEEDY" -> Triple(Color(0xFF8D6E63), Color(0xFF6D4C41), Color(0xFFA1887F))
                "TWIN RVB", "SUPER RVB" -> Triple(Color(0xFF37474F), Color(0xFF263238), Color(0xFF78909C))
                "HIWIRE" -> Triple(Color(0xFF102027), Color(0xFF000000), Color(0xFFFFFFFF))
                "CALI CRUNCH" -> Triple(Color(0xFF4E342E), Color(0xFF3E2723), Color(0xFF8D6E63))
                "CLASS A15", "CLASS A30" -> Triple(Color(0xFF3E2723), Color(0xFF2E1C18), Color(0xFF5D4037))
                "PLEXI 100", "PLEXI 45", "1987X 50W", "BRIT 800" -> Triple(Color(0xFF212121), Color(0xFF000000), Color(0xFFC5A059))
                "SLO 100" -> Triple(Color(0xFF1B5E20), Color(0xFF0D3311), Color(0xFF2E7D32))
                "FIREMAN HBE" -> Triple(Color(0xFF3E2723), Color(0xFF212121), Color(0xFFD4AF37))
                "DUAL RECT" -> Triple(Color(0xFF212121), Color(0xFF000000), Color(0xFFB0BEC5))
                "DIE VH4" -> Triple(Color(0xFF263238), Color(0xFF102027), Color(0xFF455A64))
                "VIBRO KING" -> Triple(Color(0xFF5D4037), Color(0xFF3E2723), Color(0xFF8D6E63))
                "BUDDA" -> Triple(Color(0xFF8D6E63), Color(0xFF6D4C41), Color(0xFF9C27B0))
                "MR.Z 38" -> Triple(Color(0xFF424242), Color(0xFF212121), Color(0xFFEEEEEE))
                "BRIT BLUES" -> Triple(Color(0xFF263238), Color(0xFF102027), Color(0xFFB0BEC5))
                "MATCH D30" -> Triple(Color(0xFF3E2723), Color(0xFF212121), Color(0xFFC5A059))
                "BRIT 2000" -> Triple(Color(0xFF212121), Color(0xFF000000), Color(0xFFD32F2F))
                "UBER HIGAIN" -> Triple(Color(0xFF1B0000), Color(0xFF000000), Color(0xFFFFFFFF))
                else -> Triple(Color(0xFF212121), Color(0xFF000000), Color(0xFFC5A059))
            }
            PedalGraphicConfig(pri, sec, face, modelName.take(10), 6, isAmp = true)
        }
        EffectSlotType.CAB -> {
            val (pri, sec) = when (clean) {
                "TWEED 410", "TWEED 112", "BUTQ 212" -> Pair(Color(0xFF8D6E63), Color(0xFFA1887F))
                "BLUE 112", "BLUE 212" -> Pair(Color(0xFF1565C0), Color(0xFF0D47A1))
                "GREEN 412", "G12M 412" -> Pair(Color(0xFF2E7D32), Color(0xFF1B5E20))
                "SLO 412" -> Pair(Color(0xFF1B5E20), Color(0xFF0D3311))
                "CALI 112", "VIBRO 212", "DC 212", "HBE 412" -> Pair(Color(0xFF4E342E), Color(0xFF3E2723))
                else -> Pair(Color(0xFF212121), Color(0xFF37474F))
            }
            PedalGraphicConfig(pri, sec, Color(0xFF455A64), modelName.take(9), 0, isCab = true)
        }
        EffectSlotType.EQ -> PedalGraphicConfig(
            primaryColor = Color(0xFF263238),
            secondaryColor = Color(0xFF102027),
            faceplateColor = Color(0xFF37474F),
            label = modelName,
            knobCount = 0,
            isEq = true
        )
        EffectSlotType.MOD -> when (clean) {
            "CE-1" -> PedalGraphicConfig(Color(0xFF1565C0), Color(0xFF0D47A1), Color(0xFF90CAF9), "CE-1", 2)
            "CLASSIC CHORUS" -> PedalGraphicConfig(Color(0xFF00ACC1), Color(0xFF00838F), Color(0xFFE0F7FA), "CHORUS", 2)
            "ST.CHORUS" -> PedalGraphicConfig(Color(0xFF0288D1), Color(0xFF01579B), Color(0xFFE1F5FE), "CHORUS", 2)
            "VIBRATO" -> PedalGraphicConfig(Color(0xFF0277BD), Color(0xFF01579B), Color(0xFF81D4FA), "VIBRATO", 2)
            "DETUNE" -> PedalGraphicConfig(Color(0xFF00796B), Color(0xFF004D40), Color(0xFFE0F2F1), "DETUNE", 2)
            "FLANGE" -> PedalGraphicConfig(Color(0xFF6A1B9A), Color(0xFF4A148C), Color(0xFFE1BEE7), "FLANGE", 3)
            "PH 90" -> PedalGraphicConfig(Color(0xFFEF6C00), Color(0xFFE65100), Color(0xFFFF9800), "PH 90", 1)
            "PHASE 100" -> PedalGraphicConfig(Color(0xFFE65100), Color(0xFFBF360C), Color(0xFFFF9800), "PH 100", 2)
            "CHORUS+" -> PedalGraphicConfig(Color(0xFF0097A7), Color(0xFF00838F), Color(0xFF263238), "CHORUS+", 3)
            "U-VIBE" -> PedalGraphicConfig(Color(0xFF616161), Color(0xFF424242), Color(0xFFBDBDBD), "VIBE", 2)
            "TREM" -> PedalGraphicConfig(Color(0xFF00695C), Color(0xFF004D40), Color(0xFF80CBC4), "TREM", 2)
            "RTY SPK" -> PedalGraphicConfig(Color(0xFF4E342E), Color(0xFF3E2723), Color(0xFF8D6E63), "RTY SPK", 2)
            "HARMONY" -> PedalGraphicConfig(Color(0xFFC62828), Color(0xFFB71C1C), Color(0xFFEF9A9A), "HARMONY", 4)
            "SCH-1" -> PedalGraphicConfig(Color(0xFF00838F), Color(0xFF006064), Color(0xFFB2EBF2), "SCH-1", 3)
            else -> PedalGraphicConfig(Color(0xFF0288D1), Color(0xFF01579B), Color(0xFFB3E5FC), "MOD", 2)
        }
        EffectSlotType.DELAY -> when (clean) {
            "ANALOG DELAY" -> PedalGraphicConfig(Color(0xFFC2185B), Color(0xFF880E4F), Color(0xFFF8BBD0), "ANALOG", 3)
            "DIGI DELAY" -> PedalGraphicConfig(Color(0xFF0288D1), Color(0xFF01579B), Color(0xFFE1F5FE), "DIGI DLY", 3)
            "MODULATE" -> PedalGraphicConfig(Color(0xFF0097A7), Color(0xFF006064), Color(0xFFB2EBF2), "MOD DLY", 3)
            "TAPE ECHO" -> PedalGraphicConfig(Color(0xFF37474F), Color(0xFF212121), Color(0xFF455A64), "TAPE", 3)
            "REVERSE" -> PedalGraphicConfig(Color(0xFF4A148C), Color(0xFF311B92), Color(0xFFE1BEE7), "REVERSE", 3)
            "PAN" -> PedalGraphicConfig(Color(0xFF00838F), Color(0xFF006064), Color(0xFF80DEEA), "PAN DLY", 3)
            "PHI DELAY" -> PedalGraphicConfig(Color(0xFF263238), Color(0xFF102027), Color(0xFFECEFF1), "PHI DLY", 3)
            else -> PedalGraphicConfig(Color(0xFF00838F), Color(0xFF006064), Color(0xFF80DEEA), "DLY", 3)
        }
        EffectSlotType.REVERB -> when (clean) {
            "ROOM" -> PedalGraphicConfig(Color(0xFF37474F), Color(0xFF212121), Color(0xFF455A64), "ROOM", 3)
            "HALL" -> PedalGraphicConfig(Color(0xFF212121), Color(0xFF000000), Color(0xFF424242), "HALL", 3)
            "PLATE" -> PedalGraphicConfig(Color(0xFFB0BEC5), Color(0xFF78909C), Color(0xFFECEFF1), "PLATE", 3)
            "SPRING" -> PedalGraphicConfig(Color(0xFFD84315), Color(0xFFBF360C), Color(0xFFFFCC80), "SPRING", 3)
            "SHIMMER" -> PedalGraphicConfig(Color(0xFF4A148C), Color(0xFF311B92), Color(0xFFD1C4E9), "SHIMMER", 3)
            else -> PedalGraphicConfig(Color(0xFF37474F), Color(0xFF263238), Color(0xFF78909C), "RVB", 3)
        }
        EffectSlotType.WAH -> PedalGraphicConfig(
            primaryColor = Color(0xFFDD6FB5),
            secondaryColor = Color(0xFFAD1457),
            faceplateColor = Color(0xFFF8BBD0),
            label = "WAH",
            knobCount = 1
        )
        EffectSlotType.SR -> PedalGraphicConfig(
            primaryColor = Color(0xFF4CAF50),
            secondaryColor = Color(0xFF2E7D32),
            faceplateColor = Color(0xFFA5D6A7),
            label = "S/R",
            knobCount = 2
        )
        EffectSlotType.VOL -> PedalGraphicConfig(
            primaryColor = Color(0xFFFFEFB9),
            secondaryColor = Color(0xFFFBC02D),
            faceplateColor = Color(0xFFFFF9C4),
            label = "VOL",
            knobCount = 1
        )
    }
}

/**
 * High-fidelity graphic representation of Guitar Pedals, Amplifiers, and Cabinets
 */
@Composable
fun PedalGraphicView(
    modelName: String,
    slotType: EffectSlotType,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val config = getPedalGraphicConfig(modelName, slotType)

    val activeModifier = if (isActive) {
        modifier
    } else {
        modifier.drawWithContent {
            drawContent()
            drawRect(Color.Black.copy(alpha = 0.55f))
        }
    }

    when {
        config.isAmp -> {
            AmpHeadGraphic(config = config, isActive = isActive, modifier = activeModifier)
        }
        config.isCab -> {
            CabGraphic(config = config, isActive = isActive, modifier = activeModifier)
        }
        config.isEq -> {
            EqPedalGraphic(config = config, isActive = isActive, modifier = activeModifier)
        }
        config.isRoundFuzz -> {
            RoundFuzzGraphic(config = config, isActive = isActive, modifier = activeModifier)
        }
        else -> {
            StandardStompboxGraphic(config = config, isActive = isActive, modifier = activeModifier)
        }
    }
}

@Composable
private fun StandardStompboxGraphic(
    config: PedalGraphicConfig,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.verticalGradient(
                    listOf(config.primaryColor, config.secondaryColor)
                )
            )
            .border(
                1.dp,
                if (isActive) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.4f),
                RoundedCornerShape(6.dp)
            )
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Status LED Jewel
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Color(0xFFFF1744) else Color(0xFF37474F))
                    .border(0.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
            )

            // Simulated Mini Knobs
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
            ) {
                val knobs = config.knobCount.coerceIn(1, 4)
                repeat(knobs) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF212121))
                            .border(0.5.dp, Color(0xFFBDBDBD), CircleShape)
                    )
                }
            }

            // Name Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(config.faceplateColor.copy(alpha = 0.85f))
                    .padding(vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = config.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (config.faceplateColor == Color(0xFFFFFFFF) || config.faceplateColor == Color(0xFFFFF9C4) || config.faceplateColor == Color(0xFFECEFF1)) Color.Black else Color.White,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }

            // Stomp switch (Chrome / Metal plunger)
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFFFFF), Color(0xFF757575))
                        )
                    )
                    .border(1.dp, Color(0xFF424242), CircleShape)
            )
        }
    }
}

@Composable
private fun RoundFuzzGraphic(
    config: PedalGraphicConfig,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(config.primaryColor, config.secondaryColor)
                )
            )
            .border(
                1.5.dp,
                if (isActive) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
                CircleShape
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Two Big Eye Knobs
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF101010))
                        .border(0.5.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF101010))
                        .border(0.5.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                )
            }

            // Face Smile Stomp Switch
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White, Color.Gray)
                        )
                    )
                    .border(1.dp, Color.Black, CircleShape)
            )

            Text(
                text = "FUZZ",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontSize = 6.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun AmpHeadGraphic(
    config: PedalGraphicConfig,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                Brush.verticalGradient(
                    listOf(config.primaryColor, config.secondaryColor)
                )
            )
            .border(
                1.dp,
                if (isActive) Color(0xFFC5A059) else Color.Black.copy(alpha = 0.5f),
                RoundedCornerShape(4.dp)
            )
            .padding(3.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Faceplate & Knobs Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(config.faceplateColor)
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Jewel light
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Color(0xFFFF1744) else Color(0xFF5D4037))
                            .border(0.5.dp, Color.White, CircleShape)
                    )

                    // Mini Amp Knobs
                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                        repeat(5) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF212121))
                                    .border(0.5.dp, Color(0xFFBDBDBD), CircleShape)
                            )
                        }
                    }
                }
            }

            // Grill / Baffle area with Amp Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 2.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF1B1B1B))
                    .border(0.5.dp, Color.Black, RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = config.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) Color(0xFFC5A059) else Color(0xFF9E9E9E),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CabGraphic(
    config: PedalGraphicConfig,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(config.primaryColor)
            .border(
                1.dp,
                if (isActive) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.6f),
                RoundedCornerShape(4.dp)
            )
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF1E1E1E))
                .border(0.5.dp, Color.DarkGray, RoundedCornerShape(2.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Speaker Cloth Grill Texture Simulation
            Canvas(modifier = Modifier.fillMaxSize().padding(2.dp)) {
                val step = 6f
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += step
                }
            }

            // Cabinet Center Plate
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 3.dp, vertical = 2.dp)
            ) {
                Text(
                    text = config.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) Color(0xFF81D4FA) else Color.LightGray,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EqPedalGraphic(
    config: PedalGraphicConfig,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.verticalGradient(
                    listOf(config.primaryColor, config.secondaryColor)
                )
            )
            .border(
                1.dp,
                if (isActive) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.5f),
                RoundedCornerShape(6.dp)
            )
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // LED
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Color(0xFFFF1744) else Color.Gray)
            )

            // EQ Sliders simulation
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { idx ->
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(16.dp)
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(if (isActive) Color(0xFF00E676) else Color.LightGray)
                        )
                    }
                }
            }

            // EQ Label
            Text(
                text = config.label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            // Footswitch
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .border(1.dp, Color.Black, CircleShape)
            )
        }
    }
}

@Composable
fun ExpressionPedalGraphic(
    targetName: String,
    pedalValue: Float,
    isEngaged: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF263238), Color(0xFF101418))
                )
            )
            .border(
                1.dp,
                if (isEngaged) Color(0xFF00E676) else Color(0xFF37474F),
                RoundedCornerShape(6.dp)
            )
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Status LED Jewel
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (isEngaged) Color(0xFF00E676) else Color(0xFFFF5252))
                    .border(0.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
            )

            // Rubber Treadle Grooves / Footbed
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 3.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF1E2328))
                    .padding(vertical = 3.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(5) { idx ->
                    val isLit = (pedalValue / 20f) >= (4 - idx)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(if (isLit && isEngaged) Color(0xFF00E676).copy(alpha = 0.8f) else Color(0xFF37474F))
                    )
                }
            }

            // Target Label
            Text(
                text = targetName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontSize = 7.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            // Treadle Pivot Base
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF455A64))
                    .border(0.5.dp, Color.Black, RoundedCornerShape(2.dp))
            )
        }
    }
}

