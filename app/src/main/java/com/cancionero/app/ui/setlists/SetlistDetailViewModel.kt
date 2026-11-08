package com.cancionero.app.ui.setlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cancionero.app.data.db.SetlistDao
import com.cancionero.app.data.db.SongDao
import com.cancionero.app.data.model.Setlist
import com.cancionero.app.data.model.SetlistSong
import com.cancionero.app.data.model.Song
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SetlistDetailViewModel(
    private val setlistDao: SetlistDao,
    private val songDao: SongDao,
    private val setlistId: Long
) : ViewModel() {

    val setlist: StateFlow<Setlist?> = flow {
        emit(setlistDao.getById(setlistId))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val songs: StateFlow<List<Song>> =
        setlistDao.observeSongsOfSetlist(setlistId).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    val allSongs: StateFlow<List<Song>> =
        songDao.observeAll().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    fun rename(newName: String) {
        val clean = newName.trim()
        if (clean.isBlank()) return
        viewModelScope.launch {
            val current = setlistDao.getById(setlistId) ?: return@launch
            setlistDao.update(
                current.copy(name = clean, updatedAt = System.currentTimeMillis())
            )
        }
    }

    fun addSong(song: Song) {
        viewModelScope.launch {
            val pos = setlistDao.maxPosition(setlistId) + 1
            setlistDao.addSong(
                SetlistSong(setlistId = setlistId, songId = song.id, position = pos)
            )
        }
    }

    fun removeSong(song: Song) {
        viewModelScope.launch {
            setlistDao.removeSong(setlistId, song.id)
        }
    }

    fun persistReorder(orderedSongIds: List<Long>) {
        viewModelScope.launch {
            orderedSongIds.forEachIndexed { idx, songId ->
                setlistDao.updatePosition(setlistId, songId, idx)
            }
        }
    }

    suspend fun getNoteFor(songId: Long): String? =
        setlistDao.getNoteFor(setlistId, songId)

    fun saveNote(songId: Long, note: String?) {
        viewModelScope.launch {
            setlistDao.updateNote(setlistId, songId, note)
        }
    }

    companion object {
        fun factory(
            setlistDao: SetlistDao,
            songDao: SongDao,
            setlistId: Long
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SetlistDetailViewModel(setlistDao, songDao, setlistId) as T
            }
    }
}