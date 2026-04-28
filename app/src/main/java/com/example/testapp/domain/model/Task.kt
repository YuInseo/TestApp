package com.example.testapp.domain.model

data class Task(
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val listId: Long,
    val priority: Priority = Priority.NONE,
    val dueAt: Long? = null,
    val reminderAt: Long? = null,
    val recurrence: Recurrence = Recurrence.NONE,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
    val pinned: Boolean = false,
    val subtasks: List<Subtask> = emptyList(),
    val tags: List<Tag> = emptyList()
)

data class Subtask(
    val id: Long = 0,
    val taskId: Long,
    val title: String,
    val completed: Boolean = false,
    val sortOrder: Int = 0
)

data class TaskList(
    val id: Long = 0,
    val name: String,
    val colorArgb: Long = 0xFF42A5F5,
    val sortOrder: Int = 0,
    val isInbox: Boolean = false
)

data class Tag(
    val id: Long = 0,
    val name: String,
    val colorArgb: Long = 0xFF7E57C2
)
