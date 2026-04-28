package com.example.testapp.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.testapp.data.db.entity.SubtaskEntity
import com.example.testapp.data.db.entity.TaskEntity
import com.example.testapp.data.db.entity.TaskTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun get(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY pinned DESC, sortOrder, dueAt IS NULL, dueAt, priority DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE listId = :listId ORDER BY pinned DESC, sortOrder, dueAt IS NULL, dueAt, priority DESC")
    fun observeByList(listId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY pinned DESC, sortOrder, dueAt IS NULL, dueAt, priority DESC")
    fun observeIncomplete(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE completed = 1 ORDER BY completedAt DESC")
    fun observeCompleted(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE completed = 0 AND dueAt IS NOT NULL AND dueAt BETWEEN :start AND :end ORDER BY dueAt")
    fun observeDueBetween(start: Long, end: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE completed = 0 AND dueAt IS NOT NULL AND dueAt < :end ORDER BY dueAt")
    fun observeDueBefore(end: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE tasks SET completed = :completed, completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean, completedAt: Long?, updatedAt: Long)

    // Subtasks
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY sortOrder, id")
    fun observeSubtasks(taskId: Long): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY sortOrder, id")
    suspend fun getSubtasks(taskId: Long): List<SubtaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity)

    @Delete
    suspend fun deleteSubtask(subtask: SubtaskEntity)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasksOf(taskId: Long)

    // Tag cross-refs
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTag(crossRef: TaskTagCrossRef)

    @Query("DELETE FROM task_tag_cross_ref WHERE taskId = :taskId AND tagId = :tagId")
    suspend fun removeTag(taskId: Long, tagId: Long)

    @Query("DELETE FROM task_tag_cross_ref WHERE taskId = :taskId")
    suspend fun clearTags(taskId: Long)

    @Query("SELECT tagId FROM task_tag_cross_ref WHERE taskId = :taskId")
    suspend fun getTagIds(taskId: Long): List<Long>

    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 0 AND dueAt IS NOT NULL AND dueAt BETWEEN :start AND :end")
    suspend fun countDueBetween(start: Long, end: Long): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 1 AND completedAt BETWEEN :start AND :end")
    suspend fun countCompletedBetween(start: Long, end: Long): Int
}
