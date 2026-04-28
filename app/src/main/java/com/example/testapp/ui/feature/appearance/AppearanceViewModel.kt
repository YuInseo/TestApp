package com.example.testapp.ui.feature.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.preferences.AccentPreset
import com.example.testapp.data.preferences.AppSettings
import com.example.testapp.data.preferences.CompletedStyle
import com.example.testapp.data.preferences.FontScale
import com.example.testapp.data.preferences.SettingsRepository
import com.example.testapp.data.preferences.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppearanceViewModel(
    private val repo: SettingsRepository
) : ViewModel() {
    val settings: StateFlow<AppSettings> =
        repo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun setAccent(preset: AccentPreset) = viewModelScope.launch { repo.setAccent(preset) }
    fun setTheme(mode: ThemeMode) = viewModelScope.launch { repo.setTheme(mode) }
    fun setFontScale(scale: FontScale) = viewModelScope.launch { repo.setFontScale(scale) }
    fun setShowSidebarCounts(value: Boolean) =
        viewModelScope.launch { repo.setShowSidebarCounts(value) }
    fun setHideNotes(value: Boolean) = viewModelScope.launch { repo.setHideNotes(value) }
    fun setShowListColor(value: Boolean) =
        viewModelScope.launch { repo.setShowListColor(value) }
    fun setCompletedStyle(style: CompletedStyle) =
        viewModelScope.launch { repo.setCompletedStyle(style) }
}
