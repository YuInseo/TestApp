package com.example.testapp.ui.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.repository.TaskListRepository
import com.example.testapp.data.repository.TaskRepository
import com.example.testapp.data.repository.TagRepository
import com.example.testapp.domain.model.Priority
import com.example.testapp.domain.model.Tag
import com.example.testapp.domain.model.Task
import com.example.testapp.domain.model.TaskList
import com.example.testapp.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SmartFilter(val label: String, val emoji: String) {
    INBOX("받은편지함", "📥"),
    TODAY("오늘", "📅"),
    TOMORROW("내일", "🌅"),
    NEXT_7("다음 7일", "🗓"),
    ALL("전체", "🌐"),
    COMPLETED("완료", "✅")
}

data class TasksUiState(
    val filter: SmartFilter = SmartFilter.TODAY,
    val selectedListId: Long? = null,
    val tasks: List<Task> = emptyList(),
    val lists: List<TaskList> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val searchQuery: String = ""
)

class TasksViewModel(
    private val taskRepo: TaskRepository,
    private val listRepo: TaskListRepository,
    private val tagRepo: TagRepository
) : ViewModel() {

    private val filter = MutableStateFlow(SmartFilter.TODAY)
    private val selectedListId = MutableStateFlow<Long?>(null)
    private val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<TasksUiState> =
        combine(filter, selectedListId, searchQuery, listRepo.observeAll(), tagRepo.observeAll()) { f, listId, q, lists, tags ->
            Quint(f, listId, q, lists, tags)
        }.flatMapLatest { (f, listId, q, lists, tags) ->
            val source = if (q.isNotBlank()) {
                taskRepo.search(q)
            } else if (listId != null) {
                taskRepo.observeByList(listId)
            } else when (f) {
                SmartFilter.INBOX -> {
                    val inboxId = lists.firstOrNull { it.isInbox }?.id
                    if (inboxId != null) taskRepo.observeByList(inboxId) else flowOf(emptyList())
                }
                SmartFilter.TODAY -> taskRepo.observeDueBetween(
                    DateUtils.startOfDay(),
                    DateUtils.endOfDay()
                )
                SmartFilter.TOMORROW -> taskRepo.observeDueBetween(
                    DateUtils.addDays(DateUtils.startOfDay(), 1),
                    DateUtils.addDays(DateUtils.endOfDay(), 1)
                )
                SmartFilter.NEXT_7 -> taskRepo.observeDueBetween(
                    DateUtils.startOfDay(),
                    DateUtils.addDays(DateUtils.endOfDay(), 6)
                )
                SmartFilter.ALL -> taskRepo.observeIncomplete()
                SmartFilter.COMPLETED -> taskRepo.observeCompleted()
            }
            combine(source, flowOf(lists), flowOf(tags)) { tasks, l, t ->
                TasksUiState(
                    filter = f,
                    selectedListId = listId,
                    tasks = tasks,
                    lists = l,
                    tags = t,
                    searchQuery = q
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TasksUiState())

    fun setFilter(f: SmartFilter) {
        filter.value = f
        selectedListId.value = null
    }

    fun selectList(id: Long?) {
        selectedListId.value = id
    }

    fun setSearch(q: String) { searchQuery.value = q }

    fun toggleCompleted(task: Task) {
        viewModelScope.launch { taskRepo.setCompleted(task.id, !task.completed) }
    }

    fun update(
        task: Task,
        title: String? = null,
        notes: String? = null,
        listId: Long? = null,
        priority: Priority? = null,
        dueAt: Long? = null,
        clearDueAt: Boolean = false
    ) {
        viewModelScope.launch {
            val updated = task.copy(
                title = title ?: task.title,
                notes = notes ?: task.notes,
                listId = listId ?: task.listId,
                priority = priority ?: task.priority,
                dueAt = if (clearDueAt) null else (dueAt ?: task.dueAt)
            )
            taskRepo.upsert(updated, task.tags.map { it.id }, task.subtasks)
        }
    }

    fun quickAdd(title: String, listId: Long?) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val targetList = listId ?: listRepo.getInbox()?.id ?: return@launch
            taskRepo.upsert(
                Task(title = title.trim(), listId = targetList),
                tagIds = emptyList(),
                subtasks = emptyList()
            )
        }
    }
}

private data class Quint<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
