package com.example.testapp.domain.model

data class PomodoroSession(
    val id: Long = 0,
    val taskId: Long? = null,
    val startedAt: Long,
    val endedAt: Long,
    val durationMillis: Long,
    val type: SessionType = SessionType.FOCUS
)

enum class SessionType { FOCUS, SHORT_BREAK, LONG_BREAK }
