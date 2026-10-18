package com.creativeblisslabs.nuxmg101

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.creativeblisslabs.nuxmg101.data.db.AppDatabase
import com.creativeblisslabs.nuxmg101.data.model.EffectSlotType
import com.creativeblisslabs.nuxmg101.data.model.Patch
import com.creativeblisslabs.nuxmg101.data.repository.NuxUsbRepository
import com.creativeblisslabs.nuxmg101.data.repository.PatchRepository
import com.creativeblisslabs.nuxmg101.ui.components.BottomStudioNav
import com.creativeblisslabs.nuxmg101.ui.components.HardwareDisconnectOverlay
import com.creativeblisslabs.nuxmg101.ui.components.MidiMappingDialog
import com.creativeblisslabs.nuxmg101.ui.components.StudioNavRoute
import com.creativeblisslabs.nuxmg101.ui.components.TopStudioBar
import com.creativeblisslabs.nuxmg101.ui.components.UsbManagerDialog

import com.creativeblisslabs.nuxmg101.ui.screens.CloudAndConverterScreen
import com.creativeblisslabs.nuxmg101.ui.screens.DrumLoopScreen
import com.creativeblisslabs.nuxmg101.ui.screens.EffectEditScreen
import com.creativeblisslabs.nuxmg101.ui.screens.HardwareControlsScreen
import com.creativeblisslabs.nuxmg101.ui.screens.LibraryScreen
import com.creativeblisslabs.nuxmg101.ui.screens.SignalDashboardScreen
import com.creativeblisslabs.nuxmg101.ui.screens.SystemScreen
import com.creativeblisslabs.nuxmg101.ui.screens.TunerScreen
import com.creativeblisslabs.nuxmg101.ui.theme.QuickToneTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var patchRepository: PatchRepository
    private lateinit var usbRepository: NuxUsbRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val midiMappingRepository = com.example.data.repository.MidiMappingRepository(database.midiMappingDao())
        patchRepository = PatchRepository(database.patchDao(), database.irDao())
        usbRepository = NuxUsbRepository(context = applicationContext, midiMappingRepository = midiMappingRepository)

        val prefs = applicationContext.getSharedPreferences("quicktone_prefs", Context.MODE_PRIVATE)

        setContent {
            var isDarkTheme by remember { mutableStateOf(prefs.getBoolean("is_dark_theme", true)) }

            QuickToneTheme(darkTheme = isDarkTheme) {
                QuickToneApp(
                    patchRepository = patchRepository,
                    usbRepository = usbRepository,
                    midiMappingRepository = midiMappingRepository,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = {
                        val newTheme = !isDarkTheme
                        isDarkTheme = newTheme
                        prefs.edit().putBoolean("is_dark_theme", newTheme).apply()
                    }
                )
            }
        }
    }
}

@Composable
fun QuickToneApp(
    patchRepository: PatchRepository,
    usbRepository: NuxUsbRepository,
    midiMappingRepository: com.example.data.repository.MidiMappingRepository? = null,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    // Seed database on startup
    LaunchedEffect(Unit) {
        patchRepository.seedInitialDataIfEmpty()
        midiMappingRepository?.seedInitialDataIfEmpty()
    }

    val hardwareStatus by usbRepository.hardwareStatus.collectAsState()
    val effectChain by usbRepository.effectChain.collectAsState()
    val selectedSlot by usbRepository.selectedSlot.collectAsState()

    val patches by patchRepository.allPatches.collectAsState(initial = emptyList())
    val irList by patchRepository.allIRs.collectAsState(initial = emptyList())

    var activePatch by remember(patches) {
        mutableStateOf(patches.firstOrNull() ?: PatchRepository.initialPresetList.first())
    }

    var currentRoute by rememberSaveable { mutableStateOf(StudioNavRoute.SIGNAL) }
    var isEditingEffectSlot by rememberSaveable { mutableStateOf(false) }
    var showUsbManagerDialog by rememberSaveable { mutableStateOf(false) }
    var showMidiTableDialog by rememberSaveable { mutableStateOf(false) }
    var modelChooserSlot by remember { mutableStateOf<EffectSlotType?>(null) }

    // Automatic connection retry tracking (up to 3 times, 10s gap)
    var retryCount by remember { mutableIntStateOf(0) }
    var countdownSeconds by remember { mutableIntStateOf(10) }

    LaunchedEffect(hardwareStatus.isConnected) {
        if (!hardwareStatus.isConnected) {
            isEditingEffectSlot = false
            showUsbManagerDialog = false
            showMidiTableDialog = false
            modelChooserSlot = null

            // Automatic 3 retries with 10s gap and live countdown ticks
            while (!hardwareStatus.isConnected && retryCount < 3) {
                countdownSeconds = 10
                while (countdownSeconds > 0 && !hardwareStatus.isConnected) {
                    delay(1000L)
                    countdownSeconds--
                }

                if (!hardwareStatus.isConnected) {
                    retryCount++
                    usbRepository.scanUsbPorts()
                }
            }
        } else {
            // Reset retry count once connection is re-established successfully
            retryCount = 0
            countdownSeconds = 10
        }
    }

    val loadPatch: (com.example.data.model.Patch) -> Unit = { patch ->
        activePatch = patch
        val isFactory = patch.isFactory || patch.id.startsWith("f_")
        val numStr = patch.slotNumber.filter { it.isDigit() }
        val letterChar = patch.slotNumber.filter { it.isLetter() }.uppercase().firstOrNull() ?: 'A'
        val bankNum = numStr.toIntOrNull() ?: 1
        val letterIdx = when (letterChar) {
            'A' -> 0
            'B' -> 1
            'C' -> 2
            'D' -> 3
            else -> 0
        }
        val slotIndex = ((bankNum - 1) * 4) + letterIdx
        val pcNum = if (isFactory) 36 + slotIndex else slotIndex
        usbRepository.sendMidiPcBytes(pcNum)
    }

    val remotePc by usbRepository.remotePcReceived.collectAsState()

    LaunchedEffect(remotePc) {
        remotePc?.let { pcVal ->
            val targetId = if (pcVal < 36) {
                val num = (pcVal / 4) + 1
                val letter = listOf("A", "B", "C", "D")[pcVal % 4]
                "u_${num}${letter}"
            } else {
                val offset = pcVal - 36
                val num = (offset / 4) + 1
                val letter = listOf("A", "B", "C", "D")[offset % 4]
                "f_${num}${letter}"
            }
            val match = patches.find { it.id == targetId }
            if (match != null) {
                activePatch = match
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                val title = if (hardwareStatus.activePatchName != "NOT FETCHED") {
                    hardwareStatus.activePatchName
                } else {
                    "PATCH ${activePatch.slotNumber}"
                }
                Spacer(modifier = Modifier.height(32.dp))
                TopStudioBar(
                    hardwareStatus = hardwareStatus,
                    currentPatchTitle = title,
                    isDarkTheme = isDarkTheme,
                    onPrevPatch = {
                        val allAvailable = if (patches.isNotEmpty()) patches else PatchRepository.initialPresetList
                        val currentIndex = allAvailable.indexOfFirst { it.id == activePatch.id }
                        if (currentIndex > 0) {
                            loadPatch(allAvailable[currentIndex - 1])
                        } else if (allAvailable.isNotEmpty()) {
                            loadPatch(allAvailable.last())
                        }
                    },
                    onNextPatch = {
                        val allAvailable = if (patches.isNotEmpty()) patches else PatchRepository.initialPresetList
                        val currentIndex = allAvailable.indexOfFirst { it.id == activePatch.id }
                        if (currentIndex >= 0 && currentIndex < allAvailable.size - 1) {
                            loadPatch(allAvailable[currentIndex + 1])
                        } else if (allAvailable.isNotEmpty()) {
                            loadPatch(allAvailable.first())
                        }
                    },
                    onToggleTheme = onToggleTheme,
                    onMidiTableClick = { currentRoute = StudioNavRoute.HARDWARE },
                    onSettingsClick = { currentRoute = StudioNavRoute.SYSTEM },
                    onUsbClick = { showUsbManagerDialog = true }
                )
            },

            bottomBar = {
                BottomStudioNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        isEditingEffectSlot = false
                        currentRoute = route
                    }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentRoute) {
                            StudioNavRoute.SIGNAL -> {
                                SignalDashboardScreen(
                                    hardwareStatus = hardwareStatus,
                                    activePatch = activePatch,
                                    effectChain = effectChain,
                                    selectedSlot = selectedSlot,
                                    onSelectSlot = { slot ->
                                        usbRepository.selectSlot(slot)
                                        if (slot == EffectSlotType.CAB) {
                                            currentRoute = StudioNavRoute.SIGNAL
                                        }
                                    },
                                    onToggleSlotActive = { slot ->
                                        usbRepository.toggleSlotActive(slot)
                                    },
                                    onToggleMute = {
                                        usbRepository.toggleMute()
                                    },
                                    onPrevPatch = {
                                        val idx = patches.indexOf(activePatch)
                                        if (idx > 0) loadPatch(patches[idx - 1])
                                    },
                                    onNextPatch = {
                                        val idx = patches.indexOf(activePatch)
                                        if (idx < patches.size - 1) loadPatch(patches[idx + 1])
                                    },
                                    onTapTempo = {
                                        val newBpm = (hardwareStatus.currentBpm + 5).let { if (it > 240) 60 else it }
                                        usbRepository.setTapTempoBpm(newBpm)
                                    },
                                    onEditSlot = { slot ->
                                        usbRepository.selectSlot(slot)
                                        isEditingEffectSlot = true
                                    },
                                    onOpenModelChooser = { slot ->
                                        usbRepository.selectSlot(slot)
                                        isEditingEffectSlot = true
                                    },
                                    onGlobalParamChange = { ccNum, value ->
                                        usbRepository.sendMidiCcBytes(ccNum, value.toInt().coerceIn(0, 127))
                                    },
                                    onUpdateHardwareLevels = { out, phones ->
                                        usbRepository.updateHardwarePreferences(outputLevel = out, phonesLevel = phones)
                                    },
                                    onTogglePlPosition = { usbRepository.togglePlPosition() },
                                    onToggleExpEngaged = { usbRepository.toggleExpEngaged() },
                                    onSetExpTarget = { target -> usbRepository.setExpTarget(target) },
                                    onSetExpValue = { value -> usbRepository.setExpValue(value) }
                                )
                            }

                                StudioNavRoute.LIBRARY -> {
                                    LibraryScreen(
                                        patches = patches,
                                        activePatchId = activePatch.id,
                                        effectChain = effectChain,
                                        onSelectPatch = { patch -> loadPatch(patch) },
                                        onToggleFavorite = { patch ->
                                            coroutineScope.launch {
                                                patchRepository.toggleFavorite(patch.id, patch.isFavorite)
                                            }
                                        },
                                        onRenamePatch = { patch, newName, newTag ->
                                            coroutineScope.launch {
                                                val updated = patch.copy(name = newName, customTag = newTag)
                                                patchRepository.insertPatch(updated)
                                                if (activePatch.id == patch.id) {
                                                    activePatch = updated
                                                }
                                            }
                                        },
                                        onImportPatch = { jsonStr ->
                                            coroutineScope.launch {
                                                val imported = patchRepository.importPatchFromJson(jsonStr, activePatch.id)
                                                if (imported != null) {
                                                    activePatch = imported
                                                }
                                            }
                                        },
                                        onRestoreBackup = { jsonStr ->
                                            coroutineScope.launch {
                                                patchRepository.importAllPatchesBackup(jsonStr)
                                            }
                                        },
                                        onLoadEffect = { slotType, modelName ->
                                            usbRepository.changeEffectModel(slotType, modelName)
                                        }
                                    )
                                }

                                StudioNavRoute.TUNER -> {
                                    TunerScreen(
                                        isMuted = hardwareStatus.isMuted,
                                        onToggleMute = { usbRepository.toggleMute() }
                                    )
                                }

                                StudioNavRoute.DRUM_LOOP -> {
                                    DrumLoopScreen(
                                        hardwareStatus = hardwareStatus,
                                        onDrumSettingsChange = { ccNum, ccVal ->
                                            usbRepository.sendMidiCcBytes(ccNum, ccVal)
                                        },
                                        onUpdatePrefs = { enabled, vol, bass, mid, treb, loop, lState, mono ->
                                            usbRepository.updateHardwarePreferences(
                                                drumEnabled = enabled,
                                                drumLevel = vol,
                                                drumBass = bass,
                                                drumMid = mid,
                                                drumTreble = treb,
                                                loopLevel = loop,
                                                looperState = lState,
                                                drumRoutingMono = mono
                                            )
                                        }
                                    )
                                }

                                 StudioNavRoute.HARDWARE -> {
                                    val midiLogs by usbRepository.midiConsoleLogs.collectAsState()
                                    HardwareControlsScreen(
                                        hardwareStatus = hardwareStatus,
                                        activePatch = activePatch,
                                        effectChain = effectChain,
                                        midiLogs = midiLogs,
                                        midiMappingRepository = midiMappingRepository,
                                        onSendCc = { ccNum: Int, ccVal: Int -> usbRepository.sendMidiCcBytes(ccNum, ccVal) },
                                        onSendPc = { pcNum: Int -> usbRepository.sendMidiPcBytes(pcNum) },
                                        onToggleSlotActive = { slot -> usbRepository.toggleSlotActive(slot) },
                                        onTogglePlPosition = { usbRepository.togglePlPosition() },
                                        onPrevPatch = {
                                            val currentIndex = patches.indexOfFirst { it.id == activePatch.id }
                                            if (currentIndex > 0) {
                                                loadPatch(patches[currentIndex - 1])
                                            }
                                        },
                                        onNextPatch = {
                                            val currentIndex = patches.indexOfFirst { it.id == activePatch.id }
                                            if (currentIndex >= 0 && currentIndex < patches.size - 1) {
                                                loadPatch(patches[currentIndex + 1])
                                            }
                                        },
                                        onTapTempo = {
                                            val newBpm = (hardwareStatus.currentBpm + 5).let { if (it > 240) 60 else it }
                                            usbRepository.setTapTempoBpm(newBpm)
                                        }
                                    )
                                }

                                StudioNavRoute.CLOUD_CONV -> {
                                    CloudAndConverterScreen(
                                        cloudPresets = patchRepository.getCloudPresets(),
                                        onDownloadPreset = { cloudPreset ->
                                            val newPatch = Patch(
                                                id = System.currentTimeMillis().toString(),
                                                slotNumber = cloudPreset.slotIndex,
                                                name = cloudPreset.title,
                                                customTag = "CLEAN",
                                                isFactory = false,
                                                isFavorite = true
                                            )
                                            coroutineScope.launch {
                                                patchRepository.insertPatch(newPatch)
                                                activePatch = newPatch
                                                currentRoute = StudioNavRoute.LIBRARY
                                            }
                                        }
                                    )
                                }

                                StudioNavRoute.SYSTEM -> {
                                    SystemScreen(
                                        hardwareStatus = hardwareStatus,
                                        isDarkTheme = isDarkTheme,
                                        onToggleTheme = onToggleTheme,
                                        onSendCc = { ccNum, ccVal -> usbRepository.sendMidiCcBytes(ccNum, ccVal) },
                                        onSendPc = { pcNum -> usbRepository.sendMidiPcBytes(pcNum) },
                                        onNavigateToHardware = { currentRoute = StudioNavRoute.HARDWARE },
                                        onUpdatePreferences = { dryDi, reamp, gain, brightness, led, aux, uacIn, uacOut ->
                                            usbRepository.updateHardwarePreferences(
                                                dryDi = dryDi,
                                                reamp = reamp,
                                                usbGain = gain,
                                                screenBrightness = brightness,
                                                ledRing = led,
                                                auxVol = aux,
                                                uacIn = uacIn,
                                                uacOut = uacOut
                                            )
                                        }
                                    )
                                }
                            }

                        // Layered Modal Effect Edit Dialog (Maintains visible background beneath)
                        if (isEditingEffectSlot) {
                            val currentBlock = effectChain.find { it.slotType == selectedSlot } ?: effectChain.first()
                            val availableModels = usbRepository.getAvailableModels(selectedSlot)
                            EffectEditScreen(
                                effectBlock = currentBlock,
                                onParameterChange = { paramId, newVal ->
                                    usbRepository.updateParameterValue(selectedSlot, paramId, newVal)
                                },
                                onToggleActive = {
                                    usbRepository.toggleSlotActive(selectedSlot)
                                },
                                onSubTypeChange = { subType ->
                                    usbRepository.setSubType(selectedSlot, subType)
                                },
                                onTapTempo = {
                                    val newBpm = (hardwareStatus.currentBpm + 5).let { if (it > 240) 60 else it }
                                    usbRepository.setTapTempoBpm(newBpm)
                                },
                                availableModels = availableModels,
                                onSelectModel = { selectedModel ->
                                    usbRepository.changeEffectModel(selectedSlot, selectedModel)
                                },
                                onDismissRequest = {
                                    isEditingEffectSlot = false
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showMidiTableDialog) {
            val midiLogs by usbRepository.midiConsoleLogs.collectAsState()
            MidiMappingDialog(
                midiLogs = midiLogs,
                onSendCc = { ccNum, ccVal -> usbRepository.sendMidiCcBytes(ccNum, ccVal) },
                onSendPc = { pcNum -> usbRepository.sendMidiPcBytes(pcNum) },
                onDismissRequest = { showMidiTableDialog = false }
            )
        }

        if (showUsbManagerDialog) {
            UsbManagerDialog(
                hardwareStatus = hardwareStatus,
                onToggleConnection = { usbRepository.toggleUsbConnection() },
                onScanUsb = { usbRepository.scanUsbPorts() },
                onDismissRequest = { showUsbManagerDialog = false }
            )
        }

        if (!hardwareStatus.isConnected) {
            HardwareDisconnectOverlay(
                hardwareStatus = hardwareStatus,
                retryCount = retryCount,
                maxRetries = 3,
                countdownSeconds = countdownSeconds,
                onConnectScan = {
                    retryCount = 0
                    countdownSeconds = 10
                    usbRepository.scanUsbPorts()
                }
            )
        }
    }
}
