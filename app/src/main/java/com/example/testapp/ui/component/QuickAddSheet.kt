package com.example.testapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.model.Tag
import com.example.testapp.domain.model.TaskList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onSubmit: (title: String, listId: Long, tagIds: List<Long>) -> Unit,
    onOpenFullEditor: ((title: String) -> Unit)? = null,
    lists: List<TaskList>,
    tags: List<Tag>,
    initialListId: Long?,
    initialTagIds: Set<Long> = emptySet()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    var title by remember { mutableStateOf("") }
    var listId by remember { mutableStateOf(initialListId ?: lists.firstOrNull()?.id ?: 0L) }
    var pickedTagIds by remember { mutableStateOf(initialTagIds) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val selectedList = lists.firstOrNull { it.id == listId }
    val accent = selectedList?.let { Color(it.colorArgb) } ?: MaterialTheme.colorScheme.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Tag chips
            if (tags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 6.dp)
                ) {
                    items(tags, key = { it.id }) { tag ->
                        val selected = pickedTagIds.contains(tag.id)
                        TagChip(
                            tag = tag,
                            selected = selected,
                            onToggle = {
                                pickedTagIds =
                                    if (selected) pickedTagIds - tag.id else pickedTagIds + tag.id
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        "할 일을 입력하세요",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = false,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(accent.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        selectedList?.name?.firstOrNull()?.toString() ?: "·",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(Modifier.width(8.dp))
                ListPickerButton(
                    label = selectedList?.name ?: "받은편지함",
                    accent = accent,
                    lists = lists,
                    onPick = { listId = it }
                )
                IconButton(onClick = {
                    onOpenFullEditor?.invoke(title)
                    onDismiss()
                }) {
                    Icon(
                        Icons.Filled.MoreHoriz,
                        contentDescription = "자세히",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.weight(1f))

                IconButton(
                    onClick = {
                        if (title.isNotBlank() && listId != 0L) {
                            onSubmit(title.trim(), listId, pickedTagIds.toList())
                            onDismiss()
                        }
                    },
                    enabled = title.isNotBlank() && listId != 0L,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (title.isNotBlank()) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "보내기",
                        tint = if (title.isNotBlank())
                            MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TagChip(tag: Tag, selected: Boolean, onToggle: () -> Unit) {
    val color = Color(tag.colorArgb)
    Box(
        modifier = Modifier
            .background(
                if (selected) color.copy(alpha = 0.25f) else color.copy(alpha = 0.15f),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            "#${tag.name}",
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ListPickerButton(
    label: String,
    accent: Color,
    lists: List<TaskList>,
    onPick: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Text(
            label,
            color = accent,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(vertical = 4.dp)
        )
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            lists.forEach { list ->
                androidx.compose.material3.DropdownMenuItem(
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(list.colorArgb), CircleShape)
                        )
                    },
                    text = { Text(list.name) },
                    onClick = { onPick(list.id); expanded = false }
                )
            }
        }
    }
}
