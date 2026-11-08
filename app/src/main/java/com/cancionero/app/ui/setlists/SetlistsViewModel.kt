package com.cancionero.app.ui.setlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cancionero.app.data.db.SetlistDao
import com.cancionero.app.data.model.Setlist
import com.cancionero.app.data.model.SetlistSong
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SetlistsViewModel(private val dao: SetlistDao) : ViewModel() {

    val setlists: StateFlow<List<Setlist>> =
        dao.observeAll().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    val templates: StateFlow<List<Setlist>> =
        dao.observeTemplates().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    fun create(name: String) {
        viewModelScope.launch {
            dao.insert(Setlist(name = name))
        }
    }

    fun createFromTemplate(name: String, templateId: Long) {
        viewModelScope.launch {
            val newId = dao.insert(Setlist(name = name))
            val songs = dao.getSongsOnce(templateId)
            songs.forEachIndexed { idx, song ->
                dao.addSong(SetlistSong(setlistId = newId, songId = song.id, position = idx))
            }
        }
    }

    fun duplicate(setlist: Setlist) {
        viewModelScope.launch {
            val newId = dao.insert(
                Setlist(name = "${setlist.name} (copia)", isTemplate = false)
            )
            val songs = dao.getSongsOnce(setlist.id)
            songs.forEachIndexed { idx, song ->
                dao.addSong(SetlistSong(setlistId = newId, songId = song.id, position = idx))
            }
        }
    }

    fun toggleTemplate(setlist: Setlist) {
        viewModelScope.launch {
            dao.update(setlist.copy(isTemplate = !setlist.isTemplate))
        }
    }

    fun delete(setlist: Setlist) {
        viewModelScope.launch { dao.delete(setlist) }
    }

    companion object {
        fun factory(dao: SetlistDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SetlistsViewModel(dao) as T
            }
    }
}
