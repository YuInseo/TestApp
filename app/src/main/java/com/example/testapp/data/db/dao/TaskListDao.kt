package com.example.testapp.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.testapp.data.db.entity.TaskListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskListDao {
    @Query("SELECT * FROM task_lists ORDER BY isInbox DESC, sortOrder, name")
    fun observeAll(): Flow<List<TaskListEntity>>

    @Query("SELECT * FROM task_lists WHERE id = :id")
    suspend fun get(id: Long): TaskListEntity?

    @Query("SELECT * FROM task_lists WHERE isInbox = 1 LIMIT 1")
    suspend fun getInbox(): TaskListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: TaskListEntity): Long

    @Update
    suspend fun update(list: TaskListEntity)

    @Delete
    suspend fun delete(list: TaskListEntity)
}
