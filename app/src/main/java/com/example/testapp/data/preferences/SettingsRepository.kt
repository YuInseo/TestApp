package com.example.testapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val startScreen: String = "tasks",
    val pomodoroFocusMin: Int = 25,
    val pomodoroShortBreakMin: Int = 5,
    val pomodoroLongBreakMin: Int = 15,
    val autoUpdateEnabled: Boolean = true
)

class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            theme = runCatching { ThemeMode.valueOf(p[KEY_THEME] ?: "SYSTEM") }
                .getOrDefault(ThemeMode.SYSTEM),
            dynamicColor = p[KEY_DYNAMIC] ?: true,
            startScreen = p[KEY_START] ?: "tasks",
            pomodoroFocusMin = p[KEY_FOCUS] ?: 25,
            pomodoroShortBreakMin = p[KEY_SHORT_BREAK] ?: 5,
            pomodoroLongBreakMin = p[KEY_LONG_BREAK] ?: 15,
            autoUpdateEnabled = p[KEY_AUTO_UPDATE] ?: true
        )
    }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[KEY_THEME] = mode.name }
    suspend fun setDynamicColor(enabled: Boolean) = context.dataStore.edit { it[KEY_DYNAMIC] = enabled }
    suspend fun setStartScreen(value: String) = context.dataStore.edit { it[KEY_START] = value }
    suspend fun setFocusMin(value: Int) = context.dataStore.edit { it[KEY_FOCUS] = value }
    suspend fun setShortBreakMin(value: Int) = context.dataStore.edit { it[KEY_SHORT_BREAK] = value }
    suspend fun setLongBreakMin(value: Int) = context.dataStore.edit { it[KEY_LONG_BREAK] = value }
    suspend fun setAutoUpdate(enabled: Boolean) = context.dataStore.edit { it[KEY_AUTO_UPDATE] = enabled }

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_DYNAMIC = booleanPreferencesKey("dynamic_color")
        private val KEY_START = stringPreferencesKey("start_screen")
        private val KEY_FOCUS = intPreferencesKey("focus_min")
        private val KEY_SHORT_BREAK = intPreferencesKey("short_break_min")
        private val KEY_LONG_BREAK = intPreferencesKey("long_break_min")
        private val KEY_AUTO_UPDATE = booleanPreferencesKey("auto_update")
    }
}
