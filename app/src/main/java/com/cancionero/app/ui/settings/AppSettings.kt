package com.cancionero.app.ui.settings

import android.content.Context
import androidx.compose.ui.graphics.Color

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

    val DefaultBg = Color(0xFF000000)
    val DefaultChord = Color(0xFF00E5FF)
    val DefaultLyrics = Color(0xFFFFFFFF)
    val DefaultSection = Color(0xFF9E9E9E)

    fun getBgColor(context: Context): Color = Color(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_BG_COLOR, DefaultBg.value.toInt()))
    fun setBgColor(context: Context, color: Color) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_BG_COLOR, color.value.toInt()).apply()

    fun getChordColor(context: Context): Color = Color(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_CHORD_COLOR, DefaultChord.value.toInt()))
    fun setChordColor(context: Context, color: Color) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_CHORD_COLOR, color.value.toInt()).apply()

    fun getLyricsColor(context: Context): Color = Color(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_LYRICS_COLOR, DefaultLyrics.value.toInt()))
    fun setLyricsColor(context: Context, color: Color) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_LYRICS_COLOR, color.value.toInt()).apply()

    fun getSectionColor(context: Context): Color = Color(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_SECTION_COLOR, DefaultSection.value.toInt()))
    fun setSectionColor(context: Context, color: Color) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_SECTION_COLOR, color.value.toInt()).apply()
}