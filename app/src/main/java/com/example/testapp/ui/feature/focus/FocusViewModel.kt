package com.example.testapp.ui.feature.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.preferences.SettingsRepository
import com.example.testapp.data.repository.PomodoroRepository
import com.example.testapp.domain.model.PomodoroSession
import com.example.testapp.domain.model.SessionType
import com.example.testapp.util.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class FocusSubject(
    val id: String,
    val name: String,
    val emoji: String,
    val accentColor: Long,
    val totalMillisToday: Long = 0L
)

data class FocusUiState(
    val subjects: List<FocusSubject> = emptyList(),
    val running: RunningTimer? = null
)

data class RunningTimer(
    val subject: FocusSubject,
    val durationMillis: Long,
    val remainingMillis: Long,
    val type: SessionType
)

class FocusViewModel(
    private val pomoRepo: PomodoroRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val defaultSubjects = listOf(
        FocusSubject("study", "공부", "📚", 0xFF60A5FA),
        FocusSubject("read", "독서", "📖", 0xFFFCD34D),
        FocusSubject("work", "업무", "💼", 0xFFA78BFA),
        FocusSubject("exercise", "운동", "🏃", 0xFF34D399),
        FocusSubject("meditate", "명상", "🧘", 0xFFFBBF24),
        FocusSubject("rest", "휴식", "☕", 0xFFFB923C)
    )

    private val _state = MutableStateFlow(FocusUiState(subjects = defaultSubjects))
    val state: StateFlow<FocusUiState> = _state.asStateFlow()

    private var tickJob: Job? = null

    init {
        viewModelScope.launch { refreshTotals() }
    }

    private suspend fun refreshTotals() {
        val start = DateUtils.startOfDay()
        val end = DateUtils.endOfDay()
        val totalMs = pomoRepo.focusMillisBetween(start, end)
        // Apply totals to first subject (study) for simplicity; future: per-subject tracking
        val updated = defaultSubjects.mapIndexed { idx, s ->
            if (idx == 0) s.copy(totalMillisToday = totalMs) else s
        }
        _state.value = _state.value.copy(subjects = updated)
    }

    fun start(subject: FocusSubject, type: SessionType = SessionType.FOCUS) {
        val s = settingsRepo
        viewModelScope.launch {
            val settings = s.settings.first()
            val minutes = when (type) {
                SessionType.FOCUS -> settings.pomodoroFocusMin
                SessionType.SHORT_BREAK -> settings.pomodoroShortBreakMin
                SessionType.LONG_BREAK -> settings.pomodoroLongBreakMin
            }
            val duration = minutes * 60_000L
            val timer = RunningTimer(subject, duration, duration, type)
            _state.value = _state.value.copy(running = timer)
            tickJob?.cancel()
            tickJob = viewModelScope.launch {
                val started = System.currentTimeMillis()
                while (true) {
                    delay(1000)
                    val elapsed = System.currentTimeMillis() - started
                    val remaining = (duration - elapsed).coerceAtLeast(0L)
                    val cur = _state.value.running ?: break
                    _state.value = _state.value.copy(running = cur.copy(remainingMillis = remaining))
                    if (remaining == 0L) break
                }
                onFinish(subject, started, duration, type)
            }
        }
    }

    fun stop() {
        tickJob?.cancel()
        val cur = _state.value.running ?: return
        viewModelScope.launch {
            val elapsed = cur.durationMillis - cur.remainingMillis
            if (elapsed >= 60_000L) {
                pomoRepo.insert(
                    PomodoroSession(
                        startedAt = System.currentTimeMillis() - elapsed,
                        endedAt = System.currentTimeMillis(),
                        durationMillis = elapsed,
                        type = cur.type
                    )
                )
            }
            _state.value = _state.value.copy(running = null)
            refreshTotals()
        }
    }

    private suspend fun onFinish(
        subject: FocusSubject,
        startedAt: Long,
        duration: Long,
        type: SessionType
    ) {
        pomoRepo.insert(
            PomodoroSession(
                startedAt = startedAt,
                endedAt = startedAt + duration,
                durationMillis = duration,
                type = type
            )
        )
        _state.value = _state.value.copy(running = null)
        refreshTotals()
    }
}
