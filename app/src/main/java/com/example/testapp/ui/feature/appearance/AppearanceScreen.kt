package com.example.testapp.ui.feature.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testapp.data.preferences.AccentPreset
import com.example.testapp.data.preferences.CompletedStyle
import com.example.testapp.data.preferences.FontScale
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    vm: AppearanceViewModel = koinViewModel()
) {
    val s by vm.settings.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("테마", "앱 아이콘", "표시")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TabRow(
                        selectedTabIndex = tab,
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        tabs.forEachIndexed { i, t ->
                            Tab(
                                selected = tab == i,
                                onClick = { tab = i },
                                text = {
                                    Text(
                                        t,
                                        fontWeight = if (tab == i) FontWeight.Bold
                                        else FontWeight.Normal,
                                        color = if (tab == i) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                0 -> ThemeTab(
                    selected = s.accentPreset,
                    onPick = vm::setAccent
                )
                1 -> AppIconTab()
                2 -> DisplayTab(
                    fontScale = s.fontScale,
                    showSidebarCounts = s.showSidebarCounts,
                    hideNotes = s.hideNotes,
                    showListColor = s.showListColor,
                    completedStyle = s.completedStyle,
                    onFontScale = vm::setFontScale,
                    onSidebar = vm::setShowSidebarCounts,
                    onHideNotes = vm::setHideNotes,
                    onShowListColor = vm::setShowListColor,
                    onCompletedStyle = vm::setCompletedStyle
                )
            }
        }
    }
}

@Composable
private fun ThemeTab(
    selected: AccentPreset,
    onPick: (AccentPreset) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        SectionCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "시스템 어두운 모드로 이동",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "끄기",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        SectionCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "색상 시리즈",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                ColorPresetGrid(selected = selected, onPick = onPick)
            }
        }
        Spacer(Modifier.height(16.dp))

        SectionCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "계절 테마",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                SeasonalGrid()
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ColorPresetGrid(
    selected: AccentPreset,
    onPick: (AccentPreset) -> Unit
) {
    val items = AccentPreset.entries
    val rows = items.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { preset ->
                    PresetSwatch(
                        preset = preset,
                        isSelected = preset == selected,
                        onClick = { onPick(preset) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PresetSwatch(
    preset: AccentPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable(onClick = onClick)
                .background(
                    swatchBrush(preset),
                    RoundedCornerShape(16.dp)
                )
                .then(
                    if (isSelected) Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(16.dp)
                    ) else Modifier
                ),
            contentAlignment = Alignment.TopEnd
        ) {
            if (preset == AccentPreset.DARK) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .align(Alignment.Center)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFF3B82F6), CircleShape)
                        )
                    }
                }
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .size(22.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else if (preset == AccentPreset.MATERIAL_YOU) {
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .size(20.dp)
                        .background(Color(0xFFFFC107), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            preset.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun swatchBrush(preset: AccentPreset): Brush = when (preset) {
    AccentPreset.MATERIAL_YOU -> Brush.linearGradient(
        listOf(Color(0xFF8AB4F8), Color(0xFFA5D6A7), Color(0xFFFFCC80))
    )
    AccentPreset.DARK -> Brush.linearGradient(
        listOf(Color(0xFF1F1F1F), Color(0xFF1F1F1F))
    )
    AccentPreset.PEARL -> Brush.linearGradient(
        listOf(Color(0xFFF5F5F5), Color(0xFFE0E0E0))
    )
    else -> Brush.linearGradient(
        listOf(
            Color(preset.argb).copy(alpha = 0.95f),
            Color(preset.argb).copy(alpha = 0.7f)
        )
    )
}

@Composable
private fun SeasonalGrid() {
    val seasons = listOf(
        "봄" to listOf(Color(0xFFC5E1A5), Color(0xFF81C784)),
        "여름" to listOf(Color(0xFF4FC3F7), Color(0xFF0288D1)),
        "가을" to listOf(Color(0xFFFFAB91), Color(0xFFFF7043)),
        "겨울" to listOf(Color(0xFFB3E5FC), Color(0xFF81D4FA))
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        seasons.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (name, colors) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.6f)
                                .background(
                                    Brush.linearGradient(colors),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                                    .background(
                                        Color(0xFFFFC107),
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.WorkspacePremium,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppIconTab() {
    val accents = listOf(
        Color(0xFFFFFFFF) to Color(0xFF3B82F6),
        Color(0xFFE3F2FD) to Color(0xFF1976D2),
        Color(0xFFE8EAF6) to Color(0xFF3F51B5),
        Color(0xFF000000) to Color(0xFFFF7043),
        Color(0xFFB0BEC5) to Color(0xFF1B5E20),
        Color(0xFF263238) to Color(0xFFFFFFFF),
        Color(0xFF0D47A1) to Color(0xFFFFEB3B),
        Color(0xFF424242) to Color(0xFFFFFFFF),
        Color(0xFF1E88E5) to Color(0xFFFFFFFF),
        Color(0xFFB39DDB) to Color(0xFFFFFFFF),
        Color(0xFFFF8A80) to Color(0xFFFFFFFF),
        Color(0xFFFFE0B2) to Color(0xFFFFFFFF),
        Color(0xFF90CAF9) to Color(0xFFFFFFFF),
        Color(0xFF81C784) to Color(0xFFFFFFFF),
        Color(0xFFEF9A9A) to Color(0xFFFFFFFF),
        Color(0xFFFFE082) to Color(0xFFFFFFFF),
        Color(0xFF7986CB) to Color(0xFFFFFFFF),
        Color(0xFFA5D6A7) to Color(0xFFFFFFFF),
        Color(0xFFCFD8DC) to Color(0xFF424242),
        Color(0xFF000000) to Color(0xFFFFFFFF),
        Color(0xFFFFFFFF) to Color(0xFF26A69A),
        Color(0xFFFFFFFF) to Color(0xFFFF7043),
        Color(0xFFFFFFFF) to Color(0xFFE91E63),
        Color(0xFF1B5E20) to Color(0xFFFFFFFF),
        Color(0xFFD32F2F) to Color(0xFFFFFFFF)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        SectionCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "앱 아이콘",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(560.dp)
                ) {
                    items(accents) { (bg, fg) ->
                        IconTile(bg, fg, isSelected = accents.first() == (bg to fg))
                    }
                }
            }
        }
    }
}

@Composable
private fun IconTile(bg: Color, fg: Color, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(bg, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .border(3.dp, fg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(28.dp)
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(20.dp)
                    .background(Color(0xFFFFC107), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.WorkspacePremium,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun DisplayTab(
    fontScale: FontScale,
    showSidebarCounts: Boolean,
    hideNotes: Boolean,
    showListColor: Boolean,
    completedStyle: CompletedStyle,
    onFontScale: (FontScale) -> Unit,
    onSidebar: (Boolean) -> Unit,
    onHideNotes: (Boolean) -> Unit,
    onShowListColor: (Boolean) -> Unit,
    onCompletedStyle: (CompletedStyle) -> Unit
) {
    var fontExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        SectionCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { fontExpanded = !fontExpanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "글자 크기",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    fontScale.displayName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (fontExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FontScale.entries.forEach { scale ->
                        OptionPill(
                            label = scale.displayName,
                            selected = scale == fontScale,
                            onClick = { onFontScale(scale) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(16.dp))

        SectionCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "사이드바 개수",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SidebarPreview(
                        showCount = true,
                        selected = showSidebarCounts,
                        label = "보이기",
                        onClick = { onSidebar(true) },
                        modifier = Modifier.weight(1f)
                    )
                    SidebarPreview(
                        showCount = false,
                        selected = !showSidebarCounts,
                        label = "숨기기",
                        onClick = { onSidebar(false) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "노트 숨기기",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(checked = hideNotes, onCheckedChange = onHideNotes)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        SectionCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "목록 색상",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ListColorPreview(
                        showColor = true,
                        selected = showListColor,
                        label = "보이기",
                        onClick = { onShowListColor(true) },
                        modifier = Modifier.weight(1f)
                    )
                    ListColorPreview(
                        showColor = false,
                        selected = !showListColor,
                        label = "숨기기",
                        onClick = { onShowListColor(false) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "작업 목록에서 목록 색상을 표시하거나 숨기기.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        SectionCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "완료된 작업 스타일",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CompletedStylePreview(
                        style = CompletedStyle.CHECKBOX,
                        label = "기본값",
                        selected = completedStyle == CompletedStyle.CHECKBOX,
                        onClick = { onCompletedStyle(CompletedStyle.CHECKBOX) },
                        modifier = Modifier.weight(1f)
                    )
                    CompletedStylePreview(
                        style = CompletedStyle.STRIKETHROUGH,
                        label = "취소선",
                        selected = completedStyle == CompletedStyle.STRIKETHROUGH,
                        onClick = { onCompletedStyle(CompletedStyle.STRIKETHROUGH) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun OptionPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceContainerHighest,
                RoundedCornerShape(10.dp)
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun SelectablePreview(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                    RoundedCornerShape(12.dp)
                )
                .border(
                    if (selected) 2.dp else 1.dp,
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            content()
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SidebarPreview(
    showCount: Boolean,
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SelectablePreview(selected = selected, label = label, onClick = onClick, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SidebarRow(name = "오늘", count = if (showCount) 3 else null)
            SidebarRow(name = "모든 할일", count = if (showCount) 16 else null)
        }
    }
}

@Composable
private fun SidebarRow(name: String, count: Int?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(4.dp)
                )
        )
        Spacer(Modifier.width(6.dp))
        Text(
            name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        if (count != null) {
            Text(
                "$count",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ListColorPreview(
    showColor: Boolean,
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SelectablePreview(selected = selected, label = label, onClick = onClick, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ListColorRow(showColor = showColor, color = Color(0xFF3B82F6))
            ListColorRow(showColor = showColor, color = Color(0xFFFFB300))
        }
    }
}

@Composable
private fun ListColorRow(showColor: Boolean, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showColor) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .background(color)
            )
            Spacer(Modifier.width(6.dp))
        }
        Box(
            modifier = Modifier
                .size(14.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp))
        )
        Spacer(Modifier.width(6.dp))
        Text("작업 제목", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CompletedStylePreview(
    style: CompletedStyle,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SelectablePreview(selected = selected, label = label, onClick = onClick, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CompletedRow(style = style)
            CompletedRow(style = style)
        }
    }
}

@Composable
private fun CompletedRow(style: CompletedStyle) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    RoundedCornerShape(3.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(10.dp)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            "작업 제목",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = if (style == CompletedStyle.STRIKETHROUGH)
                TextDecoration.LineThrough else null
        )
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
    ) {
        Column { content() }
    }
}
