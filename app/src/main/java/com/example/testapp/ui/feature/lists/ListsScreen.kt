package com.example.testapp.ui.feature.lists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testapp.domain.model.Tag
import com.example.testapp.domain.model.TaskList
import org.koin.androidx.compose.koinViewModel

private val PRESET_COLORS: List<Long> = listOf(
    0xFF42A5F5L, 0xFFEC407AL, 0xFF66BB6AL, 0xFFFFB300L,
    0xFF7E57C2L, 0xFFE53935L, 0xFF26A69AL, 0xFFFF7043L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    onBack: () -> Unit,
    vm: ListsViewModel = koinViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<TaskList?>(null) }
    var addingTag by remember { mutableStateOf(false) }
    var addMenuOpen by remember { mutableStateOf(false) }
    var calendarsExpanded by remember { mutableStateOf(true) }
    var tagsExpanded by remember { mutableStateOf(false) }
    var selectedListId by remember { mutableStateOf(0L) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SidebarHeader(onBack = onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                PinnedShortcuts()
                Spacer(Modifier.height(4.dp))

                NavRow(
                    icon = Icons.Filled.Today,
                    iconTint = MaterialTheme.colorScheme.primary,
                    label = "오늘",
                    count = state.todayCount,
                    selected = false,
                    onClick = { /* today filter */ }
                )
                NavRow(
                    icon = Icons.Filled.Inbox,
                    iconTint = MaterialTheme.colorScheme.primary,
                    label = "기본함",
                    count = state.countByList[state.inboxId] ?: 0,
                    selected = selectedListId == state.inboxId,
                    onClick = { selectedListId = state.inboxId }
                )
                ExpandableRow(
                    icon = Icons.Filled.RssFeed,
                    label = "구독한 캘린더",
                    expanded = calendarsExpanded,
                    onToggle = { calendarsExpanded = !calendarsExpanded }
                )
                if (calendarsExpanded) {
                    SubRow(
                        icon = Icons.AutoMirrored.Filled.EventNote,
                        label = "inseo0121@gmail.com",
                        count = 9
                    )
                }
                ExpandableRow(
                    icon = Icons.Filled.LocalOffer,
                    label = "태그",
                    expanded = tagsExpanded,
                    onToggle = { tagsExpanded = !tagsExpanded }
                )
                if (tagsExpanded) {
                    state.tags.forEach { tag ->
                        SubRow(
                            icon = Icons.Filled.LocalOffer,
                            iconTint = Color(tag.colorArgb),
                            label = "#${tag.name}",
                            count = state.countByTag[tag.id] ?: 0
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                state.lists.filter { !it.isInbox }.forEach { list ->
                    NavRow(
                        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                        iconTint = Color(list.colorArgb),
                        label = list.name,
                        count = state.countByList[list.id] ?: 0,
                        selected = selectedListId == list.id,
                        trailing = Icons.Filled.ChevronRight,
                        onClick = { selectedListId = list.id },
                        onLongClick = { editing = list }
                    )
                }
                Spacer(Modifier.height(80.dp))
            }
            BottomBar(
                onAddClick = { addMenuOpen = true },
                onSettingsClick = { /* sidebar settings */ },
                addMenuOpen = addMenuOpen,
                onAddMenuDismiss = { addMenuOpen = false },
                onAddList = {
                    addMenuOpen = false
                    editing = TaskList(name = "", colorArgb = PRESET_COLORS.first())
                },
                onAddFilter = {
                    addMenuOpen = false
                    /* filter creation placeholder */
                },
                onAddTag = {
                    addMenuOpen = false
                    addingTag = true
                }
            )
        }
    }

    editing?.let { current ->
        ListEditDialog(
            list = current,
            onDismiss = { editing = null },
            onSave = { vm.upsert(it); editing = null },
            onDelete = if (!current.isInbox && current.id != 0L) {
                { vm.delete(current); editing = null }
            } else null
        )
    }

    if (addingTag) {
        TagAddDialog(
            onDismiss = { addingTag = false },
            onSave = { vm.upsertTag(it); addingTag = false }
        )
    }
}

@Composable
private fun SidebarHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush(),
                        CircleShape
                    )
                    .clickable(onClick = onBack)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .background(Color(0xFFFFC107), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "♛",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
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
            Icon(Icons.Filled.NotificationsNone, contentDescription = "알림")
        }
        IconButton(onClick = { /* settings */ }) {
            Icon(Icons.Filled.Settings, contentDescription = "설정")
        }
    }
}

@Composable
private fun Brush() = androidx.compose.ui.graphics.Brush.linearGradient(
    listOf(Color(0xFF7C5C36), Color(0xFFE2A763))
)

@Composable
private fun PinnedShortcuts() {
    val shortcuts = listOf("🏃" to "Side")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(shortcuts) { (emoji, label) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        IconButton(onClick = { /* configure shortcuts */ }) {
            Icon(
                Icons.Filled.Tune,
                contentDescription = "구성",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NavRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    count: Int,
    selected: Boolean,
    trailing: ImageVector? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .background(
                if (selected) MaterialTheme.colorScheme.surfaceContainerHigh
                else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconTint)
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (count > 0) {
            Text(
                "$count",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            Icon(
                trailing,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExpandableRow(
    icon: ImageVector,
    label: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SubRow(
    icon: ImageVector,
    label: String,
    count: Int,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 52.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        if (count > 0) {
            Text(
                "$count",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(6.dp))
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BottomBar(
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
    addMenuOpen: Boolean,
    onAddMenuDismiss: () -> Unit,
    onAddList: () -> Unit,
    onAddFilter: () -> Unit,
    onAddTag: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(onClick = onAddClick)
                    .padding(end = 12.dp, top = 6.dp, bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "추가",
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("추가", style = MaterialTheme.typography.bodyLarge)
            }
            DropdownMenu(expanded = addMenuOpen, onDismissRequest = onAddMenuDismiss) {
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null) },
                    text = { Text("목록") },
                    onClick = onAddList
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Filled.FilterAlt, contentDescription = null) },
                    text = { Text("필터") },
                    onClick = onAddFilter
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Filled.LocalOffer, contentDescription = null) },
                    text = { Text("태그") },
                    onClick = onAddTag
                )
            }
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onSettingsClick) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = "구성",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ListEditDialog(
    list: TaskList,
    onDismiss: () -> Unit,
    onSave: (TaskList) -> Unit,
    onDelete: (() -> Unit)?
) {
    var name by remember { mutableStateOf(list.name) }
    var color by remember { mutableStateOf(list.colorArgb) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (list.id == 0L) "새 목록" else "목록 편집") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("이름") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text("색상", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PRESET_COLORS.forEach { c ->
                        val selected = c == color
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(c), CircleShape)
                                .border(
                                    width = if (selected) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .clickable { color = c }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(list.copy(name = name.trim(), colorArgb = color))
                }
            }) { Text("저장") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("삭제") }
                }
                TextButton(onClick = onDismiss) { Text("취소") }
            }
        }
    )
}

@Composable
private fun TagAddDialog(
    onDismiss: () -> Unit,
    onSave: (Tag) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 태그") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("이름") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onSave(Tag(name = name.trim()))
            }) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}
