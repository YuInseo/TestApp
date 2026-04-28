package com.example.testapp.domain.model

data class Habit(
    val id: Long = 0,
    val name: String,
    val emoji: String = "✨",
    val colorArgb: Long = 0xFF66BB6A,
    val targetPerWeek: Int = 7,
    val createdAt: Long = System.currentTimeMillis(),
    val archived: Boolean = false
)

data class HabitEntry(
    val id: Long = 0,
    val habitId: Long,
    val date: String,
    val completedAt: Long = System.currentTimeMillis()
)
