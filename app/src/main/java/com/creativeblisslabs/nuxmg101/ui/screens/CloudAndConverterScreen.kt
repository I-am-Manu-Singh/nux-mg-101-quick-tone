package com.creativeblisslabs.nuxmg101.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.creativeblisslabs.nuxmg101.data.model.CabinetIR
import com.creativeblisslabs.nuxmg101.data.model.CloudPreset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
@Composable
fun CloudAndConverterScreen(
    cloudPresets: List<CloudPreset>,
    onDownloadPreset: (CloudPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Cloud, 1 = Converter
    var irList by remember { mutableStateOf(emptyList<CabinetIR>()) }

// 2. Hold the currently selected/active IR (can be nullable if none selected initially)
    var activeIr by remember { mutableStateOf<CabinetIR?>(null) }
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
Spacer(modifier = Modifier.height(12.dp))
           Text(
                text = "Cloud",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(start = 12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Cloud Presets") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Patch Converter") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Patch " +
                    "Importer") })
        }

        when (selectedTab) {
            0 -> {
                CloudPresetsScreen(cloudPresets = cloudPresets, onDownloadPreset = onDownloadPreset)
            }
            2 -> {
                // Only render if we have an active IR, or pass a default/placeholder item
                activeIr?.let { currentActiveIr ->
                    IRLoaderScreen(
                        irList = irList,
                        activeIr = currentActiveIr,
                        onSelectIR = { selectedIR ->
                            activeIr = selectedIR // Update state when user clicks an IR
                        },
                        onParameterChange = { level, highPass, lowPass ->
                            // Handle slider/knob changes here (e.g., send to Bluetooth or DSP)
                        }
                    )
                } ?: run {
                    // Optional: Show a loading indicator or placeholder if data isn't ready
                    Text("Loading IR Loader...")
                }
            }
            else -> {
                ConverterScreen()
            }
        }
    }
}
