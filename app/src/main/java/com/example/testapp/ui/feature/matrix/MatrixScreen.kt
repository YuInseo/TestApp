package com.example.testapp.ui.feature.matrix

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testapp.domain.model.Task
import com.example.testapp.ui.component.TaskCheckbox
import com.example.testapp.util.DateUtils
import com.example.testapp.ui.theme.AppColors
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

private data class DragState(
    val task: Task,
    val pointer: Offset
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixScreen(
    onTaskClick: (Long) -> Unit,
    onAddTask: () -> Unit,
    vm: MatrixViewModel = koinViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showQuickAdd by remember { mutableStateOf(false) }
    var drag by remember { mutableStateOf<DragState?>(null) }
    val quadrantBounds = remember { androidx.compose.runtime.mutableStateMapOf<Quadrant, Rect>() }
    var detailTask by remember { mutableStateOf<Task?>(null) }

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
        val highlight = drag?.pointer?.let { findQuadrantAt(it, quadrantBounds) }
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
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
                        onTaskClick = { detailTask = it },
                        onToggle = vm::toggle,
                        onDragStartTask = { task, offset ->
                            drag = DragState(task, offset)
                        },
                        onDragMove = { offset -> drag = drag?.copy(pointer = offset) },
                        onDragEnd = {
                            val q = drag?.let { findQuadrantAt(it.pointer, quadrantBounds) }
                            val d = drag
                            if (q != null && d != null && q != currentQuadrant(d.task)) {
                                vm.moveToQuadrant(d.task, q)
                            }
                            drag = null
                        },
                        highlighted = highlight == Quadrant.Q1,
                        onBounds = { quadrantBounds[Quadrant.Q1] = it },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    QuadrantCard(
                        quadrant = Quadrant.Q2,
                        label = "Ⅱ",
                        tasks = state.q2,
                        onTaskClick = { detailTask = it },
                        onToggle = vm::toggle,
                        onDragStartTask = { task, offset -> drag = DragState(task, offset) },
                        onDragMove = { offset -> drag = drag?.copy(pointer = offset) },
                        onDragEnd = {
                            val q = drag?.let { findQuadrantAt(it.pointer, quadrantBounds) }
                            val d = drag
                            if (q != null && d != null && q != currentQuadrant(d.task)) {
                                vm.moveToQuadrant(d.task, q)
                            }
                            drag = null
                        },
                        highlighted = highlight == Quadrant.Q2,
                        onBounds = { quadrantBounds[Quadrant.Q2] = it },
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
                        onTaskClick = { detailTask = it },
                        onToggle = vm::toggle,
                        onDragStartTask = { task, offset -> drag = DragState(task, offset) },
                        onDragMove = { offset -> drag = drag?.copy(pointer = offset) },
                        onDragEnd = {
                            val q = drag?.let { findQuadrantAt(it.pointer, quadrantBounds) }
                            val d = drag
                            if (q != null && d != null && q != currentQuadrant(d.task)) {
                                vm.moveToQuadrant(d.task, q)
                            }
                            drag = null
                        },
                        highlighted = highlight == Quadrant.Q3,
                        onBounds = { quadrantBounds[Quadrant.Q3] = it },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    QuadrantCard(
                        quadrant = Quadrant.Q4,
                        label = "Ⅳ",
                        tasks = state.q4,
                        onTaskClick = { detailTask = it },
                        onToggle = vm::toggle,
                        onDragStartTask = { task, offset -> drag = DragState(task, offset) },
                        onDragMove = { offset -> drag = drag?.copy(pointer = offset) },
                        onDragEnd = {
                            val q = drag?.let { findQuadrantAt(it.pointer, quadrantBounds) }
                            val d = drag
                            if (q != null && d != null && q != currentQuadrant(d.task)) {
                                vm.moveToQuadrant(d.task, q)
                            }
                            drag = null
                        },
                        highlighted = highlight == Quadrant.Q4,
                        onBounds = { quadrantBounds[Quadrant.Q4] = it },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }

            drag?.let { d ->
                val density = LocalDensity.current
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (d.pointer.x - with(density) { 100.dp.toPx() }).roundToInt(),
                                (d.pointer.y - with(density) { 24.dp.toPx() }).roundToInt()
                            )
                        }
                        .zIndex(10f)
                        .alpha(0.9f)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .width(200.dp)
                ) {
                    Text(
                        d.task.title,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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

    detailTask?.let { task ->
        val live = listOf(state.q1, state.q2, state.q3, state.q4)
            .flatten().firstOrNull { it.id == task.id } ?: task
        TaskDetailSheet(
            task = live,
            allLists = state.lists,
            onDismiss = { detailTask = null },
            onUpdate = { title, notes, listId, priority ->
                vm.update(live, title, notes, listId, priority)
            }
        )
    }
}

private fun currentQuadrant(task: Task): Quadrant {
    val today0 = DateUtils.startOfDay()
    val tomorrow0 = DateUtils.addDays(today0, 1)
    val urgent = task.dueAt != null && DateUtils.startOfDay(task.dueAt) < tomorrow0
    val important = task.priority.level >= com.example.testapp.domain.model.Priority.MEDIUM.level
    return when {
        urgent && important -> Quadrant.Q1
        !urgent && important -> Quadrant.Q2
        urgent && !important -> Quadrant.Q3
        else -> Quadrant.Q4
    }
}

private fun findQuadrantAt(point: Offset, bounds: Map<Quadrant, Rect>): Quadrant? {
    return bounds.entries.firstOrNull { it.value.contains(point) }?.key
}


@Composable
private fun QuadrantCard(
    quadrant: Quadrant,
    label: String,
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onToggle: (Task) -> Unit,
    onDragStartTask: (Task, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    highlighted: Boolean,
    onBounds: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = Color(quadrant.colorArgb)
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
            .then(
                if (highlighted) Modifier.border(
                    2.dp,
                    color,
                    RoundedCornerShape(16.dp)
                ) else Modifier
            )
            .onGloballyPositioned { onBounds(it.boundsInWindow()) }
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
                        onClick = { onTaskClick(task) },
                        onToggle = { onToggle(task) },
                        onDragStartTask = onDragStartTask,
                        onDragMove = onDragMove,
                        onDragEnd = onDragEnd
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
    onToggle: () -> Unit,
    onDragStartTask: (Task, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    var rowOrigin by remember { mutableStateOf(Offset.Zero) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { rowOrigin = it.boundsInWindow().topLeft }
            .pointerInput(task.id) {
                detectTapGestures(onTap = { onClick() })
            }
            .pointerInput(task.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        onDragStartTask(task, rowOrigin + offset)
                    },
                    onDrag = { change, _ ->
                        onDragMove(rowOrigin + change.position)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            },
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

