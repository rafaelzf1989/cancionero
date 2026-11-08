package com.cancionero.app.ui.musicians

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cancionero.app.data.model.Musician
import com.cancionero.app.share.BackupHelper
import com.cancionero.app.share.ShareHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusiciansScreen(
    vm: MusiciansViewModel,
    onAdd: () -> Unit,
    onEdit: (Musician) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val musicians by vm.musicians.collectAsState()
    var message by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = vm.exportBackup()
                    val ok = BackupHelper.writeToUri(context, uri, json)
                    message = if (ok) "Exportados ${musicians.size} músicos" else "Error al exportar"
                } catch (e: Exception) {
                    message = "Error: ${e.message}"
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val content = BackupHelper.readFromUri(context, uri)
            if (content.isNullOrBlank()) {
                message = "Archivo vacío o ilegible"
            } else {
                scope.launch {
                    val result = vm.importBackup(content)
                    message = if (result.isSuccess) {
                        "Importados: ${result.getOrNull()} músicos nuevos"
                    } else {
                        "Error al importar: ${result.exceptionOrNull()?.message}"
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Músicos") },
                actions = {
                    IconButton(
                        onClick = {
                            val name = "musicos_" +
                                SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)
                                    .format(Date()) + ".json"
                            exportLauncher.launch(name)
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Exportar")
                    }
                    IconButton(
                        onClick = {
                            importLauncher.launch(
                                arrayOf("application/json", "text/plain", "*/*")
                            )
                        }
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = "Importar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Añadir músico")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            message?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            if (musicians.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay músicos.\nPulsa + para añadir uno.",
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(musicians, key = { it.id }) { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEdit(m) }
                                .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(m.name, style = MaterialTheme.typography.titleMedium)
                                Text(m.roles, style = MaterialTheme.typography.bodySmall)
                                if (!m.phone.isNullOrBlank()) {
                                    Text(
                                        m.phone!!,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            if (!m.phone.isNullOrBlank()) {
                                Spacer(Modifier.width(2.dp))
                                IconButton(onClick = {
                                    val msg = buildString {
                                        appendLine("Hola ${m.name}, te comparto tu perfil de músico:")
                                        appendLine()
                                        appendLine("🎵 Músico: ${m.name}")
                                        appendLine("🎭 Roles: ${m.roles}")
                                        appendLine("📱 Teléfono: ${m.phone}")
                                        m.notes?.takeIf { it.isNotBlank() }?.let {
                                            appendLine("📝 Notas: $it")
                                        }
                                        appendLine()
                                        append("— Enviado desde Cancionero")
                                    }
                                    ShareHelper.openWhatsApp(context, m.phone!!, msg)
                                }) {
                                    Icon(Icons.Default.Chat, contentDescription = "WhatsApp")
                                }
                            }
                            IconButton(onClick = {
                                ShareHelper.shareMusician(context, m)
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Compartir")
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}