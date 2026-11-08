package com.cancionero.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    currentFontScale: Float,
    onModeChanged: (Int) -> Unit,
    onFontScaleChanged: (Float) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var message by remember { mutableStateOf<String?>(null) }
    var pendingRestore by remember { mutableStateOf<String?>(null) }
    var showConfirmRestore by remember { mutableStateOf(false) }

    var bgColor by remember { mutableStateOf(AppSettings.getBgColor(context)) }
    var chordColor by remember { mutableStateOf(AppSettings.getChordColor(context)) }
    var lyricsColor by remember { mutableStateOf(AppSettings.getLyricsColor(context)) }
    var sectionColor by remember { mutableStateOf(AppSettings.getSectionColor(context)) }

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
                    modifier = Modifier.fillMaxWidth().clickable { onModeChanged(mode) }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = currentMode == mode, onClick = { onModeChanged(mode) })
                    Spacer(Modifier.width(8.dp))
                    Text(label)
                }
            }

            HorizontalDivider()
            Text("Tamaño de letra", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("A", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = currentFontScale,
                    onValueChange = onFontScaleChanged,
                    valueRange = AppSettings.FONT_SCALE_MIN..AppSettings.FONT_SCALE_MAX,
                    steps = 7,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Text("A", style = MaterialTheme.typography.titleLarge)
            }

            HorizontalDivider()
            Text("Colores de la vista en vivo", style = MaterialTheme.typography.titleMedium)
            
            Card(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("(SECCIÓN)", color = sectionColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("// G  D  Em", color = chordColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Que todo lo que soy", color = lyricsColor, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("// C  D", color = chordColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Bendiga al Señor", color = lyricsColor, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                }
            }

            ColorPickerRow("Fondo", bgColor) { bgColor = it; AppSettings.setBgColor(context, it) }
            ColorPickerRow("Acordes", chordColor) { chordColor = it; AppSettings.setChordColor(context, it) }
            ColorPickerRow("Letras", lyricsColor) { lyricsColor = it; AppSettings.setLyricsColor(context, it) }
            ColorPickerRow("Secciones", sectionColor) { sectionColor = it; AppSettings.setSectionColor(context, it) }

            HorizontalDivider()
            Text("Copia de seguridad", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(
                onClick = {
                    val name = "cancionero_backup_" + SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date()) + ".json"
                    exportLauncher.launch(name)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Exportar backup")
            }
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Restaurar desde backup")
            }
            message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }

        if (showConfirmRestore && pendingRestore != null) {
            AlertDialog(
                onDismissRequest = { showConfirmRestore = false; pendingRestore = null },
                title = { Text("Restaurar backup") },
                text = { Text("Esto BORRARÁ todos tus datos actuales. ¿Continuar?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val content = pendingRestore!!
                                val result = BackupHelper.restoreBackup(db, content)
                                message = if (result.isSuccess) "Restaurado con éxito" else "Error: ${result.exceptionOrNull()?.message}"
                                showConfirmRestore = false
                                pendingRestore = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Sí, restaurar") }
                },
                dismissButton = { TextButton(onClick = { showConfirmRestore = false; pendingRestore = null }) { Text("Cancelar") } }
            )
        }
    }
}

@Composable
fun ColorPickerRow(label: String, selectedColor: Color, onColorSelected: (Color) -> Unit) {
    val colors = listOf(
        Color.White, Color.Black, Color(0xFF00E5FF), Color.Yellow,
        Color.Green, Color(0xFFFF4081), Color.Red, Color.Blue, Color(0xFF9E9E9E)
    )
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (color == selectedColor) 3.dp else 1.dp,
                            color = if (color == selectedColor) MaterialTheme.colorScheme.primary else Color.Gray,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}