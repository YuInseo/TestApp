package com.example.testapp.domain.model

import androidx.compose.ui.graphics.Color

enum class Priority(val level: Int, val label: String, val color: Long) {
    NONE(0, "없음", 0xFF9E9E9E),
    LOW(1, "낮음", 0xFF42A5F5),
    MEDIUM(2, "보통", 0xFFFFB300),
    HIGH(3, "높음", 0xFFE53935);

    fun composeColor() = Color(color)

    companion object {
        fun fromLevel(level: Int) = entries.firstOrNull { it.level == level } ?: NONE
    }
}
