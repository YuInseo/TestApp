package com.example.testapp.ui.feature.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.repository.TaskListRepository
import com.example.testapp.domain.model.TaskList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListsViewModel(
    private val repo: TaskListRepository
) : ViewModel() {
    val lists: StateFlow<List<TaskList>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun upsert(list: TaskList) = viewModelScope.launch { repo.upsert(list) }
    fun delete(list: TaskList) = viewModelScope.launch { repo.delete(list) }
}
