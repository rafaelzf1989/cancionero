package com.cancionero.app.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.cancionero.app.data.model.Musician
import com.cancionero.app.data.model.Song
import java.io.File

object ShareHelper {

    fun shareText(context: Context, name: String, songs: List<Song>) {
        val text = SetlistShare.toText(name, songs)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, name)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir setlist"))
    }

    fun shareJson(context: Context, name: String, songs: List<Song>) {
        try {
            val json = SetlistShare.toJson(name, songs)
            val safeName = name.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(40)
            val file = File(context.cacheDir, "setlist_$safeName.json")
            file.writeText(json)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir setlist"))
        } catch (e: Exception) {
        }
    }

    fun shareSong(context: Context, song: Song) {
        val text = buildString {
            appendLine("Canción: ${song.title}")
            appendLine("Tonalidad: ${song.songKey}")
            song.tempoBpm?.let { appendLine("BPM: $it") }
            song.tempoText?.let { appendLine("Tempo: $it") }
            val sources = mutableListOf<String>()
            if (song.sourceLaCuerda) sources.add("LaCuerda")
            if (song.sourceCancionero) sources.add("Cancionero")
            if (song.libretaNumber != null) sources.add("Libreta #${song.libretaNumber}")
            if (sources.isNotEmpty()) appendLine("Fuente: ${sources.joinToString(", ")}")
            song.moment?.let { appendLine("Momento: $it") }
            song.tags?.takeIf { it.isNotBlank() }?.let { appendLine("Tags: $it") }
            song.notes?.let { appendLine("Notas: $it") }
            appendLine()
            appendLine("— Compartido con Cancionero")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, song.title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir canción"))
    }

    fun shareSongsList(context: Context, songs: List<Song>) {
        if (songs.isEmpty()) return
        val text = buildString {
            appendLine("Repertorio (${songs.size} canciones)")
            appendLine()
            songs.forEachIndexed { i, s ->
                val meta = buildString {
                    append(s.songKey)
                    s.tempoBpm?.let { append(" · $it BPM") }
                    s.tempoText?.takeIf { it.isNotBlank() }?.let { append(" · $it") }
                    s.moment?.let { append(" · $it") }
                }
                appendLine("${i + 1}. ${s.title} ($meta)")
            }
            appendLine()
            appendLine("— Compartido con Cancionero")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Repertorio")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir canciones"))
    }

    fun shareSongsJson(context: Context, songs: List<Song>) {
        try {
            val json = SetlistShare.toJson("Repertorio", songs)
            val file = File(context.cacheDir, "repertorio_${System.currentTimeMillis()}.json")
            file.writeText(json)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir canciones"))
        } catch (e: Exception) {
        }
    }

    fun openWhatsApp(context: Context, phone: String, message: String) {
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        if (cleanPhone.isBlank()) return
        val url = "https://wa.me/$cleanPhone?text=${Uri.encode(message)}"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val smsIntent = Intent(
                    Intent.ACTION_SENDTO,
                    Uri.parse("smsto:$cleanPhone")
                ).apply {
                    putExtra("sms_body", message)
                }
                context.startActivity(smsIntent)
            } catch (e2: Exception) {
            }
        }
    }

    /** Compartir como texto por cualquier app (chooser). */
    fun shareTextMessage(context: Context, message: String, subject: String? = null) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
        }
        context.startActivity(Intent.createChooser(intent, "Compartir"))
    }

    /** Compartir intentando abrir WhatsApp directamente; si falla, chooser general. */
    fun shareViaWhatsApp(context: Context, message: String) {
        val waIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(waIntent)
            return
        } catch (_: Exception) { }
        val waBizIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp.w4b")
        }
        try {
            context.startActivity(waBizIntent)
            return
        } catch (_: Exception) { }
        shareTextMessage(context, message)
    }

    /** Compartir todos los datos de un músico (chooser). */
    fun shareMusician(context: Context, musician: Musician) {
        val text = buildString {
            appendLine("🎵 Músico: ${musician.name}")
            appendLine("🎭 Roles: ${musician.roles}")
            musician.phone?.takeIf { it.isNotBlank() }?.let {
                appendLine("📱 Teléfono: $it")
            }
            musician.notes?.takeIf { it.isNotBlank() }?.let {
                appendLine("📝 Notas: $it")
            }
            appendLine()
            append("— Enviado desde Cancionero")
        }
        shareTextMessage(context, text, "Músico: ${musician.name}")
    }
}