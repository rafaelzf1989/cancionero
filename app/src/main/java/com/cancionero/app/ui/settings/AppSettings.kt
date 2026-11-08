package com.cancionero.app.ui.settings

import android.content.Context

object AppSettings {
    private const val PREFS = "cancionero_app_prefs"
    private const val KEY_FONT_SCALE = "font_scale"

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
}