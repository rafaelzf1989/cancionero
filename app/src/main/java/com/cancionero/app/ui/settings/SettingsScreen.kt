package com.cancionero.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentMode: Int,
    onModeChanged: (Int) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Ajustes") }) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)
        ) {
            Text("Tema", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

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

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text("Cancionero v1.0", style = MaterialTheme.typography.bodySmall)
            Text("Fase 5 completada ✅", style = MaterialTheme.typography.bodySmall)
        }
    }
}
