package com.cancionero.app.ui.cultos

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cancionero.app.data.model.Culto
import com.cancionero.app.data.model.Setlist
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private fun toMidnight(millis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun getHour(millis: Long): Int {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    return cal.get(Calendar.HOUR_OF_DAY)
}

private fun getMinute(millis: Long): Int {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    return cal.get(Calendar.MINUTE)
}

private fun defaultDateMidnight(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun combineDateAndTime(dateMidnight: Long, hour: Int, minute: Int): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = dateMidnight
    cal.set(Calendar.HOUR_OF_DAY, hour)
    cal.set(Calendar.MINUTE, minute)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun localMidnightToUtcPickerMillis(localMidnight: Long): Long {
    val local = Calendar.getInstance()
    local.timeInMillis = localMidnight
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    utc.clear()
    utc.set(
        local.get(Calendar.YEAR),
        local.get(Calendar.MONTH),
        local.get(Calendar.DAY_OF_MONTH),
        0, 0, 0
    )
    utc.set(Calendar.MILLISECOND, 0)
    return utc.timeInMillis
}

private fun utcPickerMillisToLocalMidnight(utcMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    utc.timeInMillis = utcMillis
    val local = Calendar.getInstance()
    local.clear()
    local.set(
        utc.get(Calendar.YEAR),
        utc.get(Calendar.MONTH),
        utc.get(Calendar.DAY_OF_MONTH),
        0, 0, 0
    )
    local.set(Calendar.MILLISECOND, 0)
    return local.timeInMillis
}

private fun formatDateButton(dateMidnight: Long): String {
    val sdf = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es"))
    return sdf.format(Date(dateMidnight)).replaceFirstChar { it.uppercase() }
}

private fun formatTimeButton(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val h12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.US, "%d:%02d %s", h12, minute, amPm)
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
    var dateMidnight by remember {
        mutableStateOf(
            initial?.let { toMidnight(it.dateMillis) } ?: defaultDateMidnight()
        )
    }
    var hour by remember {
        mutableStateOf(initial?.let { getHour(it.dateMillis) } ?: 19)
    }
    var minute by remember {
        mutableStateOf(initial?.let { getMinute(it.dateMillis) } ?: 0)
    }
    var setlistId by remember { mutableStateOf(initial?.setlistId) }
    var setlistExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

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

            Text("Fecha", style = MaterialTheme.typography.labelLarge)
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(formatDateButton(dateMidnight))
            }

            Text("Hora", style = MaterialTheme.typography.labelLarge)
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(formatTimeButton(hour, minute))
            }

            Text("Setlist asignado", style = MaterialTheme.typography.labelLarge)
            Box {
                OutlinedButton(
                    onClick = { setlistExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        setlists.find { it.id == validSetlistId }?.name
                            ?: "Sin asignar"
                    )
                }
                DropdownMenu(
                    expanded = setlistExpanded,
                    onDismissRequest = { setlistExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sin asignar") },
                        onClick = {
                            setlistId = null
                            setlistExpanded = false
                        }
                    )
                    setlists.forEach { sl ->
                        DropdownMenuItem(
                            text = { Text(sl.name) },
                            onClick = {
                                setlistId = sl.id
                                setlistExpanded = false
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
                            dateMillis = combineDateAndTime(dateMidnight, hour, minute),
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

        if (showDatePicker) {
            val dpState = rememberDatePickerState(
                initialSelectedDateMillis = localMidnightToUtcPickerMillis(dateMidnight)
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dpState.selectedDateMillis?.let {
                                dateMidnight = utcPickerMillisToLocalMidnight(it)
                            }
                            showDatePicker = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                }
            ) {
                DatePicker(state = dpState)
            }
        }

        if (showTimePicker) {
            val tpState = rememberTimePickerState(
                initialHour = hour,
                initialMinute = minute,
                is24Hour = false
            )
            Dialog(onDismissRequest = { showTimePicker = false }) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Hora", style = MaterialTheme.typography.headlineSmall)
                        TimePicker(state = tpState)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showTimePicker = false }) {
                                Text("Cancelar")
                            }
                            TextButton(onClick = {
                                hour = tpState.hour
                                minute = tpState.minute
                                showTimePicker = false
                            }) { Text("OK") }
                        }
                    }
                }
            }
        }
    }
}