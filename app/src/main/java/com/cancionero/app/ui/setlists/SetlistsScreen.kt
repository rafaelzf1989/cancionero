package com.cancionero.app.ui.setlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.cancionero.app.data.model.Setlist
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistsScreen(
    vm: SetlistsViewModel,
    onOpenSetlist: (Setlist) -> Unit,
    onCreateSetlist: (String) -> Unit,
    onImportClick: () -> Unit
) {
    val setlists by vm.setlists.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf<Setlist?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setlists") },
                actions = {
                    IconButton(onClick = onImportClick) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Importar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                newName = ""
                showCreate = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Crear setlist")
            }
        }
    ) { padding ->
        if (setlists.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay setlists.\nPulsa + para crear uno.",
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(Modifier.padding(padding).fillMaxSize()) {
                items(setlists, key = { it.id }) { sl ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSetlist(sl) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(sl.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Creado: " + SimpleDateFormat(
                                    "dd/MM/yyyy", Locale.getDefault()
                                ).format(Date(sl.createdAt)),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { confirmDelete = sl }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        if (showCreate) {
            AlertDialog(
                onDismissRequest = { showCreate = false },
                title = { Text("Nuevo setlist") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newName.isNotBlank()) {
                                onCreateSetlist(newName.trim())
                                showCreate = false
                            }
                        },
                        enabled = newName.isNotBlank()
                    ) { Text("Crear") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreate = false }) { Text("Cancelar") }
                }
            )
        }

        confirmDelete?.let { sl ->
            AlertDialog(
                onDismissRequest = { confirmDelete = null },
                title = { Text("Eliminar setlist") },
                text = {
                    Text("¿Eliminar \"${sl.name}\"? Las canciones no se borran del repertorio.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        vm.delete(sl)
                        confirmDelete = null
                    }) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDelete = null }) { Text("Cancelar") }
                }
            )
        }
    }
}
