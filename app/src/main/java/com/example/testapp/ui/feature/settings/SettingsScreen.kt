package com.example.testapp.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testapp.BuildConfig
import com.example.testapp.data.preferences.ThemeMode
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onCheckForUpdate: () -> Unit,
    vm: SettingsViewModel = koinViewModel()
) {
    val s by vm.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "테마",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                ThemeMode.entries.forEach { m ->
                    FilterChip(
                        selected = s.theme == m,
                        onClick = { vm.setTheme(m) },
                        label = {
                            Text(when (m) {
                                ThemeMode.SYSTEM -> "시스템"
                                ThemeMode.LIGHT -> "라이트"
                                ThemeMode.DARK -> "다크"
                            })
                        }
                    )
                }
            }
            ListItem(
                headlineContent = { Text("동적 색상 (Material You)") },
                trailingContent = {
                    Switch(checked = s.dynamicColor, onCheckedChange = vm::setDynamic)
                }
            )
            HorizontalDivider()

            Text(
                "포모도로 길이 (분)",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                IntRow("집중", s.pomodoroFocusMin, listOf(15, 20, 25, 30, 45, 50)) { vm.setFocus(it) }
                IntRow("짧은 휴식", s.pomodoroShortBreakMin, listOf(3, 5, 7, 10)) { vm.setShortBreak(it) }
                IntRow("긴 휴식", s.pomodoroLongBreakMin, listOf(10, 15, 20, 30)) { vm.setLongBreak(it) }
            }
            HorizontalDivider()

            Text(
                "자동 업데이트",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )
            ListItem(
                headlineContent = { Text("앱 시작 시 새 버전 확인") },
                supportingContent = { Text("GitHub Release에서 최신 APK 확인") },
                trailingContent = {
                    Switch(checked = s.autoUpdateEnabled, onCheckedChange = vm::setAutoUpdate)
                }
            )
            ListItem(
                headlineContent = { Text("지금 업데이트 확인") },
                modifier = Modifier.fillMaxWidth(),
                trailingContent = {
                    androidx.compose.material3.TextButton(onClick = onCheckForUpdate) {
                        Text("확인")
                    }
                }
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("버전") },
                supportingContent = { Text("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) · ${BuildConfig.GIT_SHA}") }
            )
        }
    }
}

@Composable
private fun IntRow(label: String, current: Int, options: List<Int>, onPick: (Int) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { opt ->
                FilterChip(
                    selected = current == opt,
                    onClick = { onPick(opt) },
                    label = { Text("$opt") }
                )
            }
        }
    }
}
