package com.creativeblisslabs.nuxmg101.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.documentfile.provider.DocumentFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var fileNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var outputDirUri by remember { mutableStateOf<Uri?>(null) }

    var targetDevice by remember { mutableStateOf("MG-101") }
    var isConverting by remember { mutableStateOf(false) }

    val recentDownloads = remember { mutableStateListOf<Map<String, String>>() }

    val multipleFilesPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedFiles = uris
            fileNames = uris.map { uri ->
                var name = "UNKNOWN_FILE"
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            name = it.getString(nameIndex)
                        }
                    }
                }
                name
            }
        }
    }

    val dirPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            outputDirUri = uri
            val takeFlags: Int = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(25.dp)
    ) {
        Text(
            text = "BATCH PATCH CONVERTER",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 2.sp
        )

        // 1. SELECT SOURCE
        ConverterSectionGroup("1. SELECT SOURCE FILES") {
            Button(
                onClick = {
                    multipleFilesPicker.launch("*/*")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFiles.isEmpty()) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.FileOpen, contentDescription = null, tint = if (selectedFiles.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (selectedFiles.isEmpty()) "PICK FILES" else "${selectedFiles.size} FILES SELECTED",
                    color = if (selectedFiles.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                )
            }

            if (fileNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                fileNames.forEach { name ->
                    Text(
                        text = "• $name",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // 2. TARGET DEVICE
        ConverterSectionGroup("2. TARGET DEVICE") {
            var expanded by remember { mutableStateOf(false) }
            val options = listOf("MG-101", "MG-300", "MG-400")

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = targetDevice,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("TARGET DEVICE") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                targetDevice = option
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // 3. OUTPUT FOLDER
        ConverterSectionGroup("3. OUTPUT FOLDER") {
            Button(
                onClick = {
                    dirPicker.launch(null)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (outputDirUri == null) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = if (outputDirUri == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (outputDirUri == null) "SELECT FOLDER" else "FOLDER SELECTED",
                    color = if (outputDirUri == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                )
            }
            if (outputDirUri != null) {
                Text(
                    text = "DIR: ${outputDirUri?.lastPathSegment}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // CONVERT & SAVE BUTTON
        Button(
            onClick = {
                if (selectedFiles.isNotEmpty() && outputDirUri != null && !isConverting) {
                    coroutineScope.launch {
                        isConverting = true

                        val ext = when(targetDevice) {
                            "MG-300" -> ".mg300MK2patch"
                            "MG-400" -> ".nuxpreset"
                            "MG-101" -> ".mg101patch"
                            else -> ".patch"
                        }

                        fileNames.forEach { originalName ->
                            delay(500)
                            val baseName = originalName.substringBeforeLast(".")
                            val finalName = "${baseName}_$targetDevice$ext"

                            val dirFile = DocumentFile.fromTreeUri(context, outputDirUri!!)
                            val newFile = dirFile?.createFile("application/octet-stream", finalName)
                            newFile?.uri?.let { destUri ->
                                context.contentResolver.openOutputStream(destUri)?.use { out ->
                                    out.write("CONVERTED PATCH DATA FOR $targetDevice ($originalName)".toByteArray())
                                }
                            }

                            val dateStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                            recentDownloads.add(0, mapOf(
                                "name" to finalName,
                                "device" to targetDevice,
                                "date" to dateStr
                            ))
                        }
                        isConverting = false
                        selectedFiles = emptyList()
                        fileNames = emptyList()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(65.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isConverting) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("CONVERTING...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            } else {
                Text("CONVERT & SAVE BATCH", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = MaterialTheme.colorScheme.outlineVariant)

        Text(
            text = "RECENT CONVERSIONS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        recentDownloads.forEach { dl ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(dl["name"] ?: "", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Text("${dl["device"]} | ${dl["date"]}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ConverterSectionGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(15.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(15.dp))
            .padding(25.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}