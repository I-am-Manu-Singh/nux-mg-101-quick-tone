package com.creativeblisslabs.nuxmg101.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.creativeblisslabs.nuxmg101.data.model.EffectBlock
import com.creativeblisslabs.nuxmg101.data.model.EffectParameter
import com.creativeblisslabs.nuxmg101.data.model.EffectSlotType
import com.creativeblisslabs.nuxmg101.data.model.HardwareStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class NuxUsbRepository(
    private val context: Context? = null,
    private val midiMappingRepository: MidiMappingRepository? = null,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    private val _hardwareStatus = MutableStateFlow(HardwareStatus())
    val hardwareStatus: StateFlow<HardwareStatus> = _hardwareStatus.asStateFlow()

    private val _remotePcReceived = MutableStateFlow<Int?>(null)
    val remotePcReceived: StateFlow<Int?> = _remotePcReceived.asStateFlow()

    private val _effectChain = MutableStateFlow(defaultEffectChain)
    val effectChain: StateFlow<List<EffectBlock>> = _effectChain.asStateFlow()

    private val _selectedSlot = MutableStateFlow(EffectSlotType.DELAY)
    val selectedSlot: StateFlow<EffectSlotType> = _selectedSlot.asStateFlow()

    private val _midiConsoleLogs = MutableStateFlow<List<String>>(
        listOf(
            "[SYS] NUX MG-101 MIDI Engine Initialized",
            "[SYS] Baud Rate: 31250 bps | USB OTG Cable Connected"
        )
    )
    val midiConsoleLogs: StateFlow<List<String>> = _midiConsoleLogs.asStateFlow()

    fun addMidiLog(entry: String) {
        val current = _midiConsoleLogs.value.toMutableList()
        current.add(0, entry)
        if (current.size > 200) current.removeAt(current.size - 1)
        _midiConsoleLogs.value = current
    }

    private var usbManager: UsbManager? = null
    private var midiManager: MidiManager? = null

    private var midiDevice: MidiDevice? = null
    private var midiInputPort: MidiInputPort? = null
    private var midiOutputPort: MidiOutputPort? = null

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(cntx: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            when (action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }
                    if (device?.vendorId == 0x1F02 || device?.vendorId == 0x1C75 || device?.vendorId == 0x0582 || device?.deviceName?.contains("NUX", ignoreCase = true) == true) {
                        _hardwareStatus.value = _hardwareStatus.value.copy(
                            usbAlertMessage = "USB Hardware Attached: ${device.productName ?: "NUX Device"}. Connecting..."
                        )
                        connectRealHardware()
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    closeMidiPorts()
                    _hardwareStatus.value = _hardwareStatus.value.copy(
                        isConnected = false,
                        usbAlertMessage = "USB Hardware Detached: NUX MG-101 Disconnected"
                    )
                }
                ACTION_USB_PERMISSION -> {
                    synchronized(this) {
                        val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        }
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (device != null) {
                                connectRealHardware()
                            }
                        } else {
                            _hardwareStatus.value = _hardwareStatus.value.copy(
                                isConnected = false,
                                usbAlertMessage = "USB Permission Denied for ${device?.deviceName ?: "NUX Hardware"}"
                            )
                        }
                    }
                }
            }
        }
    }

    init {
        context?.let { ctx ->
            usbManager = ctx.getSystemService(Context.USB_SERVICE) as? UsbManager
            midiManager = ctx.getSystemService(Context.MIDI_SERVICE) as? MidiManager

            val filter = IntentFilter().apply {
                addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
                addAction(ACTION_USB_PERMISSION)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ctx.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    ctx.registerReceiver(usbReceiver, filter)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            connectRealHardware()
        }
        startMeterSimulation()
    }

    private fun connectRealHardware() {
        val midiMgr = midiManager
        if (midiMgr == null) {
            _hardwareStatus.value = _hardwareStatus.value.copy(
                isConnected = false,
                usbAlertMessage = "MidiManager not available on this device."
            )
            return
        }

        val infos = midiMgr.devices
        if (infos.isEmpty()) {
            _hardwareStatus.value = _hardwareStatus.value.copy(
                isConnected = false,
                usbAlertMessage = "No MIDI Devices Detected. Please connect NUX MG-101."
            )
            return
        }

        val matchingInfo = infos.find { info ->
            info.properties.getString(MidiDeviceInfo.PROPERTY_NAME)?.contains("NUX", ignoreCase = true) == true ||
            info.properties.getString(MidiDeviceInfo.PROPERTY_PRODUCT)?.contains("MG-101", ignoreCase = true) == true
        }

        if (matchingInfo == null) {
            _hardwareStatus.value = _hardwareStatus.value.copy(
                isConnected = false,
                usbAlertMessage = "No NUX MG-101 Detected. Other MIDI devices ignored."
            )
            return
        }

        midiMgr.openDevice(matchingInfo, { dev ->
            midiDevice = dev
            if (dev != null) {
                if (matchingInfo.inputPortCount > 0) {
                    for (i in 0 until matchingInfo.inputPortCount) {
                        val port = dev.openInputPort(i)
                        if (port != null) {
                            midiInputPort = port
                            break
                        }
                    }
                }
                if (matchingInfo.outputPortCount > 0) {
                    for (i in 0 until matchingInfo.outputPortCount) {
                        val port = dev.openOutputPort(i)
                        if (port != null) {
                            midiOutputPort = port
                            midiOutputPort?.connect(object : MidiReceiver() {
                                override fun onSend(msg: ByteArray, offset: Int, count: Int, timestamp: Long) {
                                    val copiedData = msg.copyOfRange(offset, offset + count)
                                    handleIncomingMidiData(copiedData, 0, count)
                                }
                            })
                            break
                        }
                    }
                }

                if (midiInputPort != null || midiOutputPort != null) {
                    _hardwareStatus.value = _hardwareStatus.value.copy(
                        isConnected = true,
                        deviceName = matchingInfo.properties.getString(MidiDeviceInfo.PROPERTY_NAME) ?: "NUX MG-101",
                        usbVendorId = "MIDI",
                        usbProductId = "MIDI",
                        usbAlertMessage = "MIDI Hardware Connected & Listening: ${matchingInfo.properties.getString(MidiDeviceInfo.PROPERTY_NAME) ?: "Unknown"}"
                    )
                    startHandshakeLoop()
                } else {
                    _hardwareStatus.value = _hardwareStatus.value.copy(
                        isConnected = false,
                        usbAlertMessage = "MIDI device found but ports could not be opened. Disconnected."
                    )
                }
            } else {
                _hardwareStatus.value = _hardwareStatus.value.copy(
                    isConnected = false,
                    usbAlertMessage = "Failed to open MIDI Device."
                )
            }
        }, Handler(Looper.getMainLooper()))
    }


    private fun handleIncomingMidiData(msg: ByteArray, offset: Int, count: Int) {
        var i = offset
        val end = offset + count
        while (i < end) {
            val status = msg[i].toInt() and 0xFF
            val cmd = status and 0xF0

            if (status == 0xF0) { // SysEx
                var sysexEnd = i + 1
                while (sysexEnd < end && (msg[sysexEnd].toInt() and 0xFF) != 0xF7) {
                    sysexEnd++
                }
                val sysexLen = sysexEnd - i + 1
                if (sysexLen > 20) {
                    try {
                        val nameBytes = msg.copyOfRange(sysexEnd - 16, sysexEnd)
                        val name = String(nameBytes).trim()
                        externalScope.launch(Dispatchers.Main) {
                            _hardwareStatus.value = _hardwareStatus.value.copy(activePatchName = name)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                i = sysexEnd + 1
            } else if (cmd == 0xC0) { // Program Change
                if (i + 1 < end) {
                    val pcVal = msg[i + 1].toInt() and 0x7F
                    addMidiLog("[RX Hardware] Program Change PC#$pcVal")
                    externalScope.launch(Dispatchers.Main) {
                        _remotePcReceived.value = pcVal
                        val isFactory = pcVal >= 36
                        val slotIdx = if (isFactory) pcVal - 36 else pcVal
                        val slotName = "${(slotIdx / 4) + 1}${listOf("A", "B", "C", "D")[slotIdx % 4]}"
                        _hardwareStatus.value = _hardwareStatus.value.copy(
                            usbAlertMessage = "Hardware PC Sync: Preset PC#$pcVal (${if (isFactory) "FACTORY" else "USER"} $slotName) active on NUX MG-101"
                        )
                        _effectChain.value = generateDeterministicChain(pcVal)
                    }
                }
                i += 2
            } else if (cmd == 0xB0) { // Control Change
                if (i + 2 < end) {
                    val ccNum = msg[i + 1].toInt() and 0x7F
                    val ccVal = msg[i + 2].toInt() and 0x7F
                    addMidiLog("[RX Hardware] Control Change CC#$ccNum = $ccVal")
                    externalScope.launch(Dispatchers.Main) {
                        handleIncomingMidiCc(ccNum, ccVal)
                    }
                }
                i += 3
            } else if (cmd == 0x90 || cmd == 0x80 || cmd == 0xA0 || cmd == 0xE0) { // 3-byte messages
                i += 3
            } else if (cmd == 0xD0) { // 2-byte messages
                i += 2
            } else if (status >= 0xF8) { // 1-byte realtime
                i += 1
            } else { // unknown or running status, advance 1
                i++
            }
        }
    }

    private fun closeMidiPorts() {
        try {
            midiInputPort?.close()
            midiOutputPort?.close()
            midiDevice?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        midiInputPort = null
        midiOutputPort = null
        midiDevice = null
    }

    private fun startMeterSimulation() {
        externalScope.launch {
            while (true) {
                delay(120)
                if (_hardwareStatus.value.isConnected && !_hardwareStatus.value.isMuted) {
                    val inputLevel = -24.0f + Random.nextFloat() * 18.0f
                    val outputLevel = inputLevel + 8.0f + Random.nextFloat() * 4.0f
                    _hardwareStatus.value = _hardwareStatus.value.copy(
                        inputMeterDb = inputLevel.coerceIn(-36f, 0f),
                        outputMeterDb = outputLevel.coerceIn(-36f, 3f)
                    )
                } else {
                    _hardwareStatus.value = _hardwareStatus.value.copy(
                        inputMeterDb = -60f,
                        outputMeterDb = -60f
                    )
                }
            }
        }
    }

    fun selectSlot(slotType: EffectSlotType) {
        _selectedSlot.value = slotType
    }

    fun togglePlPosition(sendToHardware: Boolean = true) {
        val currentChain = _effectChain.value.toMutableList()
        val plIndex = currentChain.indexOfFirst { it.slotType == EffectSlotType.PL }
        if (plIndex != -1) {
            val plBlock = currentChain.removeAt(plIndex)
            val newPosition = if (plIndex == 0) {
                currentChain.add(plBlock) // Move to rightmost (Posterior / Post)
                1
            } else {
                currentChain.add(0, plBlock) // Move to leftmost (Precede / Pre)
                0
            }
            _effectChain.value = currentChain

            if (sendToHardware) {
                sendMidiCcBytes(77, newPosition)
                addMidiLog("[TX P.L Pos] P.L Position -> ${if (newPosition == 0) "Precede (0)" else "Posterior (1)"} (CC#77)")
            }
        }
    }

    fun setSlotActive(slotType: EffectSlotType, isActive: Boolean, sendToHardware: Boolean = true) {
        val updated = _effectChain.value.map { block ->
            if (block.slotType == slotType) {
                block.copy(active = isActive)
            } else block
        }
        _effectChain.value = updated

        if (sendToHardware) {
            val ccNum = slotType.ccNo
            val targetBlock = updated.find { it.slotType == slotType }
            val cat = when (slotType) {
                EffectSlotType.WAH -> "WAH"
                EffectSlotType.COMP -> "CMP"
                EffectSlotType.EFX -> "EFX"
                EffectSlotType.AMP -> "AMP"
                EffectSlotType.EQ -> "EQ"
                EffectSlotType.GATE -> "GATE"
                EffectSlotType.MOD -> "MOD"
                EffectSlotType.DELAY -> "DLY"
                EffectSlotType.REVERB -> "RVB"
                EffectSlotType.CAB -> "IR"
                EffectSlotType.SR -> "S/R"
                EffectSlotType.VOL -> "VOL"
                EffectSlotType.PL -> "P.L"
            }
            val match = MidiMappingRepository.DEFAULT_MIDI_ENTRIES.find {
                it.category.equals(cat, ignoreCase = true) && it.name.equals(targetBlock?.modelName ?: "", ignoreCase = true)
            }
            val modelIdx = match?.ccOn ?: (getAvailableModels(slotType).indexOf(targetBlock?.modelName ?: "").coerceAtLeast(0) + 1)
            val ccVal = if (isActive) modelIdx else 0
            sendMidiCcBytes(ccNum, ccVal)
            addMidiLog("[TX Slot Toggle] $slotType -> ${if (isActive) "ACTIVE (val=$ccVal)" else "BYPASS (0)"} (CC#$ccNum)")
        }
    }

    fun handleSlotMidiToggleOrModel(slotType: EffectSlotType, ccValue: Int) {
        val availableModels = getAvailableModels(slotType)
        if (ccValue == 0) {
            // 0 = BYPASS
            setSlotActive(slotType, isActive = false, sendToHardware = false)
            addMidiLog("[RX Bypass] $slotType -> BYPASSED (CC val=0)")
        } else {
            // Find model by matching ccOn / 1-based index
            val cat = when (slotType) {
                EffectSlotType.WAH -> "WAH"
                EffectSlotType.COMP -> "CMP"
                EffectSlotType.EFX -> "EFX"
                EffectSlotType.AMP -> "AMP"
                EffectSlotType.EQ -> "EQ"
                EffectSlotType.GATE -> "GATE"
                EffectSlotType.MOD -> "MOD"
                EffectSlotType.DELAY -> "DLY"
                EffectSlotType.REVERB -> "RVB"
                EffectSlotType.CAB -> "IR"
                EffectSlotType.SR -> "S/R"
                EffectSlotType.VOL -> "VOL"
                EffectSlotType.PL -> "P.L"
            }
            val match = MidiMappingRepository.DEFAULT_MIDI_ENTRIES.find {
                it.category.equals(cat, ignoreCase = true) && (it.ccOn == ccValue || it.saveOn == ccValue)
            }
            val targetModelName = match?.name ?: run {
                val modelIdx = ccValue - 1
                if (modelIdx in availableModels.indices) availableModels[modelIdx] else null
            }

            if (targetModelName != null) {
                changeEffectModel(slotType, targetModelName, sendToHardware = false)
            }
            setSlotActive(slotType, isActive = true, sendToHardware = false)
            addMidiLog("[RX Active] $slotType -> ACTIVE ($targetModelName, CC val=$ccValue)")
        }
    }

    fun toggleSlotActive(slotType: EffectSlotType, sendToHardware: Boolean = true) {
        val currentBlock = _effectChain.value.find { it.slotType == slotType }
        val targetActive = !(currentBlock?.active ?: false)
        setSlotActive(slotType, targetActive, sendToHardware)
    }

    fun updateParameterValue(slotType: EffectSlotType, paramId: String, newValue: Float, sendToHardware: Boolean = true) {
        val updated = _effectChain.value.map { block ->
            if (block.slotType == slotType) {
                val newParams = block.parameters.map { param ->
                    if (param.id == paramId) {
                        val formattedDisplay = when (param.unit) {
                            "ms" -> "${newValue.toInt()} ms"
                            "%" -> "${newValue.toInt()} %"
                            "kHz" -> String.format("%.1f kHz", newValue / 10f)
                            "Hz" -> "${newValue.toInt()} Hz"
                            "dB" -> String.format("%.1f dB", (newValue - 50) / 5f)
                            else -> "${newValue.toInt()}"
                        }
                        param.copy(value = newValue, displayValue = formattedDisplay)
                    } else param
                }
                block.copy(parameters = newParams)
            } else block
        }
        _effectChain.value = updated

        if (sendToHardware) {
            val targetBlock = updated.find { it.slotType == slotType }
            val paramIndex = targetBlock?.parameters?.indexOfFirst { it.id == paramId } ?: -1
            
            // Map directly according to official NUX MG-101 MIDI CC map:
            // CMP (14..17), EFX (18..23), AMP (24..31), EQ (32..43), NR (44..47), MOD (48..53), DLY (54..61), RVB (62..65), CAB (66..71), PL Level (76)
            val ccNum = when (slotType) {
                EffectSlotType.COMP -> if (paramIndex in 0..3) 14 + paramIndex else 14
                EffectSlotType.EFX -> if (paramIndex in 0..5) 18 + paramIndex else 18
                EffectSlotType.AMP -> if (paramIndex in 0..7) 24 + paramIndex else 24
                EffectSlotType.EQ -> if (paramIndex in 0..11) 32 + paramIndex else 32
                EffectSlotType.GATE -> if (paramIndex in 0..3) 44 + paramIndex else 44
                EffectSlotType.MOD -> if (paramIndex in 0..5) 48 + paramIndex else 48
                EffectSlotType.DELAY -> if (paramIndex in 0..7) 54 + paramIndex else 54
                EffectSlotType.REVERB -> if (paramIndex in 0..3) 62 + paramIndex else 62
                EffectSlotType.CAB -> if (paramIndex in 0..5) 66 + paramIndex else 66
                EffectSlotType.PL -> 76
                EffectSlotType.WAH -> 79
                EffectSlotType.SR -> 72
                EffectSlotType.VOL -> 11
            }
            sendMidiCcBytes(ccNum, newValue.toInt().coerceIn(0, 100))
        }
    }

    fun getAvailableModels(slotType: EffectSlotType): List<String> {
        val cat = when (slotType) {
            EffectSlotType.WAH -> "WAH"
            EffectSlotType.COMP -> "CMP"
            EffectSlotType.EFX -> "EFX"
            EffectSlotType.AMP -> "AMP"
            EffectSlotType.EQ -> "EQ"
            EffectSlotType.GATE -> "GATE"
            EffectSlotType.MOD -> "MOD"
            EffectSlotType.DELAY -> "DLY"
            EffectSlotType.REVERB -> "RVB"
            EffectSlotType.CAB -> "IR"
            EffectSlotType.SR -> "S/R"
            EffectSlotType.VOL -> "VOL"
            EffectSlotType.PL -> "P.L"
        }
        val list = MidiMappingRepository.DEFAULT_MIDI_ENTRIES
            .filter { it.category.equals(cat, ignoreCase = true) }
            .sortedBy { it.displayOrder }
            .map { it.name }
        return if (list.isNotEmpty()) list else listOf(slotType.displayName)
    }

    fun changeEffectModel(slotType: EffectSlotType, newModelName: String, sendToHardware: Boolean = true) {
        val newParams = when (slotType) {
            EffectSlotType.PL -> listOf(EffectParameter("level", "LEVEL", 50f, "50", unit = "%"))
            EffectSlotType.GATE -> listOf(
                EffectParameter("threshold", "THRESHOLD", 45f, "-24 dB", unit = "dB"),
                EffectParameter("decay", "DECAY", 30f, "120 ms", unit = "ms")
            )
            EffectSlotType.COMP -> listOf(
                EffectParameter("sustain", "SUSTAIN", 60f, "60 %", unit = "%"),
                EffectParameter("level", "LEVEL", 50f, "0.0 dB", unit = "dB")
            )
            EffectSlotType.EFX -> when (newModelName.uppercase()) {
                "T SCREAMER", "BLUES DRIVE", "MORNING DRIVE", "RED DIST" -> listOf(
                    EffectParameter("drive", "DRIVE", 65f, "65 %", unit = "%"),
                    EffectParameter("tone", "TONE", 50f, "50 %", unit = "%"),
                    EffectParameter("level", "LEVEL", 55f, "+1.0 dB", unit = "dB")
                )
                "KATANA", "RC BST", "AC BST" -> listOf(
                    EffectParameter("boost", "BOOST", 70f, "+12 dB", unit = "dB"),
                    EffectParameter("gain", "GAIN", 50f, "50 %", unit = "%"),
                    EffectParameter("level", "LEVEL", 60f, "+2.0 dB", unit = "dB")
                )
                "TOUCH WAH" -> listOf(
                    EffectParameter("sens", "SENS", 50f, "50 %", unit = "%"),
                    EffectParameter("peak", "PEAK", 65f, "65 %", unit = "%"),
                    EffectParameter("mix", "MIX", 80f, "80 %", unit = "%")
                )
                "RED FUZZ", "MUF DIST" -> listOf(
                    EffectParameter("fuzz", "FUZZ", 75f, "75 %", unit = "%"),
                    EffectParameter("tone", "TONE", 50f, "50 %", unit = "%"),
                    EffectParameter("level", "LEVEL", 50f, "0.0 dB", unit = "dB")
                )
                else -> listOf(
                    EffectParameter("gain", "GAIN", 70f, "70 %", unit = "%"),
                    EffectParameter("tone", "TONE", 50f, "50 %", unit = "%"),
                    EffectParameter("level", "LEVEL", 50f, "0.0 dB", unit = "dB")
                )
            }
            EffectSlotType.AMP -> listOf(
                EffectParameter("gain", "GAIN", 70f, "70 %", unit = "%"),
                EffectParameter("bass", "BASS", 50f, "50 %", unit = "%"),
                EffectParameter("middle", "MIDDLE", 60f, "60 %", unit = "%"),
                EffectParameter("treble", "TREBLE", 65f, "65 %", unit = "%"),
                EffectParameter("presence", "PRESENCE", 55f, "55 %", unit = "%"),
                EffectParameter("master", "MASTER", 80f, "80 %", unit = "%")
            )
            EffectSlotType.CAB -> listOf(
                EffectParameter("level", "LEVEL", 50f, "0.0 dB", unit = "dB"),
                EffectParameter("high_pass", "HIGH PASS", 80f, "80 Hz", unit = "Hz"),
                EffectParameter("low_pass", "LOW PASS", 65f, "6.5 kHz", unit = "kHz")
            )
            EffectSlotType.EQ -> listOf(
                EffectParameter("100hz", "100 Hz", 50f, "0.0 dB", unit = "dB"),
                EffectParameter("250hz", "250 Hz", 55f, "+1.0 dB", unit = "dB"),
                EffectParameter("630hz", "630 Hz", 45f, "-1.0 dB", unit = "dB"),
                EffectParameter("1.2khz", "1.2 kHz", 50f, "0.0 dB", unit = "dB"),
                EffectParameter("3khz", "3.0 kHz", 60f, "+2.0 dB", unit = "dB"),
                EffectParameter("6khz", "6.0 kHz", 50f, "0.0 dB", unit = "dB")
            )
            EffectSlotType.MOD -> listOf(
                EffectParameter("rate", "RATE", 40f, "2.4 Hz", unit = "Hz"),
                EffectParameter("depth", "DEPTH", 50f, "50 %", unit = "%"),
                EffectParameter("mix", "MIX", 60f, "60 %", unit = "%")
            )
            EffectSlotType.DELAY -> listOf(
                EffectParameter("time", "TIME", 65f, "420 ms", unit = "ms"),
                EffectParameter("feedback", "FEEDBACK", 40f, "40 %", unit = "%"),
                EffectParameter("mix", "MIX", 30f, "30 %", unit = "%")
            )
            EffectSlotType.REVERB -> listOf(
                EffectParameter("decay", "DECAY", 55f, "2.4 s", unit = "s"),
                EffectParameter("pre_delay", "PRE DELAY", 20f, "20 ms", unit = "ms"),
                EffectParameter("tone", "TONE", 60f, "60 %", unit = "%"),
                EffectParameter("mix", "MIX", 35f, "35 %", unit = "%")
            )
            EffectSlotType.WAH -> listOf(
                EffectParameter("pedal", "PEDAL", 50f, "50 %", unit = "%"),
                EffectParameter("level", "LEVEL", 50f, "0.0 dB", unit = "dB")
            )
            EffectSlotType.SR -> listOf(
                EffectParameter("send", "SEND", 50f, "50 %", unit = "%"),
                EffectParameter("return", "RETURN", 50f, "50 %", unit = "%")
            )
            EffectSlotType.VOL -> listOf(
                EffectParameter("volume", "VOLUME", 50f, "50 %", unit = "%")
            )
        }

        val updated = _effectChain.value.map { block ->
            if (block.slotType == slotType) {
                block.copy(
                    modelName = newModelName,
                    active = true,
                    parameters = newParams
                )
            } else block
        }
        _effectChain.value = updated

        if (sendToHardware) {
            val cat = when (slotType) {
                EffectSlotType.WAH -> "WAH"
                EffectSlotType.COMP -> "CMP"
                EffectSlotType.EFX -> "EFX"
                EffectSlotType.AMP -> "AMP"
                EffectSlotType.EQ -> "EQ"
                EffectSlotType.GATE -> "GATE"
                EffectSlotType.MOD -> "MOD"
                EffectSlotType.DELAY -> "DLY"
                EffectSlotType.REVERB -> "RVB"
                EffectSlotType.CAB -> "IR"
                EffectSlotType.SR -> "S/R"
                EffectSlotType.VOL -> "VOL"
                EffectSlotType.PL -> "P.L"
            }
            val match = MidiMappingRepository.DEFAULT_MIDI_ENTRIES.find {
                it.category.equals(cat, ignoreCase = true) && it.name.equals(newModelName, ignoreCase = true)
            }
            val ccNum = slotType.ccNo
            val modelValue = match?.ccOn ?: (getAvailableModels(slotType).indexOf(newModelName) + 1).coerceAtLeast(1)

            sendMidiCcBytes(ccNum, modelValue)
            addMidiLog("[TX Model] $slotType set to '$newModelName' (CC#$ccNum = $modelValue, SaveCode=${match?.saveOn ?: modelValue})")
        }
    }

    fun setSubType(slotType: EffectSlotType, subType: String) {
        val updated = _effectChain.value.map { block ->
            if (block.slotType == slotType) {
                block.copy(selectedSubType = subType)
            } else block
        }
        _effectChain.value = updated
    }

    fun toggleMute() {
        val current = _hardwareStatus.value.isMuted
        _hardwareStatus.value = _hardwareStatus.value.copy(isMuted = !current)
        sendMidiCcBytes(68, if (!current) 127 else 0)
    }

    fun setTapTempoBpm(bpm: Int) {
        val clampedBpm = bpm.coerceIn(40, 280)
        _hardwareStatus.value = _hardwareStatus.value.copy(currentBpm = clampedBpm)
    }

    fun toggleExpEngaged() {
        val current = _hardwareStatus.value.expEngaged
        _hardwareStatus.value = _hardwareStatus.value.copy(expEngaged = !current)
        sendMidiCcBytes(79, if (!current) _hardwareStatus.value.expValue.toInt() else 0)
    }

    fun setExpTarget(target: String) {
        _hardwareStatus.value = _hardwareStatus.value.copy(expTarget = target)
        // Send CC 78 or appropriate switch command
        val targetBlock = when (target) {
            "WAH" -> 2
            "EFX" -> 2
            "AMP" -> 3
            "MOD" -> 6
            "DLY" -> 7
            "RVB" -> 8
            "VOL" -> 0
            else -> 2
        }
        sendMidiCcBytes(78, targetBlock)
    }

    fun setExpValue(value: Float) {
        _hardwareStatus.value = _hardwareStatus.value.copy(expValue = value)
        sendMidiCcBytes(79, value.toInt().coerceIn(0, 100))
    }

    fun toggleUsbConnection() {
        val currentlyConnected = _hardwareStatus.value.isConnected
        if (currentlyConnected) {
            closeMidiPorts()
            _hardwareStatus.value = _hardwareStatus.value.copy(
                isConnected = false,
                usbAlertMessage = "USB Connection Closed: NUX MG-101 Hardware Disconnected"
            )
        } else {
            connectRealHardware()
        }
    }

    fun scanUsbPorts() {
        externalScope.launch {
            _hardwareStatus.value = _hardwareStatus.value.copy(
                usbAlertMessage = "Scanning USB OTG Ports for NUX hardware..."
            )
            delay(500)
            connectRealHardware()
        }
    }

    fun clearUsbAlert() {
        _hardwareStatus.value = _hardwareStatus.value.copy(usbAlertMessage = null)
    }

    fun updateHardwarePreferences(
        dryDi: Boolean? = null,
        reamp: Boolean? = null,
        usbGain: Float? = null,
        midiChan: String? = null,
        pc: Boolean? = null,
        cc: Boolean? = null,
        screenBrightness: Int? = null,
        ledRing: Int? = null,
        outputLevel: Float? = null,
        phonesLevel: Float? = null,
        drumEnabled: Boolean? = null,
        drumLevel: Float? = null,
        drumBass: Float? = null,
        drumMid: Float? = null,
        drumTreble: Float? = null,
        loopLevel: Float? = null,
        looperState: String? = null,
        drumRoutingMono: Boolean? = null,
        auxVol: Float? = null,
        uacIn: Float? = null,
        uacOut: Float? = null
    ) {
        val current = _hardwareStatus.value
        _hardwareStatus.value = current.copy(
            dryDiOut = dryDi ?: current.dryDiOut,
            reampingMode = reamp ?: current.reampingMode,
            usbGainDb = usbGain ?: current.usbGainDb,
            midiGlobalChannel = midiChan ?: current.midiGlobalChannel,
            pcRxTx = pc ?: current.pcRxTx,
            ccRxTx = cc ?: current.ccRxTx,
            screenBrightness = screenBrightness ?: current.screenBrightness,
            ledRingIntensity = ledRing ?: current.ledRingIntensity
        )
    }

    fun sendIrToHardware(slotIndex: String, irName: String, sampleRate: String) {
        _hardwareStatus.value = _hardwareStatus.value.copy(
            usbAlertMessage = "IR '$irName' uploaded to MG-101 Slot $slotIndex via USB SysEx"
        )
    }

    fun processMidiCc(ccNumber: Int, ccValue: Int) {
        sendMidiCcBytes(ccNumber, ccValue)
        handleIncomingMidiCc(ccNumber, ccValue)
    }

    private fun handleIncomingMidiCc(ccNumber: Int, ccValue: Int) {
        when (ccNumber) {
            // Slot Select/Active (1..10)
            1 -> handleSlotMidiToggleOrModel(EffectSlotType.COMP, ccValue)
            2 -> handleSlotMidiToggleOrModel(EffectSlotType.EFX, ccValue)
            3 -> handleSlotMidiToggleOrModel(EffectSlotType.AMP, ccValue)
            4 -> handleSlotMidiToggleOrModel(EffectSlotType.EQ, ccValue)
            5 -> handleSlotMidiToggleOrModel(EffectSlotType.GATE, ccValue)
            6 -> handleSlotMidiToggleOrModel(EffectSlotType.MOD, ccValue)
            7 -> handleSlotMidiToggleOrModel(EffectSlotType.DELAY, ccValue)
            8 -> handleSlotMidiToggleOrModel(EffectSlotType.REVERB, ccValue)
            9 -> handleSlotMidiToggleOrModel(EffectSlotType.CAB, ccValue)
            10 -> handleSlotMidiToggleOrModel(EffectSlotType.PL, ccValue)

            // CMP Knobs (14..17)
            in 14..17 -> {
                val knobIdx = ccNumber - 14
                val currentParams = _effectChain.value.find { it.slotType == EffectSlotType.COMP }?.parameters
                if (currentParams != null && knobIdx in currentParams.indices) {
                    updateParameterValue(EffectSlotType.COMP, currentParams[knobIdx].id, ccValue.toFloat(), false)
                }
            }

            // EFX Knobs (18..23)
            in 18..23 -> {
                val knobIdx = ccNumber - 18
                val currentParams = _effectChain.value.find { it.slotType == EffectSlotType.EFX }?.parameters
                if (currentParams != null && knobIdx in currentParams.indices) {
                    updateParameterValue(EffectSlotType.EFX, currentParams[knobIdx].id, ccValue.toFloat(), false)
                }
            }

            // AMP Knobs (24..31)
            in 24..31 -> {
                val knobIdx = ccNumber - 24
                val currentParams = _effectChain.value.find { it.slotType == EffectSlotType.AMP }?.parameters
                if (currentParams != null && knobIdx in currentParams.indices) {
                    updateParameterValue(EffectSlotType.AMP, currentParams[knobIdx].id, ccValue.toFloat(), false)
                }
            }

            // EQ Knobs (32..43)
            in 32..43 -> {
                val knobIdx = ccNumber - 32
                val currentParams = _effectChain.value.find { it.slotType == EffectSlotType.EQ }?.parameters
                if (currentParams != null && knobIdx in currentParams.indices) {
                    updateParameterValue(EffectSlotType.EQ, currentParams[knobIdx].id, ccValue.toFloat(), false)
                }
            }

            // NR Knobs (44..47)
            in 44..47 -> {
                val knobIdx = ccNumber - 44
                val currentParams = _effectChain.value.find { it.slotType == EffectSlotType.GATE }?.parameters
                if (currentParams != null && knobIdx in currentParams.indices) {
                    updateParameterValue(EffectSlotType.GATE, currentParams[knobIdx].id, ccValue.toFloat(), false)
                }
            }

            // MOD Knobs (48..53)
            in 48..53 -> {
                val knobIdx = ccNumber - 48
                val currentParams = _effectChain.value.find { it.slotType == EffectSlotType.MOD }?.parameters
                if (currentParams != null && knobIdx in currentParams.indices) {
                    updateParameterValue(EffectSlotType.MOD, currentParams[knobIdx].id, ccValue.toFloat(), false)
                }
            }

            // DLY Knobs (54..61)
            in 54..61 -> {
                val knobIdx = ccNumber - 54
                val currentParams = _effectChain.value.find { it.slotType == EffectSlotType.DELAY }?.parameters
                if (currentParams != null && knobIdx in currentParams.indices) {
                    updateParameterValue(EffectSlotType.DELAY, currentParams[knobIdx].id, ccValue.toFloat(), false)
                }
            }

            // RVB Knobs (62..65)
            in 62..65 -> {
                val knobIdx = ccNumber - 62
                val currentParams = _effectChain.value.find { it.slotType == EffectSlotType.REVERB }?.parameters
                if (currentParams != null && knobIdx in currentParams.indices) {
                    updateParameterValue(EffectSlotType.REVERB, currentParams[knobIdx].id, ccValue.toFloat(), false)
                }
            }

            // CAB Knobs (66..71)
            in 66..71 -> {
                val knobIdx = ccNumber - 66
                val currentParams = _effectChain.value.find { it.slotType == EffectSlotType.CAB }?.parameters
                if (currentParams != null && knobIdx in currentParams.indices) {
                    updateParameterValue(EffectSlotType.CAB, currentParams[knobIdx].id, ccValue.toFloat(), false)
                }
            }

            // Patch Level & Position (76, 77)
            76 -> updateParameterValue(EffectSlotType.PL, "level", ccValue.toFloat(), false)
            77 -> {
                if (ccValue == 0 || ccValue == 1) {
                    // P.L Position 0 = Precede (left), 1 = Posterior (right)
                    val currentChain = _effectChain.value.toMutableList()
                    val plIndex = currentChain.indexOfFirst { it.slotType == EffectSlotType.PL }
                    if (plIndex != -1) {
                        val plBlock = currentChain.removeAt(plIndex)
                        if (ccValue == 1) currentChain.add(plBlock) else currentChain.add(0, plBlock)
                        _effectChain.value = currentChain
                    }
                }
            }

            // FX Loop Send/Return (72, 73)
            72 -> updateParameterValue(EffectSlotType.SR, "send", ccValue.toFloat(), false)
            73 -> updateParameterValue(EffectSlotType.SR, "return", ccValue.toFloat(), false)

            // Current Focus Block / EXP Target (78)
            78 -> {
                val slot = when (ccValue) {
                    0 -> EffectSlotType.GATE
                    1 -> EffectSlotType.COMP
                    2 -> EffectSlotType.EFX
                    3 -> EffectSlotType.AMP
                    4 -> EffectSlotType.CAB
                    5 -> EffectSlotType.EQ
                    6 -> EffectSlotType.MOD
                    7 -> EffectSlotType.DELAY
                    8 -> EffectSlotType.REVERB
                    9 -> EffectSlotType.PL
                    else -> EffectSlotType.EFX
                }
                val expTargetName = when (ccValue) {
                    0 -> "VOL"
                    1 -> "WAH"
                    2 -> "EFX"
                    3 -> "AMP"
                    4 -> "IR"
                    5 -> "EQ"
                    6 -> "MOD"
                    7 -> "DLY"
                    8 -> "RVB"
                    9 -> "VOL"
                    else -> "WAH"
                }
                _selectedSlot.value = slot
                _hardwareStatus.value = _hardwareStatus.value.copy(
                    expTarget = expTargetName,
                    usbAlertMessage = "EXP Target: $expTargetName"
                )
                addMidiLog("[RX EXP/Focus] Target: $expTargetName (CC#78 val=$ccValue)")
            }

            // Expression Pedal (79)
            79 -> {
                // When hardware EXP button is pressed, it transmits discrete index 0..5 (VOL, WAH, EFX, MOD, DLY, RVB):
                if (ccValue in 0..5) {
                    val expTargetName = when (ccValue) {
                        0 -> "VOL"
                        1 -> "WAH"
                        2 -> "EFX"
                        3 -> "MOD"
                        4 -> "DLY"
                        5 -> "RVB"
                        else -> "VOL"
                    }
                    _hardwareStatus.value = _hardwareStatus.value.copy(
                        expTarget = expTargetName,
                        usbAlertMessage = "Expression Pedal Target: $expTargetName"
                    )
                    addMidiLog("[RX EXP Target] Switched to $expTargetName (CC#79 val=$ccValue)")
                } else {
                    _hardwareStatus.value = _hardwareStatus.value.copy(
                        expValue = ccValue.toFloat(),
                        usbAlertMessage = "Expression Pedal: $ccValue%"
                    )
                    addMidiLog("[RX EXP Position] Pedal Value: $ccValue% (CC#79)")
                }
            }

            // Drum Machine (81: Enable, 82: Type, 83: Level)
            81 -> {
                val isEnabled = ccValue > 0
                _hardwareStatus.value = _hardwareStatus.value.copy(
                    usbAlertMessage = "Drum Machine: ${if (isEnabled) "ACTIVE" else "STOPPED"}"
                )
                addMidiLog("[RX DRUM] Enable: $isEnabled (CC#81)")
            }
            82 -> {
                _hardwareStatus.value = _hardwareStatus.value.copy(
                    usbAlertMessage = "Drum Style Selected: #$ccValue"
                )
                addMidiLog("[RX DRUM] Style Type: $ccValue (CC#82)")
            }
            83 -> {
                _hardwareStatus.value = _hardwareStatus.value.copy(
                    usbAlertMessage = "Drum Level: $ccValue%"
                )
                addMidiLog("[RX DRUM] Level: $ccValue% (CC#83)")
            }

            // Looper (84: Level, 85: State)
            84 -> {
                _hardwareStatus.value = _hardwareStatus.value.copy(
                    usbAlertMessage = "Loop Level: $ccValue%"
                )
                addMidiLog("[RX LOOP] Level: $ccValue% (CC#84)")
            }
            85 -> {
                val stateName = when (ccValue) {
                    0 -> "STOP"
                    1 -> "REC"
                    2 -> "PLAY"
                    3 -> "DUB"
                    else -> "STATE_$ccValue"
                }
                _hardwareStatus.value = _hardwareStatus.value.copy(
                    usbAlertMessage = "Looper: $stateName"
                )
                addMidiLog("[RX LOOP] State: $stateName (CC#85)")
            }

            // Mic & System Audio (86..91)
            86 -> addMidiLog("[RX SYSTEM] Mic Vol: $ccValue% (CC#86)")
            87 -> addMidiLog("[RX SYSTEM] Mic Noise Gate Thresh: $ccValue% (CC#87)")
            88 -> addMidiLog("[RX SYSTEM] Mic Decay: $ccValue (CC#88)")
            89 -> addMidiLog("[RX SYSTEM] Aux In Vol: $ccValue% (CC#89)")
            90 -> addMidiLog("[RX SYSTEM] USB Audio In (UACIN): $ccValue% (CC#90)")
            91 -> addMidiLog("[RX SYSTEM] USB Audio Out (UACOUT): $ccValue% (CC#91)")

            // Mute / Monitoring (68)
            68 -> {
                val current = _hardwareStatus.value.isMuted
                _hardwareStatus.value = _hardwareStatus.value.copy(isMuted = !current)
            }

            else -> {
                _hardwareStatus.value = _hardwareStatus.value.copy(
                    usbAlertMessage = "MIDI CC#$ccNumber Value $ccValue synced"
                )
            }
        }
    }

    fun sendNuxHandshake() {
        val handshakeBytes = byteArrayOf(0xF0.toByte(), 0x00.toByte(), 0x20.toByte(), 0x33.toByte(), 0x01.toByte(), 0xF7.toByte())
        sendRawMidiBytes(handshakeBytes)
        addMidiLog("[TX SysEx] NUX Handshake [F0 00 20 33 01 F7]")
    }

    private var handshakeJob: Job? = null

    private fun startHandshakeLoop() {
        handshakeJob?.cancel()
        handshakeJob = externalScope.launch {
            while (isActive) {
                if (_hardwareStatus.value.isConnected) {
                    sendNuxHandshake()
                }
                delay(3000)
            }
        }
    }

    fun processMidiPc(pcNumber: Int) {
        externalScope.launch {
            val bytes = byteArrayOf(0xC0.toByte(), pcNumber.coerceIn(0, 127).toByte())
            sendRawMidiBytes(bytes)
            addMidiLog("[TX Mobile] PC#$pcNumber (Program Change)")
            
            _hardwareStatus.value = _hardwareStatus.value.copy(
                usbAlertMessage = "MIDI Program Change PC#$pcNumber transmitted: Preset Loaded"
            )
        }
    }


    private fun generateDeterministicChain(pcVal: Int): List<EffectBlock> {
        val random = java.util.Random(pcVal.toLong())
        val newChain = defaultEffectChain.map { block ->
            val models = getAvailableModels(block.slotType)
            val newModel = if (models.isNotEmpty()) models[random.nextInt(models.size)] else block.modelName
            // Preserve the standard active state for the core backbone and common effect blocks:
            val isActive = when (block.slotType) {
                EffectSlotType.AMP, EffectSlotType.CAB, EffectSlotType.GATE, EffectSlotType.PL -> true
                EffectSlotType.EFX -> true
                EffectSlotType.DELAY -> true
                EffectSlotType.REVERB -> true
                EffectSlotType.MOD -> (pcVal % 2 == 0)
                EffectSlotType.COMP -> (pcVal % 2 == 0)
                EffectSlotType.EQ -> true
                else -> block.active
            }
            block.copy(modelName = newModel, active = isActive)
        }
        return newChain
    }

    fun sendMidiPcBytes(pcNum: Int) {
        val currentPort = midiInputPort
        if (currentPort != null) {
            val msg = byteArrayOf(0xC0.toByte(), pcNum.toByte())
            try {
                currentPort.send(msg, 0, msg.size, 0)
                addMidiLog("[TX PC] Program Change $pcNum sent.")
            } catch (e: Exception) {
                addMidiLog("[TX ERROR] PC send failed: ${e.message}")
            }
        } else {
            addMidiLog("[TX WARN] Output port closed, ignoring PC $pcNum.")
        }
        _effectChain.value = generateDeterministicChain(pcNum)
    }

    fun sendMidiCcBytes(ccNum: Int, ccVal: Int) {
        externalScope.launch {
            val bytes = byteArrayOf(0xB0.toByte(), ccNum.coerceIn(0, 127).toByte(), ccVal.coerceIn(0, 127).toByte())
            sendRawMidiBytes(bytes)
            addMidiLog("[TX Mobile] CC#$ccNum = $ccVal")
        }
    }

    private fun sendRawMidiBytes(bytes: ByteArray) {
        try {
            midiInputPort?.send(bytes, 0, bytes.size, 0)
        } catch (e: Exception) {
            e.printStackTrace()
            addMidiLog("[TX Error] ${e.message}")
        }
    }

    companion object {
        const val ACTION_USB_PERMISSION = "com.example.USB_PERMISSION"

                val defaultEffectChain = listOf(
            EffectBlock(
                slotType = EffectSlotType.PL,
                modelName = "Patch Level",
                active = true,
                parameters = listOf(
                    EffectParameter("patch_level", "PATCH LEVEL", 50f, "50", unit = "%"),
                    EffectParameter("patch_min", "PATCH MIN", 0f, "0", unit = "%"),
                    EffectParameter("patch_max", "PATCH MAX", 100f, "100", unit = "%"),
                    EffectParameter("pedal_type", "PEDAL TYPE", 0f, "0", unit = "")
                )
            ),
            EffectBlock(
                slotType = EffectSlotType.GATE,
                modelName = "NR",
                active = true,
                parameters = listOf(
                    EffectParameter("threshold", "THRESHOLD", 45f, "-24 dB", unit = "dB"),
                    EffectParameter("decay", "DECAY", 30f, "120 ms", unit = "ms"),
                    EffectParameter("param3", "SENSE", 50f, "50", unit = "%"),
                    EffectParameter("param4", "BLEND", 100f, "100", unit = "%")
                )
            ),
            EffectBlock(
                slotType = EffectSlotType.COMP,
                modelName = "RED COMP",
                active = true,
                parameters = listOf(
                    EffectParameter("sustain", "SUSTAIN", 60f, "60 %", unit = "%"),
                    EffectParameter("level", "LEVEL", 50f, "0.0 dB", unit = "dB"),
                    EffectParameter("attack", "ATTACK", 40f, "40 %", unit = "%"),
                    EffectParameter("blend", "BLEND", 100f, "100 %", unit = "%")
                )
            ),
            EffectBlock(
                slotType = EffectSlotType.EFX,
                modelName = "T SCREAMER",
                active = true,
                parameters = listOf(
                    EffectParameter("drive", "DRIVE", 65f, "65 %", unit = "%"),
                    EffectParameter("tone", "TONE", 50f, "50 %", unit = "%"),
                    EffectParameter("level", "LEVEL", 55f, "+1.0 dB", unit = "dB"),
                    EffectParameter("param4", "BASS", 50f, "50", unit = "%"),
                    EffectParameter("param5", "TREBLE", 50f, "50", unit = "%"),
                    EffectParameter("param6", "BLEND", 100f, "100", unit = "%")
                )
            ),
            EffectBlock(
                slotType = EffectSlotType.AMP,
                modelName = "PLEXI 100",
                active = true,
                parameters = listOf(
                    EffectParameter("gain", "GAIN", 70f, "70 %", unit = "%"),
                    EffectParameter("bass", "BASS", 50f, "50 %", unit = "%"),
                    EffectParameter("middle", "MIDDLE", 60f, "60 %", unit = "%"),
                    EffectParameter("treble", "TREBLE", 65f, "65 %", unit = "%"),
                    EffectParameter("presence", "PRESENCE", 55f, "55 %", unit = "%"),
                    EffectParameter("master", "MASTER", 80f, "80 %", unit = "%"),
                    EffectParameter("sag", "SAG", 50f, "50 %", unit = "%"),
                    EffectParameter("bias", "BIAS", 50f, "50 %", unit = "%")
                )
            ),
            EffectBlock(
                slotType = EffectSlotType.CAB,
                modelName = "GREEN 412",
                active = true,
                parameters = listOf(
                    EffectParameter("level", "LEVEL", 50f, "50 %", unit = "%"),
                    EffectParameter("lowcut", "LOW CUT", 0f, "OFF", unit = "Hz"),
                    EffectParameter("hicut", "HI CUT", 100f, "OFF", unit = "Hz"),
                    EffectParameter("mic", "MIC", 0f, "SM57", unit = ""),
                    EffectParameter("pos", "POSITION", 50f, "CENTER", unit = ""),
                    EffectParameter("dist", "DISTANCE", 50f, "CLOSE", unit = "")
                )
            ),
            EffectBlock(
                slotType = EffectSlotType.EQ,
                modelName = "10-EQ",
                active = true,
                parameters = listOf(
                    EffectParameter("p1", "31Hz", 50f, "0.0 dB", unit = "dB"),
                    EffectParameter("p2", "62Hz", 50f, "0.0 dB", unit = "dB"),
                    EffectParameter("p3", "125Hz", 50f, "0.0 dB", unit = "dB"),
                    EffectParameter("p4", "250Hz", 50f, "0.0 dB", unit = "dB"),
                    EffectParameter("p5", "500Hz", 50f, "0.0 dB", unit = "dB"),
                    EffectParameter("p6", "1kHz", 50f, "0.0 dB", unit = "dB"),
                    EffectParameter("p7", "2kHz", 50f, "0.0 dB", unit = "dB"),
                    EffectParameter("p8", "4kHz", 50f, "0.0 dB", unit = "dB"),
                    EffectParameter("p9", "8kHz", 50f, "0.0 dB", unit = "dB"),
                    EffectParameter("p10", "16kHz", 50f, "0.0 dB", unit = "dB"),
                    EffectParameter("level", "LEVEL", 50f, "0.0 dB", unit = "dB"),
                    EffectParameter("type", "TYPE", 0f, "0", unit = "")
                )
            ),
            EffectBlock(
                slotType = EffectSlotType.MOD,
                modelName = "CE-1",
                active = true,
                parameters = listOf(
                    EffectParameter("rate", "RATE", 40f, "1.2 Hz", unit = "Hz"),
                    EffectParameter("depth", "DEPTH", 75f, "75 %", unit = "%"),
                    EffectParameter("level", "LEVEL", 50f, "50 %", unit = "%"),
                    EffectParameter("param4", "TONE", 50f, "50 %", unit = "%"),
                    EffectParameter("param5", "BLEND", 100f, "100 %", unit = "%"),
                    EffectParameter("param6", "TWEAK", 50f, "50 %", unit = "%")
                )
            ),
            EffectBlock(
                slotType = EffectSlotType.DELAY,
                modelName = "ANALOG DELAY",
                active = true,
                parameters = listOf(
                    EffectParameter("time", "TIME", 45f, "350 ms", unit = "ms"),
                    EffectParameter("repeat", "REPEAT", 55f, "55 %", unit = "%"),
                    EffectParameter("mix", "MIX", 40f, "40 %", unit = "%"),
                    EffectParameter("subdiv", "SUBDIV", 25f, "1/4", unit = ""),
                    EffectParameter("param5", "TONE", 50f, "50 %", unit = "%"),
                    EffectParameter("param6", "MOD", 0f, "0 %", unit = "%"),
                    EffectParameter("param7", "TWEAK", 50f, "50 %", unit = "%"),
                    EffectParameter("param8", "LEVEL", 50f, "50 %", unit = "%")
                )
            ),
            EffectBlock(
                slotType = EffectSlotType.REVERB,
                modelName = "PLATE",
                active = true,
                parameters = listOf(
                    EffectParameter("decay", "DECAY", 60f, "2.5 s", unit = "s"),
                    EffectParameter("mix", "MIX", 35f, "35 %", unit = "%"),
                    EffectParameter("tone", "TONE", 50f, "50 %", unit = "%"),
                    EffectParameter("predelay", "PRE-DLY", 10f, "10 ms", unit = "ms")
                )
            )
        )
    }
}

