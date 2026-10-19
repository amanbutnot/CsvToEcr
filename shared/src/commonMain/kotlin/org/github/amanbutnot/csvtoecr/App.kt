package org.github.amanbutnot.csvtoecr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        var selectedFileName by remember { mutableStateOf<String?>(null) }
        var convertedText by remember { mutableStateOf("") }
        var statusMessage by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        val filePickerLauncher = rememberFilePickerLauncher(
            type = PickerType.File(listOf("csv")),
            mode = PickerMode.Single
        ) { file ->
            file?.let {
                scope.launch {
                    try {
                        selectedFileName = it.name
                        val bytes = it.readBytes()
                        val text = bytes.decodeToString()
                        val converted = text.substringAfter('\n').replace(",", "#~#").replace("'", "")
                        convertedText = converted
                        statusMessage = "Converted successfully"
                    } catch (e: Exception) {
                        statusMessage = "Error reading file: ${e.message}"
                    }
                }
            }
        }

        val fileSaverLauncher = rememberFileSaverLauncher { savedFile ->
            if (savedFile != null) {
                statusMessage = "Exported successfully to ${savedFile.name}"
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "CSV to ECR Converter",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { filePickerLauncher.launch() },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Choose CSV File")
                            }

                            if (convertedText.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            fileSaverLauncher.launch(
                                                bytes = convertedText.encodeToByteArray(),
                                                baseName = (selectedFileName?.substringBeforeLast('.') ?: "converted") + "_ecr",
                                                extension = "txt"
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("Export to TXT")
                                }
                            }
                        }

                        if (selectedFileName != null) {
                            Text(
                                text = "Selected: $selectedFileName",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        statusMessage?.let { msg ->
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Converted Output",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val scrollState = rememberScrollState()
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(12.dp)
                                .verticalScroll(scrollState)
                        ) {
                            if (convertedText.isEmpty()) {
                                Text(
                                    text = "Converted data will appear here...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            } else {
                                Text(
                                    text = convertedText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
