package com.cancionero.app.ui.settings

import android.content.Context

object ThemePreference {
    private const val PREFS = "cancionero_prefs"
    private const val KEY = "theme_mode"

    const val MODE_SYSTEM = 0
    const val MODE_LIGHT = 1
    const val MODE_DARK = 2

    fun getMode(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY, MODE_SYSTEM)

    fun setMode(context: Context, mode: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY, mode).apply()
    }
}
