package com.example.testapp.ui.feature.matrix

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.repository.TaskRepository
import com.example.testapp.domain.model.Priority
import com.example.testapp.domain.model.Task
import com.example.testapp.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MatrixState(
    val q1: List<Task> = emptyList(),
    val q2: List<Task> = emptyList(),
    val q3: List<Task> = emptyList(),
    val q4: List<Task> = emptyList()
)

enum class Quadrant(val title: String, val subtitle: String, val colorArgb: Long) {
    Q1("긴급 & 중요", "Urgent & Important", 0xFFEF4444),
    Q2("중요하지만 급하지 않은 일", "Important, Not Urgent", 0xFFFACC15),
    Q3("중요하지 않지만 급한 일", "Urgent, Not Important", 0xFF60A5FA),
    Q4("긴급하지도 중요하지도 않은 일", "Not Urgent, Not Important", 0xFF34D399)
}

class MatrixViewModel(
    private val repo: TaskRepository
) : ViewModel() {
    val state: StateFlow<MatrixState> = repo.observeIncomplete().map { tasks ->
        val now = System.currentTimeMillis()
        val today0 = DateUtils.startOfDay(now)
        val tomorrow0 = DateUtils.addDays(today0, 1)
        val q1 = mutableListOf<Task>()
        val q2 = mutableListOf<Task>()
        val q3 = mutableListOf<Task>()
        val q4 = mutableListOf<Task>()
        tasks.forEach { t ->
            val urgent = t.dueAt != null && DateUtils.startOfDay(t.dueAt) < tomorrow0
            val important = t.priority.level >= Priority.MEDIUM.level
            when {
                urgent && important -> q1.add(t)
                !urgent && important -> q2.add(t)
                urgent && !important -> q3.add(t)
                else -> q4.add(t)
            }
        }
        MatrixState(q1, q2, q3, q4)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MatrixState())

    fun toggle(task: Task) = viewModelScope.launch { repo.setCompleted(task.id, !task.completed) }
}
