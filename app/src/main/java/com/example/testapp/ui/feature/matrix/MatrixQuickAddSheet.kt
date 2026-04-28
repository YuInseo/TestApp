package com.example.testapp.ui.feature.matrix

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.model.Priority
import com.example.testapp.domain.model.Tag
import com.example.testapp.domain.model.TaskList
import com.example.testapp.util.DateUtils
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar

data class MatrixQuickAddPayload(
    val title: String,
    val notes: String,
    val quadrant: Quadrant,
    val priorityOverride: Priority?,
    val dueAtOverride: Long?,
    val listIdOverride: Long?,
    val extraTagIds: Set<Long>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixQuickAddSheet(
    allTags: List<Tag>,
    allLists: List<TaskList>,
    onDismiss: () -> Unit,
    onExpand: () -> Unit,
    onSubmit: (MatrixQuickAddPayload) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var quadrant by remember { mutableStateOf(Quadrant.Q1) }
    var manualPriority by remember { mutableStateOf<Priority?>(null) }
    var dueAt by remember { mutableStateOf<Long?>(null) }
    var listId by remember { mutableStateOf<Long?>(null) }
    var extraTagIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    val titleFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { titleFocus.requestFocus() }

    val canSend by remember {
        derivedStateOf { TAG_REGEX.replace(title, "").trim().isNotEmpty() }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf<Long?>(null) }
    var showPriorityDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showListDialog by remember { mutableStateOf(false) }

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
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocus),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = tagHighlightTransformation(
                    chipColor = MaterialTheme.colorScheme.primary,
                    chipBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                ),
                decorationBox = { inner ->
                    if (title.isEmpty()) {
                        Text(
                            "할 일",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    inner()
                }
            )

            Spacer(Modifier.height(8.dp))

            BasicTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (notes.isEmpty()) {
                        Text(
                            "설명",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    inner()
                }
            )

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                QuadrantSelector(
                    selected = quadrant,
                    onChange = { quadrant = it }
                )
                OverflowMenu(
                    onDate = { showDatePicker = true },
                    onPriority = { showPriorityDialog = true },
                    onTag = { showTagDialog = true },
                    onList = { showListDialog = true },
                    onExpand = {
                        onDismiss()
                        onExpand()
                    }
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = {
                        if (canSend) {
                            onSubmit(
                                MatrixQuickAddPayload(
                                    title = title,
                                    notes = notes,
                                    quadrant = quadrant,
                                    priorityOverride = manualPriority,
                                    dueAtOverride = dueAt,
                                    listIdOverride = listId,
                                    extraTagIds = extraTagIds
                                )
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (canSend) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "전송",
                        tint = if (canSend) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = dueAt ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pendingDate = state.selectedDateMillis ?: System.currentTimeMillis()
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("다음") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            }
        ) { DatePicker(state = state) }
    }

    if (showTimePicker) {
        val initial = dueAt ?: System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = initial }
        val timeState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = pendingDate ?: System.currentTimeMillis()
                    val ld = Instant.ofEpochMilli(date).atZone(ZoneId.of("UTC")).toLocalDate()
                    dueAt = DateUtils.localDateToMillis(
                        LocalDate.of(ld.year, ld.monthValue, ld.dayOfMonth),
                        LocalTime.of(timeState.hour, timeState.minute)
                    )
                    showTimePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("취소") }
            },
            text = { TimePicker(state = timeState) }
        )
    }

    if (showPriorityDialog) {
        PickerDialog(
            title = "우선 순위",
            items = Priority.entries,
            selected = { it == (manualPriority ?: Priority.NONE) },
            label = { it.label },
            leading = { p ->
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(p.composeColor(), CircleShape)
                )
            },
            onPick = { manualPriority = it; showPriorityDialog = false },
            onDismiss = { showPriorityDialog = false }
        )
    }

    if (showTagDialog) {
        PickerDialog(
            title = "태그",
            items = allTags,
            selected = { extraTagIds.contains(it.id) },
            label = { "#${it.name}" },
            leading = null,
            multi = true,
            onPick = {
                extraTagIds = if (extraTagIds.contains(it.id)) extraTagIds - it.id
                else extraTagIds + it.id
            },
            onDismiss = { showTagDialog = false }
        )
    }

    if (showListDialog) {
        PickerDialog(
            title = "목록",
            items = allLists,
            selected = { it.id == listId },
            label = { it.name },
            leading = { l ->
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(Color(l.colorArgb), CircleShape)
                )
            },
            onPick = { listId = it.id; showListDialog = false },
            onDismiss = { showListDialog = false }
        )
    }
}

@Composable
private fun QuadrantSelector(selected: Quadrant, onChange: (Quadrant) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val color = Color(selected.colorArgb)
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(end = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    selected.label,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                selected.subtitle,
                color = color,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Quadrant.entries.forEach { q ->
                DropdownMenuItem(
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color(q.colorArgb), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                q.label,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    text = { Text(q.subtitle) },
                    onClick = { onChange(q); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun OverflowMenu(
    onDate: () -> Unit,
    onPriority: () -> Unit,
    onTag: () -> Unit,
    onList: () -> Unit,
    onExpand: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Text(
                "···",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                text = { Text("날짜") },
                onClick = { expanded = false; onDate() }
            )
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null) },
                text = { Text("우선 순위") },
                onClick = { expanded = false; onPriority() }
            )
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Filled.LocalOffer, contentDescription = null) },
                text = { Text("태그") },
                onClick = { expanded = false; onTag() }
            )
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Filled.Inbox, contentDescription = null) },
                text = { Text("목록") },
                onClick = { expanded = false; onList() }
            )
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Filled.OpenInFull, contentDescription = null) },
                text = { Text("전체 화면") },
                onClick = { expanded = false; onExpand() }
            )
        }
    }
}

@Composable
private fun <T> PickerDialog(
    title: String,
    items: List<T>,
    selected: (T) -> Boolean,
    label: (T) -> String,
    leading: (@Composable (T) -> Unit)?,
    multi: Boolean = false,
    onPick: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(if (multi) "완료" else "취소") }
        },
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
            ) {
                if (items.isEmpty()) {
                    Text(
                        "항목 없음",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(item) }
                            .padding(vertical = 8.dp)
                    ) {
                        Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                            leading?.invoke(item)
                        }
                        Text(
                            label(item),
                            modifier = Modifier.weight(1f)
                        )
                        if (selected(item)) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    )
}

private fun tagHighlightTransformation(
    chipColor: Color,
    chipBg: Color
): VisualTransformation = VisualTransformation { input ->
    val raw = input.text
    val annotated: AnnotatedString = buildAnnotatedString {
        var cursor = 0
        TAG_REGEX.findAll(raw).forEach { match ->
            if (match.range.first > cursor) {
                append(raw.substring(cursor, match.range.first))
            }
            withStyle(
                SpanStyle(
                    color = chipColor,
                    background = chipBg,
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append(match.value)
            }
            cursor = match.range.last + 1
        }
        if (cursor < raw.length) append(raw.substring(cursor))
    }
    TransformedText(annotated, OffsetMapping.Identity)
}

internal val Quadrant.label: String
    get() = when (this) {
        Quadrant.Q1 -> "I"
        Quadrant.Q2 -> "II"
        Quadrant.Q3 -> "III"
        Quadrant.Q4 -> "IV"
    }

private val TAG_REGEX = Regex("#([\\p{L}\\p{N}_]+)")
