package com.example.testapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.model.Recurrence
import com.example.testapp.domain.model.Task
import com.example.testapp.ui.theme.AppColors
import com.example.testapp.util.DateUtils

@Composable
fun CompactTaskItem(
    task: Task,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    isSubtask: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = if (isSubtask) 32.dp else 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaskCheckbox(
            checked = task.completed,
            color = task.priority.composeColor().takeIf { task.priority.level > 0 }
                ?: MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onToggle
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (task.notes.isNotBlank() || task.subtasks.isNotEmpty()) {
                    Icon(
                        Icons.Filled.Description,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (task.completed)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            if (task.tags.isNotEmpty()) {
                Spacer(Modifier.size(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    task.tags.take(3).forEach { tag ->
                        Text(
                            "#${tag.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(tag.colorArgb)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            task.dueAt?.let { due ->
                val isOverdue = !task.completed && due < System.currentTimeMillis() &&
                    !DateUtils.isToday(due)
                val dateColor = when {
                    task.completed -> MaterialTheme.colorScheme.onSurfaceVariant
                    isOverdue -> AppColors.Overdue
                    else -> AppColors.Upcoming
                }
                Text(
                    text = DateUtils.shortRelativeDate(due),
                    style = MaterialTheme.typography.labelMedium,
                    color = dateColor
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (task.reminderAt != null) {
                    Icon(
                        Icons.Filled.AlarmOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (task.recurrence != Recurrence.NONE) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCheckbox(
    checked: Boolean,
    color: Color,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 22.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .border(
                width = 1.5.dp,
                color = color,
                shape = RoundedCornerShape(6.dp)
            )
            .background(
                if (checked) color else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(size * 0.7f)
            )
        }
    }
}

@Composable
fun ListColorDot(color: Color, size: androidx.compose.ui.unit.Dp = 10.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(color, CircleShape)
    )
}
