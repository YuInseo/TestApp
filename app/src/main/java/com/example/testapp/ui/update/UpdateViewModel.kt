package com.example.testapp.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.preferences.SettingsRepository
import com.example.testapp.update.UpdateChecker
import com.example.testapp.update.UpdateState
import com.example.testapp.update.VersionManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateViewModel(
    private val checker: UpdateChecker,
    private val settings: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    fun checkOnLaunch() {
        viewModelScope.launch {
            val s = settings.settings.first()
            if (!s.autoUpdateEnabled) return@launch
            checkInternal(silent = true)
        }
    }

    fun checkManually() {
        viewModelScope.launch { checkInternal(silent = false) }
    }

    private suspend fun checkInternal(silent: Boolean) {
        _state.value = UpdateState.Checking
        val manifest = checker.fetchManifest()
        if (manifest == null) {
            _state.value = if (silent) UpdateState.Idle else UpdateState.Error("버전 확인 실패")
            return
        }
        _state.value = if (checker.isNewer(manifest)) UpdateState.Available(manifest)
        else UpdateState.UpToDate
    }

    fun startDownload(manifest: VersionManifest) {
        viewModelScope.launch {
            _state.value = UpdateState.Downloading(0)
            val file = checker.downloadApk(manifest) { p ->
                _state.value = UpdateState.Downloading(p)
            }
            if (file == null) {
                _state.value = UpdateState.Error("다운로드 실패")
            } else {
                _state.value = UpdateState.ReadyToInstall(file)
            }
        }
    }

    fun install() {
        val s = _state.value
        if (s is UpdateState.ReadyToInstall) {
            _state.value = UpdateState.Installing
            viewModelScope.launch {
                withContext(Dispatchers.IO) { checker.installApk(s.apk) }
            }
        }
    }

    fun dismiss() { _state.value = UpdateState.Idle }

    fun openUnknownSourcesSettings() = checker.openUnknownSourcesSettings()
    fun canRequestInstall(): Boolean = checker.canRequestPackageInstalls()
}
