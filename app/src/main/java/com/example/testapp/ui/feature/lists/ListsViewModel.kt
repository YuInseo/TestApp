package com.example.testapp.ui.feature.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.repository.TagRepository
import com.example.testapp.data.repository.TaskListRepository
import com.example.testapp.data.repository.TaskRepository
import com.example.testapp.domain.model.Tag
import com.example.testapp.domain.model.TaskList
import com.example.testapp.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SidebarState(
    val lists: List<TaskList> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val countByList: Map<Long, Int> = emptyMap(),
    val countByTag: Map<Long, Int> = emptyMap(),
    val todayCount: Int = 0,
    val inboxId: Long = 0
)

class ListsViewModel(
    private val repo: TaskListRepository,
    private val taskRepo: TaskRepository,
    private val tagRepo: TagRepository
) : ViewModel() {

    val state: StateFlow<SidebarState> = combine(
        repo.observeAll(),
        tagRepo.observeAll(),
        taskRepo.observeIncomplete()
    ) { lists, tags, tasks ->
        val countByList = tasks.groupingBy { it.listId }.eachCount()
        val countByTag = tasks
            .flatMap { t -> t.tags.map { it.id } }
            .groupingBy { it }.eachCount()
        val today0 = DateUtils.startOfDay()
        val tomorrow0 = DateUtils.addDays(today0, 1)
        val todayCount = tasks.count { t ->
            t.dueAt != null && t.dueAt in today0 until tomorrow0
        }
        SidebarState(
            lists = lists,
            tags = tags,
            countByList = countByList,
            countByTag = countByTag,
            todayCount = todayCount,
            inboxId = lists.firstOrNull { it.isInbox }?.id ?: 0L
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SidebarState())

    val lists: StateFlow<List<TaskList>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun upsert(list: TaskList) = viewModelScope.launch { repo.upsert(list) }
    fun delete(list: TaskList) = viewModelScope.launch { repo.delete(list) }
    fun upsertTag(tag: Tag) = viewModelScope.launch { tagRepo.upsert(tag) }
}
