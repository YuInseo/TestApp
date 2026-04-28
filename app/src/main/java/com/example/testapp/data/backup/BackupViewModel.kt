package com.example.testapp.data.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class BackupState {
    data object Idle : BackupState()
    data object Working : BackupState()
    data class Exported(val file: File) : BackupState()
    data class Imported(val count: Int) : BackupState()
    data class Error(val message: String) : BackupState()
}

class BackupViewModel(
    private val manager: BackupManager
) : ViewModel() {

    private val _state = MutableStateFlow<BackupState>(BackupState.Idle)
    val state: StateFlow<BackupState> = _state.asStateFlow()

    fun export() {
        viewModelScope.launch {
            _state.value = BackupState.Working
            _state.value = try {
                val file = manager.export()
                BackupState.Exported(file)
            } catch (t: Throwable) {
                BackupState.Error(t.message ?: "내보내기에 실패했습니다")
            }
        }
    }

    fun importFile(file: File) {
        viewModelScope.launch {
            _state.value = BackupState.Working
            _state.value = try {
                val count = manager.import(file)
                BackupState.Imported(count)
            } catch (t: Throwable) {
                BackupState.Error(t.message ?: "가져오기에 실패했습니다")
            }
        }
    }

    fun dismiss() {
        _state.value = BackupState.Idle
    }
}
