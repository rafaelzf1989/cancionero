package com.cancionero.app.ui.cultos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cancionero.app.data.db.CultoDao
import com.cancionero.app.data.db.CultoWithCount
import com.cancionero.app.data.db.MusicianDao
import com.cancionero.app.data.db.SetlistDao
import com.cancionero.app.data.model.Culto
import com.cancionero.app.data.model.CultoMusician
import com.cancionero.app.data.model.Musician
import com.cancionero.app.data.model.Setlist
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CultosViewModel(
    private val cultoDao: CultoDao,
    private val setlistDao: SetlistDao,
    private val musicianDao: MusicianDao
) : ViewModel() {

    val cultos: StateFlow<List<CultoWithCount>> = cultoDao.observeAllWithCount().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val setlists: StateFlow<List<Setlist>> = setlistDao.observeAll().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allMusicians: StateFlow<List<Musician>> = musicianDao.observeAll().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun save(culto: Culto, musicianIds: List<Long>) {
        viewModelScope.launch {
            val id = if (culto.id == 0L) {
                cultoDao.insert(culto)
            } else {
                cultoDao.update(culto)
                culto.id
            }
            cultoDao.clearMusiciansForCulto(id)
            if (musicianIds.isNotEmpty()) {
                cultoDao.assignMusicians(
                    musicianIds.map { CultoMusician(cultoId = id, musicianId = it) }
                )
            }
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

    suspend fun getMusicianIdsFor(cultoId: Long): List<Long> {
        return cultoDao.getMusicianIdsOfCulto(cultoId)
    }

    companion object {
        fun factory(
            cultoDao: CultoDao,
            setlistDao: SetlistDao,
            musicianDao: MusicianDao
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CultosViewModel(cultoDao, setlistDao, musicianDao) as T
            }
    }
}