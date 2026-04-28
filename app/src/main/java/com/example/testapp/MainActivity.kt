package com.example.testapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.testapp.data.preferences.ThemeMode
import com.example.testapp.ui.feature.lists.ListsScreen
import com.example.testapp.ui.feature.settings.SettingsScreen
import com.example.testapp.ui.feature.settings.SettingsViewModel
import com.example.testapp.ui.feature.taskedit.TaskEditScreen
import com.example.testapp.ui.feature.tasks.SmartFilter
import com.example.testapp.ui.feature.tasks.TasksScreen
import com.example.testapp.ui.feature.tasks.TasksViewModel
import com.example.testapp.ui.navigation.Routes
import com.example.testapp.ui.theme.AppTheme
import com.example.testapp.ui.update.UpdateDialog
import com.example.testapp.ui.update.UpdateViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val notifPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* user choice noted; we just continue */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        setContent { AppRoot() }
    }
}

@Composable
private fun AppRoot() {
    val settingsVm: SettingsViewModel = koinViewModel()
    val updateVm: UpdateViewModel = koinViewModel()
    val s by settingsVm.settings.collectAsStateWithLifecycle()

    val dark = when (s.theme) {
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    LaunchedEffect(Unit) { updateVm.checkOnLaunch() }

    AppTheme(darkTheme = dark, dynamicColor = s.dynamicColor) {
        AppNav(onCheckForUpdate = updateVm::checkManually)
        UpdateDialog(updateVm)
    }
}

@Composable
private fun AppNav(onCheckForUpdate: () -> Unit) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val tasksVm: TasksViewModel = koinViewModel()
    val state by tasksVm.state.collectAsStateWithLifecycle()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "TickTickClone",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()
                SmartFilter.entries.forEach { f ->
                    NavigationDrawerItem(
                        label = { Text("${f.emoji} ${f.label}") },
                        selected = state.filter == f && state.selectedListId == null,
                        onClick = {
                            tasksVm.setFilter(f)
                            scope.launch { drawerState.close() }
                        }
                    )
                }
                HorizontalDivider()
                Text(
                    "내 목록",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                )
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(state.lists, key = { it.id }) { list ->
                        NavigationDrawerItem(
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(list.colorArgb), CircleShape)
                                )
                            },
                            label = { Text(list.name) },
                            selected = state.selectedListId == list.id,
                            onClick = {
                                tasksVm.selectList(list.id)
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }
                HorizontalDivider()
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = null) },
                    label = { Text("목록 관리") },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.LISTS)
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text("설정") },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.SETTINGS)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        NavHost(navController = navController, startDestination = Routes.TASKS) {
            composable(Routes.TASKS) {
                TasksScreen(
                    onTaskClick = { id -> navController.navigate(Routes.taskEdit(id)) },
                    onAddTask = { navController.navigate(Routes.taskEdit(0)) },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    vm = tasksVm
                )
            }
            composable(
                route = Routes.TASK_EDIT,
                arguments = listOf(navArgument("taskId") { type = NavType.LongType })
            ) { backStack ->
                val id = backStack.arguments?.getLong("taskId") ?: 0L
                TaskEditScreen(taskId = id, onBack = { navController.popBackStack() })
            }
            composable(Routes.LISTS) {
                ListsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onCheckForUpdate = onCheckForUpdate
                )
            }
        }
    }
}
