package com.cancionero.app.ui.setlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cancionero.app.data.db.SetlistDao
import com.cancionero.app.data.model.Setlist
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SetlistsViewModel(private val dao: SetlistDao) : ViewModel() {

    val setlists: StateFlow<List<Setlist>> =
        dao.observeAll().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun create(name: String) {
        viewModelScope.launch {
            dao.insert(Setlist(name = name))
        }
    }

    fun delete(setlist: Setlist) {
        viewModelScope.launch {
            dao.delete(setlist)
        }
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
