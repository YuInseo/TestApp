package com.example.testapp.data.backup

import android.content.Context
import com.example.testapp.data.repository.HabitRepository
import com.example.testapp.data.repository.TagRepository
import com.example.testapp.data.repository.TaskListRepository
import com.example.testapp.data.repository.TaskRepository
import com.example.testapp.domain.model.Habit
import com.example.testapp.domain.model.Priority
import com.example.testapp.domain.model.Recurrence
import com.example.testapp.domain.model.Subtask
import com.example.testapp.domain.model.Tag
import com.example.testapp.domain.model.Task
import com.example.testapp.domain.model.TaskList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupManager(
    private val context: Context,
    private val taskRepo: TaskRepository,
    private val listRepo: TaskListRepository,
    private val tagRepo: TagRepository,
    private val habitRepo: HabitRepository
) {

    @Serializable
    data class BackupSnapshot(
        val version: Int,
        val exportedAt: Long,
        val lists: List<BackupTaskList>,
        val tags: List<BackupTag>,
        val tasks: List<BackupTask>,
        val habits: List<BackupHabit>
    )

    @Serializable
    data class BackupTaskList(
        val id: Long,
        val name: String,
        val colorArgb: Long,
        val sortOrder: Int,
        val isInbox: Boolean
    )

    @Serializable
    data class BackupTag(
        val id: Long,
        val name: String,
        val colorArgb: Long
    )

    @Serializable
    data class BackupSubtask(
        val id: Long,
        val taskId: Long,
        val title: String,
        val completed: Boolean,
        val sortOrder: Int
    )

    @Serializable
    data class BackupTask(
        val id: Long,
        val title: String,
        val notes: String,
        val listId: Long,
        val priority: String,
        val dueAt: Long? = null,
        val reminderAt: Long? = null,
        val recurrence: String,
        val completed: Boolean,
        val completedAt: Long? = null,
        val createdAt: Long,
        val updatedAt: Long,
        val sortOrder: Int,
        val pinned: Boolean,
        val subtasks: List<BackupSubtask> = emptyList(),
        val tagIds: List<Long> = emptyList()
    )

    @Serializable
    data class BackupHabit(
        val id: Long,
        val name: String,
        val emoji: String,
        val colorArgb: Long,
        val targetPerWeek: Int,
        val createdAt: Long,
        val archived: Boolean
    )

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    suspend fun export(): File = withContext(Dispatchers.IO) {
        val lists = listRepo.observeAll().first()
        val tags = tagRepo.observeAll().first()
        val tasks = taskRepo.observeAll().first()
        val habits = habitRepo.observeActive().first()

        val snapshot = BackupSnapshot(
            version = 1,
            exportedAt = System.currentTimeMillis(),
            lists = lists.map {
                BackupTaskList(
                    id = it.id,
                    name = it.name,
                    colorArgb = it.colorArgb,
                    sortOrder = it.sortOrder,
                    isInbox = it.isInbox
                )
            },
            tags = tags.map {
                BackupTag(
                    id = it.id,
                    name = it.name,
                    colorArgb = it.colorArgb
                )
            },
            tasks = tasks.map { t ->
                BackupTask(
                    id = t.id,
                    title = t.title,
                    notes = t.notes,
                    listId = t.listId,
                    priority = t.priority.name,
                    dueAt = t.dueAt,
                    reminderAt = t.reminderAt,
                    recurrence = t.recurrence.name,
                    completed = t.completed,
                    completedAt = t.completedAt,
                    createdAt = t.createdAt,
                    updatedAt = t.updatedAt,
                    sortOrder = t.sortOrder,
                    pinned = t.pinned,
                    subtasks = t.subtasks.map { s ->
                        BackupSubtask(
                            id = s.id,
                            taskId = s.taskId,
                            title = s.title,
                            completed = s.completed,
                            sortOrder = s.sortOrder
                        )
                    },
                    tagIds = t.tags.map { it.id }
                )
            },
            habits = habits.map {
                BackupHabit(
                    id = it.id,
                    name = it.name,
                    emoji = it.emoji,
                    colorArgb = it.colorArgb,
                    targetPerWeek = it.targetPerWeek,
                    createdAt = it.createdAt,
                    archived = it.archived
                )
            }
        )

        val payload = json.encodeToString(BackupSnapshot.serializer(), snapshot)
        val dir = File(context.filesDir, "exports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val file = File(dir, "ticktick-backup-$stamp.json")
        file.writeText(payload)
        file
    }

    suspend fun import(file: File): Int = withContext(Dispatchers.IO) {
        val payload = file.readText()
        val snapshot = json.decodeFromString(BackupSnapshot.serializer(), payload)

        val listIdMap = mutableMapOf<Long, Long>()
        for (l in snapshot.lists) {
            val newId = listRepo.upsert(
                TaskList(
                    id = 0,
                    name = l.name,
                    colorArgb = l.colorArgb,
                    sortOrder = l.sortOrder,
                    isInbox = l.isInbox
                )
            )
            listIdMap[l.id] = newId
        }

        val tagIdMap = mutableMapOf<Long, Long>()
        for (t in snapshot.tags) {
            val newId = tagRepo.upsert(
                Tag(
                    id = 0,
                    name = t.name,
                    colorArgb = t.colorArgb
                )
            )
            tagIdMap[t.id] = newId
        }

        for (h in snapshot.habits) {
            habitRepo.upsert(
                Habit(
                    id = 0,
                    name = h.name,
                    emoji = h.emoji,
                    colorArgb = h.colorArgb,
                    targetPerWeek = h.targetPerWeek,
                    createdAt = h.createdAt,
                    archived = h.archived
                )
            )
        }

        var imported = 0
        for (bt in snapshot.tasks) {
            val mappedListId = listIdMap[bt.listId] ?: bt.listId
            val priority = runCatching { Priority.valueOf(bt.priority) }.getOrDefault(Priority.NONE)
            val recurrence =
                runCatching { Recurrence.valueOf(bt.recurrence) }.getOrDefault(Recurrence.NONE)
            val task = Task(
                id = 0,
                title = bt.title,
                notes = bt.notes,
                listId = mappedListId,
                priority = priority,
                dueAt = bt.dueAt,
                reminderAt = bt.reminderAt,
                recurrence = recurrence,
                completed = bt.completed,
                completedAt = bt.completedAt,
                createdAt = bt.createdAt,
                updatedAt = bt.updatedAt,
                sortOrder = bt.sortOrder,
                pinned = bt.pinned
            )
            val newTagIds = bt.tagIds.mapNotNull { tagIdMap[it] }
            val newSubtasks = bt.subtasks.map { s ->
                Subtask(
                    id = 0,
                    taskId = 0,
                    title = s.title,
                    completed = s.completed,
                    sortOrder = s.sortOrder
                )
            }
            taskRepo.upsert(task, newTagIds, newSubtasks)
            imported++
        }

        imported
    }
}
