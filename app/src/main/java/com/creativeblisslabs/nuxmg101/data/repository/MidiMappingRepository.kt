package com.creativeblisslabs.nuxmg101.data.repository

import com.creativeblisslabs.nuxmg101.data.db.MidiMappingDao
import com.creativeblisslabs.nuxmg101.data.model.MidiMappingEntry
import com.creativeblisslabs.nuxmg101.data.model.SlotColorConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MidiMappingRepository(private val midiMappingDao: MidiMappingDao) {

    val allMappings: Flow<List<MidiMappingEntry>> = midiMappingDao.getAllMappings()

    fun getMappingsByCategory(category: String): Flow<List<MidiMappingEntry>> =
        midiMappingDao.getMappingsByCategory(category)

    suspend fun getMappingByName(category: String, name: String): MidiMappingEntry? =
        midiMappingDao.getMappingByName(category, name)

    suspend fun updateMapping(entry: MidiMappingEntry) = withContext(Dispatchers.IO) {
        midiMappingDao.updateMapping(entry)
    }

    suspend fun insertMapping(entry: MidiMappingEntry) = withContext(Dispatchers.IO) {
        midiMappingDao.insertMapping(entry)
    }

    suspend fun deleteMapping(id: String) = withContext(Dispatchers.IO) {
        midiMappingDao.deleteMapping(id)
    }

    suspend fun reorderMappings(category: String, movedItem: MidiMappingEntry, moveUp: Boolean) = withContext(Dispatchers.IO) {
        val currentList = midiMappingDao.getMappingsByCategory(category).first().toMutableList()
        val index = currentList.indexOfFirst { it.id == movedItem.id }
        if (index == -1) return@withContext

        val targetIndex = if (moveUp) index - 1 else index + 1
        if (targetIndex in 0 until currentList.size) {
            val temp = currentList[index]
            currentList[index] = currentList[targetIndex]
            currentList[targetIndex] = temp

            val updatedList = currentList.mapIndexed { idx, item ->
                item.copy(displayOrder = idx)
            }
            midiMappingDao.insertMappings(updatedList)
        }
    }

    suspend fun resetToDefaultData() = withContext(Dispatchers.IO) {
        midiMappingDao.deleteAllMappings()
        midiMappingDao.insertMappings(DEFAULT_MIDI_ENTRIES)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val existing = midiMappingDao.getAllMappings().first()
        if (existing.isEmpty()) {
            midiMappingDao.insertMappings(DEFAULT_MIDI_ENTRIES)
        }
    }

    suspend fun exportToCsv(): String = withContext(Dispatchers.IO) {
        val list = midiMappingDao.getAllMappings().first()
        val sb = StringBuilder()
        sb.append("Category,Subcategory,Name,Save On,Save Off,CC On,CC Off,DisplayOrder\n")
        list.forEach { entry ->
            sb.append("${entry.category},${entry.subcategory},\"${entry.name}\",${entry.saveOn},${entry.saveOff ?: ""},${entry.ccOn},${entry.ccOff ?: ""},${entry.displayOrder}\n")
        }
        sb.toString()
    }

    suspend fun importFromCsv(csvContent: String): Int = withContext(Dispatchers.IO) {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return@withContext 0

        val parsedEntries = mutableListOf<MidiMappingEntry>()
        var startIdx = 0
        if (lines[0].contains("Category", ignoreCase = true)) {
            startIdx = 1
        }

        for (i in startIdx until lines.size) {
            val line = lines[i].trim()
            if (line.isBlank()) continue
            val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
            if (parts.size >= 6) {
                val cat = parts[0]
                val subcat = parts[1]
                val name = parts[2]
                val saveOn = parts[3].toIntOrNull() ?: 0
                val saveOff = parts.getOrNull(4)?.toIntOrNull()
                val ccOn = parts.getOrNull(5)?.toIntOrNull() ?: 1
                val ccOff = parts.getOrNull(6)?.toIntOrNull()
                val order = parts.getOrNull(7)?.toIntOrNull() ?: i

                val id = "${cat}_${name.replace(" ", "_").uppercase()}"
                parsedEntries.add(
                    MidiMappingEntry(
                        id = id,
                        category = cat,
                        subcategory = subcat,
                        name = name,
                        saveOn = saveOn,
                        saveOff = saveOff,
                        ccOn = ccOn,
                        ccOff = ccOff,
                        displayOrder = order
                    )
                )
            }
        }

        if (parsedEntries.isNotEmpty()) {
            midiMappingDao.insertMappings(parsedEntries)
        }
        parsedEntries.size
    }

    companion object {
        val SLOT_COLORS = listOf(
            SlotColorConfig("WAH", 0, 0, 221, 111, 181),
            SlotColorConfig("CMP", 1, 1, 253, 255, 31),
            SlotColorConfig("EFX", 2, 2, 255, 110, 64),
            SlotColorConfig("AMP", 3, 3, 247, 8, 8),
            SlotColorConfig("EQ", 4, 4, 231, 231, 231),
            SlotColorConfig("GATE", 5, 5, 139, 195, 74),
            SlotColorConfig("MOD", 6, 6, 18, 77, 255),
            SlotColorConfig("DLY", 7, 7, 0, 188, 212),
            SlotColorConfig("RVB", 8, 8, 224, 64, 251),
            SlotColorConfig("IR", 9, 9, 83, 109, 255),
            SlotColorConfig("S/R", 10, 10, 76, 175, 80),
            SlotColorConfig("VOL", 11, 11, 255, 239, 185)
        )

        val DEFAULT_MIDI_ENTRIES = listOf(
            // WAH
            MidiMappingEntry("WAH_CLYDE", "WAH", "WAH", "CLYDE", 2, null, 1, 65, 0),
            MidiMappingEntry("WAH_CRY_BB", "WAH", "WAH", "CRY BB", 4, null, 2, 66, 1),
            MidiMappingEntry("WAH_V847", "WAH", "WAH", "V847", 6, null, 3, 67, 2),
            MidiMappingEntry("WAH_HORSE_WAH", "WAH", "WAH", "HORSE WAH", 8, null, 4, 68, 3),
            MidiMappingEntry("WAH_OCTAVE_SHIFT", "WAH", "WAH", "OCTAVE-SHIFT", 10, null, 5, 69, 4),

            // CMP (1: RED COMP, 2: ROSE COMP)
            MidiMappingEntry("CMP_RED_COMP", "CMP", "CMP", "RED COMP", 1, 65, 1, 65, 0),
            MidiMappingEntry("CMP_ROSE_COMP", "CMP", "CMP", "ROSE COMP", 2, 66, 2, 66, 1),

            // GATE / NR (1: NR)
            MidiMappingEntry("GATE_NR", "GATE", "GATE", "NR", 1, 65, 1, 65, 0),

            // EFX (1..14 exact sequence)
            MidiMappingEntry("EFX_DIST_PLUS", "EFX", "EFX", "DIST+", 2, null, 1, 65, 0),
            MidiMappingEntry("EFX_RC_BST", "EFX", "EFX", "RC BST", 4, null, 2, 66, 1),
            MidiMappingEntry("EFX_AC_BST", "EFX", "EFX", "AC BST", 6, null, 3, 67, 2),
            MidiMappingEntry("EFX_DIST_UNO", "EFX", "EFX", "DIST UNO", 8, null, 4, 68, 3),
            MidiMappingEntry("EFX_T_SCREAMER", "EFX", "EFX", "T SCREAMER", 10, null, 5, 69, 4),
            MidiMappingEntry("EFX_BLUES_DRIVE", "EFX", "EFX", "BLUES DRIVE", 12, null, 6, 70, 5),
            MidiMappingEntry("EFX_MORNING_DRIVE", "EFX", "EFX", "MORNING DRIVE", 14, null, 7, 71, 6),
            MidiMappingEntry("EFX_MODERN_DIST", "EFX", "EFX", "MODERN DIST", 16, null, 8, 72, 7),
            MidiMappingEntry("EFX_RED_DIST", "EFX", "EFX", "RED DIST", 18, null, 9, 73, 8),
            MidiMappingEntry("EFX_CRUNCH", "EFX", "EFX", "CRUNCH", 20, null, 10, 74, 9),
            MidiMappingEntry("EFX_MUF_DIST", "EFX", "EFX", "MUF DIST", 22, null, 11, 75, 10),
            MidiMappingEntry("EFX_KATANA", "EFX", "EFX", "KATANA", 24, null, 12, 76, 11),
            MidiMappingEntry("EFX_RED_FUZZ", "EFX", "EFX", "RED FUZZ", 28, null, 13, 77, 12),
            MidiMappingEntry("EFX_TOUCH_WAH", "EFX", "EFX", "TOUCH WAH", 30, null, 14, 78, 13),

            // AMP (1..25 exact sequence)
            MidiMappingEntry("AMP_JAZZ_CLEAN", "AMP", "AMP", "JAZZ CLEAN", 1, 65, 1, 65, 0),
            MidiMappingEntry("AMP_DELUXE_RVB", "AMP", "AMP", "DELUXE RVB", 2, 66, 2, 66, 1),
            MidiMappingEntry("AMP_BASS_MATE", "AMP", "AMP", "BASS MATE", 3, 67, 3, 67, 2),
            MidiMappingEntry("AMP_TWEEDY", "AMP", "AMP", "TWEEDY", 4, 68, 4, 68, 3),
            MidiMappingEntry("AMP_TWIN_RVB", "AMP", "AMP", "TWIN RVB", 5, 69, 5, 69, 4),
            MidiMappingEntry("AMP_HIWIRE", "AMP", "AMP", "HIWIRE", 6, 70, 6, 70, 5),
            MidiMappingEntry("AMP_CALI_CRUNCH", "AMP", "AMP", "CALI CRUNCH", 7, 71, 7, 71, 6),
            MidiMappingEntry("AMP_CLASS_A15", "AMP", "AMP", "CLASS A15", 8, 72, 8, 72, 7),
            MidiMappingEntry("AMP_CLASS_A30", "AMP", "AMP", "CLASS A30", 9, 73, 9, 73, 8),
            MidiMappingEntry("AMP_PLEXI_100", "AMP", "AMP", "PLEXI 100", 10, 74, 10, 74, 9),
            MidiMappingEntry("AMP_PLEXI_45", "AMP", "AMP", "PLEXI 45", 11, 75, 11, 75, 10),
            MidiMappingEntry("AMP_BRIT_800", "AMP", "AMP", "BRIT 800", 12, 76, 12, 76, 11),
            MidiMappingEntry("AMP_1987X_50W", "AMP", "AMP", "1987X 50W", 13, 77, 13, 77, 12),
            MidiMappingEntry("AMP_SLO_100", "AMP", "AMP", "SLO 100", 14, 78, 14, 78, 13),
            MidiMappingEntry("AMP_FIREMAN_HBE", "AMP", "AMP", "FIREMAN HBE", 15, 79, 15, 79, 14),
            MidiMappingEntry("AMP_DUAL_RECT", "AMP", "AMP", "DUAL RECT", 16, 80, 16, 80, 15),
            MidiMappingEntry("AMP_DIE_VH4", "AMP", "AMP", "DIE VH4", 17, 81, 17, 81, 16),
            MidiMappingEntry("AMP_VIBRO_KING", "AMP", "AMP", "VIBRO KING", 18, 82, 18, 82, 17),
            MidiMappingEntry("AMP_BUDDA", "AMP", "AMP", "BUDDA", 19, 83, 19, 83, 18),
            MidiMappingEntry("AMP_MR_Z_38", "AMP", "AMP", "MR.Z 38", 20, 84, 20, 84, 19),
            MidiMappingEntry("AMP_SUPER_RVB", "AMP", "AMP", "SUPER RVB", 21, 85, 21, 85, 20),
            MidiMappingEntry("AMP_BRIT_BLUES", "AMP", "AMP", "BRIT BLUES", 22, 86, 22, 86, 21),
            MidiMappingEntry("AMP_MATCH_D30", "AMP", "AMP", "MATCH D30", 23, 87, 23, 87, 22),
            MidiMappingEntry("AMP_BRIT_2000", "AMP", "AMP", "BRIT 2000", 24, 88, 24, 88, 23),
            MidiMappingEntry("AMP_UBER_HIGAIN", "AMP", "AMP", "UBER HIGAIN", 25, 89, 25, 89, 24),

            // EQ (1: 6-EQ, 2: 10-EQ)
            MidiMappingEntry("EQ_6_EQ", "EQ", "EQ", "6-EQ", 2, null, 1, 65, 0),
            MidiMappingEntry("EQ_10_EQ", "EQ", "EQ", "10-EQ", 6, null, 2, 66, 1),

            // MOD (1..14 exact sequence)
            MidiMappingEntry("MOD_CE_1", "MOD", "MOD", "CE-1", 2, null, 1, 65, 0),
            MidiMappingEntry("MOD_CLASSIC_CHORUS", "MOD", "MOD", "CLASSIC CHORUS", 4, null, 2, 66, 1),
            MidiMappingEntry("MOD_ST_CHORUS", "MOD", "MOD", "ST.CHORUS", 6, null, 3, 67, 2),
            MidiMappingEntry("MOD_VIBRATO", "MOD", "MOD", "VIBRATO", 8, null, 4, 68, 3),
            MidiMappingEntry("MOD_DETUNE", "MOD", "MOD", "DETUNE", 10, null, 5, 69, 4),
            MidiMappingEntry("MOD_FLANGE", "MOD", "MOD", "FLANGE", 12, null, 6, 70, 5),
            MidiMappingEntry("MOD_PH_90", "MOD", "MOD", "PH 90", 14, null, 7, 71, 6),
            MidiMappingEntry("MOD_PHASE_100", "MOD", "MOD", "PHASE 100", 16, null, 8, 72, 7),
            MidiMappingEntry("MOD_CHORUS_PLUS", "MOD", "MOD", "CHORUS+", 18, null, 9, 73, 8),
            MidiMappingEntry("MOD_U_VIBE", "MOD", "MOD", "U-VIBE", 20, null, 10, 74, 9),
            MidiMappingEntry("MOD_TREM", "MOD", "MOD", "TREM", 22, null, 11, 75, 10),
            MidiMappingEntry("MOD_RTY_SPK", "MOD", "MOD", "RTY SPK", 24, null, 12, 76, 11),
            MidiMappingEntry("MOD_HARMONY", "MOD", "MOD", "HARMONY", 26, null, 13, 77, 12),
            MidiMappingEntry("MOD_SCH_1", "MOD", "MOD", "SCH-1", 28, null, 14, 78, 13),

            // DLY (1..7 exact sequence)
            MidiMappingEntry("DLY_ANALOG_DELAY", "DLY", "DLY", "ANALOG DELAY", 1, 65, 1, 65, 0),
            MidiMappingEntry("DLY_DIGI_DELAY", "DLY", "DLY", "DIGI DELAY", 2, 66, 2, 66, 1),
            MidiMappingEntry("DLY_MODULATE", "DLY", "DLY", "MODULATE", 3, 67, 3, 67, 2),
            MidiMappingEntry("DLY_TAPE_ECHO", "DLY", "DLY", "TAPE ECHO", 4, 68, 4, 68, 3),
            MidiMappingEntry("DLY_REVERSE", "DLY", "DLY", "REVERSE", 5, 69, 5, 69, 4),
            MidiMappingEntry("DLY_PAN", "DLY", "DLY", "PAN", 6, 70, 6, 70, 5),
            MidiMappingEntry("DLY_PHI_DELAY", "DLY", "DLY", "PHI DELAY", 7, 71, 7, 71, 6),

            // RVB (1..5 exact sequence)
            MidiMappingEntry("RVB_ROOM", "RVB", "RVB", "ROOM", 2, null, 1, 65, 0),
            MidiMappingEntry("RVB_HALL", "RVB", "RVB", "HALL", 4, null, 2, 66, 1),
            MidiMappingEntry("RVB_PLATE", "RVB", "RVB", "PLATE", 6, null, 3, 67, 2),
            MidiMappingEntry("RVB_SPRING", "RVB", "RVB", "SPRING", 8, null, 4, 68, 3),
            MidiMappingEntry("RVB_SHIMMER", "RVB", "RVB", "SHIMMER", 10, null, 5, 69, 4),

            // IR / CAB (1..25 factory cabs + 12 user IR slots)
            MidiMappingEntry("IR_JAZZ_212", "IR", "IR", "JAZZ 212", 1, 65, 1, 65, 0),
            MidiMappingEntry("IR_BLACK_112", "IR", "IR", "BLACK 112", 2, 66, 2, 66, 1),
            MidiMappingEntry("IR_TWEED_410", "IR", "IR", "TWEED 410", 3, 67, 3, 67, 2),
            MidiMappingEntry("IR_TWEED_112", "IR", "IR", "TWEED 112", 4, 68, 4, 68, 3),
            MidiMappingEntry("IR_TWIN_212", "IR", "IR", "TWIN 212", 5, 69, 5, 69, 4),
            MidiMappingEntry("IR_CUSTOM_412", "IR", "IR", "CUSTOM 412", 6, 70, 6, 70, 5),
            MidiMappingEntry("IR_CALI_112", "IR", "IR", "CALI 112", 7, 71, 7, 71, 6),
            MidiMappingEntry("IR_BLUE_112", "IR", "IR", "BLUE 112", 8, 72, 8, 72, 7),
            MidiMappingEntry("IR_BLUE_212", "IR", "IR", "BLUE 212", 9, 73, 9, 73, 8),
            MidiMappingEntry("IR_GREEN_412", "IR", "IR", "GREEN 412", 10, 74, 10, 74, 9),
            MidiMappingEntry("IR_G12L_412", "IR", "IR", "G12L 412", 11, 75, 11, 75, 10),
            MidiMappingEntry("IR_T75_412", "IR", "IR", "T75 412", 12, 76, 12, 76, 11),
            MidiMappingEntry("IR_V30_412", "IR", "IR", "V30 412", 13, 77, 13, 77, 12),
            MidiMappingEntry("IR_SLO_412", "IR", "IR", "SLO 412", 14, 78, 14, 78, 13),
            MidiMappingEntry("IR_HBE_412", "IR", "IR", "HBE 412", 15, 79, 15, 79, 14),
            MidiMappingEntry("IR_RECT_412", "IR", "IR", "RECT 412", 16, 80, 16, 80, 15),
            MidiMappingEntry("IR_BOTM_412", "IR", "IR", "BOTM 412", 17, 81, 17, 81, 16),
            MidiMappingEntry("IR_VIBRO_212", "IR", "IR", "VIBRO 212", 18, 82, 18, 82, 17),
            MidiMappingEntry("IR_EVL_112", "IR", "IR", "EVL 112", 19, 83, 19, 83, 18),
            MidiMappingEntry("IR_BUTQ_212", "IR", "IR", "BUTQ 212", 20, 84, 20, 84, 19),
            MidiMappingEntry("IR_SUPER_212", "IR", "IR", "SUPER 212", 21, 85, 21, 85, 20),
            MidiMappingEntry("IR_BRIT_212", "IR", "IR", "BRIT 212", 22, 86, 22, 86, 21),
            MidiMappingEntry("IR_DC_212", "IR", "IR", "DC 212", 23, 87, 23, 87, 22),
            MidiMappingEntry("IR_G12M_412", "IR", "IR", "G12M 412", 24, 88, 24, 88, 23),
            MidiMappingEntry("IR_UBER_412", "IR", "IR", "UBER 412", 25, 89, 25, 89, 24),
            MidiMappingEntry("IR_USER_1", "IR", "USER", "USER IR 1", 26, 90, 26, 90, 25),
            MidiMappingEntry("IR_USER_2", "IR", "USER", "USER IR 2", 27, 91, 27, 91, 26),
            MidiMappingEntry("IR_USER_3", "IR", "USER", "USER IR 3", 28, 92, 28, 92, 27),
            MidiMappingEntry("IR_USER_4", "IR", "USER", "USER IR 4", 29, 93, 29, 93, 28),
            MidiMappingEntry("IR_USER_5", "IR", "USER", "USER IR 5", 30, 94, 30, 94, 29),
            MidiMappingEntry("IR_USER_6", "IR", "USER", "USER IR 6", 31, 95, 31, 95, 30),
            MidiMappingEntry("IR_USER_7", "IR", "USER", "USER IR 7", 32, 96, 32, 96, 31),
            MidiMappingEntry("IR_USER_8", "IR", "USER", "USER IR 8", 33, 97, 33, 97, 32),
            MidiMappingEntry("IR_USER_9", "IR", "USER", "USER IR 9", 34, 98, 34, 98, 33),
            MidiMappingEntry("IR_USER_10", "IR", "USER", "USER IR 10", 35, 99, 35, 99, 34),
            MidiMappingEntry("IR_USER_11", "IR", "USER", "USER IR 11", 36, 100, 36, 100, 35),
            MidiMappingEntry("IR_USER_12", "IR", "USER", "USER IR 12", 37, 101, 37, 101, 36),

            // P.L (Precede / Posterior Position CC#77, Level CC#76)
            MidiMappingEntry("PL_PRECEDE", "P.L", "P.L", "Precede (Leftmost)", 0, null, 0, null, 0),
            MidiMappingEntry("PL_POSTERIOR", "P.L", "P.L", "Posterior (Rightmost)", 1, null, 1, null, 1),

            // S/R & VOL
            MidiMappingEntry("SR_SR", "S/R", "S/R", "S/R", 0, 1, 1, 65, 0),
            MidiMappingEntry("VOL_VOL", "VOL", "VOL", "VOL", 1, 65, 1, 65, 0)
        )
    }
}
