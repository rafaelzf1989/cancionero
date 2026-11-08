package com.cancionero.app.ui.moments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cancionero.app.data.db.MomentDao
import com.cancionero.app.data.model.Moment
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MomentsViewModel(private val dao: MomentDao) : ViewModel() {

    val moments: StateFlow<List<Moment>> = dao.observeAll().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun add(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            dao.insert(Moment(name = name.trim(), position = dao.maxPosition() + 1))
        }
    }

    fun rename(moment: Moment, newName: String) {
        val clean = newName.trim()
        if (clean.isBlank() || clean == moment.name) return
        viewModelScope.launch {
            dao.renameInSongs(moment.name, clean)
            dao.update(moment.copy(name = clean))
        }
    }

    fun delete(moment: Moment) {
        viewModelScope.launch {
            dao.clearFromSongs(moment.name)
            dao.delete(moment)
        }
    }

    companion object {
        fun factory(dao: MomentDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    MomentsViewModel(dao) as T
            }
    }
}
