package com.example.testapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val TASKS = "tab/tasks"
    const val CALENDAR = "tab/calendar"
    const val MATRIX = "tab/matrix"
    const val FOCUS = "tab/focus"
    const val MORE = "tab/more"

    const val TASK_EDIT = "task_edit/{taskId}"
    const val LISTS = "lists"
    const val SETTINGS = "settings"
    const val APPEARANCE = "appearance"
    const val HABITS = "habits"
    const val STATS = "stats"

    fun taskEdit(id: Long) = "task_edit/$id"
}

enum class Tab(
    val route: String,
    val label: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector
) {
    TASKS(Routes.TASKS, "할 일", Icons.Filled.CheckBox, Icons.Outlined.CheckBox),
    CALENDAR(Routes.CALENDAR, "캘린더", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    MATRIX(Routes.MATRIX, "매트릭스", Icons.Filled.GridView, Icons.Outlined.GridView),
    FOCUS(Routes.FOCUS, "포커스", Icons.Filled.RadioButtonChecked, Icons.Outlined.RadioButtonUnchecked),
    MORE(Routes.MORE, "더보기", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
}
