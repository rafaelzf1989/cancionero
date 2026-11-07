package com.cancionero.app.ui.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cancionero.app.data.db.SongDao
import com.cancionero.app.data.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SongFilters(
    val query: String = "",
    val key: String? = null,
    val tempoText: String? = null,
    val onlyLaCuerda: Boolean = false,
    val onlyCancionero: Boolean = false,
    val onlyLibreta: Boolean = false,
    val onlyFavorites: Boolean = false
)

class SongsViewModel(private val dao: SongDao) : ViewModel() {

    private val _filters = MutableStateFlow(SongFilters())
    val filters: StateFlow<SongFilters> = _filters.asStateFlow()

    val filteredSongs: StateFlow<List<Song>> =
        combine(dao.observeAll(), _filters) { songs, f ->
            songs.filter { matches(it, f) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun matches(song: Song, f: SongFilters): Boolean {
        if (f.query.isNotBlank() && !song.title.contains(f.query, ignoreCase = true)) return false
        if (f.key != null && f.key.isNotBlank() && !song.songKey.equals(f.key, ignoreCase = true)) return false
        if (f.tempoText != null && song.tempoText != f.tempoText) return false
        if (f.onlyLaCuerda && !song.sourceLaCuerda) return false
        if (f.onlyCancionero && !song.sourceCancionero) return false
        if (f.onlyLibreta && song.libretaNumber == null) return false
        if (f.onlyFavorites && !song.isFavorite) return false
        return true
    }

    fun updateQuery(q: String) = _filters.update { it.copy(query = q) }
    fun setKey(k: String?) = _filters.update { it.copy(key = k) }
    fun setTempo(t: String?) = _filters.update { it.copy(tempoText = t) }
    fun toggleLaCuerda() = _filters.update { it.copy(onlyLaCuerda = !it.onlyLaCuerda) }
    fun toggleCancionero() = _filters.update { it.copy(onlyCancionero = !it.onlyCancionero) }
    fun toggleLibreta() = _filters.update { it.copy(onlyLibreta = !it.onlyLibreta) }
    fun toggleFavorites() = _filters.update { it.copy(onlyFavorites = !it.onlyFavorites) }
    fun clearFilters() { _filters.value = SongFilters() }

    fun save(song: Song) {
        viewModelScope.launch {
            if (song.id == 0L) dao.insert(song) else dao.update(song)
        }
    }

    fun delete(song: Song) {
        viewModelScope.launch { dao.delete(song) }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch { dao.setFavorite(song.id, !song.isFavorite) }
    }

    companion object {
        fun factory(dao: SongDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SongsViewModel(dao) as T
            }
    }
}
