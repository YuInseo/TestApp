package com.example.testapp.data.repository

import com.example.testapp.data.db.dao.TaskListDao
import com.example.testapp.domain.model.TaskList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskListRepository(private val dao: TaskListDao) {
    fun observeAll(): Flow<List<TaskList>> = dao.observeAll().map { it.map { e -> e.toDomain() } }

    suspend fun get(id: Long): TaskList? = dao.get(id)?.toDomain()

    suspend fun getInbox(): TaskList? = dao.getInbox()?.toDomain()

    suspend fun upsert(list: TaskList): Long {
        return if (list.id == 0L) dao.insert(list.toEntity())
        else { dao.update(list.toEntity()); list.id }
    }

    suspend fun delete(list: TaskList) {
        if (list.isInbox) return
        dao.delete(list.toEntity())
    }
}
