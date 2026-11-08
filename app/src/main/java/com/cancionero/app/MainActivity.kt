package com.cancionero.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cancionero.app.data.db.AppDatabase
import com.cancionero.app.data.model.Culto
import com.cancionero.app.data.model.Musician
import com.cancionero.app.data.model.Song
import com.cancionero.app.ui.cultos.CultoFormScreen
import com.cancionero.app.ui.cultos.CultosScreen
import com.cancionero.app.ui.cultos.CultosViewModel
import com.cancionero.app.ui.moments.ManageMomentsScreen
import com.cancionero.app.ui.moments.MomentsViewModel
import com.cancionero.app.ui.musicians.MusicianFormScreen
import com.cancionero.app.ui.musicians.MusiciansScreen
import com.cancionero.app.ui.musicians.MusiciansViewModel
import com.cancionero.app.ui.setlists.ImportSetlistScreen
import com.cancionero.app.ui.setlists.ImportViewModel
import com.cancionero.app.ui.setlists.SetlistDetailScreen
import com.cancionero.app.ui.setlists.SetlistDetailViewModel
import com.cancionero.app.ui.setlists.SetlistsScreen
import com.cancionero.app.ui.setlists.SetlistsViewModel
import com.cancionero.app.ui.settings.SettingsScreen
import com.cancionero.app.ui.settings.ThemePreference
import com.cancionero.app.ui.songs.RepertorioScreen
import com.cancionero.app.ui.songs.SongFormScreen
import com.cancionero.app.ui.songs.SongsViewModel

sealed interface Screen {
    data object Repertorio : Screen
    data object SongForm : Screen
    data object Setlists : Screen
    data class SetlistDetail(val id: Long) : Screen
    data object ImportSetlist : Screen
    data object Cultos : Screen
    data object CultoForm : Screen
    data object Musicians : Screen
    data object MusicianForm : Screen
    data object Settings : Screen
    data object ManageMoments : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.get(this)
        setContent { AppRoot(db) }
    }
}

@Composable
fun AppRoot(db: AppDatabase) {
    val context = LocalContext.current
    var themeMode by remember { mutableStateOf(ThemePreference.getMode(context)) }
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemePreference.MODE_LIGHT -> false
        ThemePreference.MODE_DARK -> true
        else -> systemDark
    }

    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppContent(db, themeMode) { newMode ->
                ThemePreference.setMode(context, newMode)
                themeMode = newMode
            }
        }
    }
}

@Composable
fun AppContent(db: AppDatabase, themeMode: Int, onThemeModeChanged: (Int) -> Unit) {
    var screen by remember { mutableStateOf<Screen>(Screen.Repertorio) }
    var editingSong by remember { mutableStateOf<Song?>(null) }
    var editingCulto by remember { mutableStateOf<Culto?>(null) }
    var editingMusician by remember { mutableStateOf<Musician?>(null) }
    var returnAfterMoments by remember { mutableStateOf<Screen>(Screen.Repertorio) }

    val showBottomBar = screen is Screen.Repertorio ||
            screen is Screen.Setlists ||
            screen is Screen.Cultos ||
            screen is Screen.Musicians ||
            screen is Screen.Settings

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = screen is Screen.Repertorio,
                        onClick = { screen = Screen.Repertorio },
                        icon = { Icon(Icons.Default.MusicNote, contentDescription = null) },
                        label = { Text("Repertorio") }
                    )
                    NavigationBarItem(
                        selected = screen is Screen.Setlists,
                        onClick = { screen = Screen.Setlists },
                        icon = { Icon(Icons.Default.QueueMusic, contentDescription = null) },
                        label = { Text("Setlists") }
                    )
                    NavigationBarItem(
                        selected = screen is Screen.Cultos,
                        onClick = { screen = Screen.Cultos },
                        icon = { Icon(Icons.Default.Event, contentDescription = null) },
                        label = { Text("Cultos") }
                    )
                    NavigationBarItem(
                        selected = screen is Screen.Musicians,
                        onClick = { screen = Screen.Musicians },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Músicos") }
                    )
                    NavigationBarItem(
                        selected = screen is Screen.Settings,
                        onClick = { screen = Screen.Settings },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Ajustes") }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = screen) {
                is Screen.Repertorio -> {
                    val vm: SongsViewModel = viewModel(
                        factory = SongsViewModel.factory(db.songDao(), db.momentDao())
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
                        },
                        onManageMoments = {
                            returnAfterMoments = Screen.Repertorio
                            screen = Screen.ManageMoments
                        }
                    )
                }
                is Screen.SongForm -> {
                    val vm: SongsViewModel = viewModel(
                        factory = SongsViewModel.factory(db.songDao(), db.momentDao())
                    )
                    val moments by vm.moments.collectAsState()
                    SongFormScreen(
                        initial = editingSong,
                        moments = moments,
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
                        onDuplicate = { song ->
                            vm.duplicate(song)
                            editingSong = null
                            screen = Screen.Repertorio
                        },
                        onManageMoments = {
                            returnAfterMoments = Screen.SongForm
                            screen = Screen.ManageMoments
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
                        onImportClick = { screen = Screen.ImportSetlist }
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
                is Screen.ImportSetlist -> {
                    val vm: ImportViewModel = viewModel(
                        factory = ImportViewModel.factory(db.setlistDao(), db.songDao())
                    )
                    ImportSetlistScreen(
                        vm = vm,
                        onBack = { screen = Screen.Setlists },
                        onImported = { screen = Screen.Setlists }
                    )
                }
                is Screen.Cultos -> {
                    val vm: CultosViewModel = viewModel(
                        factory = CultosViewModel.factory(
                            db.cultoDao(), db.setlistDao(), db.musicianDao()
                        )
                    )
                    CultosScreen(
                        vm = vm,
                        onCreateCulto = {
                            editingCulto = null
                            screen = Screen.CultoForm
                        },
                        onEditCulto = { culto ->
                            editingCulto = culto
                            screen = Screen.CultoForm
                        }
                    )
                }
                is Screen.CultoForm -> {
                    val vm: CultosViewModel = viewModel(
                        factory = CultosViewModel.factory(
                            db.cultoDao(), db.setlistDao(), db.musicianDao()
                        )
                    )
                    val setlists by vm.setlists.collectAsState()
                    val musicians by vm.allMusicians.collectAsState()
                    CultoFormScreen(
                        vm = vm,
                        initial = editingCulto,
                        setlists = setlists,
                        allMusicians = musicians,
                        onSave = { culto, musicianIds ->
                            vm.save(culto, musicianIds)
                            editingCulto = null
                            screen = Screen.Cultos
                        },
                        onDelete = { culto ->
                            vm.delete(culto)
                            editingCulto = null
                            screen = Screen.Cultos
                        },
                        onCancel = {
                            editingCulto = null
                            screen = Screen.Cultos
                        }
                    )
                }
                is Screen.Musicians -> {
                    val vm: MusiciansViewModel = viewModel(
                        factory = MusiciansViewModel.factory(db.musicianDao())
                    )
                    MusiciansScreen(
                        vm = vm,
                        onAdd = {
                            editingMusician = null
                            screen = Screen.MusicianForm
                        },
                        onEdit = { musician ->
                            editingMusician = musician
                            screen = Screen.MusicianForm
                        }
                    )
                }
                is Screen.MusicianForm -> {
                    val vm: MusiciansViewModel = viewModel(
                        factory = MusiciansViewModel.factory(db.musicianDao())
                    )
                    MusicianFormScreen(
                        initial = editingMusician,
                        onSave = { musician ->
                            vm.save(musician)
                            editingMusician = null
                            screen = Screen.Musicians
                        },
                        onDelete = { musician ->
                            vm.delete(musician)
                            editingMusician = null
                            screen = Screen.Musicians
                        },
                        onCancel = {
                            editingMusician = null
                            screen = Screen.Musicians
                        }
                    )
                }
                is Screen.Settings -> {
                    SettingsScreen(
                        db = db,
                        currentMode = themeMode,
                        onModeChanged = onThemeModeChanged
                    )
                }
                is Screen.ManageMoments -> {
                    val vm: MomentsViewModel = viewModel(
                        factory = MomentsViewModel.factory(db.momentDao())
                    )
                    ManageMomentsScreen(
                        vm = vm,
                        onBack = { screen = returnAfterMoments }
                    )
                }
            }
        }
    }
}