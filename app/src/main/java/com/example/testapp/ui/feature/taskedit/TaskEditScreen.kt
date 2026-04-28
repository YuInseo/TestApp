package com.example.testapp.ui.feature.taskedit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testapp.domain.model.Priority
import com.example.testapp.domain.model.Recurrence
import com.example.testapp.util.DateUtils
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    taskId: Long,
    onBack: () -> Unit
) {
    val vm: TaskEditViewModel = koinViewModel(parameters = { parametersOf(taskId) })
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pickReminder by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf<Long?>(null) }
    var listMenuOpen by remember { mutableStateOf(false) }
    var recurrenceMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == 0L) "새 할 일" else "할 일 편집") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.togglePin() }) {
                        Icon(
                            Icons.Filled.PushPin,
                            tint = if (state.pinned) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = "고정"
                        )
                    }
                    if (taskId != 0L) {
                        IconButton(onClick = { vm.delete() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "삭제")
                        }
                    }
                    TextButton(onClick = { vm.save() }) { Text("저장") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = vm::setTitle,
                label = { Text("제목") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.notes,
                onValueChange = vm::setNotes,
                label = { Text("메모") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Text("우선순위", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Priority.entries.forEach { p ->
                    FilterChip(
                        selected = state.priority == p,
                        onClick = { vm.setPriority(p) },
                        label = { Text(p.label) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(p.composeColor(), CircleShape)
                            )
                        }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            Text("마감", style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(
                    onClick = { pickReminder = false; showDatePicker = true },
                    label = {
                        Text(state.dueAt?.let { DateUtils.formatDateTime(it) } ?: "마감일 설정")
                    }
                )
                if (state.dueAt != null) {
                    IconButton(onClick = { vm.setDueAt(null) }) {
                        Icon(Icons.Filled.Close, contentDescription = "지우기")
                    }
                }
            }

            Text("리마인더", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(
                    onClick = { pickReminder = true; showDatePicker = true },
                    label = {
                        Text(state.reminderAt?.let { DateUtils.formatDateTime(it) } ?: "리마인더 설정")
                    }
                )
                if (state.reminderAt != null) {
                    IconButton(onClick = { vm.setReminderAt(null) }) {
                        Icon(Icons.Filled.Close, contentDescription = "지우기")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("반복", style = MaterialTheme.typography.labelLarge)
            Box {
                AssistChip(
                    onClick = { recurrenceMenuOpen = true },
                    label = { Text(state.recurrence.label) }
                )
                DropdownMenu(expanded = recurrenceMenuOpen, onDismissRequest = { recurrenceMenuOpen = false }) {
                    Recurrence.entries.forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r.label) },
                            onClick = {
                                vm.setRecurrence(r); recurrenceMenuOpen = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("목록", style = MaterialTheme.typography.labelLarge)
            Box {
                val current = state.lists.firstOrNull { it.id == state.listId }
                AssistChip(
                    onClick = { listMenuOpen = true },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(current?.colorArgb ?: 0xFF42A5F5), CircleShape)
                        )
                    },
                    label = { Text(current?.name ?: "목록 선택") }
                )
                DropdownMenu(expanded = listMenuOpen, onDismissRequest = { listMenuOpen = false }) {
                    state.lists.forEach { list ->
                        DropdownMenuItem(
                            text = { Text(list.name) },
                            onClick = { vm.setListId(list.id); listMenuOpen = false }
                        )
                    }
                }
            }

            if (state.allTags.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("태그", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    state.allTags.forEach { tag ->
                        FilterChip(
                            selected = state.tagIds.contains(tag.id),
                            onClick = { vm.toggleTag(tag.id) },
                            label = { Text("#${tag.name}") }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Text("서브태스크", style = MaterialTheme.typography.titleMedium)
            state.subtasks.forEachIndexed { index, sub ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = sub.completed, onCheckedChange = { vm.toggleSubtaskAt(index) })
                    Text(
                        sub.title,
                        modifier = Modifier.weight(1f),
                        textDecoration = if (sub.completed) TextDecoration.LineThrough else null
                    )
                    IconButton(onClick = { vm.removeSubtaskAt(index) }) {
                        Icon(Icons.Filled.Close, contentDescription = "삭제")
                    }
                }
            }
            var newSub by remember { mutableStateOf("") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newSub,
                    onValueChange = { newSub = it },
                    placeholder = { Text("서브태스크 추가") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = {
                    vm.addSubtask(newSub); newSub = ""
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "추가")
                }
            }
            Spacer(Modifier.height(48.dp))
        }
    }

    if (showDatePicker) {
        val initial = (if (pickReminder) state.reminderAt else state.dueAt)
            ?: System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val ms = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    pendingDate = ms
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("다음") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val initial = (if (pickReminder) state.reminderAt else state.dueAt)
            ?: System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = initial }
        val timeState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = pendingDate ?: System.currentTimeMillis()
                    val localDate = Instant.ofEpochMilli(date).atZone(ZoneId.of("UTC")).toLocalDate()
                    val merged = DateUtils.localDateToMillis(
                        LocalDate.of(localDate.year, localDate.monthValue, localDate.dayOfMonth),
                        LocalTime.of(timeState.hour, timeState.minute)
                    )
                    if (pickReminder) vm.setReminderAt(merged) else vm.setDueAt(merged)
                    showTimePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("취소") }
            },
            text = { TimePicker(state = timeState) }
        )
    }
}
