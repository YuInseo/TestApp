package com.example.testapp.ui.feature.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val lists by vm.lists.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<TaskList?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("목록 관리") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = TaskList(name = "", colorArgb = PRESET_COLORS.first())
            }) {
                Icon(Icons.Filled.Add, contentDescription = "목록 추가")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(lists, key = { it.id }) { list ->
                ListItem(
                    headlineContent = { Text(list.name) },
                    supportingContent = { if (list.isInbox) Text("기본 목록") },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(list.colorArgb), CircleShape)
                        )
                    },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { editing = list }) {
                                Icon(Icons.Filled.Edit, contentDescription = "편집")
                            }
                            if (!list.isInbox) {
                                IconButton(onClick = { vm.delete(list) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "삭제")
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    editing?.let { current ->
        var name by remember { mutableStateOf(current.name) }
        var color by remember { mutableStateOf(current.colorArgb) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(if (current.id == 0L) "새 목록" else "목록 편집") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("이름") }, modifier = Modifier.fillMaxWidth()
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
                        vm.upsert(current.copy(name = name.trim(), colorArgb = color))
                    }
                    editing = null
                }) { Text("저장") }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) { Text("취소") }
            }
        )
    }
}
