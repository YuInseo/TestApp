package com.example.testapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val TASKS = "tab/tasks"
    const val CALENDAR = "tab/calendar"
    const val MATRIX = "tab/matrix"
    const val FOCUS = "tab/focus"
    const val HABITS_TAB = "tab/habits"
    const val STATS_TAB = "tab/stats"
    const val SETTINGS_TAB = "tab/settings"
    const val MORE = "tab/more"

    const val LISTS = "lists"
    const val SETTINGS = "settings"
    const val APPEARANCE = "appearance"
    const val TAB_BAR_CONFIG = "tab_bar_config"
    const val HABITS = "habits"
    const val STATS = "stats"
}

enum class Tab(
    val id: String,
    val route: String,
    val label: String,
    val description: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector
) {
    TASKS(
        "tasks", Routes.TASKS, "과제",
        "리스트와 필터로 작업을 관리하세요.",
        Icons.Filled.CheckBox, Icons.Outlined.CheckBox
    ),
    CALENDAR(
        "calendar", Routes.CALENDAR, "달력",
        "5가지 캘린더 뷰로 작업을 관리하세요.",
        Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth
    ),
    MATRIX(
        "matrix", Routes.MATRIX, "아이젠하워 매트릭스",
        "중요하고 긴급한 일에 집중하세요.",
        Icons.Filled.GridView, Icons.Outlined.GridView
    ),
    FOCUS(
        "focus", Routes.FOCUS, "포모도로",
        "포모 타이머나 스톱워치를 사용하여 집중력을 유지하세요.",
        Icons.Filled.RadioButtonChecked, Icons.Outlined.RadioButtonUnchecked
    ),
    HABITS(
        "habits", Routes.HABITS_TAB, "습관",
        "습관을 기르고 그것을 추적하십시오.",
        Icons.Filled.EventRepeat, Icons.Outlined.EventRepeat
    ),
    STATS(
        "stats", Routes.STATS_TAB, "통계",
        "활동을 추적하고 분석하세요.",
        Icons.Filled.BarChart, Icons.Outlined.BarChart
    ),
    SETTINGS(
        "settings", Routes.SETTINGS_TAB, "설정",
        "현재 설정을 변경하고 확인하세요.",
        Icons.Filled.Settings, Icons.Outlined.Settings
    ),
    MORE(
        "more", Routes.MORE, "더보기",
        "더 많은 도구와 설정을 살펴보세요.",
        Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz
    );

    companion object {
        fun byId(id: String): Tab? = entries.firstOrNull { it.id == id }
        val configurable: List<Tab> = listOf(TASKS, CALENDAR, MATRIX, FOCUS, HABITS, STATS, SETTINGS)
    }
}
