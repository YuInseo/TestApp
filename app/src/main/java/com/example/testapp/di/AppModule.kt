package com.example.testapp.di

import com.example.testapp.data.db.AppDatabase
import com.example.testapp.data.backup.BackupManager
import com.example.testapp.data.backup.BackupViewModel
import com.example.testapp.data.preferences.SettingsRepository
import com.example.testapp.data.repository.HabitRepository
import com.example.testapp.data.repository.PomodoroRepository
import com.example.testapp.data.repository.TagRepository
import com.example.testapp.data.repository.TaskListRepository
import com.example.testapp.data.repository.TaskRepository
import com.example.testapp.notification.ReminderScheduler
import com.example.testapp.ui.feature.calendar.CalendarViewModel
import com.example.testapp.ui.feature.focus.FocusViewModel
import com.example.testapp.ui.feature.habits.HabitsViewModel
import com.example.testapp.ui.feature.appearance.AppearanceViewModel
import com.example.testapp.ui.feature.lists.ListsViewModel
import com.example.testapp.ui.feature.tabbarconfig.TabBarConfigViewModel
import com.example.testapp.ui.feature.matrix.MatrixViewModel
import com.example.testapp.ui.feature.settings.SettingsViewModel
import com.example.testapp.ui.feature.stats.StatsViewModel
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
    single { BackupManager(androidContext(), get(), get(), get(), get()) }

    viewModel { TasksViewModel(get(), get(), get()) }
    viewModel { ListsViewModel(get(), get(), get()) }
    viewModel { CalendarViewModel(get(), get()) }
    viewModel { MatrixViewModel(get(), get(), get()) }
    viewModel { FocusViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { AppearanceViewModel(get()) }
    viewModel { TabBarConfigViewModel(get()) }
    viewModel { UpdateViewModel(get(), get()) }
    viewModel { HabitsViewModel(get()) }
    viewModel { StatsViewModel(get(), get()) }
    viewModel { BackupViewModel(get()) }
}
