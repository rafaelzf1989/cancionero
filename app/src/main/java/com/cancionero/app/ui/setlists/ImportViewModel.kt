package com.cancionero.app.ui.setlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cancionero.app.data.db.SetlistDao
import com.cancionero.app.data.db.SongDao
import com.cancionero.app.data.model.Setlist
import com.cancionero.app.data.model.SetlistSong
import com.cancionero.app.data.model.Song
import com.cancionero.app.share.SetlistShare
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImportViewModel(
    private val setlistDao: SetlistDao,
    private val songDao: SongDao
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _importing = MutableStateFlow(false)
    val importing: StateFlow<Boolean> = _importing.asStateFlow()

    private val _imported = MutableStateFlow(false)
    val imported: StateFlow<Boolean> = _imported.asStateFlow()

    fun consumeImported() {
        _imported.value = false
    }

    fun importFromText(content: String) {
        if (content.isBlank()) {
            _message.value = "Contenido vacío"
            return
        }
        viewModelScope.launch {
            _importing.value = true
            _message.value = null
            try {
                val parsed = if (content.trimStart().startsWith("{"))
                    SetlistShare.parseJson(content)
                else
                    SetlistShare.parseText(content, "Setlist importado")

                if (parsed == null) {
                    _message.value = "Formato no reconocido"
                } else {
                    val newId = setlistDao.insert(Setlist(name = parsed.name))
                    var pos = 0
                    for (s in parsed.songs) {
                        val existing = songDao.findByTitle(s.title)
                        val songId = existing?.id ?: songDao.insert(
                            Song(
                                title = s.title,
                                songKey = s.songKey,
                                tempoBpm = s.tempoBpm,
                                tempoText = s.tempoText,
                                sourceLaCuerda = s.sourceLaCuerda,
                                sourceCancionero = s.sourceCancionero,
                                libretaNumber = s.libretaNumber,
                                notes = s.notes
                            )
                        )
                        setlistDao.addSong(
                            SetlistSong(setlistId = newId, songId = songId, position = pos)
                        )
                        pos++
                    }
                    _message.value = "Importado: \"${parsed.name}\" (${parsed.songs.size} canciones)"
                    _imported.value = true
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            } finally {
                _importing.value = false
            }
        }
    }

    companion object {
        fun factory(setlistDao: SetlistDao, songDao: SongDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ImportViewModel(setlistDao, songDao) as T
            }
    }
}
