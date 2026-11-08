package com.cancionero.app.ui.songs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cancionero.app.data.model.Moment
import com.cancionero.app.data.model.Song
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepertorioScreen(
    vm: SongsViewModel,
    onAddSong: () -> Unit,
    onEditSong: (Song) -> Unit,
    onManageMoments: () -> Unit
) {
    val songs by vm.filteredSongs.collectAsState()
    val filters by vm.filters.collectAsState()
    val moments by vm.moments.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val activeCount = filters.activeCount()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Repertorio") },
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtros"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSong) {
                Icon(Icons.Default.Add, contentDescription = "Añadir canción")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = filters.query,
                onValueChange = vm::updateQuery,
                label = { Text("Buscar por nombre") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { showFilters = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (activeCount == 0) "Filtros"
                        else "Filtros ($activeCount)"
                    )
                }
                if (activeCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { vm.clearFilters() }) {
                        Text("Limpiar")
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "${songs.size} canciones",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (songs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay canciones.\nPulsa + para añadir.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(songs, key = { it.id }) { song ->
                        SongRow(
                            song = song,
                            onClick = { onEditSong(song) },
                            onToggleFavorite = { vm.toggleFavorite(song) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        if (showFilters) {
            ModalBottomSheet(
                onDismissRequest = { showFilters = false },
                sheetState = sheetState
            ) {
                FiltersSheetContent(
                    filters = filters,
                    moments = moments,
                    vm = vm,
                    onManageMoments = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showFilters = false
                            onManageMoments()
                        }
                    },
                    onClose = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showFilters = false
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersSheetContent(
    filters: SongFilters,
    moments: List<Moment>,
    vm: SongsViewModel,
    onManageMoments: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filtros", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { vm.clearFilters() }) {
                Text("Limpiar todo")
            }
        }

        HorizontalDivider()
        Text("Buscar", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(
            value = filters.key ?: "",
            onValueChange = { vm.setKey(it.ifBlank { null }) },
            label = { Text("Tonalidad (ej. C, G, Em)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = filters.tag ?: "",
            onValueChange = { vm.setTag(it.ifBlank { null }) },
            label = { Text("Tag (ej. navidad, himno)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        HorizontalDivider()
        Text("Tempo", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Lento", "Medio", "Rápido").forEach { t ->
                FilterChip(
                    selected = filters.tempoText == t,
                    onClick = { vm.setTempo(if (filters.tempoText == t) null else t) },
                    label = { Text(t) }
                )
            }
        }

        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Momento del culto", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onManageMoments) {
                Text("Gestionar")
            }
        }
        if (moments.isEmpty()) {
            Text(
                "No hay momentos definidos. Pulsa Gestionar para crear.",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moments.forEach { m ->
                    FilterChip(
                        selected = filters.moment == m.name,
                        onClick = {
                            vm.setMoment(if (filters.moment == m.name) null else m.name)
                        },
                        label = { Text(m.name) }
                    )
                }
            }
        }

        HorizontalDivider()
        Text("Fuentes y favoritos", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filters.onlyFavorites,
                onClick = { vm.toggleFavorites() },
                label = { Text("⭐ Favoritas") }
            )
            FilterChip(
                selected = filters.onlyLaCuerda,
                onClick = { vm.toggleLaCuerda() },
                label = { Text("LaCuerda") }
            )
            FilterChip(
                selected = filters.onlyCancionero,
                onClick = { vm.toggleCancionero() },
                label = { Text("Cancionero") }
            )
            FilterChip(
                selected = filters.onlyLibreta,
                onClick = { vm.toggleLibreta() },
                label = { Text("Libreta") }
            )
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Listo")
        }
    }
}

@Composable
private fun SongRow(
    song: Song,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, style = MaterialTheme.typography.titleMedium)
            val subtitle = buildString {
                append(song.songKey)
                val tempo = listOfNotNull(
                    song.tempoBpm?.let { "$it BPM" },
                    song.tempoText?.takeIf { it.isNotBlank() }
                ).joinToString(" · ")
                if (tempo.isNotEmpty()) {
                    append(" · ")
                    append(tempo)
                }
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
            val meta = buildString {
                song.moment?.let { append(it) }
                if (!song.tags.isNullOrBlank()) {
                    if (isNotEmpty()) append(" · ")
                    append(song.tags)
                }
            }
            if (meta.isNotEmpty()) {
                Text(
                    meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (song.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Favorito",
                tint = if (song.isFavorite) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}