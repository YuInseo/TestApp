package com.example.testapp.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pomodoro_sessions",
    indices = [Index("startedAt"), Index("taskId")]
)
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long? = null,
    val startedAt: Long,
    val endedAt: Long,
    val durationMillis: Long,
    val type: String = "FOCUS"
)
