package com.cancionero.app.ui.cultos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cancionero.app.data.db.CultoDao
import com.cancionero.app.data.db.SetlistDao
import com.cancionero.app.data.model.Culto
import com.cancionero.app.data.model.Setlist
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CultosViewModel(
    private val cultoDao: CultoDao,
    private val setlistDao: SetlistDao
) : ViewModel() {

    val cultos: StateFlow<List<Culto>> = cultoDao.observeAll().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val setlists: StateFlow<List<Setlist>> = setlistDao.observeAll().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun save(culto: Culto) {
        viewModelScope.launch {
            if (culto.id == 0L) cultoDao.insert(culto) else cultoDao.update(culto)
        }
    }

    fun delete(culto: Culto) {
        viewModelScope.launch { cultoDao.delete(culto) }
    }

    fun assignToMany(cultoIds: List<Long>, setlistId: Long?) {
        if (cultoIds.isEmpty()) return
        viewModelScope.launch {
            cultoDao.assignSetlistToMany(setlistId, cultoIds)
        }
    }

    companion object {
        fun factory(cultoDao: CultoDao, setlistDao: SetlistDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CultosViewModel(cultoDao, setlistDao) as T
            }
    }
}
