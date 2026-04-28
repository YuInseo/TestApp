package com.example.testapp.ui.feature.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.testapp.BuildConfig
import com.example.testapp.ui.navigation.Tab

@Composable
fun MoreScreen(
    overflowTabs: List<Tab> = emptyList(),
    onTabClick: (Tab) -> Unit = {},
    onLists: () -> Unit,
    onSettings: () -> Unit,
    onAppearance: () -> Unit,
    onTabBarConfig: () -> Unit,
    onHabits: () -> Unit,
    onStats: () -> Unit,
    onCheckForUpdate: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                "더보기",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(Modifier.height(16.dp))

            ProfileCard()
            Spacer(Modifier.height(12.dp))

            if (overflowTabs.isNotEmpty()) {
                CardSection {
                    overflowTabs.forEachIndexed { i, tab ->
                        if (i > 0) Divider()
                        MoreItem(
                            icon = tab.iconSelected,
                            label = tab.label,
                            onClick = { onTabClick(tab) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            CardSection {
                MoreItem(
                    icon = Icons.Filled.GridView,
                    label = "탭 바",
                    onClick = onTabBarConfig
                )
            }
            Spacer(Modifier.height(12.dp))

            CardSection {
                MoreItem(Icons.Filled.Palette, "외관", onAppearance)
                Divider()
                MoreItem(Icons.Filled.AccessTime, "날짜 & 시간", onSettings)
                Divider()
                MoreItem(Icons.Filled.MusicNote, "사운드 & 알림", onSettings)
                Divider()
                MoreItem(Icons.Filled.Widgets, "위젯", onSettings)
                Divider()
                MoreItem(Icons.Filled.Settings, "일반", onSettings)
            }
            Spacer(Modifier.height(12.dp))

            CardSection {
                MoreItem(Icons.Filled.FormatListBulleted, "목록 관리", onLists)
                Divider()
                MoreItem(Icons.Filled.EventRepeat, "습관", onHabits)
                Divider()
                MoreItem(Icons.Filled.BarChart, "통계", onStats)
                Divider()
                MoreItem(Icons.Filled.Notifications, "알림 권한", onSettings)
            }
            Spacer(Modifier.height(12.dp))

            CardSection {
                MoreItem(
                    icon = Icons.Filled.Download,
                    label = "업데이트 확인",
                    onClick = onCheckForUpdate
                )
            }
            Spacer(Modifier.height(12.dp))

            CardSection {
                MoreItem(Icons.Filled.Star, "친구에게 추천하기", onClick = {})
                Divider()
                MoreItem(Icons.Filled.Help, "도움말 & 피드백", onClick = {})
                Divider()
                MoreItem(
                    icon = Icons.Filled.Info,
                    label = "정보",
                    sub = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    onClick = {}
                )
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ProfileCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("👤", style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "사용자",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFF34D399).copy(alpha = 0.25f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "Lv.${BuildConfig.VERSION_CODE}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF34D399)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "${BuildConfig.GIT_SHA}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CardSection(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

@Composable
private fun MoreItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    sub: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (sub != null) {
                Text(
                    sub,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Divider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
