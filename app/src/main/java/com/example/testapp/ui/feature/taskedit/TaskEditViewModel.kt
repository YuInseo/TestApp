package com.example.testapp.ui.feature.taskedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.repository.TagRepository
import com.example.testapp.data.repository.TaskListRepository
import com.example.testapp.data.repository.TaskRepository
import com.example.testapp.domain.model.Priority
import com.example.testapp.domain.model.Recurrence
import com.example.testapp.domain.model.Subtask
import com.example.testapp.domain.model.Tag
import com.example.testapp.domain.model.Task
import com.example.testapp.domain.model.TaskList
import com.example.testapp.notification.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class FormState(
    val id: Long = 0,
    val title: String = "",
    val notes: String = "",
    val priority: Priority = Priority.NONE,
    val dueAt: Long? = null,
    val reminderAt: Long? = null,
    val recurrence: Recurrence = Recurrence.NONE,
    val listId: Long = 0,
    val pinned: Boolean = false,
    val subtasks: List<Subtask> = emptyList(),
    val tagIds: Set<Long> = emptySet(),
    val initialised: Boolean = false,
    val saved: Boolean = false
)

data class TaskEditUiState(
    val id: Long = 0,
    val title: String = "",
    val notes: String = "",
    val priority: Priority = Priority.NONE,
    val dueAt: Long? = null,
    val reminderAt: Long? = null,
    val recurrence: Recurrence = Recurrence.NONE,
    val listId: Long = 0,
    val pinned: Boolean = false,
    val subtasks: List<Subtask> = emptyList(),
    val tagIds: Set<Long> = emptySet(),
    val lists: List<TaskList> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val isLoading: Boolean = true,
    val saved: Boolean = false
)

class TaskEditViewModel(
    private val taskRepo: TaskRepository,
    private val listRepo: TaskListRepository,
    private val tagRepo: TagRepository,
    private val reminderScheduler: ReminderScheduler,
    private val taskId: Long
) : ViewModel() {

    private val form = MutableStateFlow(FormState())

    val state: StateFlow<TaskEditUiState> = combine(
        form,
        listRepo.observeAll(),
        tagRepo.observeAll()
    ) { f, lists, tags ->
        TaskEditUiState(
            id = f.id,
            title = f.title,
            notes = f.notes,
            priority = f.priority,
            dueAt = f.dueAt,
            reminderAt = f.reminderAt,
            recurrence = f.recurrence,
            listId = if (f.listId == 0L) lists.firstOrNull { it.isInbox }?.id ?: 0L else f.listId,
            pinned = f.pinned,
            subtasks = f.subtasks,
            tagIds = f.tagIds,
            lists = lists,
            allTags = tags,
            isLoading = !f.initialised,
            saved = f.saved
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TaskEditUiState())

    init {
        viewModelScope.launch {
            if (taskId == 0L) {
                form.value = FormState(initialised = true)
            } else {
                val t = taskRepo.get(taskId)
                if (t != null) {
                    form.value = FormState(
                        id = t.id,
                        title = t.title,
                        notes = t.notes,
                        priority = t.priority,
                        dueAt = t.dueAt,
                        reminderAt = t.reminderAt,
                        recurrence = t.recurrence,
                        listId = t.listId,
                        pinned = t.pinned,
                        subtasks = t.subtasks,
                        tagIds = t.tags.map { it.id }.toSet(),
                        initialised = true
                    )
                } else {
                    form.value = FormState(initialised = true)
                }
            }
        }
    }

    fun setTitle(v: String) { form.value = form.value.copy(title = v) }
    fun setNotes(v: String) { form.value = form.value.copy(notes = v) }
    fun setPriority(v: Priority) { form.value = form.value.copy(priority = v) }
    fun setDueAt(v: Long?) { form.value = form.value.copy(dueAt = v) }
    fun setReminderAt(v: Long?) { form.value = form.value.copy(reminderAt = v) }
    fun setRecurrence(v: Recurrence) { form.value = form.value.copy(recurrence = v) }
    fun setListId(v: Long) { form.value = form.value.copy(listId = v) }
    fun togglePin() { form.value = form.value.copy(pinned = !form.value.pinned) }
    fun toggleTag(id: Long) {
        val cur = form.value.tagIds
        form.value = form.value.copy(tagIds = if (cur.contains(id)) cur - id else cur + id)
    }

    fun addSubtask(title: String) {
        if (title.isBlank()) return
        form.value = form.value.copy(
            subtasks = form.value.subtasks + Subtask(taskId = taskId, title = title.trim())
        )
    }

    fun toggleSubtaskAt(index: Int) {
        val list = form.value.subtasks.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(completed = !list[index].completed)
            form.value = form.value.copy(subtasks = list)
        }
    }

    fun removeSubtaskAt(index: Int) {
        val list = form.value.subtasks.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            form.value = form.value.copy(subtasks = list)
        }
    }

    fun save() {
        val f = form.value
        val effectiveListId = state.value.listId
        if (f.title.isBlank() || effectiveListId == 0L) return
        viewModelScope.launch {
            val task = Task(
                id = f.id,
                title = f.title.trim(),
                notes = f.notes,
                listId = effectiveListId,
                priority = f.priority,
                dueAt = f.dueAt,
                reminderAt = f.reminderAt,
                recurrence = f.recurrence,
                pinned = f.pinned
            )
            val newId = taskRepo.upsert(task, f.tagIds.toList(), f.subtasks)
            if (f.reminderAt != null && f.reminderAt > System.currentTimeMillis()) {
                reminderScheduler.schedule(newId, f.title.trim(), f.reminderAt)
            } else {
                reminderScheduler.cancel(newId)
            }
            form.value = f.copy(saved = true)
        }
    }

    fun delete() {
        viewModelScope.launch {
            if (taskId != 0L) {
                reminderScheduler.cancel(taskId)
                taskRepo.delete(taskId)
            }
            form.value = form.value.copy(saved = true)
        }
    }
}
