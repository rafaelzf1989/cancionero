package com.cancionero.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cancionero.app.data.model.Setlist
import com.cancionero.app.data.model.SetlistSong
import com.cancionero.app.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SetlistDao {
    @Insert
    suspend fun insert(setlist: Setlist): Long

    @Update
    suspend fun update(setlist: Setlist)

    @Delete
    suspend fun delete(setlist: Setlist)

    @Query("SELECT * FROM setlists ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Setlist>>

    @Query("SELECT * FROM setlists WHERE isTemplate = 1 ORDER BY name COLLATE NOCASE ASC")
    fun observeTemplates(): Flow<List<Setlist>>

    @Query("SELECT * FROM setlists WHERE id = :id")
    suspend fun getById(id: Long): Setlist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSong(ref: SetlistSong)

    @Query("DELETE FROM setlist_songs WHERE setlistId = :setlistId AND songId = :songId")
    suspend fun removeSong(setlistId: Long, songId: Long)

    @Query(
        "SELECT s.* FROM songs s " +
        "INNER JOIN setlist_songs ss ON s.id = ss.songId " +
        "WHERE ss.setlistId = :setlistId ORDER BY ss.position ASC"
    )
    fun observeSongsOfSetlist(setlistId: Long): Flow<List<Song>>

    @Query(
        "SELECT s.* FROM songs s " +
        "INNER JOIN setlist_songs ss ON s.id = ss.songId " +
        "WHERE ss.setlistId = :setlistId ORDER BY ss.position ASC"
    )
    suspend fun getSongsOnce(setlistId: Long): List<Song>

    @Query("SELECT COALESCE(MAX(position), -1) FROM setlist_songs WHERE setlistId = :setlistId")
    suspend fun maxPosition(setlistId: Long): Int

    @Query("SELECT * FROM setlist_songs WHERE setlistId = :setlistId ORDER BY position ASC")
    suspend fun getAllRefs(setlistId: Long): List<SetlistSong>

    @Query("UPDATE setlist_songs SET position = :pos WHERE setlistId = :setlistId AND songId = :songId")
    suspend fun updatePosition(setlistId: Long, songId: Long, pos: Int)

    @Query("SELECT note FROM setlist_songs WHERE setlistId = :setlistId AND songId = :songId")
    suspend fun getNoteFor(setlistId: Long, songId: Long): String?

    @Query("UPDATE setlist_songs SET note = :note WHERE setlistId = :setlistId AND songId = :songId")
    suspend fun updateNote(setlistId: Long, songId: Long, note: String?)

    @Query("SELECT COUNT(*) FROM setlists")
    suspend fun count(): Int
}