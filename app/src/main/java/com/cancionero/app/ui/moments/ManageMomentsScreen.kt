package com.cancionero.app.ui.moments

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.cancionero.app.data.model.Moment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageMomentsScreen(
    vm: MomentsViewModel,
    onBack: () -> Unit
) {
    val moments by vm.moments.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editingMoment by remember { mutableStateOf<Moment?>(null) }
    var confirmDelete by remember { mutableStateOf<Moment?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Momentos del culto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir momento")
            }
        }
    ) { padding ->
        if (moments.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay momentos.\nPulsa + para crear uno.",
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(Modifier.padding(padding).fillMaxSize()) {
                items(moments, key = { it.id }) { m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            m.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { editingMoment = m }) {
                            Icon(Icons.Default.Edit, contentDescription = "Renombrar")
                        }
                        IconButton(onClick = { confirmDelete = m }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        if (showAdd) {
            NameDialog(
                title = "Nuevo momento",
                initial = "",
                confirmLabel = "Crear",
                onDismiss = { showAdd = false },
                onConfirm = { name ->
                    vm.add(name)
                    showAdd = false
                }
            )
        }

        editingMoment?.let { m ->
            NameDialog(
                title = "Renombrar momento",
                initial = m.name,
                confirmLabel = "Guardar",
                onDismiss = { editingMoment = null },
                onConfirm = { newName ->
                    vm.rename(m, newName)
                    editingMoment = null
                }
            )
        }

        confirmDelete?.let { m ->
            AlertDialog(
                onDismissRequest = { confirmDelete = null },
                title = { Text("Eliminar momento") },
                text = {
                    Text(
                        "¿Eliminar \"${m.name}\"? Las canciones con este " +
                            "momento se quedarán sin momento (no se borran)."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { vm.delete(m); confirmDelete = null },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDelete = null }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
private fun NameDialog(
    title: String,
    initial: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
