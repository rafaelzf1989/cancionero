package com.cancionero.app.ui.musicians

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cancionero.app.data.db.MusicianDao
import com.cancionero.app.data.model.Musician
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusiciansViewModel(private val dao: MusicianDao) : ViewModel() {

    val musicians: StateFlow<List<Musician>> =
        dao.observeAll().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    fun save(musician: Musician) {
        viewModelScope.launch {
            if (musician.id == 0L) dao.insert(musician) else dao.update(musician)
        }
    }

    fun delete(musician: Musician) {
        viewModelScope.launch { dao.delete(musician) }
    }

    companion object {
        fun factory(dao: MusicianDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    MusiciansViewModel(dao) as T
            }
    }
}