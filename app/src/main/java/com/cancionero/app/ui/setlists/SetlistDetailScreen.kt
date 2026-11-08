package com.cancionero.app.ui.setlists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cancionero.app.data.model.Song
import com.cancionero.app.share.ShareHelper
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private val MOMENT_ORDER = listOf(
    "Entrada", "Adoración", "Alabanza", "Ofrenda",
    "Comunión", "Especial", "Despedida"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SetlistDetailScreen(
    vm: SetlistDetailViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val setlist by vm.setlist.collectAsState()
    val songsFromDb by vm.songs.collectAsState()
    val allSongs by vm.allSongs.collectAsState()

    var localSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var groupByMoment by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf<Song?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }

    LaunchedEffect(songsFromDb, groupByMoment) {
        if (!groupByMoment) {
            localSongs = songsFromDb
        }
    }

    val displaySongs = if (groupByMoment) songsFromDb else localSongs

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val mutable = localSongs.toMutableList()
        if (from.index in mutable.indices && to.index in mutable.indices) {
            mutable.add(to.index, mutable.removeAt(from.index))
            localSongs = mutable
            vm.persistReorder(mutable.map { it.id })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(setlist?.name ?: "Setlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showShareDialog = true },
                        enabled = displaySongs.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
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
        Column(Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = groupByMoment,
                    onClick = { groupByMoment = !groupByMoment },
                    label = { Text("Agrupar por momento") }
                )
            }

            if (displaySongs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Setlist vacío.\nPulsa + para añadir canciones.",
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else if (groupByMoment) {
                LazyColumn(Modifier.fillMaxSize()) {
                    val grouped = displaySongs.groupBy { it.moment }
                    var counter = 0
                    MOMENT_ORDER.forEach { moment ->
                        val group = grouped[moment].orEmpty()
                        if (group.isNotEmpty()) {
                            item(key = "h_$moment") {
                                Text(
                                    text = "— $moment —",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            items(group, key = { it.id }) { song ->
                                counter++
                                GroupedSongRow(
                                    index = counter,
                                    song = song,
                                    onRemove = { confirmRemove = song }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                    val noMoment = grouped[null].orEmpty()
                    if (noMoment.isNotEmpty()) {
                        item(key = "h_none") {
                            Text(
                                text = "— Sin momento —",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(noMoment, key = { it.id }) { song ->
                            counter++
                            GroupedSongRow(
                                index = counter,
                                song = song,
                                onRemove = { confirmRemove = song }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            } else {
                LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(localSongs, key = { _, s -> s.id }) { index, song ->
                        ReorderableItem(reorderState, key = song.id) { isDragging ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isDragging)
                                            MaterialTheme.colorScheme.surfaceVariant
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DragHandle,
                                    contentDescription = "Reordenar",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .padding(8.dp)
                                        .longPressDraggableHandle()
                                )
                                Text(
                                    "${index + 1}.",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.width(28.dp)
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
                                        song.moment?.let {
                                            append(" · ")
                                            append(it)
                                        }
                                    }
                                    Text(sub, style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { confirmRemove = song }) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar")
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            val used = displaySongs.map { it.id }.toSet()
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
                                        val sub = buildString {
                                            append(s.songKey)
                                            s.moment?.let {
                                                append(" · ")
                                                append(it)
                                            }
                                        }
                                        Text(sub, style = MaterialTheme.typography.bodySmall)
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

        if (showShareDialog) {
            AlertDialog(
                onDismissRequest = { showShareDialog = false },
                title = { Text("Compartir setlist") },
                text = { Text("Selecciona el formato:") },
                confirmButton = {
                    TextButton(onClick = {
                        ShareHelper.shareJson(
                            context,
                            setlist?.name ?: "Setlist",
                            displaySongs
                        )
                        showShareDialog = false
                    }) { Text("Archivo JSON") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        ShareHelper.shareText(
                            context,
                            setlist?.name ?: "Setlist",
                            displaySongs
                        )
                        showShareDialog = false
                    }) { Text("Texto") }
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

@Composable
private fun GroupedSongRow(
    index: Int,
    song: Song,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$index.",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(36.dp)
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
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, contentDescription = "Quitar")
        }
    }
}
