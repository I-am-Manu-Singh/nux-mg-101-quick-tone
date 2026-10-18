package com.creativeblisslabs.nuxmg101.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Category types for Patches
 */
// Removed PatchCategory enum

/**
 * Types of Effect Slots in NUX MG-101 Signal Chain
 */
enum class EffectSlotType(val displayName: String, val shortCode: String, val saveCode: Int, val ccNo: Int) {
    WAH("Wah / Pitch", "WAH", 0, 0),
    COMP("Compressor", "CMP", 1, 1),
    EFX("Drive / FX", "EFX", 2, 2),
    AMP("Amp Model", "AMP", 3, 3),
    EQ("Equalizer", "EQ", 4, 4),
    GATE("Noise Gate", "NG", 5, 5),
    MOD("Modulation", "MOD", 6, 6),
    DELAY("Delay", "DLY", 7, 7),
    REVERB("Reverb", "RVB", 8, 8),
    CAB("Cabinet / IR", "IR", 9, 9),
    SR("Send / Return", "S/R", 10, 10),
    VOL("Master Volume", "VOL", 11, 11),
    PL("Patch Level", "P.L", 10, 76)
}

/**
 * Dynamic Tabular MIDI & Pedal Model Entry (Room Database Entity)
 */
@Entity(tableName = "midi_mappings")
data class MidiMappingEntry(
    @PrimaryKey
    val id: String, // e.g. "AMP_JAZZ_CLEAN"
    val category: String, // "WAH", "CMP", "EFX", "AMP", "EQ", "GATE", "MOD", "DLY", "RVB", "IR", "S/R", "VOL"
    val subcategory: String,
    val name: String,
    val saveOn: Int,
    val saveOff: Int? = null,
    val ccOn: Int,
    val ccOff: Int? = null,
    val displayOrder: Int = 0
)

data class SlotColorConfig(
    val id: String,
    val saveCode: Int,
    val ccNo: Int,
    val r: Int,
    val g: Int,
    val b: Int
)

/**
 * Amp Models available in NUX MG-101
 */
data class AmpModelInfo(
    val id: String,
    val name: String,
    val category: String,
    val description: String
)

val MG101_AMP_MODELS = listOf(
    AmpModelInfo("jazz_clean", "JAZZ CLEAN", "Clean", "Roland JC-120 solid-state clean amp"),
    AmpModelInfo("deluxe_rvb", "DELUXE RVB", "Clean", "Fender Deluxe Reverb 22W blackface tube amp"),
    AmpModelInfo("bass_mate", "BASS MATE", "Clean", "1959 Tweed Bassman style vintage amp"),
    AmpModelInfo("tweedy", "TWEEDY", "Clean", "Fender Tweed Champ 5W tube amp"),
    AmpModelInfo("twin_rvb", "TWIN RVB", "Clean", "Fender Twin Reverb 100W twin clean amp"),
    AmpModelInfo("hiwire", "HIWIRE", "Clean", "British Hiwatt Custom 100W tube amp"),
    AmpModelInfo("cali_crunch", "CALI CRUNCH", "Crunch", "Mesa Boogie Mark II C+ crunch channel"),
    AmpModelInfo("class_a15", "CLASS A15", "Clean", "Vox AC15 Class-A chimey British amp"),
    AmpModelInfo("class_a30", "CLASS A30", "Crunch", "Vox AC30 Top Boost 30W Class-A amp"),
    AmpModelInfo("plexi_100", "PLEXI 100", "Crunch", "1959 Marshall Super Lead 100W Plexi"),
    AmpModelInfo("plexi_45", "PLEXI 45", "Crunch", "Marshall JTM45 45W vintage plexi head"),
    AmpModelInfo("brit_800", "BRIT 800", "Rock", "Marshall JCM800 high gain British rock amp"),
    AmpModelInfo("1987x_50w", "1987X 50W", "Rock", "Marshall 1987X 50W vintage Plexi head"),
    AmpModelInfo("slo_100", "SLO 100", "Lead", "Soldano Super Lead Overdrive 100W lead amp"),
    AmpModelInfo("fireman_hbe", "FIREMAN HBE", "Lead", "Friedman BE-100 Brown Eye high gain lead"),
    AmpModelInfo("dual_rect", "DUAL RECT", "Lead", "Mesa Dual Rectifier modern high gain amp"),
    AmpModelInfo("die_vh4", "DIE VH4", "Lead", "Diezel VH4 100W 4-channel heavy monster amp"),
    AmpModelInfo("vibro_king", "VIBRO KING", "Clean", "Fender Vibro-King 60W custom amp"),
    AmpModelInfo("budda", "BUDDA", "Crunch", "Budda Twinmaster boutique tube amp"),
    AmpModelInfo("mr_z_38", "MR.Z 38", "Crunch", "Dr Z Maz 38 Senior boutique amp"),
    AmpModelInfo("super_rvb", "SUPER RVB", "Clean", "Fender Super Reverb 4x10 tube amp"),
    AmpModelInfo("brit_blues", "BRIT BLUES", "Crunch", "Marshall Bluesbreaker 1962 combo amp"),
    AmpModelInfo("match_d30", "MATCH D30", "Clean", "Matchless DC-30 boutique Class-A amp"),
    AmpModelInfo("brit_2000", "BRIT 2000", "Rock", "Marshall JCM2000 DSL100 dual super lead"),
    AmpModelInfo("uber_higain", "UBER HIGAIN", "Lead", "Bogner Uberschall high gain armageddon amp")
)

/**
 * Parameter definition for an effect block knob or switch
 */
data class EffectParameter(
    val id: String,
    val name: String,
    val value: Float, // Normalized 0.0f - 1.0f or scaled
    val displayValue: String, // e.g. "420 ms", "40 %", "6.5 kHz"
    val minValue: Float = 0f,
    val maxValue: Float = 100f,
    val unit: String = "%"
)

/**
 * Effect Block data representing a single pedal/slot state
 */
data class EffectBlock(
    val slotType: EffectSlotType,
    val modelName: String,
    val active: Boolean = true,
    val parameters: List<EffectParameter> = emptyList(),
    val subTypeOptions: List<String> = emptyList(),
    val selectedSubType: String = ""
)

/**
 * Database Entity representing a Patch / Preset in NUX MG-101
 */
@Entity(tableName = "patches")
data class Patch(
    @PrimaryKey val id: String, // e.g. "001", "002"
    val slotNumber: String, // e.g. "001", "1A", "5C"
    val name: String, // e.g. "Plexi Drive"
    val customTag: String = "",
    val isFactory: Boolean = false,
    val isFavorite: Boolean = false,
    val ampModelName: String = "BRIT TUBE",
    val cabModelName: String = "4x12 VINTAGE",
    val bpm: Int = 120,
    val rawEffectData: String = "" // Serialized parameters
)

/**
 * Cabinet Impulse Response (IR) Entity
 */
@Entity(tableName = "ir_files")
data class CabinetIR(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val slotIndex: String, // "001", "002"
    val name: String, // e.g. "1960A 4x12 V30"
    val sampleRate: String = "48kHz",
    val lengthMs: String = "500ms",
    val isActiveSlot: Boolean = false,
    val levelDb: Float = 0.0f,
    val highPassHz: Float = 80.0f,
    val lowPassKHz: Float = 6.5f
)

/**
 * Community Cloud Preset Model
 */
data class CloudPreset(
    val id: String,
    val slotIndex: String,
    val title: String,
    val author: String,
    val rating: Float,
    val downloads: Int,
    val category: String,
    val description: String = ""
)

/**
 * Live Device Hardware Status
 */
data class HardwareStatus(
    val deviceName: String = "NUX MG-101",
    val isConnected: Boolean = true,
    val isWiredUsb: Boolean = true,
    val batteryPercent: Int = 84,
    val signalDbm: Int = -45,
    val firmwareVersion: String = "v2.4.1 (Stable)",
    val inputMeterDb: Float = -12.0f,
    val outputMeterDb: Float = -3.0f,
    val isMuted: Boolean = false,
    val currentBpm: Int = 120,
    val outputLevel: Float = 50f,
    val phonesLevel: Float = 50f,
    val dryDiOut: Boolean = false,
    val reampingMode: Boolean = true,
    val usbGainDb: Float = 0f,
    val midiGlobalChannel: String = "CH 1",
    val pcRxTx: Boolean = true,
    val ccRxTx: Boolean = true,
    val screenBrightness: Int = 85,
    val ledRingIntensity: Int = 100,
    val drumEnabled: Boolean = false,
    val drumLevel: Float = 75f,
    val drumBass: Float = 50f,
    val drumMid: Float = 50f,
    val drumTreble: Float = 50f,
    val loopLevel: Float = 80f,
    val looperState: String = "STOP",
    val drumRoutingMono: Boolean = true,
    val auxVol: Float = 50f,
    val uacIn: Float = 50f,
    val uacOut: Float = 50f,
    val usbVendorId: String = "0x1F02 (NUX Audio)",
    val usbProductId: String = "0x0101 (MG-101 Multi-FX)",
    val usbAudioFormat: String = "48000 Hz / 24-bit PCM",
    val usbAlertMessage: String? = "NUX MG-101 USB OTG Device Connected",
    val activePatchName: String = "NOT FETCHED",
    val expEngaged: Boolean = true,
    val expTarget: String = "WAH", // "WAH", "EFX", "AMP", "MOD", "DLY", "RVB", "VOL"
    val expValue: Float = 75f
)
