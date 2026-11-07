package com.cancionero.app.ui.setlists

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSetlistScreen(
    vm: ImportViewModel,
    onBack: () -> Unit,
    onImported: () -> Unit
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    val importing by vm.importing.collectAsState()
    val message by vm.message.collectAsState()
    val imported by vm.imported.collectAsState()

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.use { it.readText() }
                if (content != null) vm.importFromText(content)
            } catch (e: Exception) {
                // silencioso
            }
        }
    }

    LaunchedEffect(imported) {
        if (imported) {
            delay(1200)
            vm.consumeImported()
            onImported()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar setlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    fileLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Abrir archivo .json / .txt")
            }

            Text(
                "O pega aquí el texto del setlist:",
                style = MaterialTheme.typography.labelLarge
            )

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().weight(1f),
                placeholder = {
                    Text(
                        "Ejemplo:\n" +
                        "Culto de jóvenes\n\n" +
                        "1. Canción A (G · 72 BPM · Medio)\n" +
                        "2. Canción B (D)"
                    )
                }
            )

            Button(
                onClick = { vm.importFromText(text) },
                enabled = text.isNotBlank() && !importing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (importing) "Importando..." else "Importar desde texto")
            }

            message?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
