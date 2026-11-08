package com.cancionero.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cancionero.app.data.model.Moment
import kotlinx.coroutines.flow.Flow

@Dao
interface MomentDao {
    @Query("SELECT * FROM moments ORDER BY position ASC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Moment>>

    @Query("SELECT * FROM moments ORDER BY position ASC, name COLLATE NOCASE ASC")
    suspend fun getAll(): List<Moment>

    @Insert
    suspend fun insert(moment: Moment): Long

    @Update
    suspend fun update(moment: Moment)

    @Delete
    suspend fun delete(moment: Moment)

    @Query("SELECT COALESCE(MAX(position), -1) FROM moments")
    suspend fun maxPosition(): Int

    @Query("UPDATE songs SET moment = :newName WHERE moment = :oldName")
    suspend fun renameInSongs(oldName: String, newName: String)

    @Query("UPDATE songs SET moment = NULL WHERE moment = :name")
    suspend fun clearFromSongs(name: String)
}
