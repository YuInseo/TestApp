package com.example.testapp.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.preferences.AppSettings
import com.example.testapp.data.preferences.SettingsRepository
import com.example.testapp.data.preferences.ThemeMode
import com.example.testapp.update.UpdateChecker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repo: SettingsRepository,
    private val updateChecker: UpdateChecker
) : ViewModel() {
    val settings: StateFlow<AppSettings> =
        repo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { repo.setTheme(mode) }
    fun setDynamic(enabled: Boolean) = viewModelScope.launch { repo.setDynamicColor(enabled) }
    fun setAutoUpdate(enabled: Boolean) = viewModelScope.launch { repo.setAutoUpdate(enabled) }
    fun setFocus(min: Int) = viewModelScope.launch { repo.setFocusMin(min) }
    fun setShortBreak(min: Int) = viewModelScope.launch { repo.setShortBreakMin(min) }
    fun setLongBreak(min: Int) = viewModelScope.launch { repo.setLongBreakMin(min) }
}
