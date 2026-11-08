package com.cancionero.app

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cancionero.app.data.db.AppDatabase
import com.cancionero.app.data.model.Culto
import com.cancionero.app.data.model.Musician
import com.cancionero.app.data.model.Song
import com.cancionero.app.ui.cultos.CultoFormScreen
import com.cancionero.app.ui.cultos.CultosScreen
import com.cancionero.app.ui.cultos.CultosViewModel
import com.cancionero.app.ui.live.LiveCultoScreen
import com.cancionero.app.ui.live.LiveViewModel
import com.cancionero.app.ui.live.StageScreen
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
import com.cancionero.app.ui.settings.AppSettings
import com.cancionero.app.ui.settings.SettingsScreen
import com.cancionero.app.ui.settings.ThemePreference
import com.cancionero.app.ui.songs.RepertorioScreen
import com.cancionero.app.ui.songs.SongFormScreen
import com.cancionero.app.ui.songs.SongsViewModel
import kotlinx.coroutines.delay

sealed interface Screen {
    data object Repertorio : Screen
    data object SongForm : Screen
    data class SongPreview(val song: Song) : Screen
    data object Setlists : Screen
    data class SetlistDetail(val id: Long) : Screen
    data object ImportSetlist : Screen
    data object Cultos : Screen
    data object CultoForm : Screen
    data class LiveCulto(val cultoId: Long) : Screen
    data class Stage(val cultoId: Long, val startIndex: Int) : Screen
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
    var fontScale by remember { mutableStateOf(AppSettings.getFontScale(context)) }
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemePreference.MODE_LIGHT -> false
        ThemePreference.MODE_DARK -> true
        else -> systemDark
    }
    val baseDensity = LocalDensity.current

    CompositionLocalProvider(
        LocalDensity provides Density(
            density = baseDensity.density,
            fontScale = baseDensity.fontScale * fontScale
        )
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                AppContent(
                    db = db,
                    themeMode = themeMode,
                    fontScale = fontScale,
                    onThemeModeChanged = { newMode ->
                        ThemePreference.setMode(context, newMode)
                        themeMode = newMode
                    },
                    onFontScaleChanged = { newScale ->
                        AppSettings.setFontScale(context, newScale)
                        fontScale = newScale
                    }
                )
            }
        }
    }
}

@Composable
fun AppContent(
    db: AppDatabase,
    themeMode: Int,
    fontScale: Float,
    onThemeModeChanged: (Int) -> Unit,
    onFontScaleChanged: (Float) -> Unit
) {
    val context = LocalContext.current

    var screen by remember { mutableStateOf<Screen>(Screen.Cultos) }
    var previousScreens by remember { mutableStateOf<List<Screen>>(emptyList()) }
    var backPressedOnce by remember { mutableStateOf(false) }

    var editingSong by remember { mutableStateOf<Song?>(null) }
    var editingCulto by remember { mutableStateOf<Culto?>(null) }
    var editingMusician by remember { mutableStateOf<Musician?>(null) }

    fun navigateTo(newScreen: Screen) {
        if (screen == newScreen) return
        previousScreens = previousScreens + screen
        screen = newScreen
    }

    fun goBack(): Boolean {
        if (previousScreens.isEmpty()) return false
        screen = previousScreens.last()
        previousScreens = previousScreens.dropLast(1)
        return true
    }

    LaunchedEffect(screen) {
        backPressedOnce = false
    }
    LaunchedEffect(backPressedOnce) {
        if (backPressedOnce) {
            delay(2000)
            backPressedOnce = false
        }
    }

    val showBottomBar = screen is Screen.Repertorio ||
            screen is Screen.Setlists ||
            screen is Screen.Cultos ||
            screen is Screen.Musicians ||
            screen is Screen.Settings

    val customBackHandlerScreens = screen is Screen.Stage || screen is Screen.LiveCulto

    if (!customBackHandlerScreens) {
        BackHandler {
            if (goBack()) {
                // ok
            } else if (screen is Screen.Cultos) {
                if (backPressedOnce) {
                    (context as? Activity)?.finish()
                } else {
                    backPressedOnce = true
                    Toast.makeText(
                        context,
                        "Pulsa atrás de nuevo para salir",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                screen = Screen.Cultos
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = screen is Screen.Repertorio,
                        onClick = { navigateTo(Screen.Repertorio) },
                        icon = { Icon(Icons.Default.MusicNote, contentDescription = null) },
                        label = { Text("Repertorio") }
                    )
                    NavigationBarItem(
                        selected = screen is Screen.Setlists,
                        onClick = { navigateTo(Screen.Setlists) },
                        icon = { Icon(Icons.Default.QueueMusic, contentDescription = null) },
                        label = { Text("Setlists") }
                    )
                    NavigationBarItem(
                        selected = screen is Screen.Cultos,
                        onClick = { navigateTo(Screen.Cultos) },
                        icon = { Icon(Icons.Default.Event, contentDescription = null) },
                        label = { Text("Cultos") }
                    )
                    NavigationBarItem(
                        selected = screen is Screen.Musicians,
                        onClick = { navigateTo(Screen.Musicians) },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Músicos") }
                    )
                    NavigationBarItem(
                        selected = screen is Screen.Settings,
                        onClick = { navigateTo(Screen.Settings) },
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
                            navigateTo(Screen.SongForm)
                        },
                        onEditSong = { song ->
                            editingSong = song
                            navigateTo(Screen.SongForm)
                        },
                        onManageMoments = {
                            navigateTo(Screen.ManageMoments)
                        },
                        onPreviewSong = { song ->
                            navigateTo(Screen.SongPreview(song))
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
                            goBack()
                        },
                        onDelete = { song ->
                            vm.delete(song)
                            editingSong = null
                            goBack()
                        },
                        onDuplicate = { song ->
                            vm.duplicate(song)
                            editingSong = null
                            goBack()
                        },
                        onManageMoments = {
                            navigateTo(Screen.ManageMoments)
                        },
                        onCancel = {
                            editingSong = null
                            goBack()
                        },
                        onPreview = { song ->
                            navigateTo(Screen.SongPreview(song))
                        }
                    )
                }
                is Screen.SongPreview -> {
                    StageScreen(
                        cultoName = "Vista previa",
                        songs = listOf(s.song),
                        startIndex = 0,
                        onBack = { goBack() }
                    )
                }
                is Screen.Setlists -> {
                    val vm: SetlistsViewModel = viewModel(
                        factory = SetlistsViewModel.factory(db.setlistDao())
                    )
                    SetlistsScreen(
                        vm = vm,
                        onOpenSetlist = { sl -> navigateTo(Screen.SetlistDetail(sl.id)) },
                        onImportClick = { navigateTo(Screen.ImportSetlist) }
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
                        onBack = { goBack() }
                    )
                }
                is Screen.ImportSetlist -> {
                    val vm: ImportViewModel = viewModel(
                        factory = ImportViewModel.factory(db.setlistDao(), db.songDao())
                    )
                    ImportSetlistScreen(
                        vm = vm,
                        onBack = { goBack() },
                        onImported = { goBack() }
                    )
                }
                is Screen.Cultos -> {
                    val vm: CultosViewModel = viewModel(
                        factory = CultosViewModel.factory(
                            db.cultoDao(), db.setlistDao(), db.musicianDao(),
                            db.songDao(), db.momentDao()
                        )
                    )
                    CultosScreen(
                        vm = vm,
                        onCreateCulto = {
                            editingCulto = null
                            navigateTo(Screen.CultoForm)
                        },
                        onEditCulto = { culto ->
                            editingCulto = culto
                            navigateTo(Screen.CultoForm)
                        },
                        onStartLive = { cultoId ->
                            navigateTo(Screen.LiveCulto(cultoId))
                        }
                    )
                }
                is Screen.CultoForm -> {
                    val vm: CultosViewModel = viewModel(
                        factory = CultosViewModel.factory(
                            db.cultoDao(), db.setlistDao(), db.musicianDao(),
                            db.songDao(), db.momentDao()
                        )
                    )
                    val setlists by vm.setlists.collectAsState()
                    val musicians by vm.allMusicians.collectAsState()
                    CultoFormScreen(
                        vm = vm,
                        initial = editingCulto,
                        setlists = setlists,
                        allMusicians = musicians,
                        onSave = { culto, musicianIds, musicianRoles, setlistIds ->
                            vm.save(culto, musicianIds, musicianRoles, setlistIds)
                            editingCulto = null
                            goBack()
                        },
                        onDelete = { culto ->
                            vm.delete(culto)
                            editingCulto = null
                            goBack()
                        },
                        onCancel = {
                            editingCulto = null
                            goBack()
                        }
                    )
                }
                is Screen.LiveCulto -> {
                    val vm: LiveViewModel = viewModel(
                        key = "live_${s.cultoId}",
                        factory = LiveViewModel.factory(db.cultoDao(), s.cultoId)
                    )
                    LiveCultoScreen(
                        vm = vm,
                        onSongClick = { index ->
                            navigateTo(Screen.Stage(s.cultoId, index))
                        },
                        onBack = { goBack() }
                    )
                }
                is Screen.Stage -> {
                    val vm: LiveViewModel = viewModel(
                        key = "live_${s.cultoId}",
                        factory = LiveViewModel.factory(db.cultoDao(), s.cultoId)
                    )
                    val songs by vm.songs.collectAsState()
                    val culto by vm.culto.collectAsState()
                    StageScreen(
                        cultoName = culto?.name ?: "Culto",
                        songs = songs,
                        startIndex = s.startIndex,
                        onBack = { goBack() }
                    )
                }
                is Screen.Musicians -> {
                    val vm: MusiciansViewModel = viewModel(
                        factory = MusiciansViewModel.factory(db.musicianDao(), db)
                    )
                    MusiciansScreen(
                        vm = vm,
                        onAdd = {
                            editingMusician = null
                            navigateTo(Screen.MusicianForm)
                        },
                        onEdit = { musician ->
                            editingMusician = musician
                            navigateTo(Screen.MusicianForm)
                        }
                    )
                }
                is Screen.MusicianForm -> {
                    val vm: MusiciansViewModel = viewModel(
                        factory = MusiciansViewModel.factory(db.musicianDao(), db)
                    )
                    MusicianFormScreen(
                        initial = editingMusician,
                        onSave = { musician ->
                            vm.save(musician)
                            editingMusician = null
                            goBack()
                        },
                        onDelete = { musician ->
                            vm.delete(musician)
                            editingMusician = null
                            goBack()
                        },
                        onCancel = {
                            editingMusician = null
                            goBack()
                        }
                    )
                }
                is Screen.Settings -> {
                    SettingsScreen(
                        db = db,
                        currentMode = themeMode,
                        currentFontScale = fontScale,
                        onModeChanged = onThemeModeChanged,
                        onFontScaleChanged = onFontScaleChanged
                    )
                }
                is Screen.ManageMoments -> {
                    val vm: MomentsViewModel = viewModel(
                        factory = MomentsViewModel.factory(db.momentDao())
                    )
                    ManageMomentsScreen(
                        vm = vm,
                        onBack = { goBack() }
                    )
                }
            }
        }
    }
}