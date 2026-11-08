package com.cancionero.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cancionero.app.data.db.AppDatabase
import com.cancionero.app.share.BackupHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    db: AppDatabase,
    currentMode: Int,
    onModeChanged: (Int) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var message by remember { mutableStateOf<String?>(null) }
    var pendingRestore by remember { mutableStateOf<String?>(null) }
    var showConfirmRestore by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = BackupHelper.buildBackup(db)
                    val ok = BackupHelper.writeToUri(context, uri, json)
                    message = if (ok) "Backup guardado correctamente" else "Error al guardar"
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
                pendingRestore = content
                showConfirmRestore = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                        }
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
            Text("Tema", style = MaterialTheme.typography.titleMedium)
            val options = listOf(
                ThemePreference.MODE_SYSTEM to "Sistema",
                ThemePreference.MODE_LIGHT to "Claro",
                ThemePreference.MODE_DARK to "Oscuro"
            )
            options.forEach { (mode, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onModeChanged(mode) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentMode == mode,
                        onClick = { onModeChanged(mode) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(label)
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            Text("Copia de seguridad", style = MaterialTheme.typography.titleMedium)
            Text(
                "Guarda un archivo con TODOS tus datos (canciones, " +
                    "setlists, cultos, músicos…) para no perder nada.",
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedButton(
                onClick = {
                    val name = "cancionero_backup_" +
                        SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)
                            .format(Date()) + ".json"
                    exportLauncher.launch(name)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Exportar backup")
            }

            OutlinedButton(
                onClick = {
                    importLauncher.launch(
                        arrayOf("application/json", "text/plain", "*/*")
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Restaurar desde backup")
            }

            Text(
                "⚠️ Restaurar reemplaza TODOS los datos actuales.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )

            message?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Text("Cancionero v1.0", style = MaterialTheme.typography.bodySmall)
            Text("Fase 12 completada ✅", style = MaterialTheme.typography.bodySmall)
        }

        if (showConfirmRestore && pendingRestore != null) {
            AlertDialog(
                onDismissRequest = {
                    showConfirmRestore = false
                    pendingRestore = null
                },
                title = { Text("Restaurar backup") },
                text = {
                    Text(
                        "Esto BORRARÁ todos tus datos actuales y los " +
                            "reemplazará por los del archivo.\n\n¿Continuar?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val content = pendingRestore!!
                                val result = BackupHelper.restoreBackup(db, content)
                                message = if (result.isSuccess) {
                                    val d = result.getOrNull()!!
                                    "Restaurado: ${d.songs.size} canciones, " +
                                        "${d.setlists.size} setlists, " +
                                        "${d.cultos.size} cultos, " +
                                        "${d.musicians.size} músicos"
                                } else {
                                    "Error al restaurar: " +
                                        (result.exceptionOrNull()?.message ?: "?")
                                }
                                showConfirmRestore = false
                                pendingRestore = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Sí, restaurar") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showConfirmRestore = false
                        pendingRestore = null
                    }) { Text("Cancelar") }
                }
            )
        }
    }
}