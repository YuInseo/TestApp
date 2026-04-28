package com.example.testapp.ui.feature.matrix

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testapp.domain.model.Task
import com.example.testapp.ui.component.TaskCheckbox
import com.example.testapp.util.DateUtils
import com.example.testapp.ui.theme.AppColors
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixScreen(
    onTaskClick: (Long) -> Unit,
    onAddTask: () -> Unit,
    vm: MatrixViewModel = koinViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showQuickAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "아이젠하워 매트릭스",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
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
                onClick = { showQuickAdd = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "추가")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuadrantCard(
                    quadrant = Quadrant.Q1,
                    label = "Ⅰ",
                    tasks = state.q1,
                    onTaskClick = onTaskClick,
                    onToggle = vm::toggle,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                QuadrantCard(
                    quadrant = Quadrant.Q2,
                    label = "Ⅱ",
                    tasks = state.q2,
                    onTaskClick = onTaskClick,
                    onToggle = vm::toggle,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuadrantCard(
                    quadrant = Quadrant.Q3,
                    label = "Ⅲ",
                    tasks = state.q3,
                    onTaskClick = onTaskClick,
                    onToggle = vm::toggle,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                QuadrantCard(
                    quadrant = Quadrant.Q4,
                    label = "Ⅳ",
                    tasks = state.q4,
                    onTaskClick = onTaskClick,
                    onToggle = vm::toggle,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }

    if (showQuickAdd) {
        MatrixQuickAddSheet(
            allTags = state.allTags,
            allLists = state.lists,
            onDismiss = { showQuickAdd = false },
            onExpand = onAddTask,
            onSubmit = { p ->
                vm.quickAdd(
                    rawTitle = p.title,
                    notes = p.notes,
                    quadrant = p.quadrant,
                    priorityOverride = p.priorityOverride,
                    dueAtOverride = p.dueAtOverride,
                    listIdOverride = p.listIdOverride,
                    extraTagIds = p.extraTagIds
                )
            }
        )
    }
}

@Composable
private fun QuadrantCard(
    quadrant: Quadrant,
    label: String,
    tasks: List<Task>,
    onTaskClick: (Long) -> Unit,
    onToggle: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = Color(quadrant.colorArgb)
    Box(
        modifier = modifier.background(
            MaterialTheme.colorScheme.surface,
            RoundedCornerShape(16.dp)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    quadrant.title,
                    color = color,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    QuadrantTaskRow(
                        task = task,
                        accent = color,
                        onClick = { onTaskClick(task.id) },
                        onToggle = { onToggle(task) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuadrantTaskRow(
    task: Task,
    accent: Color,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaskCheckbox(
            checked = task.completed,
            color = accent,
            onClick = onToggle,
            size = 18.dp
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                task.title,
                style = MaterialTheme.typography.bodySmall,
                color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            task.dueAt?.let {
                Text(
                    DateUtils.shortRelativeWithTime(it),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (it < System.currentTimeMillis() && !task.completed)
                        AppColors.Overdue else AppColors.Upcoming
                )
            }
        }
    }
}
