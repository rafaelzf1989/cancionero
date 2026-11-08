package com.cancionero.app.share

import android.content.Context
import android.net.Uri
import com.cancionero.app.data.db.AppDatabase
import com.cancionero.app.data.model.Culto
import com.cancionero.app.data.model.CultoMusician
import com.cancionero.app.data.model.Moment
import com.cancionero.app.data.model.Musician
import com.cancionero.app.data.model.Setlist
import com.cancionero.app.data.model.SetlistSong
import com.cancionero.app.data.model.Song
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val songs: List<Song> = emptyList(),
    val setlists: List<Setlist> = emptyList(),
    val setlistSongs: List<SetlistSong> = emptyList(),
    val cultos: List<Culto> = emptyList(),
    val moments: List<Moment> = emptyList(),
    val musicians: List<Musician> = emptyList(),
    val cultoMusicians: List<CultoMusician> = emptyList()
)

object BackupHelper {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun buildBackup(db: AppDatabase): String {
        val dao = db.backupDao()
        val data = BackupData(
            songs = dao.getAllSongs(),
            setlists = dao.getAllSetlists(),
            setlistSongs = dao.getAllSetlistSongs(),
            cultos = dao.getAllCultos(),
            moments = dao.getAllMoments(),
            musicians = dao.getAllMusicians(),
            cultoMusicians = dao.getAllCultoMusicians()
        )
        return json.encodeToString(BackupData.serializer(), data)
    }

    suspend fun restoreBackup(db: AppDatabase, content: String): Result<BackupData> {
        return try {
            val data = json.decodeFromString(BackupData.serializer(), content)
            val dao = db.backupDao()
            dao.clearCultoMusicians()
            dao.clearSetlistSongs()
            dao.clearCultos()
            dao.clearSetlists()
            dao.clearSongs()
            dao.clearMusicians()
            dao.clearMoments()
            if (data.songs.isNotEmpty()) dao.insertSongs(data.songs)
            if (data.setlists.isNotEmpty()) dao.insertSetlists(data.setlists)
            if (data.setlistSongs.isNotEmpty()) dao.insertSetlistSongs(data.setlistSongs)
            if (data.moments.isNotEmpty()) dao.insertMoments(data.moments)
            if (data.musicians.isNotEmpty()) dao.insertMusicians(data.musicians)
            if (data.cultos.isNotEmpty()) dao.insertCultos(data.cultos)
            if (data.cultoMusicians.isNotEmpty()) dao.insertCultoMusicians(data.cultoMusicians)
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun writeToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(content.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun readFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }
}