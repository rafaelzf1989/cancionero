package com.cancionero.app.ui.cultos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.cancionero.app.data.db.CultoWithCount
import com.cancionero.app.data.model.Culto
import com.cancionero.app.data.model.Setlist
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal fun formatDateHeader(millis: Long): String {
    val sdf = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es"))
    return sdf.format(Date(millis)).replaceFirstChar { it.uppercase() }
}

internal fun formatTime12h(millis: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    val h = cal.get(Calendar.HOUR_OF_DAY)
    val m = cal.get(Calendar.MINUTE)
    val amPm = if (h < 12) "AM" else "PM"
    val h12 = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return String.format(Locale.US, "%d:%02d %s", h12, m, amPm)
}

internal fun formatDateTime(millis: Long): String =
    "${formatDateHeader(millis)} · ${formatTime12h(millis)}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultosScreen(
    vm: CultosViewModel,
    onCreateCulto: () -> Unit,
    onEditCulto: (Culto) -> Unit
) {
    val cultos by vm.cultos.collectAsState()
    val setlists by vm.setlists.collectAsState()
    var showBulkAssign by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<CultoWithCount?>(null) }

    val grouped = cultos
        .sortedBy { it.dateMillis }
        .groupBy { formatDateHeader(it.dateMillis) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cultos programados") },
                actions = {
                    IconButton(
                        onClick = { showBulkAssign = true },
                        enabled = cultos.isNotEmpty() && setlists.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Group, contentDescription = "Asignar setlist")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateCulto) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo culto")
            }
        }
    ) { padding ->
        if (cultos.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay cultos programados.\nPulsa + para crear uno.",
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(Modifier.padding(padding).fillMaxSize()) {
                grouped.forEach { (dateHeader, dayCultos) ->
                    item(key = "header_$dateHeader") {
                        Text(
                            text = dateHeader,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        HorizontalDivider()
                    }
                    items(dayCultos, key = { it.id }) { cwc ->
                        CultoRow(
                            culto = cwc,
                            onClick = {
                                onEditCulto(
                                    Culto(
                                        id = cwc.id,
                                        name = cwc.name,
                                        dateMillis = cwc.dateMillis,
                                        leader = cwc.leader
                                    )
                                )
                            },
                            onDelete = { confirmDelete = cwc }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        if (showBulkAssign) {
            BulkAssignDialog(
                cultos = cultos,
                setlists = setlists,
                onDismiss = { showBulkAssign = false },
                onApply = { cultoIds, setlistId ->
                    vm.assignSetlistToMany(cultoIds, setlistId)
                    showBulkAssign = false
                }
            )
        }

        confirmDelete?.let { c ->
            AlertDialog(
                onDismissRequest = { confirmDelete = null },
                title = { Text("Eliminar culto") },
                text = { Text("¿Eliminar \"${c.name}\"?") },
                confirmButton = {
                    TextButton(onClick = {
                        vm.delete(
                            Culto(
                                id = c.id,
                                name = c.name,
                                dateMillis = c.dateMillis,
                                leader = c.leader
                            )
                        )
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
private fun CultoRow(
    culto: CultoWithCount,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(culto.name, style = MaterialTheme.typography.titleMedium)
            Text(
                formatTime12h(culto.dateMillis),
                style = MaterialTheme.typography.bodySmall
            )
            val meta = buildString {
                if (!culto.leader.isNullOrBlank()) {
                    append("Líder: ")
                    append(culto.leader)
                }
                if (culto.musicianCount > 0) {
                    if (isNotEmpty()) append(" · ")
                    append(culto.musicianCount)
                    append(if (culto.musicianCount == 1) " músico" else " músicos")
                }
                if (culto.setlistCount > 0) {
                    if (isNotEmpty()) append(" · ")
                    append(culto.setlistCount)
                    append(if (culto.setlistCount == 1) " setlist" else " setlists")
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
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
        }
    }
}

@Composable
private fun BulkAssignDialog(
    cultos: List<CultoWithCount>,
    setlists: List<Setlist>,
    onDismiss: () -> Unit,
    onApply: (List<Long>, Long?) -> Unit
) {
    var selectedSetlistId by remember { mutableStateOf<Long?>(null) }
    var selectedCultoIds by remember { mutableStateOf(setOf<Long>()) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar setlist a varios cultos") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            setlists.find { it.id == selectedSetlistId }?.name
                                ?: "Sin asignar"
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin asignar") },
                            onClick = {
                                selectedSetlistId = null
                                expanded = false
                            }
                        )
                        setlists.forEach { sl ->
                            DropdownMenuItem(
                                text = { Text(sl.name) },
                                onClick = {
                                    selectedSetlistId = sl.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    "Reemplaza los setlists de estos cultos:",
                    style = MaterialTheme.typography.labelMedium
                )

                Column(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    cultos.sortedBy { it.dateMillis }.forEach { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCultoIds = if (c.id in selectedCultoIds)
                                        selectedCultoIds - c.id
                                    else
                                        selectedCultoIds + c.id
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = c.id in selectedCultoIds,
                                onCheckedChange = {
                                    selectedCultoIds = if (c.id in selectedCultoIds)
                                        selectedCultoIds - c.id
                                    else
                                        selectedCultoIds + c.id
                                }
                            )
                            Column {
                                Text(c.name)
                                Text(
                                    formatDateTime(c.dateMillis),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApply(selectedCultoIds.toList(), selectedSetlistId) },
                enabled = selectedCultoIds.isNotEmpty()
            ) { Text("Aplicar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}