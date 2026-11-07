package com.cancionero.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cancionero.app.data.model.Culto
import kotlinx.coroutines.flow.Flow

@Dao
interface CultoDao {
    @Insert
    suspend fun insert(culto: Culto): Long

    @Update
    suspend fun update(culto: Culto)

    @Delete
    suspend fun delete(culto: Culto)

    @Query("SELECT * FROM cultos ORDER BY dayOfWeek ASC, time ASC")
    fun observeAll(): Flow<List<Culto>>

    @Query("UPDATE cultos SET setlistId = :setlistId WHERE id IN (:ids)")
    suspend fun assignSetlistToMany(setlistId: Long?, ids: List<Long>)

    @Query("SELECT COUNT(*) FROM cultos")
    suspend fun count(): Int
}
