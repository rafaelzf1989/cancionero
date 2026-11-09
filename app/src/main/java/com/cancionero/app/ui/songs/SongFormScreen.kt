package com.cancionero.app.ui.songs

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cancionero.app.data.model.Moment
import com.cancionero.app.data.model.Song
import com.cancionero.app.share.ShareHelper
import com.cancionero.app.ui.settings.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongFormScreen(
    initial: Song?,
    moments: List<Moment>,
    onSave: (Song) -> Unit,
    onDelete: (Song) -> Unit,
    onDuplicate: (Song) -> Unit,
    onManageMoments: () -> Unit,
    onCancel: () -> Unit,
    onPreview: (Song) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(initial?.title ?: "") }
    var songKey by remember { mutableStateOf(initial?.songKey ?: "C") }
    var tempoBpm by remember { mutableStateOf(initial?.tempoBpm?.toString() ?: "") }
    var tempoText by remember { mutableStateOf(initial?.tempoText ?: "") }
    var sourceLaCuerda by remember { mutableStateOf(initial?.sourceLaCuerda ?: false) }
    var sourceCancionero by remember { mutableStateOf(initial?.sourceCancionero ?: false) }
    var sourceLibreta by remember { mutableStateOf(initial?.libretaNumber != null) }
    var libretaNumber by remember { mutableStateOf(initial?.libretaNumber?.toString() ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var moment by remember { mutableStateOf(initial?.moment) }
    var tags by remember { mutableStateOf(initial?.tags ?: "") }
    var lyrics by remember { mutableStateOf(initial?.lyrics ?: "") }

    val chordColor = AppSettings.getChordColor(context)
    val sectionColor = AppSettings.getSectionColor(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) "Nueva canción" else "Editar canción") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val tempSong = Song(
                            id = initial?.id ?: 0,
                            title = title.trim(),
                            tempoBpm = tempoBpm.toIntOrNull(),
                            tempoText = tempoText.ifBlank { null },
                            songKey = songKey.trim().ifBlank { "C" },
                            notes = notes.ifBlank { null },
                            moment = moment,
                            lyrics = lyrics.ifBlank { null }
                        )
                        onPreview(tempSong)
                    }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Vista previa")
                    }
                    IconButton(onClick = {
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
                            updatedAt = System.currentTimeMillis(),
                            moment = moment,
                            tags = tags.ifBlank { null },
                            lyrics = lyrics.ifBlank { null }
                        )
                        ShareHelper.shareSong(context, song)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                    if (initial != null) {
                        IconButton(onClick = { onDuplicate(initial) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicar")
                        }
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

            OutlinedTextField(
                value = tempoBpm,
                onValueChange = { tempoBpm = it.filter { c -> c.isDigit() } },
                label = { Text("BPM") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text("Tempo", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Lento", "Medio", "Rápido").forEach { t ->
                    FilterChip(
                        selected = tempoText == t,
                        onClick = { tempoText = if (tempoText == t) "" else t },
                        label = { Text(t) }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Momento del culto", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onManageMoments) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Gestionar")
                }
            }
            if (moments.isEmpty()) {
                Text(
                    "No hay momentos. Pulsa Gestionar para crear.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moments.forEach { m ->
                        FilterChip(
                            selected = moment == m.name,
                            onClick = { moment = if (moment == m.name) null else m.name },
                            label = { Text(m.name) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (separados por coma)") },
                placeholder = { Text("adoración, navidad, himno") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

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

            OutlinedTextField(
                value = lyrics,
                onValueChange = { lyrics = it },
                label = { Text("Letra y Acordes (estilo Holyrics)") },
                placeholder = { 
                    Text("##Coro\n//G D\nQue todo lo que soy\n//Em C\nBendiga al Señor") 
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 12,
                visualTransformation = HolyricsVisualTransformation(chordColor, sectionColor),
                // FORZAMOS LA FUENTE MONOESPACIADA EN TODO EL CUADRO
                textStyle = TextStyle(fontFamily = FontFamily.Monospace)
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
                        updatedAt = System.currentTimeMillis(),
                        moment = moment,
                        tags = tags.ifBlank { null },
                        lyrics = lyrics.ifBlank { null }
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