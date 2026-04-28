package com.example.testapp.ui.feature.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.repository.HabitRepository
import com.example.testapp.domain.model.Habit
import com.example.testapp.domain.model.HabitEntry
import com.example.testapp.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class HabitWithStreak(
    val habit: Habit,
    val currentStreak: Int,
    val checkedToday: Boolean
)

data class HabitsUiState(
    val habits: List<HabitWithStreak> = emptyList(),
    val entriesThisWeek: Map<Long, Set<String>> = emptyMap()
)

@OptIn(ExperimentalCoroutinesApi::class)
class HabitsViewModel(
    private val repo: HabitRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HabitsUiState())
    val state: StateFlow<HabitsUiState> = _state.asStateFlow()

    init {
        observe()
    }

    private fun observe() {
        // Compute the current week (Sun..Sat) range
        val today = LocalDate.now()
        // DayOfWeek.value: MON=1..SUN=7. Days since most recent Sunday:
        val daysFromSunday = today.dayOfWeek.value % 7
        val sunday = today.minusDays(daysFromSunday.toLong())

        val rangeStart = DateUtils.dayKey(today.minusDays(365))
        val rangeEnd = DateUtils.dayKey(today)

        repo.observeActive()
            .flatMapLatest { habits ->
                if (habits.isEmpty()) {
                    flowOf(habits to emptyList<HabitEntry>())
                } else {
                    repo.observeEntriesBetween(rangeStart, rangeEnd)
                        .map { entries -> habits to entries }
                }
            }
            .onEach { (habits, allEntries) ->
                val byHabit: Map<Long, Set<String>> = allEntries
                    .groupBy { it.habitId }
                    .mapValues { (_, list) -> list.map { it.date }.toSet() }

                val todayKey = DateUtils.dayKey(today)
                val weekKeys: Set<String> = (0..6).map { offset ->
                    DateUtils.dayKey(sunday.plusDays(offset.toLong()))
                }.toSet()

                val withStreaks = habits.map { habit ->
                    val dates = byHabit[habit.id].orEmpty()
                    val streak = computeStreak(dates, today)
                    HabitWithStreak(
                        habit = habit,
                        currentStreak = streak,
                        checkedToday = todayKey in dates
                    )
                }

                val entriesThisWeek: Map<Long, Set<String>> = habits.associate { habit ->
                    val dates = byHabit[habit.id].orEmpty()
                    habit.id to dates.intersect(weekKeys)
                }

                _state.value = HabitsUiState(
                    habits = withStreaks,
                    entriesThisWeek = entriesThisWeek
                )
            }
            .launchIn(viewModelScope)
    }

    private fun computeStreak(dates: Set<String>, today: LocalDate): Int {
        var streak = 0
        var cursor = today
        while (DateUtils.dayKey(cursor) in dates) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    fun toggleToday(habit: Habit) {
        viewModelScope.launch {
            repo.toggleEntry(habit.id, DateUtils.dayKey(LocalDate.now()))
        }
    }

    fun upsert(habit: Habit) {
        viewModelScope.launch {
            repo.upsert(habit)
        }
    }

    fun delete(habit: Habit) {
        viewModelScope.launch {
            repo.delete(habit)
        }
    }
}
