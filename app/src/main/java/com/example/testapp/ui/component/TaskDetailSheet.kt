package com.example.testapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.testapp.domain.model.Priority
import com.example.testapp.domain.model.Subtask
import com.example.testapp.domain.model.Task
import com.example.testapp.domain.model.TaskList
import com.example.testapp.util.DateUtils
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun TaskDetailSheet(
    initial: Task,
    allLists: List<TaskList>,
    onDismiss: () -> Unit,
    onSave: (Task, List<Subtask>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val seedListId = remember(initial.id, initial.listId, allLists) {
        if (initial.listId != 0L) initial.listId
        else allLists.firstOrNull { it.isInbox }?.id ?: allLists.firstOrNull()?.id ?: 0L
    }

    var title by remember(initial.id) { mutableStateOf(initial.title) }
    var notes by remember(initial.id) { mutableStateOf(initial.notes) }
    var listId by remember(initial.id) { mutableStateOf(seedListId) }
    var priority by remember(initial.id) { mutableStateOf(initial.priority) }
    var dueAt by remember(initial.id) { mutableStateOf(initial.dueAt) }
    val subtasks = remember(initial.id) {
        mutableStateListOf<Subtask>().apply { addAll(initial.subtasks) }
    }
    var moving by remember { mutableStateOf(false) }
    var pickingDate by remember { mutableStateOf(false) }
    var pickingTime by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf<Long?>(null) }
    var newSubtaskTitle by remember { mutableStateOf("") }

    LaunchedEffect(initial.id) {
        snapshotFlow {
            DraftSnapshot(title, notes, listId, priority, dueAt, subtasks.toList())
        }.debounce(400).collect { snap ->
            if (snap.title.isBlank()) return@collect
            val composed = initial.copy(
                title = snap.title.trim(),
                notes = snap.notes,
                listId = snap.listId,
                priority = snap.priority,
                dueAt = snap.dueAt
            )
            onSave(composed, snap.subtasks)
        }
    }

    val currentList = allLists.firstOrNull { it.id == listId }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .windowInsetsPadding(WindowInsets.ime)
        ) {
            // Header: list chip + flag + more
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { moving = true }
                        .padding(end = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                Color(currentList?.colorArgb ?: 0xFF42A5F5L),
                                CircleShape
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        currentList?.name ?: "기본함",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Filled.SwapVert,
                        contentDescription = "이동",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { priority = nextPriority(priority) }) {
                    Icon(
                        Icons.Filled.Flag,
                        contentDescription = "우선순위",
                        tint = priority.composeColor()
                    )
                }
                IconButton(onClick = { /* more */ }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "더보기")
                }
            }

            Spacer(Modifier.height(8.dp))

            // Date row — opens picker
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { pickingDate = true }
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = if (dueAt != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = dueAt?.let { DateUtils.shortRelativeWithTime(it) } ?: "마감일 추가",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (dueAt != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (dueAt != null) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "지우기",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { dueAt = null }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Title — large bold
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (title.isEmpty()) {
                        Text(
                            "할 일",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    inner()
                }
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(12.dp))

            // Notes
            BasicTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (notes.isEmpty()) {
                        Text(
                            "설명",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    inner()
                }
            )

            Spacer(Modifier.height(16.dp))

            // Subtasks
            subtasks.forEachIndexed { index, sub ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = sub.completed,
                        onCheckedChange = { checked ->
                            subtasks[index] = sub.copy(completed = checked)
                        }
                    )
                    Text(
                        sub.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (sub.completed) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (sub.completed) TextDecoration.LineThrough else null
                    )
                    IconButton(onClick = { subtasks.removeAt(index) }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "삭제",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Box(modifier = Modifier.size(48.dp))
                BasicTextField(
                    value = newSubtaskTitle,
                    onValueChange = { newSubtaskTitle = it },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { inner ->
                        if (newSubtaskTitle.isEmpty()) {
                            Text(
                                "서브태스크 추가",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        inner()
                    }
                )
                IconButton(onClick = {
                    val trimmed = newSubtaskTitle.trim()
                    if (trimmed.isNotEmpty()) {
                        subtasks.add(Subtask(taskId = initial.id, title = trimmed))
                        newSubtaskTitle = ""
                    }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "추가")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Bottom toolbar
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { /* tag picker */ }) {
                    Icon(Icons.Filled.LocalOffer, contentDescription = "태그")
                }
                IconButton(onClick = { /* subtasks */ }) {
                    Icon(Icons.Filled.Checklist, contentDescription = "서브태스크")
                }
                IconButton(onClick = { /* attach */ }) {
                    Icon(Icons.Filled.AttachFile, contentDescription = "첨부")
                }
            }
        }
    }

    if (moving) {
        MoveListDialog(
            allLists = allLists,
            currentListId = listId,
            onDismiss = { moving = false },
            onPick = { listId = it; moving = false }
        )
    }

    if (pickingDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = dueAt ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { pickingDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pendingDate = state.selectedDateMillis ?: System.currentTimeMillis()
                    pickingDate = false
                    pickingTime = true
                }) { Text("다음") }
            },
            dismissButton = {
                TextButton(onClick = { pickingDate = false }) { Text("취소") }
            }
        ) { DatePicker(state = state) }
    }

    if (pickingTime) {
        val initialMs = dueAt ?: System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = initialMs }
        val timeState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { pickingTime = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = pendingDate ?: System.currentTimeMillis()
                    val ld = Instant.ofEpochMilli(date).atZone(ZoneId.of("UTC")).toLocalDate()
                    dueAt = DateUtils.localDateToMillis(
                        LocalDate.of(ld.year, ld.monthValue, ld.dayOfMonth),
                        LocalTime.of(timeState.hour, timeState.minute)
                    )
                    pickingTime = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { pickingTime = false }) { Text("취소") }
            },
            text = { TimePicker(state = timeState) }
        )
    }
}

private data class DraftSnapshot(
    val title: String,
    val notes: String,
    val listId: Long,
    val priority: Priority,
    val dueAt: Long?,
    val subtasks: List<Subtask>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoveListDialog(
    allLists: List<TaskList>,
    currentListId: Long,
    onDismiss: () -> Unit,
    onPick: (Long) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, allLists) {
        if (query.isBlank()) allLists
        else allLists.filter { it.name.contains(query, ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("이동") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "닫기")
                        }
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("검색") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(4.dp))
                filtered.forEach { list ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(list.id) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(list.colorArgb), CircleShape)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            list.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            color = if (list.id == currentListId)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                        if (list.id == currentListId) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

private fun nextPriority(p: Priority): Priority = when (p) {
    Priority.NONE -> Priority.LOW
    Priority.LOW -> Priority.MEDIUM
    Priority.MEDIUM -> Priority.HIGH
    Priority.HIGH -> Priority.NONE
}
