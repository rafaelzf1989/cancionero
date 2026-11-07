package com.cancionero.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cancionero.app.data.db.AppDatabase
import com.cancionero.app.data.model.Song
import com.cancionero.app.ui.setlists.SetlistDetailScreen
import com.cancionero.app.ui.setlists.SetlistDetailViewModel
import com.cancionero.app.ui.setlists.SetlistsScreen
import com.cancionero.app.ui.setlists.SetlistsViewModel
import com.cancionero.app.ui.songs.RepertorioScreen
import com.cancionero.app.ui.songs.SongFormScreen
import com.cancionero.app.ui.songs.SongsViewModel

sealed interface Screen {
    data object Repertorio : Screen
    data object SongForm : Screen
    data object Setlists : Screen
    data class SetlistDetail(val id: Long) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.get(this)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(db)
                }
            }
        }
    }
}

@Composable
fun AppRoot(db: AppDatabase) {
    var screen by remember { mutableStateOf<Screen>(Screen.Repertorio) }
    var editingSong by remember { mutableStateOf<Song?>(null) }
    val showBottomBar = screen is Screen.Repertorio || screen is Screen.Setlists

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = screen is Screen.Repertorio,
                        onClick = { screen = Screen.Repertorio },
                        icon = {
                            Icon(Icons.Default.MusicNote, contentDescription = null)
                        },
                        label = { Text("Repertorio") }
                    )
                    NavigationBarItem(
                        selected = screen is Screen.Setlists,
                        onClick = { screen = Screen.Setlists },
                        icon = {
                            Icon(Icons.Default.QueueMusic, contentDescription = null)
                        },
                        label = { Text("Setlists") }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = screen) {
                is Screen.Repertorio -> {
                    val vm: SongsViewModel = viewModel(
                        factory = SongsViewModel.factory(db.songDao())
                    )
                    RepertorioScreen(
                        vm = vm,
                        onAddSong = {
                            editingSong = null
                            screen = Screen.SongForm
                        },
                        onEditSong = { song ->
                            editingSong = song
                            screen = Screen.SongForm
                        }
                    )
                }
                is Screen.SongForm -> {
                    val vm: SongsViewModel = viewModel(
                        factory = SongsViewModel.factory(db.songDao())
                    )
                    SongFormScreen(
                        initial = editingSong,
                        onSave = { song ->
                            vm.save(song)
                            editingSong = null
                            screen = Screen.Repertorio
                        },
                        onDelete = { song ->
                            vm.delete(song)
                            editingSong = null
                            screen = Screen.Repertorio
                        },
                        onCancel = {
                            editingSong = null
                            screen = Screen.Repertorio
                        }
                    )
                }
                is Screen.Setlists -> {
                    val vm: SetlistsViewModel = viewModel(
                        factory = SetlistsViewModel.factory(db.setlistDao())
                    )
                    SetlistsScreen(
                        vm = vm,
                        onOpenSetlist = { sl -> screen = Screen.SetlistDetail(sl.id) },
                        onCreateSetlist = { name -> vm.create(name) }
                    )
                }
                is Screen.SetlistDetail -> {
                    val vm: SetlistDetailViewModel = viewModel(
                        key = "setlist_${s.id}",
                        factory = SetlistDetailViewModel.factory(
                            db.setlistDao(), db.songDao(), s.id
                        )
                    )
                    SetlistDetailScreen(
                        vm = vm,
                        onBack = { screen = Screen.Setlists }
                    )
                }
            }
        }
    }
}
