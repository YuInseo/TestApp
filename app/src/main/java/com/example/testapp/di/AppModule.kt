package com.example.testapp.di

import com.example.testapp.data.db.AppDatabase
import com.example.testapp.data.preferences.SettingsRepository
import com.example.testapp.data.repository.HabitRepository
import com.example.testapp.data.repository.PomodoroRepository
import com.example.testapp.data.repository.TagRepository
import com.example.testapp.data.repository.TaskListRepository
import com.example.testapp.data.repository.TaskRepository
import com.example.testapp.notification.ReminderScheduler
import com.example.testapp.ui.feature.lists.ListsViewModel
import com.example.testapp.ui.feature.settings.SettingsViewModel
import com.example.testapp.ui.feature.taskedit.TaskEditViewModel
import com.example.testapp.ui.feature.tasks.TasksViewModel
import com.example.testapp.ui.update.UpdateViewModel
import com.example.testapp.update.UpdateChecker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.build(androidContext()) }
    single { get<AppDatabase>().taskDao() }
    single { get<AppDatabase>().taskListDao() }
    single { get<AppDatabase>().tagDao() }
    single { get<AppDatabase>().pomodoroDao() }
    single { get<AppDatabase>().habitDao() }

    single { TaskRepository(get(), get()) }
    single { TaskListRepository(get()) }
    single { TagRepository(get()) }
    single { PomodoroRepository(get()) }
    single { HabitRepository(get()) }
    single { SettingsRepository(androidContext()) }
    single { ReminderScheduler(androidContext()) }
    single { UpdateChecker(androidContext()) }

    viewModel { TasksViewModel(get(), get(), get()) }
    viewModel { (taskId: Long) -> TaskEditViewModel(get(), get(), get(), get(), taskId) }
    viewModel { ListsViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { UpdateViewModel(get(), get()) }
}
