package com.example.testapp.ui.navigation

object Routes {
    const val TASKS = "tasks"
    const val TASK_EDIT = "task_edit/{taskId}"
    const val LISTS = "lists"
    const val SETTINGS = "settings"

    fun taskEdit(id: Long) = "task_edit/$id"
}
