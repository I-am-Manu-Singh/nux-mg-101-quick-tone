package com.creativeblisslabs.nuxmg101.data.repository

import com.creativeblisslabs.nuxmg101.data.db.IRDao
import com.creativeblisslabs.nuxmg101.data.db.PatchDao
import com.creativeblisslabs.nuxmg101.data.model.CabinetIR
import com.creativeblisslabs.nuxmg101.data.model.CloudPreset
import com.creativeblisslabs.nuxmg101.data.model.Patch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.util.UUID

class PatchRepository(
    private val patchDao: PatchDao,
    private val irDao: IRDao
) {
    val allPatches: Flow<List<Patch>> = patchDao.getAllPatches()
    val allIRs: Flow<List<CabinetIR>> = irDao.getAllIRs()

    suspend fun seedInitialDataIfEmpty() {
        val currentPatches = patchDao.getAllPatches().first()
        if (currentPatches.isEmpty() || currentPatches.any { !it.name.startsWith("PATCH ") }) {
            patchDao.deleteAllPatches()
            patchDao.insertPatches(initialPresetList)
        }

        val currentIRs = irDao.getAllIRs().first()
        if (currentIRs.isEmpty()) {
            irDao.insertIRs(initialIRList)
        }
    }

    suspend fun toggleFavorite(patchId: String, currentFav: Boolean) {
        patchDao.setFavorite(patchId, !currentFav)
    }

    suspend fun updatePatch(patch: Patch) {
        patchDao.updatePatch(patch)
    }

    suspend fun insertPatch(patch: Patch) {
        patchDao.insertPatch(patch)
    }

    suspend fun setActiveIRSlot(slotIndex: String) {
        irDao.setActiveIRSlot(slotIndex)
    }

    suspend fun exportPatchToJson(patch: Patch): String {
        val obj = org.json.JSONObject().apply {
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
        }
        return obj.toString(2)
    }

    suspend fun importPatchFromJson(jsonString: String, targetSlotId: String? = null): Patch? {
        return try {
            val obj = org.json.JSONObject(jsonString)
            val id = targetSlotId ?: obj.optString("id", UUID.randomUUID().toString())
            val slotNum = obj.optString("slotNumber", "1A")
            val name = obj.optString("name", "IMPORTED PATCH")
            val customTag = obj.optString("customTag", "Imported")
            val amp = obj.optString("ampModelName", "BRIT 800")
            val cab = obj.optString("cabModelName", "V30 412")
            val bpm = obj.optInt("bpm", 120)
            val raw = obj.optString("rawEffectData", "")

            val importedPatch = Patch(
                id = id,
                slotNumber = slotNum,
                name = name,
                customTag = customTag,
                isFactory = false,
                isFavorite = false,
                ampModelName = amp,
                cabModelName = cab,
                bpm = bpm,
                rawEffectData = raw
            )
            patchDao.insertPatch(importedPatch)
            importedPatch
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun exportAllPatchesBackup(): String {
        val patches = patchDao.getAllPatches().first()
        val root = org.json.JSONObject().apply {
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
                    put("rawEffectData", p.rawEffectData)
                }
                arr.put(pObj)
            }
            put("patches", arr)
        }
        return root.toString(2)
    }

    suspend fun importAllPatchesBackup(jsonString: String): Int {
        return try {
            val root = org.json.JSONObject(jsonString)
            val arr = root.getJSONArray("patches")
            val list = mutableListOf<Patch>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Patch(
                        id = obj.getString("id"),
                        slotNumber = obj.getString("slotNumber"),
                        name = obj.getString("name"),
                        customTag = obj.optString("customTag", ""),
                        isFactory = obj.optBoolean("isFactory", false),
                        isFavorite = obj.optBoolean("isFavorite", false),
                        ampModelName = obj.optString("ampModelName", "BRIT 800"),
                        cabModelName = obj.optString("cabModelName", "V30 412"),
                        bpm = obj.optInt("bpm", 120),
                        rawEffectData = obj.optString("rawEffectData", "")
                    )
                )
            }
            if (list.isNotEmpty()) {
                patchDao.deleteAllPatches()
                patchDao.insertPatches(list)
            }
            list.size
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    fun getCloudPresets(): List<CloudPreset> = sampleCloudPresets

    companion object {
        val initialPresetList = buildList {
            for (bank in 1..9) {
                for (letter in listOf("A", "B", "C", "D")) {
                    val slot = "$bank$letter"
                    add(Patch("u_$slot", slot, "PATCH $slot", "", isFactory = false))
                }
            }
            for (bank in 1..9) {
                for (letter in listOf("A", "B", "C", "D")) {
                    val slot = "$bank$letter"
                    add(Patch("f_$slot", slot, "PATCH $slot", "", isFactory = true))
                }
            }
        }

        val initialIRList = listOf(
            CabinetIR(1, "001", "1960A 4x12 V30", "48kHz", "500ms", isActiveSlot = true, levelDb = 0.0f, highPassHz = 80.0f, lowPassKHz = 6.5f),
            CabinetIR(2, "002", "V30 Modern Chug", "48kHz", "200ms", isActiveSlot = false, levelDb = -1.2f, highPassHz = 95.0f, lowPassKHz = 7.0f),
            CabinetIR(3, "003", "Blue Alnico 2x12", "44.1kHz", "500ms", isActiveSlot = false, levelDb = +0.5f, highPassHz = 75.0f, lowPassKHz = 8.0f),
            CabinetIR(4, "004", "Tweed 1x12 Custom", "48kHz", "500ms", isActiveSlot = false, levelDb = 0.0f, highPassHz = 60.0f, lowPassKHz = 5.5f)
        )

        val sampleCloudPresets = listOf(
            CloudPreset("c01", "094", "Ambient Crystal", "ToneJunkie", 4.8f, 1420, "Top Rated", "Lush ambient swells with dual tape delays and stereo chorus."),
            CloudPreset("c02", "012", "Djent Machine", "MetalCoreX", 4.1f, 980, "Trending", "Tight high-pass noise gate with scooped mids for modern metal."),
            CloudPreset("c03", "055", "Vintage Fuzz Face", "HendrixFan99", 5.0f, 2150, "Top Rated", "Saturated silicon fuzz into cranked Plexi stack."),
            CloudPreset("c04", "102", "Clean Chorus 80s", "SynthPop", 3.9f, 630, "Artist Packs", "Bright pristine 80s clean tone with subtle shimmer reverb.")
        )
    }
}
