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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.creativeblisslabs.nuxmg101.data.model.Patch
import com.creativeblisslabs.nuxmg101.data.model.EffectSlotType
import com.creativeblisslabs.nuxmg101.data.model.EffectBlock

data class EffectModelItem(
    val id: String,
    val name: String,
    val type: String,
    val category: String,
    val tags: List<String>,
    val description: String,
    val pedalColor: Color,
    val pedalLabel: String,
    val subLabel: String = ""
)

val MG101_EFFECT_MODELS_LIBRARY = listOf(
    // NR / NG
    EffectModelItem("nr_1", "NR", "Noise Gate", "NG", listOf("THRESHOLD", "GATE"), "Studio grade noise reduction gate.", Color(0xFF546E7A), "NR"),

    // CMP
    EffectModelItem("cmp_1", "RED COMP", "Compressor", "CMP", listOf("SUSTAIN", "ATTACK"), "Classic red dynacomp style analog compressor.", Color(0xFFD32F2F), "COMP", "Red Comp"),
    EffectModelItem("cmp_2", "ROSE COMP", "Compressor", "CMP", listOf("SMOOTH", "BLEND"), "Boutique studio optical compressor pedal.", Color(0xFFB0BEC5), "ROSE", "Rose Comp"),

    // EFX (1-14 matching user exact sequence)
    EffectModelItem("efx_1", "DIST+", "Drive", "EFX", listOf("OUTPUT", "DISTORTION"), "Classic yellow distortion plus overdrive pedal.", Color(0xFFFBC02D), "DIST +"),
    EffectModelItem("efx_2", "RC BST", "Booster", "EFX", listOf("GAIN", "BASS", "TREBLE"), "Clean booster pedal for transparent signal push.", Color(0xFFCFD8DC), "RC BST"),
    EffectModelItem("efx_3", "AC BST", "Booster", "EFX", listOf("GAIN", "TONE"), "Tube style overdrive boost pedal.", Color(0xFFFBC02D), "AC BST"),
    EffectModelItem("efx_4", "DIST UNO", "Distortion", "EFX", listOf("DRIVE", "TONE"), "Classic orange distortion box.", Color(0xFFEF6C00), "DIST ONE"),
    EffectModelItem("efx_5", "T SCREAMER", "Overdrive", "EFX", listOf("OVERDRIVE", "TONE"), "Legendary green tube screamer mid-boost overdrive.", Color(0xFF2E7D32), "TS"),
    EffectModelItem("efx_6", "BLUES DRIVE", "Overdrive", "EFX", listOf("LEVEL", "GAIN"), "Classic blue tube blues overdrive pedal.", Color(0xFF1565C0), "BLUES DRV"),
    EffectModelItem("efx_7", "MORNING DRIVE", "Overdrive", "EFX", listOf("GAIN", "VOLUME"), "Transparent amp-in-a-box morning overdrive.", Color(0xFFFFF9C4), "MORNING"),
    EffectModelItem("efx_8", "MODERN DIST", "Distortion", "EFX", listOf("EDGE", "PUNCH"), "Heavy modern high-gain distortion pedal with RAT tone.", Color(0xFF212121), "KAT"),
    EffectModelItem("efx_9", "RED DIST", "Overdrive", "EFX", listOf("DIRT", "TONE"), "Boutique modified red dirt tube overdrive pedal.", Color(0xFFC62828), "RED DIST"),
    EffectModelItem("efx_10", "CRUNCH", "Distortion", "EFX", listOf("CRUNCH", "TONE"), "British style crunch box overdrive pedal.", Color(0xFFE53935), "CRUNCH"),
    EffectModelItem("efx_11", "MUF DIST", "Big Fuzz", "EFX", listOf("SUSTAIN", "TONE"), "Massive big muff style fuzz distortion.", Color(0xFFCFD8DC), "MUFF"),
    EffectModelItem("efx_12", "KATANA", "Clean Boost", "EFX", listOf("VOLUME", "BOOST"), "Pure clean boost for lead channel solos.", Color(0xFFECEFF1), "KATANA"),
    EffectModelItem("efx_13", "RED FUZZ", "Fuzz", "EFX", listOf("FUZZ", "LEVEL"), "Vintage germanium round red fuzz pedal.", Color(0xFFC62828), "FUZZ"),
    EffectModelItem("efx_14", "TOUCH WAH", "Envelope Filter", "EFX", listOf("SENSE", "PEAK"), "Dynamic envelope touch wah filter pedal.", Color(0xFF7E57C2), "T WAH"),

    // AMP (1-25 matching user exact sequence)
    EffectModelItem("amp_1", "JAZZ CLEAN", "Amplifier", "AMP", listOf("CLEAN", "CHORUS"), "Roland JC-120 solid-state clean amp.", Color(0xFF37474F), "JAZZ CLEAN"),
    EffectModelItem("amp_2", "DELUXE RVB", "Amplifier", "AMP", listOf("TUBE", "REVERB"), "Fender Deluxe Reverb 22W blackface tube amp.", Color(0xFF263238), "DELUXE RVB"),
    EffectModelItem("amp_3", "BASS MATE", "Amplifier", "AMP", listOf("TWEED", "WARM"), "1959 Tweed Bassman style vintage amp.", Color(0xFF8D6E63), "BASS MATE"),
    EffectModelItem("amp_4", "TWEEDY", "Amplifier", "AMP", listOf("TWEED", "DRIVE"), "Classic Fender Tweed Champ 5W tube amp.", Color(0xFFA1887F), "TWEEDY"),
    EffectModelItem("amp_5", "TWIN RVB", "Amplifier", "AMP", listOf("TWIN", "CLEAN"), "Fender Twin Reverb 100W twin clean amp.", Color(0xFF37474F), "TWIN RVB"),
    EffectModelItem("amp_6", "HIWIRE", "Amplifier", "AMP", listOf("HIWATT", "HEADROOM"), "Classic British Hiwatt Custom 100W amp.", Color(0xFF102027), "HIWIRE"),
    EffectModelItem("amp_7", "CALI CRUNCH", "Amplifier", "AMP", listOf("CALI", "LEAD"), "Mesa Boogie Mark II C+ crunch channel.", Color(0xFF4E342E), "CALI CRUNCH"),
    EffectModelItem("amp_8", "CLASS A15", "Amplifier", "AMP", listOf("VOX", "CHIME"), "Vox AC15 Class-A chimey British amp.", Color(0xFF3E2723), "CLASS A15"),
    EffectModelItem("amp_9", "CLASS A30", "Amplifier", "AMP", listOf("VOX", "TOP BOOST"), "Vox AC30 Top Boost 30W Class-A amp.", Color(0xFF4E342E), "CLASS A30"),
    EffectModelItem("amp_10", "PLEXI 100", "Amplifier", "AMP", listOf("MARSHALL", "100W"), "1959 Marshall Super Lead 100W Plexi.", Color(0xFF212121), "PLEXI 100"),
    EffectModelItem("amp_11", "PLEXI 45", "Amplifier", "AMP", listOf("MARSHALL", "JTM45"), "Marshall JTM45 45W vintage plexi head.", Color(0xFF424242), "PLEXI 45"),
    EffectModelItem("amp_12", "BRIT 800", "Amplifier", "AMP", listOf("JCM800", "ROCK"), "Marshall JCM800 high gain British amp.", Color(0xFF212121), "BRIT 800"),
    EffectModelItem("amp_13", "1987X 50W", "Amplifier", "AMP", listOf("MARSHALL", "50W"), "Marshall 1987X 50W vintage Plexi head.", Color(0xFF303030), "1987X 50W"),
    EffectModelItem("amp_14", "SLO 100", "Amplifier", "AMP", listOf("SOLDANO", "LEAD"), "Soldano Super Lead Overdrive 100W lead amp.", Color(0xFF1B5E20), "SLO 100"),
    EffectModelItem("amp_15", "FIREMAN HBE", "Amplifier", "AMP", listOf("FRIEDMAN", "BROWN"), "Friedman BE-100 Brown Eye high gain lead.", Color(0xFF3E2723), "FIREMAN HBE"),
    EffectModelItem("amp_16", "DUAL RECT", "Amplifier", "AMP", listOf("MESA", "HIGH GAIN"), "Mesa Dual Rectifier modern high gain amp.", Color(0xFF212121), "DUAL RECT"),
    EffectModelItem("amp_17", "DIE VH4", "Amplifier", "AMP", listOf("DIEZEL", "HEAVY"), "Diezel VH4 100W 4-channel heavy monster amp.", Color(0xFF263238), "DIE VH4"),
    EffectModelItem("amp_18", "VIBRO KING", "Amplifier", "AMP", listOf("FENDER", "VIBRO"), "Fender Vibro-King 60W custom amp.", Color(0xFF5D4037), "VIBRO KING"),
    EffectModelItem("amp_19", "BUDDA", "Amplifier", "AMP", listOf("BOUTIQUE", "DRIVE"), "Budda Twinmaster boutique tube amp.", Color(0xFF8D6E63), "BUDDA"),
    EffectModelItem("amp_20", "MR.Z 38", "Amplifier", "AMP", listOf("DR Z", "38W"), "Dr Z Maz 38 Senior boutique amp.", Color(0xFF424242), "MR.Z 38"),
    EffectModelItem("amp_21", "SUPER RVB", "Amplifier", "AMP", listOf("SUPER", "4X10"), "Fender Super Reverb 4x10 tube amp.", Color(0xFF37474F), "SUPER RVB"),
    EffectModelItem("amp_22", "BRIT BLUES", "Amplifier", "AMP", listOf("BLUESBREAKER", "TUBE"), "Marshall Bluesbreaker 1962 combo amp.", Color(0xFF263238), "BRIT BLUES"),
    EffectModelItem("amp_23", "MATCH D30", "Amplifier", "AMP", listOf("MATCHLESS", "DC30"), "Matchless DC-30 boutique Class-A amp.", Color(0xFF3E2723), "MATCH D30"),
    EffectModelItem("amp_24", "BRIT 2000", "Amplifier", "AMP", listOf("JCM2000", "DSL"), "Marshall JCM2000 DSL100 dual super lead amp.", Color(0xFF212121), "BRIT 2000"),
    EffectModelItem("amp_25", "UBER HIGAIN", "Amplifier", "AMP", listOf("BOGNER", "UBERSCHALL"), "Bogner Uberschall high gain armageddon amp.", Color(0xFF1B0000), "UBER HIGAIN"),

    // IR (1-25 + User IRs)
    EffectModelItem("ir_1", "JAZZ 212", "Cabinet IR", "IR", listOf("2X12", "JC120"), "Roland JC120 2x12 cabinet.", Color(0xFF37474F), "JAZZ 212"),
    EffectModelItem("ir_2", "BLACK 112", "Cabinet IR", "IR", listOf("1X12", "BLACKFACE"), "Fender Deluxe Reverb 1x12 cabinet.", Color(0xFF263238), "BLACK 112"),
    EffectModelItem("ir_3", "TWEED 410", "Cabinet IR", "IR", listOf("4X10", "TWEED"), "Fender Bassman 4x10 tweed cabinet.", Color(0xFF8D6E63), "TWEED 410"),
    EffectModelItem("ir_4", "TWEED 112", "Cabinet IR", "IR", listOf("1X12", "TWEED"), "Fender Tweed Deluxe 1x12 cabinet.", Color(0xFFA1887F), "TWEED 112"),
    EffectModelItem("ir_5", "TWIN 212", "Cabinet IR", "IR", listOf("2X12", "JENSEN"), "Fender Twin Reverb 2x12 cabinet.", Color(0xFF37474F), "TWIN 212"),
    EffectModelItem("ir_6", "CUSTOM 412", "Cabinet IR", "IR", listOf("4X12", "CUSTOM"), "Custom boutique 4x12 cabinet.", Color(0xFF212121), "CUSTOM 412"),
    EffectModelItem("ir_7", "CALI 112", "Cabinet IR", "IR", listOf("1X12", "EVM"), "Mesa Boogie 1x12 EVM cabinet.", Color(0xFF4E342E), "CALI 112"),
    EffectModelItem("ir_8", "BLUE 112", "Cabinet IR", "IR", listOf("1X12", "ALNICO"), "Vox AC15 1x12 Alnico Blue cabinet.", Color(0xFF1565C0), "BLUE 112"),
    EffectModelItem("ir_9", "BLUE 212", "Cabinet IR", "IR", listOf("2X12", "ALNICO"), "Vox AC30 2x12 Alnico Blue cabinet.", Color(0xFF0D47A1), "BLUE 212"),
    EffectModelItem("ir_10", "GREEN 412", "Cabinet IR", "IR", listOf("4X12", "GREENBACK"), "Marshall 1960AX 4x12 Greenback.", Color(0xFF2E7D32), "GREEN 412"),
    EffectModelItem("ir_11", "G12L 412", "Cabinet IR", "IR", listOf("4X12", "G12L"), "Classic 4x12 G12L cabinet.", Color(0xFF424242), "G12L 412"),
    EffectModelItem("ir_12", "T75 412", "Cabinet IR", "IR", listOf("4X12", "G12T-75"), "Marshall 1960A 4x12 Celestion G12T-75.", Color(0xFF212121), "T75 412"),
    EffectModelItem("ir_13", "V30 412", "Cabinet IR", "IR", listOf("4X12", "V30"), "Mesa Rectifier 4x12 Vintage 30.", Color(0xFF37474F), "V30 412"),
    EffectModelItem("ir_14", "SLO 412", "Cabinet IR", "IR", listOf("4X12", "SOLDANO"), "Soldano 4x12 custom heavy cabinet.", Color(0xFF1B5E20), "SLO 412"),
    EffectModelItem("ir_15", "HBE 412", "Cabinet IR", "IR", listOf("4X12", "FRIEDMAN"), "Friedman 4x12 V30/Greenback blend.", Color(0xFF3E2723), "HBE 412"),
    EffectModelItem("ir_16", "RECT 412", "Cabinet IR", "IR", listOf("4X12", "MESA"), "Mesa Boogie 4x12 OS Slant Recto cabinet.", Color(0xFF212121), "RECT 412"),
    EffectModelItem("ir_17", "BOTM 412", "Cabinet IR", "IR", listOf("4X12", "HEAVY"), "Bottom heavy modern metal 4x12 cabinet.", Color(0xFF102027), "BOTM 412"),
    EffectModelItem("ir_18", "VIBRO 212", "Cabinet IR", "IR", listOf("2X12", "VIBRO"), "Fender Vibro 2x12 cabinet.", Color(0xFF4E342E), "VIBRO 212"),
    EffectModelItem("ir_19", "EVL 112", "Cabinet IR", "IR", listOf("1X12", "EV"), "Electro-Voice 1x12 cabinet.", Color(0xFF37474F), "EVL 112"),
    EffectModelItem("ir_20", "BUTQ 212", "Cabinet IR", "IR", listOf("2X12", "BOUTIQUE"), "Boutique 2x12 open back cabinet.", Color(0xFF8D6E63), "BUTQ 212"),
    EffectModelItem("ir_21", "SUPER 212", "Cabinet IR", "IR", listOf("2X12", "SUPER"), "Fender Super 2x12 cabinet.", Color(0xFF37474F), "SUPER 212"),
    EffectModelItem("ir_22", "BRIT 212", "Cabinet IR", "IR", listOf("2X12", "BRITISH"), "British 2x12 custom cabinet.", Color(0xFF263238), "BRIT 212"),
    EffectModelItem("ir_23", "DC 212", "Cabinet IR", "IR", listOf("2X12", "MATCHLESS"), "Matchless DC30 2x12 cabinet.", Color(0xFF3E2723), "DC 212"),
    EffectModelItem("ir_24", "G12M 412", "Cabinet IR", "IR", listOf("4X12", "G12M"), "Marshall 4x12 G12M Greenback.", Color(0xFF2E7D32), "G12M 412"),
    EffectModelItem("ir_25", "UBER 412", "Cabinet IR", "IR", listOf("4X12", "UBERSCHALL"), "Bogner Uberschall 4x12 cabinet.", Color(0xFF1B0000), "UBER 412"),

    // EQ
    EffectModelItem("eq_1", "6-EQ", "Graphic Equalizer", "EQ", listOf("6-BAND", "GRAPHIC"), "6-band guitar graphic equalizer.", Color(0xFF37474F), "6-EQ"),
    EffectModelItem("eq_2", "10-EQ", "Graphic Equalizer", "EQ", listOf("10-BAND", "PRECISION"), "10-band precision graphic equalizer.", Color(0xFF212121), "10-EQ"),

    // MOD (1-14 matching user exact sequence)
    EffectModelItem("mod_1", "CE-1", "Chorus", "MOD", listOf("CE-1", "ANALOG"), "Boss CE-1 stereo chorus.", Color(0xFF1565C0), "CE-1"),
    EffectModelItem("mod_2", "CLASSIC CHORUS", "Chorus", "MOD", listOf("CLASSIC", "WARM"), "Classic analog chorus pedal.", Color(0xFF00ACC1), "CHORUS"),
    EffectModelItem("mod_3", "ST.CHORUS", "Stereo Chorus", "MOD", listOf("STEREO", "LUSH"), "Lush stereo chorus pedal.", Color(0xFF0288D1), "ST.CHORUS"),
    EffectModelItem("mod_4", "VIBRATO", "Vibrato", "MOD", listOf("PITCH", "PULSE"), "Pitch vibrato modulation pedal.", Color(0xFF0277BD), "VIBRATO"),
    EffectModelItem("mod_5", "DETUNE", "Detuner", "MOD", listOf("MICRO-PITCH", "THICK"), "Micro-pitch detuner pedal.", Color(0xFF00796B), "DETUNE"),
    EffectModelItem("mod_6", "FLANGE", "Flanger", "MOD", listOf("JET", "SWEEP"), "Analog jet flanger with feedback control.", Color(0xFF6A1B9A), "FLANGE"),
    EffectModelItem("mod_7", "PH 90", "Phaser", "MOD", listOf("PHASE 90", "SCRIPT"), "Phase 90 script phaser pedal.", Color(0xFFEF6C00), "PH 90"),
    EffectModelItem("mod_8", "PHASE 100", "Phaser", "MOD", listOf("4-POSITION", "SWEEP"), "Phase 100 multi-stage phaser pedal.", Color(0xFFE65100), "PHASE 100"),
    EffectModelItem("mod_9", "CHORUS+", "Chorus", "MOD", listOf("PITCH", "MOD"), "TC Chorus+ pitch modulation pedal.", Color(0xFF0097A7), "CHORUS+"),
    EffectModelItem("mod_10", "U-VIBE", "Uni-Vibe", "MOD", listOf("ROTARY", "PSYCH"), "Shin-ei Uni-Vibe rotary chorus pedal.", Color(0xFF616161), "U-VIBE"),
    EffectModelItem("mod_11", "TREM", "Tremolo", "MOD", listOf("OPTICAL", "PULSE"), "Optical tremolo volume pulse pedal.", Color(0xFF00695C), "TREM"),
    EffectModelItem("mod_12", "RTY SPK", "Rotary Speaker", "MOD", listOf("LESLIE", "DOPPLER"), "Leslie rotary speaker doppler simulation.", Color(0xFF4E342E), "RTY SPK"),
    EffectModelItem("mod_13", "HARMONY", "Pitch Shifter", "MOD", listOf("OCTAVE", "HARMONIZER"), "Smart harmony pitch shifter pedal.", Color(0xFFC62828), "HARMONY"),
    EffectModelItem("mod_14", "SCH-1", "Chorus", "MOD", listOf("VINTAGE", "CHORUS"), "Arion SCH-1 vintage analog chorus pedal.", Color(0xFF00838F), "SCH-1"),

    // DLY (1-7 matching user exact sequence)
    EffectModelItem("dly_1", "ANALOG DELAY", "Delay", "DLY", listOf("BBD", "WARM"), "Bucket brigade analog delay with warm decay.", Color(0xFFC2185B), "ANALOG DLY"),
    EffectModelItem("dly_2", "DIGI DELAY", "Digital Delay", "DLY", listOf("CRYSTAL", "CLEAN"), "Crystal clean 2000ms digital delay pedal.", Color(0xFF0288D1), "DIGI DELAY"),
    EffectModelItem("dly_3", "MODULATE", "Modulated Delay", "DLY", listOf("CHORUS", "DELAY"), "Chorus modulated delay repeats pedal.", Color(0xFF0097A7), "MODULATE"),
    EffectModelItem("dly_4", "TAPE ECHO", "Tape Echo", "DLY", listOf("FLUTTER", "SATURATION"), "Space echo tape delay with wow and flutter.", Color(0xFF37474F), "TAPE ECHO"),
    EffectModelItem("dly_5", "REVERSE", "Reverse Delay", "DLY", listOf("BACKWARDS", "PSYCH"), "Backwards playing reverse echo delay pedal.", Color(0xFF4A148C), "REVERSE"),
    EffectModelItem("dly_6", "PAN", "Panning Delay", "DLY", listOf("PING PONG", "STEREO"), "Ping-pong stereo panning delay pedal.", Color(0xFF00838F), "PAN"),
    EffectModelItem("dly_7", "PHI DELAY", "Golden Delay", "DLY", listOf("GOLDEN RATIO", "REPEAT"), "Golden ratio rhythm multi-tap delay.", Color(0xFF263238), "PHI DELAY"),

    // RVB (1-5 matching user exact sequence)
    EffectModelItem("rvb_1", "ROOM", "Reverb", "RVB", listOf("AMBIANCE", "ROOM"), "Natural studio room reverb acoustics.", Color(0xFF37474F), "ROOM"),
    EffectModelItem("rvb_2", "HALL", "Reverb", "RVB", listOf("CONCERT", "HALL"), "Concert hall expansive reverb acoustics.", Color(0xFF212121), "HALL"),
    EffectModelItem("rvb_3", "PLATE", "Reverb", "RVB", listOf("EMT", "PLATE"), "EMT 140 mechanical metal plate reverb.", Color(0xFFB0BEC5), "PLATE"),
    EffectModelItem("rvb_4", "SPRING", "Reverb", "RVB", listOf("TANK", "SPRING"), "Classic Fender amp spring tank reverb.", Color(0xFFD84315), "SPRING"),
    EffectModelItem("rvb_5", "SHIMMER", "Reverb", "RVB", listOf("OCTAVE", "ETHEREAL"), "Ethereal shimmer reverb with octave up trails.", Color(0xFF4A148C), "SHIMMER")
)

@Composable
fun LibraryScreen(
    patches: List<Patch>,
    activePatchId: String,
    effectChain: List<EffectBlock>,
    onSelectPatch: (Patch) -> Unit,
    onToggleFavorite: (Patch) -> Unit,
    onRenamePatch: (Patch, String, String) -> Unit = { _, _, _ -> },
    onImportPatch: (String) -> Unit = {},
    onRestoreBackup: (String) -> Unit = {},
    onLoadEffect: (EffectSlotType, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var libraryTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedEffectCat by remember { mutableStateOf("All") }

    var patchToRename by remember { mutableStateOf<Patch?>(null) }
    var renameName by remember { mutableStateOf("") }
    var renameTag by remember { mutableStateOf("") }

    var isImportSingleOpen by remember { mutableStateOf(false) }
    var isExportAllOpen by remember { mutableStateOf(false) }
    var isImportAllOpen by remember { mutableStateOf(false) }
    var singleExportPatch by remember { mutableStateOf<Patch?>(null) }
    var actionStatusMessage by remember { mutableStateOf<String?>(null) }

    var importJsonText by remember { mutableStateOf("") }
    var importAllJsonText by remember { mutableStateOf("") }

    val clipboardManager = LocalClipboardManager.current

    // Export Single Patch Dialog
    singleExportPatch?.let { patch ->
        val jsonPayload = remember(patch) {
            org.json.JSONObject().apply {
                put("device", "NUX MG-101")
                put("format", "mg101patch")
                put("id", patch.id)
                put("slotNumber", patch.slotNumber)
                put("name", patch.name)
                put("customTag", patch.customTag)
                put("ampModelName", patch.ampModelName)
                put("cabModelName", patch.cabModelName)
                put("bpm", patch.bpm)
                put("rawEffectData", patch.rawEffectData)
            }.toString(2)
        }

        AlertDialog(
            onDismissRequest = { singleExportPatch = null },
            title = { Text("Export Patch (${patch.slotNumber} - ${patch.name})", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Patch JSON payload (.mg101patch):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = jsonPayload,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(jsonPayload))
                        actionStatusMessage = "Patch ${patch.name} copied to clipboard!"
                        singleExportPatch = null
                    }
                ) {
                    Text("Copy & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { singleExportPatch = null }) { Text("Close") }
            }
        )
    }

    // Import Single Patch Dialog
    if (isImportSingleOpen) {
        AlertDialog(
            onDismissRequest = { isImportSingleOpen = false },
            title = { Text("Import Single Patch (.mg101patch)", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste .mg101patch JSON content below:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("{\n  \"name\": \"Custom Tone\",\n  ...\n}", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            onImportPatch(importJsonText)
                            actionStatusMessage = "Imported patch successfully!"
                            importJsonText = ""
                        }
                        isImportSingleOpen = false
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { isImportSingleOpen = false }) { Text("Cancel") }
            }
        )
    }

    // Export All Backup Dialog
    if (isExportAllOpen) {
        val allBackupJson = remember(patches) {
            org.json.JSONObject().apply {
                put("device", "NUX MG-101")
                put("version", "1.0")
                put("type", "MG101AllPatch")
                val arr = org.json.JSONArray()
                patches.forEach { p ->
                    val pObj = org.json.JSONObject().apply {
                        put("id", p.id)
                        put("slotNumber", p.slotNumber)
                        put("name", p.name)
                        put("customTag", p.customTag)
                        put("isFactory", p.isFactory)
                        put("isFavorite", p.isFavorite)
                        put("ampModelName", p.ampModelName)
                        put("cabModelName", p.cabModelName)
                        put("bpm", p.bpm)
                    }
                    arr.put(pObj)
                }
                put("patches", arr)
            }.toString(2)
        }

        AlertDialog(
            onDismissRequest = { isExportAllOpen = false },
            title = { Text("Backup All Patches (${patches.size} items)", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Full backup payload (MG101AllPatch.mg101patch):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = allBackupJson,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(allBackupJson))
                        actionStatusMessage = "All ${patches.size} patches backup copied to clipboard!"
                        isExportAllOpen = false
                    }
                ) {
                    Text("Copy Backup")
                }
            },
            dismissButton = {
                TextButton(onClick = { isExportAllOpen = false }) { Text("Close") }
            }
        )
    }

    // Restore Backup Dialog
    if (isImportAllOpen) {
        AlertDialog(
            onDismissRequest = { isImportAllOpen = false },
            title = { Text("Restore All Patches Backup", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste MG101AllPatch backup JSON content:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = importAllJsonText,
                        onValueChange = { importAllJsonText = it },
                        placeholder = { Text("{\n  \"type\": \"MG101AllPatch\",\n  ...\n}", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importAllJsonText.isNotBlank()) {
                            onRestoreBackup(importAllJsonText)
                            actionStatusMessage = "Restored all patches from backup!"
                            importAllJsonText = ""
                        }
                        isImportAllOpen = false
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { isImportAllOpen = false }) { Text("Cancel") }
            }
        )
    }

    if (patchToRename != null) {
        AlertDialog(
            onDismissRequest = { patchToRename = null },
            title = { Text("Edit Patch Info") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = renameName,
                        onValueChange = { renameName = it },
                        label = { Text("Patch Name") }
                    )
                    OutlinedTextField(
                        value = renameTag,
                        onValueChange = { renameTag = it },
                        label = { Text("Custom Tag") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        patchToRename?.let { patch ->
                            onRenamePatch(patch, renameName, renameTag)
                        }
                        patchToRename = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { patchToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dynamic category chips based strictly on user-saved custom tags from patches
    val dynamicCategories = remember(patches) {
        listOf("ALL") + patches.map { it.customTag.uppercase() }.filter { it.isNotBlank() }.distinct()
    }

    val filteredPatches = remember(patches, searchQuery, selectedCategory) {
        patches.filter { patch ->
            val matchesCategory = selectedCategory == "ALL" || patch.customTag.uppercase() == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    patch.name.contains(searchQuery, ignoreCase = true) ||
                    patch.slotNumber.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    val factoryPatches = filteredPatches.filter { it.isFactory }
    val userPatches = filteredPatches.filter { !it.isFactory }

    val filteredEffects = remember(searchQuery, selectedEffectCat) {
        MG101_EFFECT_MODELS_LIBRARY.filter { item ->
            val matchesCategory = selectedEffectCat == "All" || item.category.uppercase() == selectedEffectCat.uppercase()
            val matchesQuery = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.type.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header & Search Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
         
    Text(
                text = "Library",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                // Search Toggle Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { isSearchActive = !isSearchActive },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Search Bar Input
        AnimatedVisibility(visible = isSearchActive) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (libraryTab == 0) "Search presets..." else "Search amps, delays, reverbs...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        }

        // Main Tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (libraryTab == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = if (libraryTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { libraryTab = 0 }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PRESETS",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (libraryTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (libraryTab == 1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = if (libraryTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { libraryTab = 1 }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EFFECTS & AMPS",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (libraryTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (libraryTab == 0) {
            // Dynamic category chips based on user custom tags
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
            ) {
                items(dynamicCategories) { cat ->
                    val isSel = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Actions Bar: Import, Export All Backup, Restore All
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRESETS (${filteredPatches.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { isImportSingleOpen = true },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Filled.ArrowUpward, contentDescription = "Import", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { isExportAllOpen = true },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Filled.ArrowDownward, contentDescription = "Backup", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Backup All", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { isImportAllOpen = true },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Restore", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Two-Column Layout (Left: Factory, Right: User)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Column: Factory Presets
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = "FACTORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(factoryPatches, key = { it.id }) { patch ->
                            val isActive = patch.id == activePatchId
                            PatchRowItem(
                                patch = patch,
                                isActive = isActive,
                                onSelect = { onSelectPatch(patch) },
                                onToggleFavorite = { onToggleFavorite(patch) },
                                onEditPatch = {
                                    renameName = patch.name
                                    renameTag = patch.customTag
                                    patchToRename = patch
                                },
                                onExportPatch = {
                                    singleExportPatch = patch
                                }
                            )
                        }
                    }
                }

                // Right Column: User Presets
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = "USER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(userPatches, key = { it.id }) { patch ->
                            val isActive = patch.id == activePatchId
                            PatchRowItem(
                                patch = patch,
                                isActive = isActive,
                                onSelect = { onSelectPatch(patch) },
                                onToggleFavorite = { onToggleFavorite(patch) },
                                onEditPatch = {
                                    renameName = patch.name
                                    renameTag = patch.customTag
                                    patchToRename = patch
                                },
                                onExportPatch = {
                                    singleExportPatch = patch
                                }
                            )
                        }
                    }
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
            ) {
                val effectCategories = listOf("All", "NG", "CMP", "EFX", "AMP", "IR", "MIC", "EQ", "MOD", "DLY", "RVB")
                items(effectCategories) { cat ->
                    val isSel = selectedEffectCat == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedEffectCat = cat }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(filteredEffects, key = { it.id }) { model ->
                    val slotType = when (model.category.uppercase()) {
                        "NG" -> EffectSlotType.GATE
                        "CMP" -> EffectSlotType.COMP
                        "EFX" -> EffectSlotType.EFX
                        "AMP" -> EffectSlotType.AMP
                        "IR" -> EffectSlotType.CAB
                        "EQ" -> EffectSlotType.EQ
                        "MOD" -> EffectSlotType.MOD
                        "DLY" -> EffectSlotType.DELAY
                        "RVB" -> EffectSlotType.REVERB
                        else -> EffectSlotType.EFX
                    }
                    val isLoaded = effectChain.any { it.slotType == slotType && it.modelName == model.name }

                    EffectModelCard(
                        model = model,
                        isLoaded = isLoaded,
                        onSelectEffect = { selectedModel: EffectModelItem ->
                            onLoadEffect(slotType, selectedModel.name)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PatchRowItem(
    patch: Patch,
    isActive: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditPatch: () -> Unit,
    onExportPatch: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = patch.slotNumber,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patch.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (patch.customTag.isNotBlank()) {
                    Text(
                        text = patch.customTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onExportPatch, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.ArrowDownward, contentDescription = "Export Patch", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onEditPatch, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Patch", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = if (patch.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (patch.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EffectModelCard(
    model: EffectModelItem,
    isLoaded: Boolean,
    onSelectEffect: (EffectModelItem) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isLoaded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                             else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isLoaded) 2.dp else 1.dp,
            color = if (isLoaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(84.dp)
                    .height(96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(model.pedalColor)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(6.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color.White)
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color.White.copy(alpha = 0.8f))
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color.White.copy(alpha = 0.8f))
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = model.pedalLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.LightGray)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Modifier.let { Arrangement.spacedBy(4.dp) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    model.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = model.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { onSelectEffect(model) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLoaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (isLoaded) "LOADED" else "LOAD",
                    color = if (isLoaded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}