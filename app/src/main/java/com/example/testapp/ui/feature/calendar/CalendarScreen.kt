package com.example.testapp.ui.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ViewWeek
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testapp.domain.model.Task
import com.example.testapp.ui.component.CompactTaskItem
import com.example.testapp.ui.component.TaskDetailSheet
import com.example.testapp.ui.theme.AppColors
import com.example.testapp.util.DateUtils
import org.koin.androidx.compose.koinViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    vm: CalendarViewModel = koinViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var detailTask by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${state.month.monthValue}월",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = vm::toggleMode) {
                        Icon(Icons.Filled.ViewWeek, contentDescription = "주/월 보기")
                    }
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
                    val seedListId = state.lists.firstOrNull { it.isInbox }?.id
                        ?: state.lists.firstOrNull()?.id
                        ?: 0L
                    val seedDue = DateUtils.localDateToMillis(
                        state.selectedDate, java.time.LocalTime.of(23, 59)
                    )
                    detailTask = Task(title = "", listId = seedListId, dueAt = seedDue)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "추가")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            WeekHeader()
            if (state.viewMode == CalendarViewMode.MONTH) {
                MonthGrid(
                    month = state.month,
                    selected = state.selectedDate,
                    today = LocalDate.now(),
                    busyDates = state.tasksOfMonth.keys,
                    onDateSelected = vm::selectDate
                )
            } else {
                WeekRow(
                    selected = state.selectedDate,
                    today = LocalDate.now(),
                    busyDates = state.tasksOfMonth.keys,
                    onDateSelected = vm::selectDate
                )
            }
            DaySection(
                date = state.selectedDate,
                tasks = state.tasksOfDay,
                onTaskClick = { task -> detailTask = task },
                onToggle = vm::toggleCompleted
            )
        }
    }

    detailTask?.let { task ->
        val live = state.tasksOfDay.firstOrNull { it.id == task.id && task.id != 0L } ?: task
        TaskDetailSheet(
            initial = live,
            allLists = state.lists,
            onDismiss = { detailTask = null },
            onSave = { updated, subs -> vm.save(updated, subs) }
        )
    }
}

@Composable
private fun WeekHeader() {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp)) {
        listOf("일", "월", "화", "수", "목", "금", "토").forEach { d ->
            Text(
                d,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selected: LocalDate,
    today: LocalDate,
    busyDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDay = month.atDay(1)
    val firstDayOfWeek = firstDay.dayOfWeek.value % 7 // Sunday=0
    val totalDays = month.lengthOfMonth()
    val totalCells = ((firstDayOfWeek + totalDays + 6) / 7) * 7

    Column(modifier = Modifier.fillMaxWidth()) {
        var idx = 0
        while (idx < totalCells) {
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cell = idx
                    val dayOfMonth = cell - firstDayOfWeek + 1
                    val date = when {
                        dayOfMonth < 1 -> month.minusMonths(1).atEndOfMonth().minusDays((-dayOfMonth).toLong())
                        dayOfMonth > totalDays -> month.plusMonths(1).atDay(dayOfMonth - totalDays)
                        else -> month.atDay(dayOfMonth)
                    }
                    DayCell(
                        date = date,
                        inMonth = dayOfMonth in 1..totalDays,
                        isToday = date == today,
                        isSelected = date == selected,
                        hasTasks = busyDates.contains(date),
                        onClick = { onDateSelected(date) },
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                    idx++
                }
            }
        }
    }
}

@Composable
private fun WeekRow(
    selected: LocalDate,
    today: LocalDate,
    busyDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    val weekStart = selected.with(WeekFields.of(Locale.KOREA).dayOfWeek(), 1L).minusDays(
        ((selected.dayOfWeek.value % 7).toLong())
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        repeat(7) { i ->
            val date = weekStart.plusDays(i.toLong())
            DayCell(
                date = date,
                inMonth = true,
                isToday = date == today,
                isSelected = date == selected,
                hasTasks = busyDates.contains(date),
                onClick = { onDateSelected(date) },
                modifier = Modifier.weight(1f).aspectRatio(1f)
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    hasTasks: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        when {
                            isSelected && !isToday -> accent
                            isToday -> Color.White
                            else -> Color.Transparent
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${date.dayOfMonth}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isToday -> accent
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        !inMonth -> muted.copy(alpha = 0.5f)
                        else -> onSurface
                    }
                )
            }
            if (hasTasks) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(AppColors.DotIndicator, CircleShape)
                )
            } else {
                Spacer(Modifier.size(4.dp))
            }
        }
    }
}

@Composable
private fun DaySection(
    date: LocalDate,
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onToggle: (Task) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
    ) {
        Column {
            Text(
                "${date.monthValue}월 ${date.dayOfMonth}일",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 4.dp)
            )
            if (tasks.isEmpty()) {
                Text(
                    "할 일이 없어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp, bottom = 16.dp)
                )
            } else {
                tasks.forEachIndexed { index, task ->
                    CompactTaskItem(
                        task = task,
                        onToggle = { onToggle(task) },
                        onClick = { onTaskClick(task) }
                    )
                    if (index < tasks.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 50.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
