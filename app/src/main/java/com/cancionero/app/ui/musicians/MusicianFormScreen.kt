package com.cancionero.app.ui.musicians

import androidx.compose.foundation.layout.Arrangement
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
import com.cancionero.app.data.model.Musician

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicianFormScreen(
    initial: Musician?,
    onSave: (Musician) -> Unit,
    onDelete: (Musician) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var selectedRoles by remember {
        mutableStateOf(initial?.rolesList()?.toSet() ?: emptySet())
    }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    val rolesComunes = listOf(
        "Voz", "Voz líder", "Voz de coro",
        "Guitarra", "Bajo", "Batería",
        "Teclado", "Piano"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) "Nuevo músico" else "Editar músico") },
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
                label = { Text("Nombre *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                "Roles (puedes marcar varios)",
                style = MaterialTheme.typography.labelLarge
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rolesComunes.chunked(2).forEach { rowRoles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowRoles.forEach { role ->
                            FilterChip(
                                selected = role in selectedRoles,
                                onClick = {
                                    selectedRoles = if (role in selectedRoles)
                                        selectedRoles - role
                                    else
                                        selectedRoles + role
                                },
                                label = { Text(role) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowRoles.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    phone = input.filter {
                        it.isDigit() || it == '+' || it == ' ' || it == '-'
                    }
                },
                label = { Text("Teléfono (con código de país)") },
                placeholder = { Text("+53 555 1234") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    onSave(
                        Musician(
                            id = initial?.id ?: 0,
                            name = name.trim(),
                            roles = selectedRoles.joinToString(",").ifBlank { "Músico" },
                            phone = phone.trim().ifBlank { null },
                            notes = notes.trim().ifBlank { null }
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && selectedRoles.isNotEmpty()
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
                    Text("Eliminar músico")
                }
            }
        }
    }
}