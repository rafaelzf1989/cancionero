package com.cancionero.app.ui.musicians

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cancionero.app.data.model.Musician
import com.cancionero.app.share.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusiciansScreen(
    vm: MusiciansViewModel,
    onAdd: () -> Unit,
    onEdit: (Musician) -> Unit
) {
    val context = LocalContext.current
    val musicians by vm.musicians.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Músicos") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Añadir músico")
            }
        }
    ) { padding ->
        if (musicians.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay músicos.\nPulsa + para añadir uno.",
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(Modifier.padding(padding).fillMaxSize()) {
                items(musicians, key = { it.id }) { m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEdit(m) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(m.name, style = MaterialTheme.typography.titleMedium)
                            val sub = buildString {
                                append(m.role)
                                if (!m.phone.isNullOrBlank()) {
                                    append(" · ")
                                    append(m.phone)
                                }
                            }
                            Text(sub, style = MaterialTheme.typography.bodySmall)
                        }
                        if (!m.phone.isNullOrBlank()) {
                            Spacer(Modifier.width(4.dp))
                            IconButton(onClick = {
                                ShareHelper.openWhatsApp(
                                    context,
                                    m.phone!!,
                                    "Hola ${m.name}, te escribo desde Cancionero 🎵"
                                )
                            }) {
                                Icon(Icons.Default.Chat, contentDescription = "WhatsApp")
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}