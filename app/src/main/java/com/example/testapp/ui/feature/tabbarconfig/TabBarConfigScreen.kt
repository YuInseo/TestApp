package com.example.testapp.ui.feature.tabbarconfig

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testapp.ui.navigation.Tab
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabBarConfigScreen(
    onBack: () -> Unit,
    vm: TabBarConfigViewModel = koinViewModel()
) {
    val s by vm.settings.collectAsStateWithLifecycle()

    val enabled = remember(s.enabledTabIds) {
        s.enabledTabIds.mapNotNull { Tab.byId(it) }
            .filter { Tab.configurable.contains(it) }
    }
    val disabled = remember(s.enabledTabIds) {
        Tab.configurable.filter { it !in enabled }
    }

    var showMaxDialog by remember { mutableStateOf(false) }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var rowHeightPx by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("탭 바") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Enabled list — drag to reorder
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                enabled.forEachIndexed { index, tab ->
                    TabRow(
                        tab = tab,
                        leftAction = LeftAction.Remove,
                        onLeftClick = {
                            vm.setEnabledTabs(s.enabledTabIds - tab.id)
                        },
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = if (draggingId == tab.id) dragOffsetY else 0f
                            }
                            .zIndex(if (draggingId == tab.id) 1f else 0f)
                            .alpha(if (draggingId == tab.id) 0.85f else 1f),
                        onSizeChange = { rowHeightPx = it },
                        dragHandleModifier = Modifier.pointerInput(tab.id, enabled.size) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggingId = tab.id
                                    dragOffsetY = 0f
                                },
                                onDrag = { _, dragAmount ->
                                    dragOffsetY += dragAmount.y
                                    val rowH = rowHeightPx.toFloat().takeIf { it > 0 } ?: return@detectDragGesturesAfterLongPress
                                    val moveBy = (dragOffsetY / rowH).toInt()
                                    val targetIndex = (index + moveBy).coerceIn(0, enabled.lastIndex)
                                    if (targetIndex != index && abs(dragOffsetY) >= rowH * 0.5f) {
                                        val newIds = enabled.toMutableList().also {
                                            val moved = it.removeAt(index)
                                            it.add(targetIndex, moved)
                                        }.map { it.id }
                                        vm.setEnabledTabs(newIds)
                                        dragOffsetY -= (targetIndex - index) * rowH
                                    }
                                },
                                onDragEnd = {
                                    draggingId = null
                                    dragOffsetY = 0f
                                },
                                onDragCancel = {
                                    draggingId = null
                                    dragOffsetY = 0f
                                }
                            )
                        }
                    )
                }
            }

            if (disabled.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Text(
                    "사용 안 함",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    disabled.forEach { tab ->
                        TabRow(
                            tab = tab,
                            leftAction = LeftAction.Add,
                            onLeftClick = {
                                vm.setEnabledTabs(s.enabledTabIds + tab.id)
                            },
                            dragHandleModifier = Modifier
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { showMaxDialog = true }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "탭의 최대 개수",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${s.maxTabs}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "최대한도를 초과한 탭은 '더보기'에서 표시됩니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(Modifier.height(40.dp))
        }
    }

    if (showMaxDialog) {
        MaxTabsDialog(
            current = s.maxTabs,
            onDismiss = { showMaxDialog = false },
            onConfirm = { vm.setMaxTabs(it); showMaxDialog = false }
        )
    }
}

private enum class LeftAction { Add, Remove }

@Composable
private fun TabRow(
    tab: Tab,
    leftAction: LeftAction,
    onLeftClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSizeChange: (Int) -> Unit = {},
    dragHandleModifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { onSizeChange(it.size.height) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (icon, tint) = when (leftAction) {
            LeftAction.Add -> Icons.Filled.Add to Color(0xFF22C55E)
            LeftAction.Remove -> Icons.Filled.Remove to Color(0xFFE53935)
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onLeftClick)
                .background(
                    tint.copy(alpha = 0.18f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                tab.iconSelected,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                tab.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                tab.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = dragHandleModifier
                .padding(start = 8.dp, end = 4.dp)
                .size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.DragHandle,
                contentDescription = "재정렬",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MaxTabsDialog(
    current: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var picked by remember { mutableIntStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("탭의 최대 개수") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(3, 4, 5, 6, 7).forEach { n ->
                    Text(
                        "$n",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (n == picked) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        fontWeight = if (n == picked) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clickable { picked = n }
                            .padding(vertical = 10.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(picked) }) { Text("확인") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
