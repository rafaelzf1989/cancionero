package com.cancionero.app.ui.live

data class HolyricsLine(
    val chords: String,
    val lyrics: String
)

data class HolyricsSection(
    val title: String,
    val lines: List<HolyricsLine>
)

object HolyricsParser {
    fun parse(text: String): List<HolyricsSection> {
        if (text.isBlank()) return emptyList()
        val sections = mutableListOf<HolyricsSection>()
        var currentTitle = "Canción"
        val currentLines = mutableListOf<HolyricsLine>()

        val lines = text.split("\n")
        var i = 0
        while (i < lines.size) {
            val rawLine = lines[i]
            
            // REGLA: Si empieza con // es acorde. No importa si hay espacios antes.
            // El usuario especificó: "si la linea comienza con al menos una letra o un espacio... no cuenta".
            // Por lo tanto, evaluamos rawLine directamente.
            if (rawLine.startsWith("##")) {
                if (currentLines.isNotEmpty()) {
                    sections.add(HolyricsSection(currentTitle, currentLines.toList()))
                    currentLines.clear()
                }
                // Quitamos ## y paréntesis si los tiene, pero no obligamos a tenerlos.
                var title = rawLine.substring(2).trim()
                if (title.startsWith("(") && title.endsWith(")")) {
                    title = title.substring(1, title.length - 1).trim()
                }
                currentTitle = title.ifBlank { "Sección" }
            } else if (rawLine.startsWith("//")) {
                // Es línea de acordes. Guardamos todo lo que sigue después de //
                val chords = rawLine.substring(2)
                var lyricsLine = ""
                // Buscamos hacia adelante si la siguiente línea es de letra pura
                if (i + 1 < lines.size) {
                    val next = lines[i + 1]
                    if (!next.startsWith("##") && !next.startsWith("//")) {
                        lyricsLine = next
                        i++
                    }
                }
                currentLines.add(HolyricsLine(chords, lyricsLine))
            } else {
                // Es línea de letras (incluso si está vacía)
                if (rawLine.isNotBlank()) {
                    currentLines.add(HolyricsLine("", rawLine))
                }
            }
            i++
        }
        if (currentLines.isNotEmpty()) {
            sections.add(HolyricsSection(currentTitle, currentLines.toList()))
        }
        return sections
    }
}