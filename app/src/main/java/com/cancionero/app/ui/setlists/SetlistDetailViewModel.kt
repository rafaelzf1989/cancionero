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
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val allSongs: StateFlow<List<Song>> =
        songDao.observeAll().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

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

    fun moveUp(song: Song) {
        viewModelScope.launch {
            val refs = setlistDao.getAllRefs(setlistId).sortedBy { it.position }
            val idx = refs.indexOfFirst { it.songId == song.id }
            if (idx <= 0) return@launch
            val current = refs[idx]
            val prev = refs[idx - 1]
            setlistDao.updatePosition(setlistId, current.songId, prev.position)
            setlistDao.updatePosition(setlistId, prev.songId, current.position)
        }
    }

    fun moveDown(song: Song) {
        viewModelScope.launch {
            val refs = setlistDao.getAllRefs(setlistId).sortedBy { it.position }
            val idx = refs.indexOfFirst { it.songId == song.id }
            if (idx < 0 || idx >= refs.size - 1) return@launch
            val current = refs[idx]
            val next = refs[idx + 1]
            setlistDao.updatePosition(setlistId, current.songId, next.position)
            setlistDao.updatePosition(setlistId, next.songId, current.position)
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
