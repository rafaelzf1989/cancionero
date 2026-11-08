package com.cancionero.app.ui.settings

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object AppSettings {
    private const val PREFS = "cancionero_app_prefs"
    private const val KEY_FONT_SCALE = "font_scale"
    private const val KEY_BG_COLOR = "color_bg"
    private const val KEY_CHORD_COLOR = "color_chord"
    private const val KEY_LYRICS_COLOR = "color_lyrics"
    private const val KEY_SECTION_COLOR = "color_section"

    const val FONT_SCALE_MIN = 0.8f
    const val FONT_SCALE_MAX = 1.6f
    const val FONT_SCALE_DEFAULT = 1.0f

    fun getFontScale(context: Context): Float =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getFloat(KEY_FONT_SCALE, FONT_SCALE_DEFAULT)

    fun setFontScale(context: Context, scale: Float) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putFloat(KEY_FONT_SCALE, scale).apply()
    }

    // Colores por defecto
    val DefaultBg = Color(0xFF000000)       // Negro
    val DefaultChord = Color(0xFF00E5FF)    // Cian
    val DefaultLyrics = Color(0xFFFFFFFF)   // Blanco
    val DefaultSection = Color(0xFF9E9E9E)  // Gris

    fun getBgColor(context: Context): Color = Color(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_BG_COLOR, DefaultBg.toArgb()))
    fun setBgColor(context: Context, color: Color) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_BG_COLOR, color.toArgb()).apply()

    fun getChordColor(context: Context): Color = Color(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_CHORD_COLOR, DefaultChord.toArgb()))
    fun setChordColor(context: Context, color: Color) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_CHORD_COLOR, color.toArgb()).apply()

    fun getLyricsColor(context: Context): Color = Color(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_LYRICS_COLOR, DefaultLyrics.toArgb()))
    fun setLyricsColor(context: Context, color: Color) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_LYRICS_COLOR, color.toArgb()).apply()

    fun getSectionColor(context: Context): Color = Color(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_SECTION_COLOR, DefaultSection.toArgb()))
    fun setSectionColor(context: Context, color: Color) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_SECTION_COLOR, color.toArgb()).apply()

    fun resetColors(context: Context) {
        setBgColor(context, DefaultBg)
        setChordColor(context, DefaultChord)
        setLyricsColor(context, DefaultLyrics)
        setSectionColor(context, DefaultSection)
    }
}