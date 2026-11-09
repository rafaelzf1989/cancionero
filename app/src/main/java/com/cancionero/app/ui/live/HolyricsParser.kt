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
            
            if (rawLine.startsWith("##")) {
                if (currentLines.isNotEmpty()) {
                    sections.add(HolyricsSection(currentTitle, currentLines.toList()))
                    currentLines.clear()
                }
                var title = rawLine.substring(2).trim()
                if (title.startsWith("(") && title.endsWith(")")) {
                    title = title.substring(1, title.length - 1).trim()
                }
                currentTitle = title.ifBlank { "Sección" }
            } else if (rawLine.startsWith("//")) {
                // REA: Reemplazamos los // por dos espacios para conservar el ancho exacto
                val chords = rawLine.replaceFirst(Regex("^//"), "  ")
                var lyricsLine = ""
                if (i + 1 < lines.size) {
                    val next = lines[i + 1]
                    if (!next.startsWith("##") && !next.startsWith("//")) {
                        lyricsLine = next
                        i++
                    }
                }
                currentLines.add(HolyricsLine(chords, lyricsLine))
            } else {
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