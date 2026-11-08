package com.cancionero.app.ui.setlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
    onImportClick: () -> Unit
) {
    val setlists by vm.setlists.collectAsState()
    val templates by vm.templates.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var optionsFor by remember { mutableStateOf<Setlist?>(null) }
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
            FloatingActionButton(onClick = { showCreate = true }) {
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
                            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(sl.name, style = MaterialTheme.typography.titleMedium)
                                if (sl.isTemplate) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "📌 Plantilla",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                "Creado: " + SimpleDateFormat(
                                    "dd/MM/yyyy", Locale.getDefault()
                                ).format(Date(sl.createdAt)),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { optionsFor = sl }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        if (showCreate) {
            CreateSetlistDialog(
                templates = templates,
                onDismiss = { showCreate = false },
                onCreate = { name, templateId ->
                    if (templateId != null) {
                        vm.createFromTemplate(name, templateId)
                    } else {
                        vm.create(name)
                    }
                    showCreate = false
                }
            )
        }

        if (optionsFor != null) {
            val sl = optionsFor!!
            AlertDialog(
                onDismissRequest = { optionsFor = null },
                title = { Text(sl.name) },
                text = {
                    Column {
                        TextButton(
                            onClick = { vm.duplicate(sl); optionsFor = null },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Duplicar setlist") }
                        TextButton(
                            onClick = { vm.toggleTemplate(sl); optionsFor = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (sl.isTemplate) "Quitar de plantillas"
                                else "Marcar como plantilla"
                            )
                        }
                        TextButton(
                            onClick = { confirmDelete = sl; optionsFor = null },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) { Text("Eliminar setlist") }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { optionsFor = null }) { Text("Cerrar") }
                }
            )
        }

        confirmDelete?.let { sl ->
            AlertDialog(
                onDismissRequest = { confirmDelete = null },
                title = { Text("Eliminar setlist") },
                text = { Text("¿Eliminar \"${sl.name}\"? Las canciones no se borran del repertorio.") },
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

@Composable
private fun CreateSetlistDialog(
    templates: List<Setlist>,
    onDismiss: () -> Unit,
    onCreate: (String, Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedTemplateId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo setlist") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (templates.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Empezar desde plantilla (opcional):",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                templates.find { it.id == selectedTemplateId }?.name
                                    ?: "Ninguna"
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ninguna") },
                                onClick = {
                                    selectedTemplateId = null
                                    expanded = false
                                }
                            )
                            templates.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t.name) },
                                    onClick = {
                                        selectedTemplateId = t.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name.trim(), selectedTemplateId) },
                enabled = name.isNotBlank()
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
