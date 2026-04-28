package com.example.testapp.ui.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.repository.TaskListRepository
import com.example.testapp.data.repository.TaskRepository
import com.example.testapp.domain.model.Priority
import com.example.testapp.domain.model.Task
import com.example.testapp.domain.model.TaskList
import com.example.testapp.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val tasksOfMonth: Map<LocalDate, List<Task>> = emptyMap(),
    val tasksOfDay: List<Task> = emptyList(),
    val viewMode: CalendarViewMode = CalendarViewMode.MONTH,
    val lists: List<TaskList> = emptyList()
)

enum class CalendarViewMode { MONTH, WEEK }

class CalendarViewModel(
    private val taskRepo: TaskRepository,
    private val listRepo: TaskListRepository
) : ViewModel() {

    private val month = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val mode = MutableStateFlow(CalendarViewMode.MONTH)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<CalendarUiState> = combine(month, selectedDate, mode) { m, d, vm ->
        Triple(m, d, vm)
    }.flatMapLatest { (m, d, vm) ->
        val rangeStart = m.minusMonths(1).atDay(1)
        val rangeEnd = m.plusMonths(1).atEndOfMonth()
        val startMs = DateUtils.localDateToMillis(rangeStart, java.time.LocalTime.MIN)
        val endMs = DateUtils.localDateToMillis(rangeEnd, java.time.LocalTime.MAX)

        combine(
            taskRepo.observeDueBetween(startMs, endMs),
            taskRepo.observeDueBetween(
                DateUtils.localDateToMillis(d, java.time.LocalTime.MIN),
                DateUtils.localDateToMillis(d, java.time.LocalTime.MAX)
            ),
            listRepo.observeAll()
        ) { monthTasks, dayTasks, lists ->
            val grouped = monthTasks.groupBy { t ->
                t.dueAt?.let { DateUtils.toLocalDate(it) } ?: rangeStart
            }
            CalendarUiState(
                month = m,
                selectedDate = d,
                tasksOfMonth = grouped,
                tasksOfDay = dayTasks,
                viewMode = vm,
                lists = lists
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

    fun setMonth(m: YearMonth) { month.value = m }
    fun selectDate(d: LocalDate) {
        selectedDate.value = d
        if (d.month != month.value.month) month.value = YearMonth.of(d.year, d.month)
    }
    fun toggleMode() {
        mode.value = if (mode.value == CalendarViewMode.MONTH) CalendarViewMode.WEEK else CalendarViewMode.MONTH
    }

    fun toggleCompleted(task: Task) {
        viewModelScope.launch { taskRepo.setCompleted(task.id, !task.completed) }
    }

    fun update(
        task: Task,
        title: String? = null,
        notes: String? = null,
        listId: Long? = null,
        priority: Priority? = null
    ) {
        viewModelScope.launch {
            val updated = task.copy(
                title = title ?: task.title,
                notes = notes ?: task.notes,
                listId = listId ?: task.listId,
                priority = priority ?: task.priority
            )
            taskRepo.upsert(updated, task.tags.map { it.id }, task.subtasks)
        }
    }
}
