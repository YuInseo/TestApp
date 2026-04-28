package com.example.testapp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.testapp.ui.feature.appearance.AppearanceScreen
import com.example.testapp.ui.feature.calendar.CalendarScreen
import com.example.testapp.ui.feature.focus.FocusScreen
import com.example.testapp.ui.feature.habits.HabitsScreen
import com.example.testapp.ui.feature.lists.ListsScreen
import com.example.testapp.ui.feature.matrix.MatrixScreen
import com.example.testapp.ui.feature.more.MoreScreen
import com.example.testapp.ui.feature.settings.SettingsScreen
import com.example.testapp.ui.feature.settings.SettingsViewModel
import com.example.testapp.ui.feature.stats.StatsScreen
import com.example.testapp.ui.feature.tabbarconfig.TabBarConfigScreen
import com.example.testapp.ui.feature.tasks.TasksScreen
import com.example.testapp.ui.navigation.Routes
import com.example.testapp.ui.navigation.Tab
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppShell(onCheckForUpdate: () -> Unit) {
    val navController = rememberNavController()
    val backEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backEntry?.destination?.route

    val settingsVm: SettingsViewModel = koinViewModel()
    val settings by settingsVm.settings.collectAsStateWithLifecycle()

    val visibleTabs: List<Tab> = remember(settings.enabledTabIds, settings.maxTabs) {
        val enabled = settings.enabledTabIds.mapNotNull { Tab.byId(it) }
        val cap = settings.maxTabs.coerceIn(2, 7)
        // Show up to (cap - 1) primary tabs, then always append MORE.
        val primary = enabled.take(cap - 1)
        primary + Tab.MORE
    }
    val showBottomBar = currentRoute?.startsWith("tab/") == true
    val overflowTabs: List<Tab> = remember(settings.enabledTabIds, settings.maxTabs) {
        val enabled = settings.enabledTabIds.mapNotNull { Tab.byId(it) }
        val cap = settings.maxTabs.coerceIn(2, 7)
        if (enabled.size <= cap - 1) emptyList() else enabled.drop(cap - 1)
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    tonalElevation = 0.dp
                ) {
                    visibleTabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) tab.iconSelected else tab.iconUnselected,
                                    contentDescription = tab.label
                                )
                            },
                            label = null,
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TASKS,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            composable(Routes.TASKS) { TasksScreen() }
            composable(Routes.CALENDAR) { CalendarScreen() }
            composable(Routes.MATRIX) { MatrixScreen() }
            composable(Routes.FOCUS) { FocusScreen() }
            composable(Routes.HABITS_TAB) {
                HabitsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.STATS_TAB) {
                StatsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS_TAB) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onCheckForUpdate = onCheckForUpdate
                )
            }
            composable(Routes.MORE) {
                MoreScreen(
                    overflowTabs = overflowTabs,
                    onTabClick = { tab -> navController.navigate(tab.route) },
                    onLists = { navController.navigate(Routes.LISTS) },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    onAppearance = { navController.navigate(Routes.APPEARANCE) },
                    onTabBarConfig = { navController.navigate(Routes.TAB_BAR_CONFIG) },
                    onHabits = { navController.navigate(Routes.HABITS) },
                    onStats = { navController.navigate(Routes.STATS) },
                    onCheckForUpdate = onCheckForUpdate
                )
            }
            composable(Routes.HABITS) {
                HabitsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.STATS) {
                StatsScreen(onBack = { navController.popBackStack() })
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
            composable(Routes.APPEARANCE) {
                AppearanceScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.TAB_BAR_CONFIG) {
                TabBarConfigScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
