package com.cancionero.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cancionero.app.data.model.Culto
import com.cancionero.app.data.model.CultoMusician
import com.cancionero.app.data.model.Moment
import com.cancionero.app.data.model.Musician
import com.cancionero.app.data.model.Setlist
import com.cancionero.app.data.model.SetlistSong
import com.cancionero.app.data.model.Song

@Dao
interface BackupDao {
    @Query("SELECT * FROM songs") suspend fun getAllSongs(): List<Song>
    @Query("SELECT * FROM setlists") suspend fun getAllSetlists(): List<Setlist>
    @Query("SELECT * FROM setlist_songs") suspend fun getAllSetlistSongs(): List<SetlistSong>
    @Query("SELECT * FROM cultos") suspend fun getAllCultos(): List<Culto>
    @Query("SELECT * FROM moments") suspend fun getAllMoments(): List<Moment>
    @Query("SELECT * FROM musicians") suspend fun getAllMusicians(): List<Musician>
    @Query("SELECT * FROM culto_musicians") suspend fun getAllCultoMusicians(): List<CultoMusician>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(items: List<Song>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlists(items: List<Setlist>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlistSongs(items: List<SetlistSong>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCultos(items: List<Culto>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoments(items: List<Moment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMusicians(items: List<Musician>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCultoMusicians(items: List<CultoMusician>)

    @Query("DELETE FROM culto_musicians") suspend fun clearCultoMusicians()
    @Query("DELETE FROM setlist_songs") suspend fun clearSetlistSongs()
    @Query("DELETE FROM cultos") suspend fun clearCultos()
    @Query("DELETE FROM setlists") suspend fun clearSetlists()
    @Query("DELETE FROM songs") suspend fun clearSongs()
    @Query("DELETE FROM musicians") suspend fun clearMusicians()
    @Query("DELETE FROM moments") suspend fun clearMoments()
}