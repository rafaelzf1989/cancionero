package com.cancionero.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cancionero.app.data.model.Culto
import com.cancionero.app.data.model.CultoMusician
import com.cancionero.app.data.model.CultoSetlist
import com.cancionero.app.data.model.Setlist
import com.cancionero.app.data.model.Song
import kotlinx.coroutines.flow.Flow

data class CultoWithCount(
    val id: Long,
    val name: String,
    val dateMillis: Long,
    val leader: String?,
    val musicianCount: Int,
    val setlistCount: Int
)

@Dao
interface CultoDao {
    @Insert
    suspend fun insert(culto: Culto): Long

    @Update
    suspend fun update(culto: Culto)

    @Delete
    suspend fun delete(culto: Culto)

    @Query("SELECT * FROM cultos ORDER BY dateMillis ASC")
    fun observeAll(): Flow<List<Culto>>

    @Query(
        "SELECT c.id, c.name, c.dateMillis, c.leader, " +
        "(SELECT COUNT(*) FROM culto_musicians cm WHERE cm.cultoId = c.id) AS musicianCount, " +
        "(SELECT COUNT(*) FROM culto_setlists cs WHERE cs.cultoId = c.id) AS setlistCount " +
        "FROM cultos c ORDER BY c.dateMillis ASC"
    )
    fun observeAllWithCount(): Flow<List<CultoWithCount>>

    @Query("SELECT * FROM cultos WHERE id = :cultoId")
    fun observeCultoById(cultoId: Long): Flow<Culto?>

    @Query(
        "SELECT sl.* FROM setlists sl " +
        "INNER JOIN culto_setlists cs ON cs.setlistId = sl.id " +
        "WHERE cs.cultoId = :cultoId ORDER BY cs.position ASC"
    )
    fun observeSetlistsOfCulto(cultoId: Long): Flow<List<Setlist>>

    @Query(
        "SELECT s.* FROM songs s " +
        "INNER JOIN setlist_songs ss ON s.id = ss.songId " +
        "INNER JOIN culto_setlists cs ON cs.setlistId = ss.setlistId " +
        "WHERE cs.cultoId = :cultoId " +
        "ORDER BY cs.position ASC, ss.position ASC"
    )
    fun observeSongsOfCulto(cultoId: Long): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun assignMusicians(refs: List<CultoMusician>)

    @Query("DELETE FROM culto_musicians WHERE cultoId = :cultoId")
    suspend fun clearMusiciansForCulto(cultoId: Long)

    @Query("SELECT musicianId FROM culto_musicians WHERE cultoId = :cultoId")
    suspend fun getMusicianIdsOfCulto(cultoId: Long): List<Long>

    @Query("SELECT * FROM culto_musicians WHERE cultoId = :cultoId")
    suspend fun getCultoMusiciansOfCulto(cultoId: Long): List<CultoMusician>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun assignSetlists(refs: List<CultoSetlist>)

    @Query("DELETE FROM culto_setlists WHERE cultoId = :cultoId")
    suspend fun clearSetlistsForCulto(cultoId: Long)

    @Query("SELECT setlistId FROM culto_setlists WHERE cultoId = :cultoId ORDER BY position ASC")
    suspend fun getSetlistIdsOfCulto(cultoId: Long): List<Long>

    @Query("SELECT COUNT(*) FROM cultos")
    suspend fun count(): Int
}