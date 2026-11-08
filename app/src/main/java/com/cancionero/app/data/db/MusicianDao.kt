package com.cancionero.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cancionero.app.data.model.Musician
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicianDao {
    @Insert
    suspend fun insert(musician: Musician): Long

    @Update
    suspend fun update(musician: Musician)

    @Delete
    suspend fun delete(musician: Musician)

    @Query("SELECT * FROM musicians ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Musician>>

    @Query("SELECT COUNT(*) FROM musicians")
    suspend fun count(): Int
}