package com.cancionero.app.ui.live

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cancionero.app.data.model.Song

@Composable
fun StageScreen(
    cultoName: String,
    songs: List<Song>,
    startIndex: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var currentIndex by remember(startIndex, songs.size) {
        mutableIntStateOf(startIndex.coerceIn(0, (songs.size - 1).coerceAtLeast(0)))
    }

    val activity = context as? Activity
    DisposableEffect(activity) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    BackHandler(onBack = onBack)

    if (songs.isEmpty()) {
        Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("Sin canciones", color = Color.White)
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }
        }
        return
    }

    val song = songs[currentIndex]
    val sections = remember(song.lyrics) { HolyricsParser.parse(song.lyrics ?: "") }

    Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
                Column(Modifier.weight(1f)) {
                    Text(cultoName, style = MaterialTheme.typography.labelMedium, color = Color(0xFF9E9E9E))
                    Text("Canción ${currentIndex + 1} / ${songs.size}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E))
                }
            }

            HorizontalDivider(color = Color(0xFF333333))

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(song.title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetaItem(label = "Tono", value = song.songKey)
                    song.tempoBpm?.let { MetaItem(label = "BPM", value = it.toString()) }
                    song.tempoText?.takeIf { it.isNotBlank() }?.let { MetaItem(label = "Tempo", value = it) }
                    song.moment?.let { MetaItem(label = "Momento", value = it) }
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Color(0xFF333333))
                Spacer(Modifier.height(20.dp))

                if (!song.notes.isNullOrBlank()) {
                    Text("Notas: ${song.notes}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFFD54F))
                    Spacer(Modifier.height(12.dp))
                }

                if (sections.isNotEmpty()) {
                    sections.forEach { section ->
                        // Título de la sección (Etiqueta)
                        Surface(
                            color = Color(0xFF333333),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                section.title.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF9E9E9E),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        // Líneas de acordes y letras
                        section.lines.forEach { line ->
                            if (line.chords.isNotBlank()) {
                                Text(
                                    text = line.chords,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF), // Cian contrastante
                                    fontSize = 16.sp
                                )
                            }
                            if (line.lyrics.isNotBlank()) {
                                Text(
                                    text = line.lyrics,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                } else {
                    Text(
                        song.lyrics ?: "Sin letra registrada.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(80.dp))
            }

            HorizontalDivider(color = Color(0xFF333333))

            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { if (currentIndex > 0) currentIndex-- },
                    enabled = currentIndex > 0,
                    modifier = Modifier.weight(1f).height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF303030),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF1A1A1A),
                        disabledContentColor = Color(0xFF555555)
                    )
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Anterior")
                }
                Button(
                    onClick = { if (currentIndex < songs.size - 1) currentIndex++ },
                    enabled = currentIndex < songs.size - 1,
                    modifier = Modifier.weight(1f).height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00897B),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF1A1A1A),
                        disabledContentColor = Color(0xFF555555)
                    )
                ) {
                    Text("Siguiente")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun MetaItem(label: String, value: String) {
    Column {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E))
        Text(value, style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}