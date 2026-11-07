package com.cancionero.app.ui.cultos

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cancionero.app.data.model.Culto
import com.cancionero.app.data.model.Setlist
import java.util.Locale

private fun normalizeTime(input: String): String {
    val parts = input.trim().split(":")
    val h = (parts.getOrNull(0)?.toIntOrNull() ?: 19).coerceIn(0, 23)
    val m = (parts.getOrNull(1)?.toIntOrNull() ?: 0).coerceIn(0, 59)
    return String.format(Locale.getDefault(), "%02d:%02d", h, m)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultoFormScreen(
    initial: Culto?,
    setlists: List<Setlist>,
    onSave: (Culto) -> Unit,
    onDelete: (Culto) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var dayOfWeek by remember { mutableStateOf(initial?.dayOfWeek ?: 7) }
    var time by remember { mutableStateOf(initial?.time ?: "19:00") }
    var setlistId by remember { mutableStateOf(initial?.setlistId) }
    var expanded by remember { mutableStateOf(false) }

    val validSetlistId = setlistId?.takeIf { id -> setlists.any { it.id == id } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) "Nuevo culto" else "Editar culto") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
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
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del culto *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Día de la semana", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..7).forEach { day ->
                    FilterChip(
                        selected = dayOfWeek == day,
                        onClick = { dayOfWeek = day },
                        label = { Text(dayShortName(day)) }
                    )
                }
            }

            OutlinedTextField(
                value = time,
                onValueChange = { input ->
                    time = input.filter { it.isDigit() || it == ':' }.take(5)
                },
                label = { Text("Hora (HH:MM)") },
                placeholder = { Text("19:00") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Text("Setlist asignado", style = MaterialTheme.typography.labelLarge)
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        setlists.find { it.id == validSetlistId }?.name
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
                            setlistId = null
                            expanded = false
                        }
                    )
                    setlists.forEach { sl ->
                        DropdownMenuItem(
                            text = { Text(sl.name) },
                            onClick = {
                                setlistId = sl.id
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    onSave(
                        Culto(
                            id = initial?.id ?: 0,
                            name = name.trim(),
                            dayOfWeek = dayOfWeek,
                            time = normalizeTime(time),
                            setlistId = validSetlistId
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
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
                    Text("Eliminar culto")
                }
            }
        }
    }
}
