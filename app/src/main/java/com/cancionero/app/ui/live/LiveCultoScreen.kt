package com.cancionero.app.ui.live

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cancionero.app.data.model.Song
import com.cancionero.app.ui.cultos.formatDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveCultoScreen(
    vm: LiveViewModel,
    onSongClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val culto by vm.culto.collectAsState()
    val setlists by vm.setlists.collectAsState()
    val songs by vm.songs.collectAsState()

    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("En vivo") },
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
        ) {
            culto?.let { c ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            c.name,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            formatDateTime(c.dateMillis),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (!c.leader.isNullOrBlank()) {
                            Text(
                                "Líder: ${c.leader}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (setlists.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                setlists.forEach { sl ->
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(sl.name) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (songs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Este culto no tiene canciones.\n" +
                            "Asígnales setlists con canciones antes de entrar en vivo.",
                        modifier = Modifier.padding(32.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Button(
                    onClick = { onSongClick(0) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Empezar desde la primera")
                }

                Text(
                    text = "${songs.size} canciones en total",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                LazyColumn(Modifier.fillMaxSize()) {
                    itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
                        LiveSongRow(
                            index = index,
                            song = song,
                            onClick = { onSongClick(index) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveSongRow(
    index: Int,
    song: Song,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "${index + 1}.",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(32.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(song.title, style = MaterialTheme.typography.titleMedium)
            val meta = buildString {
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
            Text(meta, style = MaterialTheme.typography.bodySmall)
        }
        Icon(
            Icons.Default.PlayArrow,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}