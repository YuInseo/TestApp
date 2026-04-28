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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testapp.domain.model.Task
import com.example.testapp.domain.model.TaskList
import com.example.testapp.ui.component.CompactTaskItem
import com.example.testapp.ui.component.EmptyState
import com.example.testapp.util.DateUtils
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onTaskClick: (Long) -> Unit,
    onAddTask: () -> Unit,
    vm: TasksViewModel = koinViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var listMenuOpen by remember { mutableStateOf(false) }

    val currentList: TaskList? = state.selectedListId?.let { id ->
        state.lists.firstOrNull { it.id == id }
    }
    val title = currentList?.name ?: state.filter.label

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { listMenuOpen = true }) {
                            Icon(Icons.Filled.ExpandMore, contentDescription = "목록 변경")
                        }
                        DropdownMenu(
                            expanded = listMenuOpen,
                            onDismissRequest = { listMenuOpen = false }
                        ) {
                            SmartFilter.entries.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text("${f.emoji} ${f.label}") },
                                    onClick = {
                                        vm.setFilter(f); listMenuOpen = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            state.lists.forEach { list ->
                                DropdownMenuItem(
                                    text = { Text(list.name) },
                                    onClick = {
                                        vm.selectList(list.id); listMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* sort/options */ }) {
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
                onClick = onAddTask,
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
                    title = "${state.filter.label}에 할 일이 없어요",
                    message = "+ 버튼을 눌러 새 할 일을 추가하세요."
                )
            }
        } else {
            val grouped = groupTasks(state.tasks)
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 8.dp + padding.calculateTopPadding(),
                    bottom = 96.dp + padding.calculateBottomPadding(),
                    start = 12.dp, end = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grouped.forEach { (sectionTitle, tasks) ->
                    if (sectionTitle.isNotEmpty()) {
                        item(key = "header_$sectionTitle") {
                            SectionHeader(sectionTitle, tasks.size)
                        }
                    }
                    item(key = "card_$sectionTitle") {
                        TaskCard(
                            tasks = tasks,
                            onTaskClick = onTaskClick,
                            onToggle = { vm.toggleCompleted(it) }
                        )
                    }
                }
            }
        }
    }
}

private fun groupTasks(tasks: List<Task>): List<Pair<String, List<Task>>> {
    val now = System.currentTimeMillis()
    val today0 = DateUtils.startOfDay(now)
    val tomorrow0 = DateUtils.addDays(today0, 1)
    val nextWeek0 = DateUtils.addDays(today0, 7)

    val overdue = mutableListOf<Task>()
    val today = mutableListOf<Task>()
    val tomorrow = mutableListOf<Task>()
    val thisWeek = mutableListOf<Task>()
    val later = mutableListOf<Task>()
    val noDate = mutableListOf<Task>()
    val completed = mutableListOf<Task>()

    tasks.forEach { t ->
        if (t.completed) {
            completed.add(t); return@forEach
        }
        val due = t.dueAt
        if (due == null) {
            noDate.add(t); return@forEach
        }
        val due0 = DateUtils.startOfDay(due)
        when {
            due0 < today0 -> overdue.add(t)
            due0 < tomorrow0 -> today.add(t)
            due0 < tomorrow0 + 86_400_000L -> tomorrow.add(t)
            due0 < nextWeek0 -> thisWeek.add(t)
            else -> later.add(t)
        }
    }

    val out = mutableListOf<Pair<String, List<Task>>>()
    if (overdue.isNotEmpty()) out += "지난 일" to overdue
    if (today.isNotEmpty()) out += "오늘" to today
    if (tomorrow.isNotEmpty()) out += "내일" to tomorrow
    if (thisWeek.isNotEmpty()) out += "이번 주" to thisWeek
    if (later.isNotEmpty()) out += "나중에" to later
    if (noDate.isNotEmpty()) out += "날짜 없음" to noDate
    if (completed.isNotEmpty()) out += "완료됨" to completed
    return out
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "$count",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TaskCard(
    tasks: List<Task>,
    onTaskClick: (Long) -> Unit,
    onToggle: (Task) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column {
            tasks.forEachIndexed { index, task ->
                CompactTaskItem(
                    task = task,
                    onToggle = { onToggle(task) },
                    onClick = { onTaskClick(task.id) }
                )
                if (index < tasks.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 50.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
