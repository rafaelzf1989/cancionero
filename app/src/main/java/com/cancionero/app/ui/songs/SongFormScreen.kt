package com.cancionero.app.ui.songs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cancionero.app.data.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongFormScreen(
    initial: Song?,
    onSave: (Song) -> Unit,
    onDelete: (Song) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var songKey by remember { mutableStateOf(initial?.songKey ?: "C") }
    var tempoBpm by remember { mutableStateOf(initial?.tempoBpm?.toString() ?: "") }
    var tempoText by remember { mutableStateOf(initial?.tempoText ?: "") }
    var sourceLaCuerda by remember { mutableStateOf(initial?.sourceLaCuerda ?: false) }
    var sourceCancionero by remember { mutableStateOf(initial?.sourceCancionero ?: false) }
    var sourceLibreta by remember { mutableStateOf(initial?.libretaNumber != null) }
    var libretaNumber by remember { mutableStateOf(initial?.libretaNumber?.toString() ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) "Nueva canción" else "Editar canción") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nombre *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = songKey,
                onValueChange = { songKey = it },
                label = { Text("Tonalidad (C, G, Em…)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = tempoBpm,
                    onValueChange = { tempoBpm = it.filter { c -> c.isDigit() } },
                    label = { Text("BPM") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Text("Tempo", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Lento", "Medio", "Rápido").forEach { t ->
                    FilterChip(
                        selected = tempoText == t,
                        onClick = {
                            tempoText = if (tempoText == t) "" else t
                        },
                        label = { Text(t) }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("Fuentes", style = MaterialTheme.typography.labelLarge)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = sourceLaCuerda,
                    onCheckedChange = { sourceLaCuerda = it }
                )
                Text("LaCuerda")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = sourceCancionero,
                    onCheckedChange = { sourceCancionero = it }
                )
                Text("Cancionero")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = sourceLibreta,
                    onCheckedChange = { sourceLibreta = it }
                )
                Text("Libreta")
                if (sourceLibreta) {
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = libretaNumber,
                        onValueChange = { libretaNumber = it.filter { c -> c.isDigit() } },
                        label = { Text("Nº") },
                        modifier = Modifier.width(100.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    val song = Song(
                        id = initial?.id ?: 0,
                        title = title.trim(),
                        tempoBpm = tempoBpm.toIntOrNull(),
                        tempoText = tempoText.ifBlank { null },
                        songKey = songKey.trim().ifBlank { "C" },
                        sourceLaCuerda = sourceLaCuerda,
                        sourceCancionero = sourceCancionero,
                        libretaNumber = if (sourceLibreta) libretaNumber.toIntOrNull() else null,
                        notes = notes.ifBlank { null },
                        isFavorite = initial?.isFavorite ?: false,
                        usageCount = initial?.usageCount ?: 0,
                        createdAt = initial?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(song)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text("Guardar")
            }

            if (initial != null) {
                OutlinedButton(
                    onClick = { onDelete(initial) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar canción")
                }
            }
        }
    }
}
