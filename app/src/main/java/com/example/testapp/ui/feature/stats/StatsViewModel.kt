package com.example.testapp.ui.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.repository.PomodoroRepository
import com.example.testapp.data.repository.TaskRepository
import com.example.testapp.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DayStat(
    val timestamp: Long,
    val completed: Int,
    val focusMs: Long
)

data class StatsUiState(
    val today: DayStat,
    val week: List<DayStat>,
    val totalFocusMsToday: Long,
    val totalFocusMsWeek: Long,
    val completedToday: Int,
    val completedWeek: Int
)

class StatsViewModel(
    private val taskRepo: TaskRepository,
    private val pomoRepo: PomodoroRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        StatsUiState(
            today = DayStat(timestamp = System.currentTimeMillis(), completed = 0, focusMs = 0L),
            week = emptyList(),
            totalFocusMsToday = 0L,
            totalFocusMsWeek = 0L,
            completedToday = 0,
            completedWeek = 0
        )
    )
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            // Build last 7 days, oldest first. Index 6 = today.
            val days = mutableListOf<DayStat>()
            for (offset in 6 downTo 0) {
                val dayTime = DateUtils.addDays(now, -offset)
                val start = DateUtils.startOfDay(dayTime)
                val end = DateUtils.endOfDay(dayTime)
                val completed = taskRepo.countCompletedBetween(start, end)
                val focusMs = pomoRepo.focusMillisBetween(start, end)
                days.add(DayStat(timestamp = dayTime, completed = completed, focusMs = focusMs))
            }

            val today = days.last()
            val totalFocusMsWeek = days.sumOf { it.focusMs }
            val completedWeek = days.sumOf { it.completed }

            _state.value = StatsUiState(
                today = today,
                week = days,
                totalFocusMsToday = today.focusMs,
                totalFocusMsWeek = totalFocusMsWeek,
                completedToday = today.completed,
                completedWeek = completedWeek
            )
        }
    }
}
