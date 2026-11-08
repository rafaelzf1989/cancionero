package com.cancionero.app.ui.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cancionero.app.data.db.CultoDao
import com.cancionero.app.data.model.Culto
import com.cancionero.app.data.model.Setlist
import com.cancionero.app.data.model.Song
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LiveViewModel(
    cultoDao: CultoDao,
    cultoId: Long
) : ViewModel() {

    val culto: StateFlow<Culto?> = cultoDao.observeCultoById(cultoId).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val setlists: StateFlow<List<Setlist>> = cultoDao.observeSetlistsOfCulto(cultoId).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val songs: StateFlow<List<Song>> = cultoDao.observeSongsOfCulto(cultoId).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    companion object {
        fun factory(cultoDao: CultoDao, cultoId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    LiveViewModel(cultoDao, cultoId) as T
            }
    }
}