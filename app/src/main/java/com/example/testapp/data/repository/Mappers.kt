package com.example.testapp.data.repository

import com.example.testapp.data.db.entity.HabitEntity
import com.example.testapp.data.db.entity.HabitEntryEntity
import com.example.testapp.data.db.entity.PomodoroSessionEntity
import com.example.testapp.data.db.entity.SubtaskEntity
import com.example.testapp.data.db.entity.TagEntity
import com.example.testapp.data.db.entity.TaskEntity
import com.example.testapp.data.db.entity.TaskListEntity
import com.example.testapp.domain.model.Habit
import com.example.testapp.domain.model.HabitEntry
import com.example.testapp.domain.model.PomodoroSession
import com.example.testapp.domain.model.Priority
import com.example.testapp.domain.model.Recurrence
import com.example.testapp.domain.model.SessionType
import com.example.testapp.domain.model.Subtask
import com.example.testapp.domain.model.Tag
import com.example.testapp.domain.model.Task
import com.example.testapp.domain.model.TaskList

fun TaskListEntity.toDomain() = TaskList(id, name, colorArgb, sortOrder, isInbox)
fun TaskList.toEntity() = TaskListEntity(id, name, colorArgb, sortOrder, isInbox)

fun TagEntity.toDomain() = Tag(id, name, colorArgb)
fun Tag.toEntity() = TagEntity(id, name, colorArgb)

fun SubtaskEntity.toDomain() = Subtask(id, taskId, title, completed, sortOrder)
fun Subtask.toEntity() = SubtaskEntity(id, taskId, title, completed, sortOrder)

fun TaskEntity.toDomain(subtasks: List<Subtask> = emptyList(), tags: List<Tag> = emptyList()) =
    Task(
        id = id,
        title = title,
        notes = notes,
        listId = listId,
        priority = Priority.fromLevel(priority),
        dueAt = dueAt,
        reminderAt = reminderAt,
        recurrence = runCatching { Recurrence.valueOf(recurrence) }.getOrDefault(Recurrence.NONE),
        completed = completed,
        completedAt = completedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        sortOrder = sortOrder,
        pinned = pinned,
        subtasks = subtasks,
        tags = tags
    )

fun Task.toEntity() = TaskEntity(
    id = id,
    listId = listId,
    title = title,
    notes = notes,
    priority = priority.level,
    dueAt = dueAt,
    reminderAt = reminderAt,
    recurrence = recurrence.name,
    completed = completed,
    completedAt = completedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    sortOrder = sortOrder,
    pinned = pinned
)

fun PomodoroSessionEntity.toDomain() = PomodoroSession(
    id = id,
    taskId = taskId,
    startedAt = startedAt,
    endedAt = endedAt,
    durationMillis = durationMillis,
    type = runCatching { SessionType.valueOf(type) }.getOrDefault(SessionType.FOCUS)
)

fun PomodoroSession.toEntity() = PomodoroSessionEntity(
    id = id,
    taskId = taskId,
    startedAt = startedAt,
    endedAt = endedAt,
    durationMillis = durationMillis,
    type = type.name
)

fun HabitEntity.toDomain() = Habit(id, name, emoji, colorArgb, targetPerWeek, createdAt, archived)
fun Habit.toEntity() = HabitEntity(id, name, emoji, colorArgb, targetPerWeek, createdAt, archived)

fun HabitEntryEntity.toDomain() = HabitEntry(id, habitId, date, completedAt)
fun HabitEntry.toEntity() = HabitEntryEntity(id, habitId, date, completedAt)
