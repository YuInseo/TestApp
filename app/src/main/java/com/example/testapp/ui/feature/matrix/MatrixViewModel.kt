package com.example.testapp.ui.feature.matrix

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.repository.TagRepository
import com.example.testapp.data.repository.TaskListRepository
import com.example.testapp.data.repository.TaskRepository
import com.example.testapp.domain.model.Priority
import com.example.testapp.domain.model.Tag
import com.example.testapp.domain.model.Task
import com.example.testapp.domain.model.TaskList
import com.example.testapp.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MatrixState(
    val q1: List<Task> = emptyList(),
    val q2: List<Task> = emptyList(),
    val q3: List<Task> = emptyList(),
    val q4: List<Task> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val lists: List<TaskList> = emptyList()
)

enum class Quadrant(val title: String, val subtitle: String, val colorArgb: Long) {
    Q1("긴급 & 중요", "Urgent & Important", 0xFFEF4444),
    Q2("중요하지만 급하지 않은 일", "Important, Not Urgent", 0xFFFACC15),
    Q3("중요하지 않지만 급한 일", "Urgent, Not Important", 0xFF60A5FA),
    Q4("긴급하지도 중요하지도 않은 일", "Not Urgent, Not Important", 0xFF34D399)
}

class MatrixViewModel(
    private val repo: TaskRepository,
    private val listRepo: TaskListRepository,
    private val tagRepo: TagRepository
) : ViewModel() {
    val state: StateFlow<MatrixState> = combine(
        repo.observeIncomplete(),
        tagRepo.observeAll(),
        listRepo.observeAll()
    ) { tasks, tags, lists ->
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
        MatrixState(q1, q2, q3, q4, tags, lists)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MatrixState())

    fun toggle(task: Task) = viewModelScope.launch { repo.setCompleted(task.id, !task.completed) }

    fun moveToQuadrant(task: Task, quadrant: Quadrant) {
        val (priority, dueAt) = quadrantDefaults(quadrant)
        viewModelScope.launch {
            val effectiveDue = when (quadrant) {
                Quadrant.Q1, Quadrant.Q3 -> dueAt
                Quadrant.Q2, Quadrant.Q4 -> {
                    val today0 = DateUtils.startOfDay()
                    val tomorrow0 = DateUtils.addDays(today0, 1)
                    if (task.dueAt != null && task.dueAt >= tomorrow0) task.dueAt else null
                }
            }
            val tagIds = task.tags.map { it.id }
            repo.upsert(
                task.copy(priority = priority, dueAt = effectiveDue),
                tagIds,
                task.subtasks
            )
        }
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
            repo.upsert(updated, task.tags.map { it.id }, task.subtasks)
        }
    }

    fun quickAdd(
        rawTitle: String,
        notes: String,
        quadrant: Quadrant,
        priorityOverride: Priority?,
        dueAtOverride: Long?,
        listIdOverride: Long?,
        extraTagIds: Set<Long>
    ) {
        val trimmed = rawTitle.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val tagNames = TAG_REGEX.findAll(trimmed).map { it.groupValues[1] }
                .filter { it.isNotEmpty() }
                .toSet()
            val title = TAG_REGEX.replace(trimmed, "").trim().replace(Regex("\\s+"), " ")
            if (title.isEmpty()) return@launch

            val listId = listIdOverride ?: listRepo.getInbox()?.id ?: return@launch
            val (defaultPriority, defaultDueAt) = quadrantDefaults(quadrant)
            val priority = priorityOverride ?: defaultPriority
            val dueAt = dueAtOverride ?: defaultDueAt

            val existing = state.value.allTags.associateBy { it.name.lowercase() }
            val parsedIds = tagNames.map { name ->
                existing[name.lowercase()]?.id ?: tagRepo.upsert(Tag(name = name))
            }
            val tagIds = (parsedIds + extraTagIds).distinct()

            val task = Task(
                title = title,
                notes = notes.trim(),
                listId = listId,
                priority = priority,
                dueAt = dueAt
            )
            repo.upsert(task, tagIds, emptyList())
        }
    }

    private fun quadrantDefaults(quadrant: Quadrant): Pair<Priority, Long?> {
        val endOfToday = DateUtils.endOfDay()
        return when (quadrant) {
            Quadrant.Q1 -> Priority.HIGH to endOfToday
            Quadrant.Q2 -> Priority.HIGH to null
            Quadrant.Q3 -> Priority.LOW to endOfToday
            Quadrant.Q4 -> Priority.NONE to null
        }
    }

    companion object {
        private val TAG_REGEX = Regex("#([\\p{L}\\p{N}_]+)")
    }
}
