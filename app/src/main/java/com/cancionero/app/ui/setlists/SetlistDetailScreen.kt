package com.cancionero.app.ui.setlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cancionero.app.data.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistDetailScreen(
    vm: SetlistDetailViewModel,
    onBack: () -> Unit
) {
    val setlist by vm.setlist.collectAsState()
    val songs by vm.songs.collectAsState()
    val allSongs by vm.allSongs.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf<Song?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(setlist?.name ?: "Setlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir canción")
            }
        }
    ) { padding ->
        if (songs.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Setlist vacío.\nPulsa + para añadir canciones.",
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(Modifier.padding(padding).fillMaxSize()) {
                itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${index + 1}.",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.width(32.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(song.title, style = MaterialTheme.typography.titleMedium)
                            val sub = buildString {
                                append(song.songKey)
                                val t = listOfNotNull(
                                    song.tempoBpm?.let { "$it BPM" },
                                    song.tempoText?.takeIf { it.isNotBlank() }
                                ).joinToString(" · ")
                                if (t.isNotEmpty()) {
                                    append(" · ")
                                    append(t)
                                }
                            }
                            Text(sub, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(
                            onClick = { vm.moveUp(song) },
                            enabled = index > 0
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Subir")
                        }
                        IconButton(
                            onClick = { vm.moveDown(song) },
                            enabled = index < songs.size - 1
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bajar")
                        }
                        IconButton(onClick = { confirmRemove = song }) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar")
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        if (showAddDialog) {
            val used = songs.map { it.id }.toSet()
            val available = allSongs.filter { it.id !in used }
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Añadir al setlist") },
                text = {
                    if (available.isEmpty()) {
                        Text("No hay más canciones disponibles en el repertorio.")
                    } else {
                        LazyColumn(Modifier.height(300.dp)) {
                            items(available, key = { it.id }) { s ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            vm.addSong(s)
                                            showAddDialog = false
                                        }
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(s.title, style = MaterialTheme.typography.bodyLarge)
                                        Text(s.songKey, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cerrar") }
                }
            )
        }

        confirmRemove?.let { s ->
            AlertDialog(
                onDismissRequest = { confirmRemove = null },
                title = { Text("Quitar del setlist") },
                text = { Text("¿Quitar \"${s.title}\" de este setlist?") },
                confirmButton = {
                    TextButton(onClick = {
                        vm.removeSong(s)
                        confirmRemove = null
                    }) { Text("Quitar") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmRemove = null }) { Text("Cancelar") }
                }
            )
        }
    }
}
