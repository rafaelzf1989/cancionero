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
            val line = rawLine.trimEnd()
            
            if (line.trim().startsWith("##")) {
                if (currentLines.isNotEmpty()) {
                    sections.add(HolyricsSection(currentTitle, currentLines.toList()))
                    currentLines.clear()
                }
                var title = line.trim().removePrefix("##").trim()
                if (title.startsWith("(") && title.endsWith(")")) {
                    title = title.removeSurrounding("(", ")").trim()
                }
                currentTitle = title.ifBlank { "Sección" }
            } else if (line.trim().startsWith("//")) {
                val chords = line.trim().removePrefix("//").trimEnd()
                var lyricsLine = ""
                if (i + 1 < lines.size) {
                    val next = lines[i + 1].trimEnd()
                    if (!next.trim().startsWith("##") && !next.trim().startsWith("//")) {
                        lyricsLine = next
                        i++
                    }
                }
                currentLines.add(HolyricsLine(chords, lyricsLine))
            } else if (line.isNotBlank()) {
                currentLines.add(HolyricsLine("", line))
            }
            i++
        }
        if (currentLines.isNotEmpty()) {
            sections.add(HolyricsSection(currentTitle, currentLines.toList()))
        }
        return sections
    }
}