package com.cancionero.app.ui.cultos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cancionero.app.data.model.Culto
import com.cancionero.app.data.model.Musician
import com.cancionero.app.data.model.Setlist
import com.cancionero.app.share.ShareHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val ALL_ROLES_ORDER = listOf(
    "Voz", "Voz líder", "Voz de coro",
    "Guitarra", "Bajo", "Batería",
    "Teclado", "Piano"
)

private fun sortRoles(roles: Collection<String>): List<String> =
    roles.sortedBy { role ->
        val idx = ALL_ROLES_ORDER.indexOf(role)
        if (idx >= 0) idx else Int.MAX_VALUE
    }

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
    vm: CultosViewModel,
    initial: Culto?,
    setlists: List<Setlist>,
    allMusicians: List<Musician>,
    onSave: (Culto, List<Long>, Map<Long, String?>, List<Long>) -> Unit,
    onDelete: (Culto) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var leader by remember { mutableStateOf(initial?.leader ?: "") }
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
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showSetlistDialog by remember { mutableStateOf(false) }

    var orderedSetlistIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    var selectedMusicianIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var musicianRoles by remember {
        mutableStateOf<Map<Long, Set<String>>>(emptyMap())
    }

    LaunchedEffect(initial?.id, allMusicians) {
        if (initial != null && initial.id > 0) {
            orderedSetlistIds = vm.getSetlistIdsFor(initial.id)
            val ids = vm.getMusicianIdsFor(initial.id).toSet()
            val rolesMap = vm.getMusicianRolesFor(initial.id)
            selectedMusicianIds = ids
            val newRoles = mutableMapOf<Long, Set<String>>()
            ids.forEach { mId ->
                val musician = allMusicians.find { it.id == mId }
                if (musician != null) {
                    val savedRoles = rolesMap[mId]
                    newRoles[mId] = if (savedRoles.isNullOrBlank()) {
                        musician.rolesList().toSet()
                    } else {
                        savedRoles.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
                    }
                }
            }
            musicianRoles = newRoles
        }
    }

    fun defaultRolesFor(musician: Musician): Set<String> = musician.rolesList().toSet()

    fun buildFullCultoMessage(): String {
        val fechaStr = formatDateButton(dateMidnight)
        val horaStr = formatTimeButton(hour, minute)
        val setlistNames = orderedSetlistIds.mapNotNull { slId ->
            setlists.find { it.id == slId }?.name
        }
        val sb = StringBuilder()
        sb.appendLine("🎵 Culto: ${name.ifBlank { "(sin nombre)" }}")
        sb.appendLine("📅 Fecha: $fechaStr")
        sb.appendLine("🕐 Hora: $horaStr")
        if (leader.isNotBlank()) sb.appendLine("👤 Líder: ${leader.trim()}")
        if (setlistNames.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("🎼 Setlists:")
            setlistNames.forEach { sb.appendLine("• $it") }
        }
        if (selectedMusicianIds.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("👥 Músicos:")
            selectedMusicianIds
                .mapNotNull { mId -> allMusicians.find { it.id == mId } }
                .sortedBy { it.name.lowercase() }
                .forEach { m ->
                    val roles = musicianRoles[m.id]?.let { sortRoles(it) }
                        ?: sortRoles(m.rolesList())
                    sb.appendLine("• ${m.name}: ${roles.joinToString(", ")}")
                }
        }
        sb.appendLine()
        sb.append("— Enviado desde Cancionero")
        return sb.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) "Nuevo culto" else "Editar culto") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        ShareHelper.shareViaWhatsApp(context, buildFullCultoMessage())
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir culto")
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

            OutlinedTextField(
                value = leader,
                onValueChange = { leader = it },
                label = { Text("Responsable (opcional)") },
                placeholder = { Text("Quién dirige") },
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

            HorizontalDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Setlists", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { showSetlistDialog = true }) {
                    Text("Añadir / quitar")
                }
            }

            if (orderedSetlistIds.isEmpty()) {
                Text(
                    "Sin setlists asignados.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                orderedSetlistIds.forEachIndexed { idx, slId ->
                    val sl = setlists.find { it.id == slId }
                    if (sl != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${idx + 1}. ${sl.name}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(
                                onClick = {
                                    if (idx > 0) {
                                        val mutable = orderedSetlistIds.toMutableList()
                                        val tmp = mutable[idx - 1]
                                        mutable[idx - 1] = mutable[idx]
                                        mutable[idx] = tmp
                                        orderedSetlistIds = mutable
                                    }
                                },
                                enabled = idx > 0
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Subir")
                            }
                            IconButton(
                                onClick = {
                                    if (idx < orderedSetlistIds.size - 1) {
                                        val mutable = orderedSetlistIds.toMutableList()
                                        val tmp = mutable[idx + 1]
                                        mutable[idx + 1] = mutable[idx]
                                        mutable[idx] = tmp
                                        orderedSetlistIds = mutable
                                    }
                                },
                                enabled = idx < orderedSetlistIds.size - 1
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bajar")
                            }
                            IconButton(onClick = {
                                orderedSetlistIds = orderedSetlistIds - slId
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar")
                            }
                        }
                    }
                }
            }

            HorizontalDivider()
            Text("Músicos asignados", style = MaterialTheme.typography.labelLarge)

            if (allMusicians.isEmpty()) {
                Text(
                    "No hay músicos creados.\nVe a la pestaña Músicos para añadir.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                allMusicians.forEach { m ->
                    val isSelected = m.id in selectedMusicianIds
                    val mRoles = sortRoles(m.rolesList())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedMusicianIds = if (isSelected) {
                                    musicianRoles = musicianRoles - m.id
                                    selectedMusicianIds - m.id
                                } else {
                                    musicianRoles = musicianRoles + (m.id to defaultRolesFor(m))
                                    selectedMusicianIds + m.id
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                selectedMusicianIds = if (isSelected) {
                                    musicianRoles = musicianRoles - m.id
                                    selectedMusicianIds - m.id
                                } else {
                                    musicianRoles = musicianRoles + (m.id to defaultRolesFor(m))
                                    selectedMusicianIds + m.id
                                }
                            }
                        )
                        Column(Modifier.weight(1f)) {
                            Text(m.name)
                            Text(
                                sortRoles(m.rolesList()).joinToString(", "),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (!m.phone.isNullOrBlank()) {
                            IconButton(onClick = {
                                val fechaStr = formatDateButton(dateMidnight)
                                val horaStr = formatTimeButton(hour, minute)
                                val roles = musicianRoles[m.id]?.let { sortRoles(it) }
                                    ?: sortRoles(m.rolesList())
                                val setlistNames = orderedSetlistIds.mapNotNull { slId ->
                                    setlists.find { it.id == slId }?.name
                                }
                                val msg = buildString {
                                    appendLine("Hola ${m.name}, te recuerdo tu participación en el culto:")
                                    appendLine()
                                    appendLine("🎵 Culto: ${name.ifBlank { "(sin nombre)" }}")
                                    appendLine("📅 Fecha: $fechaStr")
                                    appendLine("🕐 Hora: $horaStr")
                                    if (leader.isNotBlank()) {
                                        appendLine("👤 Líder: ${leader.trim()}")
                                    }
                                    appendLine("🎭 Tu rol: ${roles.joinToString(", ")}")
                                    if (setlistNames.isNotEmpty()) {
                                        appendLine("🎼 Setlists: ${setlistNames.joinToString(", ")}")
                                    }
                                    appendLine()
                                    append("— Enviado desde Cancionero")
                                }
                                ShareHelper.openWhatsApp(context, m.phone!!, msg)
                            }) {
                                Icon(Icons.Default.Chat, contentDescription = "WhatsApp")
                            }
                        }
                    }
                    if (isSelected && mRoles.size > 1) {
                        val selected = musicianRoles[m.id] ?: mRoles.toSet()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 48.dp, top = 4.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "En este culto:",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            mRoles.forEach { role ->
                                FilterChip(
                                    selected = role in selected,
                                    onClick = {
                                        val newSet = if (role in selected) {
                                            val s = selected - role
                                            if (s.isEmpty()) mRoles.toSet() else s
                                        } else {
                                            selected + role
                                        }
                                        musicianRoles = musicianRoles + (m.id to newSet)
                                    },
                                    label = { Text(role) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val rolesMap: Map<Long, String?> = selectedMusicianIds.associate { mId ->
                        val m = allMusicians.find { it.id == mId }
                        val mRoles = m?.rolesList()?.toSet() ?: emptySet()
                        val sel = musicianRoles[mId] ?: mRoles
                        val roleStr = if (sel == mRoles) null
                            else sortRoles(sel).joinToString(",").ifBlank { null }
                        mId to roleStr
                    }
                    onSave(
                        Culto(
                            id = initial?.id ?: 0,
                            name = name.trim(),
                            dateMillis = combineDateAndTime(dateMidnight, hour, minute),
                            leader = leader.trim().ifBlank { null }
                        ),
                        selectedMusicianIds.toList(),
                        rolesMap,
                        orderedSetlistIds
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

        if (showSetlistDialog) {
            SetlistPickerDialog(
                setlists = setlists,
                currentIds = orderedSetlistIds,
                onDismiss = { showSetlistDialog = false },
                onApply = { newIds ->
                    orderedSetlistIds = newIds
                    showSetlistDialog = false
                }
            )
        }
    }
}

@Composable
private fun SetlistPickerDialog(
    setlists: List<Setlist>,
    currentIds: List<Long>,
    onDismiss: () -> Unit,
    onApply: (List<Long>) -> Unit
) {
    val currentSet = currentIds.toSet()
    var selected by remember { mutableStateOf(currentSet) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar setlists") },
        text = {
            if (setlists.isEmpty()) {
                Text("No hay setlists creados.")
            } else {
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    setlists.forEach { sl ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selected = if (sl.id in selected)
                                        selected - sl.id
                                    else
                                        selected + sl.id
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = sl.id in selected,
                                onCheckedChange = {
                                    selected = if (sl.id in selected)
                                        selected - sl.id
                                    else
                                        selected + sl.id
                                }
                            )
                            Text(sl.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val result = mutableListOf<Long>()
                currentIds.filter { it in selected }.forEach { result.add(it) }
                selected.filter { it !in currentSet }.forEach { result.add(it) }
                onApply(result)
            }) { Text("Aplicar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}