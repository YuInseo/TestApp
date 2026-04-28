package com.example.testapp.ui.feature.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testapp.domain.model.Task
import com.example.testapp.domain.model.TaskList
import com.example.testapp.ui.component.CompactTaskItem
import com.example.testapp.ui.component.EmptyState
import com.example.testapp.ui.component.TaskDetailSheet
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    vm: TasksViewModel = koinViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var detailTask by remember { mutableStateOf<Task?>(null) }

    val currentList: TaskList? = state.selectedListId?.let { id ->
        state.lists.firstOrNull { it.id == id }
    }
    val title = currentList?.name ?: state.filter.label

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ListsDrawer(
                state = state,
                onPickFilter = { f ->
                    vm.setFilter(f); scope.launch { drawerState.close() }
                },
                onPickList = { id ->
                    vm.selectList(id); scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "목록")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* options */ }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "옵션")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        val seedListId = state.selectedListId
                            ?: state.lists.firstOrNull { it.isInbox }?.id
                            ?: state.lists.firstOrNull()?.id
                            ?: 0L
                        detailTask = Task(title = "", listId = seedListId)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "새 할 일")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            if (state.tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    EmptyState(
                        emoji = state.filter.emoji,
                        title = "${title}에 할 일이 없어요",
                        message = "+ 버튼을 눌러 새 할 일을 추가하세요."
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 0.dp)
                    ) {
                        items(state.tasks, key = { it.id }) { task ->
                            CompactTaskItem(
                                task = task,
                                onToggle = { vm.toggleCompleted(task) },
                                onClick = { detailTask = task }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 50.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }

    detailTask?.let { task ->
        val live = state.tasks.firstOrNull { it.id == task.id && task.id != 0L } ?: task
        TaskDetailSheet(
            initial = live,
            allLists = state.lists,
            onDismiss = { detailTask = null },
            onSave = { updated, subs -> vm.save(updated, subs) }
        )
    }
}

@Composable
private fun ListsDrawer(
    state: TasksUiState,
    onPickFilter: (SmartFilter) -> Unit,
    onPickList: (Long) -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.background
    ) {
        // Profile area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) { Text("👤") }
            Spacer(Modifier.width(12.dp))
            Text(
                "사용자",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { /* search */ }) {
                Icon(Icons.Filled.Search, contentDescription = "검색")
            }
            IconButton(onClick = { /* notifications */ }) {
                Icon(Icons.Filled.Notifications, contentDescription = "알림")
            }
            IconButton(onClick = { /* drawer settings */ }) {
                Icon(Icons.Filled.Settings, contentDescription = "설정")
            }
        }
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        // Smart filters
        SmartFilter.entries.forEach { f ->
            DrawerRow(
                emoji = f.emoji,
                label = f.label,
                count = countForFilter(f, state),
                selected = state.filter == f && state.selectedListId == null,
                onClick = { onPickFilter(f) }
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        Text(
            "내 목록",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
        )

        state.lists.filterNot { it.isInbox }.forEach { list ->
            DrawerListRow(
                list = list,
                count = state.tasks.count { it.listId == list.id },
                selected = state.selectedListId == list.id,
                onClick = { onPickList(list.id) }
            )
        }
    }
}

@Composable
private fun DrawerRow(
    emoji: String,
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Text(emoji) },
        label = { Text(label) },
        badge = { if (count > 0) Text("$count") },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            unselectedContainerColor = Color.Transparent
        )
    )
}

@Composable
private fun DrawerListRow(
    list: TaskList,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(list.colorArgb), CircleShape)
            )
        },
        label = { Text(list.name) },
        badge = { if (count > 0) Text("$count") },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            unselectedContainerColor = Color.Transparent
        )
    )
}

private fun countForFilter(f: SmartFilter, state: TasksUiState): Int {
    return when (f) {
        SmartFilter.INBOX -> {
            val inboxId = state.lists.firstOrNull { it.isInbox }?.id
            if (inboxId != null) state.tasks.count { it.listId == inboxId && !it.completed } else 0
        }
        SmartFilter.TODAY,
        SmartFilter.TOMORROW,
        SmartFilter.NEXT_7,
        SmartFilter.ALL -> state.tasks.count { !it.completed }
        SmartFilter.COMPLETED -> state.tasks.count { it.completed }
    }
}
