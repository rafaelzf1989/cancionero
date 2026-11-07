package com.cancionero.app.share

import com.cancionero.app.data.model.Song
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SharedSong(
    val title: String,
    val songKey: String = "C",
    val tempoBpm: Int? = null,
    val tempoText: String? = null,
    val sourceLaCuerda: Boolean = false,
    val sourceCancionero: Boolean = false,
    val libretaNumber: Int? = null,
    val notes: String? = null
)

@Serializable
data class SharedSetlist(
    val type: String = "cancionero_setlist",
    val version: Int = 1,
    val name: String,
    val songs: List<SharedSong>
)

object SetlistShare {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun toText(setlistName: String, songs: List<Song>): String {
        val sb = StringBuilder()
        sb.appendLine(setlistName)
        sb.appendLine()
        songs.forEachIndexed { i, s ->
            val meta = buildString {
                append(s.songKey)
                s.tempoBpm?.let { append(" · $it BPM") }
                s.tempoText?.takeIf { it.isNotBlank() }?.let { append(" · $it") }
            }
            sb.appendLine("${i + 1}. ${s.title} ($meta)")
        }
        sb.appendLine()
        sb.appendLine("— Compartido con Cancionero")
        return sb.toString()
    }

    fun toJson(setlistName: String, songs: List<Song>): String {
        val shared = SharedSetlist(
            name = setlistName,
            songs = songs.map {
                SharedSong(
                    title = it.title,
                    songKey = it.songKey,
                    tempoBpm = it.tempoBpm,
                    tempoText = it.tempoText,
                    sourceLaCuerda = it.sourceLaCuerda,
                    sourceCancionero = it.sourceCancionero,
                    libretaNumber = it.libretaNumber,
                    notes = it.notes
                )
            }
        )
        return json.encodeToString(SharedSetlist.serializer(), shared)
    }

    fun parseJson(content: String): SharedSetlist? {
        return try {
            json.decodeFromString(SharedSetlist.serializer(), content)
        } catch (e: Exception) {
            null
        }
    }

    fun parseText(content: String, fallbackName: String): SharedSetlist {
        val lines = content.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val filtered = lines.filterNot { it.startsWith("—") || it.startsWith("--") }

        val songRegex = Regex("""^(\d+)\.\s*(.+?)(?:\s*\(([^)]+)\))?$""")

        var name = fallbackName
        val songLines: List<String>
        if (filtered.isNotEmpty() && songRegex.matchEntire(filtered[0]) == null) {
            name = filtered[0]
            songLines = filtered.drop(1)
        } else {
            songLines = filtered
        }

        val songs = songLines.map { line ->
            val m = songRegex.matchEntire(line)
            if (m != null) {
                val title = m.groupValues[2].trim()
                val meta = m.groupValues[3]
                parseMetaSong(title, meta)
            } else {
                SharedSong(title = line)
            }
        }
        return SharedSetlist(name = name, songs = songs)
    }

    private fun parseMetaSong(title: String, meta: String): SharedSong {
        var key = "C"
        var bpm: Int? = null
        var tempoText: String? = null
        var keySet = false
        if (meta.isNotBlank()) {
            meta.split("·").map { it.trim() }.forEach { part ->
                val bpmMatch = Regex("""^(\d+)\s*BPM$""").matchEntire(part)
                when {
                    bpmMatch != null -> bpm = bpmMatch.groupValues[1].toIntOrNull()
                    part == "Lento" || part == "Medio" || part == "Rápido" -> tempoText = part
                    !keySet -> {
                        key = part
                        keySet = true
                    }
                }
            }
        }
        return SharedSong(
            title = title,
            songKey = key,
            tempoBpm = bpm,
            tempoText = tempoText
        )
    }
}
