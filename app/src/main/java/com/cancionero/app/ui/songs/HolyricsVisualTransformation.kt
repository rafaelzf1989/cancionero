package com.cancionero.app.ui.songs

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class HolyricsVisualTransformation(
    private val chordColor: Color,
    private val sectionColor: Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text.text)
        
        text.text.split("\n").let { lines ->
            var currentPos = 0
            lines.forEach { line ->
                if (line.startsWith("//")) {
                    builder.addStyle(
                        SpanStyle(color = chordColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                        currentPos,
                        currentPos + line.length
                    )
                } else if (line.startsWith("##")) {
                    builder.addStyle(
                        SpanStyle(color = sectionColor, fontWeight = FontWeight.Bold),
                        currentPos,
                        currentPos + line.length
                    )
                }
                currentPos += line.length + 1 // +1 por el salto de línea (\n)
            }
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}