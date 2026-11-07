package com.cancionero.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cancionero.app.data.db.AppDatabase
import com.cancionero.app.data.model.Song
import com.cancionero.app.ui.songs.RepertorioScreen
import com.cancionero.app.ui.songs.SongFormScreen
import com.cancionero.app.ui.songs.SongsViewModel

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
    val vm: SongsViewModel = viewModel(
        factory = SongsViewModel.factory(db.songDao())
    )
    var editingSong by remember { mutableStateOf<Song?>(null) }
    var showForm by remember { mutableStateOf(false) }

    if (showForm) {
        SongFormScreen(
            initial = editingSong,
            onSave = { song ->
                vm.save(song)
                showForm = false
                editingSong = null
            },
            onDelete = { song ->
                vm.delete(song)
                showForm = false
                editingSong = null
            },
            onCancel = {
                showForm = false
                editingSong = null
            }
        )
    } else {
        RepertorioScreen(
            vm = vm,
            onAddSong = {
                editingSong = null
                showForm = true
            },
            onEditSong = { song ->
                editingSong = song
                showForm = true
            }
        )
    }
}
