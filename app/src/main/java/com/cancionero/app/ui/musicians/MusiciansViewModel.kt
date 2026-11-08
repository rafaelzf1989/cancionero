package com.cancionero.app.ui.musicians

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cancionero.app.data.db.AppDatabase
import com.cancionero.app.data.db.MusicianDao
import com.cancionero.app.data.model.Musician
import com.cancionero.app.share.BackupHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusiciansViewModel(
    private val dao: MusicianDao,
    private val db: AppDatabase
) : ViewModel() {

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

    suspend fun exportBackup(): String = BackupHelper.buildMusiciansBackup(db)

    suspend fun importBackup(content: String): Result<Int> =
        BackupHelper.restoreMusiciansBackup(db, content)

    companion object {
        fun factory(dao: MusicianDao, db: AppDatabase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    MusiciansViewModel(dao, db) as T
            }
    }
}