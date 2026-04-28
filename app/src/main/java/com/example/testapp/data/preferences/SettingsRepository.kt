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

enum class AccentPreset(val displayName: String, val argb: Long) {
    DEFAULT("기본값", 0xFF3B82F6),
    TEAL_BLUE("티얼 블루", 0xFF4DD0E1),
    TURQUOISE("터쿼이즈", 0xFF26A69A),
    REED_GREEN("갈대 녹색", 0xFFAED581),
    APRICOT("살구 노랑", 0xFFFFB74D),
    PEACH("복숭아", 0xFFF48FB1),
    LILAC("라일락", 0xFFB39DDB),
    PEARL("진주색", 0xFFE0E0E0),
    GRAVEL("자갈색", 0xFF9E9E9E),
    DARK("어둠", 0xFF1F1F1F),
    MATERIAL_YOU("재료 너", 0xFF6750A4)
}

enum class FontScale(val factor: Float, val displayName: String) {
    SMALL(0.85f, "작게"),
    NORMAL(1.0f, "기본"),
    LARGE(1.15f, "크게"),
    EXTRA_LARGE(1.3f, "더 크게")
}

enum class CompletedStyle { CHECKBOX, STRIKETHROUGH }

data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val startScreen: String = "tasks",
    val pomodoroFocusMin: Int = 25,
    val pomodoroShortBreakMin: Int = 5,
    val pomodoroLongBreakMin: Int = 15,
    val autoUpdateEnabled: Boolean = true,
    val accentPreset: AccentPreset = AccentPreset.DEFAULT,
    val fontScale: FontScale = FontScale.NORMAL,
    val showSidebarCounts: Boolean = true,
    val hideNotes: Boolean = false,
    val showListColor: Boolean = true,
    val completedStyle: CompletedStyle = CompletedStyle.CHECKBOX
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
            autoUpdateEnabled = p[KEY_AUTO_UPDATE] ?: true,
            accentPreset = runCatching { AccentPreset.valueOf(p[KEY_ACCENT] ?: "DEFAULT") }
                .getOrDefault(AccentPreset.DEFAULT),
            fontScale = runCatching { FontScale.valueOf(p[KEY_FONT_SCALE] ?: "NORMAL") }
                .getOrDefault(FontScale.NORMAL),
            showSidebarCounts = p[KEY_SIDEBAR_COUNTS] ?: true,
            hideNotes = p[KEY_HIDE_NOTES] ?: false,
            showListColor = p[KEY_SHOW_LIST_COLOR] ?: true,
            completedStyle = runCatching {
                CompletedStyle.valueOf(p[KEY_COMPLETED_STYLE] ?: "CHECKBOX")
            }.getOrDefault(CompletedStyle.CHECKBOX)
        )
    }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[KEY_THEME] = mode.name }
    suspend fun setDynamicColor(enabled: Boolean) = context.dataStore.edit { it[KEY_DYNAMIC] = enabled }
    suspend fun setStartScreen(value: String) = context.dataStore.edit { it[KEY_START] = value }
    suspend fun setFocusMin(value: Int) = context.dataStore.edit { it[KEY_FOCUS] = value }
    suspend fun setShortBreakMin(value: Int) = context.dataStore.edit { it[KEY_SHORT_BREAK] = value }
    suspend fun setLongBreakMin(value: Int) = context.dataStore.edit { it[KEY_LONG_BREAK] = value }
    suspend fun setAutoUpdate(enabled: Boolean) = context.dataStore.edit { it[KEY_AUTO_UPDATE] = enabled }
    suspend fun setAccent(preset: AccentPreset) =
        context.dataStore.edit { it[KEY_ACCENT] = preset.name }
    suspend fun setFontScale(scale: FontScale) =
        context.dataStore.edit { it[KEY_FONT_SCALE] = scale.name }
    suspend fun setShowSidebarCounts(value: Boolean) =
        context.dataStore.edit { it[KEY_SIDEBAR_COUNTS] = value }
    suspend fun setHideNotes(value: Boolean) =
        context.dataStore.edit { it[KEY_HIDE_NOTES] = value }
    suspend fun setShowListColor(value: Boolean) =
        context.dataStore.edit { it[KEY_SHOW_LIST_COLOR] = value }
    suspend fun setCompletedStyle(style: CompletedStyle) =
        context.dataStore.edit { it[KEY_COMPLETED_STYLE] = style.name }

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_DYNAMIC = booleanPreferencesKey("dynamic_color")
        private val KEY_START = stringPreferencesKey("start_screen")
        private val KEY_FOCUS = intPreferencesKey("focus_min")
        private val KEY_SHORT_BREAK = intPreferencesKey("short_break_min")
        private val KEY_LONG_BREAK = intPreferencesKey("long_break_min")
        private val KEY_AUTO_UPDATE = booleanPreferencesKey("auto_update")
        private val KEY_ACCENT = stringPreferencesKey("accent_preset")
        private val KEY_FONT_SCALE = stringPreferencesKey("font_scale")
        private val KEY_SIDEBAR_COUNTS = booleanPreferencesKey("sidebar_counts")
        private val KEY_HIDE_NOTES = booleanPreferencesKey("hide_notes")
        private val KEY_SHOW_LIST_COLOR = booleanPreferencesKey("show_list_color")
        private val KEY_COMPLETED_STYLE = stringPreferencesKey("completed_style")
    }
}
