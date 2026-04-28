package com.example.testapp.ui.feature.tabbarconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.preferences.AppSettings
import com.example.testapp.data.preferences.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TabBarConfigViewModel(
    private val repo: SettingsRepository
) : ViewModel() {
    val settings: StateFlow<AppSettings> =
        repo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun setEnabledTabs(ids: List<String>) =
        viewModelScope.launch { repo.setEnabledTabs(ids) }

    fun setMaxTabs(value: Int) =
        viewModelScope.launch { repo.setMaxTabs(value) }
}
