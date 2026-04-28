package com.example.testapp.data.repository

import com.example.testapp.data.db.dao.TagDao
import com.example.testapp.data.db.dao.TaskDao
import com.example.testapp.data.db.entity.TaskTagCrossRef
import com.example.testapp.domain.model.Subtask
import com.example.testapp.domain.model.Tag
import com.example.testapp.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TaskRepository(
    private val taskDao: TaskDao,
    private val tagDao: TagDao
) {
    fun observeAll(): Flow<List<Task>> = taskDao.observeAll().map { it.map { e -> e.toDomain() } }

    fun observeByList(listId: Long): Flow<List<Task>> =
        taskDao.observeByList(listId).map { it.map { e -> e.toDomain() } }

    fun observeIncomplete(): Flow<List<Task>> =
        taskDao.observeIncomplete().map { it.map { e -> e.toDomain() } }

    fun observeCompleted(): Flow<List<Task>> =
        taskDao.observeCompleted().map { it.map { e -> e.toDomain() } }

    fun observeDueBetween(start: Long, end: Long): Flow<List<Task>> =
        taskDao.observeDueBetween(start, end).map { it.map { e -> e.toDomain() } }

    fun observeDueBefore(end: Long): Flow<List<Task>> =
        taskDao.observeDueBefore(end).map { it.map { e -> e.toDomain() } }

    fun observeWithDetails(): Flow<List<Task>> = combine(
        taskDao.observeAll(),
        tagDao.observeAll()
    ) { tasks, _ ->
        tasks.map { it.toDomain() }
    }

    fun search(query: String): Flow<List<Task>> =
        taskDao.search(query).map { it.map { e -> e.toDomain() } }

    suspend fun get(id: Long): Task? {
        val entity = taskDao.get(id) ?: return null
        val subtasks = taskDao.getSubtasks(id).map { it.toDomain() }
        val tagIds = taskDao.getTagIds(id)
        val tags = if (tagIds.isEmpty()) emptyList() else tagDao.getByIds(tagIds).map { it.toDomain() }
        return entity.toDomain(subtasks, tags)
    }

    suspend fun upsert(task: Task, tagIds: List<Long>, subtasks: List<Subtask>): Long {
        val now = System.currentTimeMillis()
        val newId = taskDao.insert(task.copy(updatedAt = now).toEntity())
        val taskId = if (task.id == 0L) newId else task.id
        // Replace subtasks
        taskDao.deleteSubtasksOf(taskId)
        subtasks.forEach { s -> taskDao.insertSubtask(s.copy(taskId = taskId).toEntity()) }
        // Replace tags
        taskDao.clearTags(taskId)
        tagIds.forEach { taskDao.addTag(TaskTagCrossRef(taskId, it)) }
        return taskId
    }

    suspend fun delete(taskId: Long) = taskDao.deleteById(taskId)

    suspend fun setCompleted(taskId: Long, completed: Boolean) {
        val now = System.currentTimeMillis()
        taskDao.setCompleted(taskId, completed, if (completed) now else null, now)
    }

    suspend fun upsertSubtask(subtask: Subtask) {
        if (subtask.id == 0L) taskDao.insertSubtask(subtask.toEntity())
        else taskDao.updateSubtask(subtask.toEntity())
    }

    suspend fun toggleSubtask(subtask: Subtask) {
        taskDao.updateSubtask(subtask.copy(completed = !subtask.completed).toEntity())
    }

    suspend fun deleteSubtask(subtask: Subtask) = taskDao.deleteSubtask(subtask.toEntity())

    fun observeSubtasks(taskId: Long): Flow<List<Subtask>> =
        taskDao.observeSubtasks(taskId).map { list -> list.map { it.toDomain() } }

    suspend fun getTagsForTask(taskId: Long): List<Tag> {
        val ids = taskDao.getTagIds(taskId)
        return if (ids.isEmpty()) emptyList() else tagDao.getByIds(ids).map { it.toDomain() }
    }

    suspend fun countDueBetween(start: Long, end: Long) = taskDao.countDueBetween(start, end)
    suspend fun countCompletedBetween(start: Long, end: Long) =
        taskDao.countCompletedBetween(start, end)
}
