package com.example.testapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testapp.data.db.entity.PomodoroSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroDao {
    @Query("SELECT * FROM pomodoro_sessions ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE startedAt BETWEEN :start AND :end ORDER BY startedAt")
    fun observeBetween(start: Long, end: Long): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT IFNULL(SUM(durationMillis), 0) FROM pomodoro_sessions WHERE type = 'FOCUS' AND startedAt BETWEEN :start AND :end")
    suspend fun focusMillisBetween(start: Long, end: Long): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PomodoroSessionEntity): Long
}
