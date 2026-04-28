package com.example.testapp.ui.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testapp.update.UpdateState
import org.koin.androidx.compose.koinViewModel

@Composable
fun UpdateDialog(vm: UpdateViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    when (val s = state) {
        is UpdateState.Available -> AlertDialog(
            onDismissRequest = vm::dismiss,
            title = { Text("새 버전이 있어요") },
            text = {
                Column {
                    Text("v${s.manifest.versionName} (code ${s.manifest.versionCode})")
                    if (s.manifest.notes.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(s.manifest.notes, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (vm.canRequestInstall()) vm.startDownload(s.manifest)
                    else vm.openUnknownSourcesSettings()
                }) {
                    Text(if (vm.canRequestInstall()) "다운로드 후 설치" else "설치 권한 허용")
                }
            },
            dismissButton = {
                TextButton(onClick = vm::dismiss) { Text("나중에") }
            }
        )
        is UpdateState.Downloading -> AlertDialog(
            onDismissRequest = {},
            title = { Text("다운로드 중...") },
            text = {
                Column {
                    LinearProgressIndicator(
                        progress = { s.progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("${s.progress}%")
                }
            },
            confirmButton = {}
        )
        is UpdateState.ReadyToInstall -> AlertDialog(
            onDismissRequest = vm::dismiss,
            title = { Text("설치 준비 완료") },
            text = {
                Text(
                    "확인을 누르면 설치가 진행되고 새 버전으로 자동 재시작됩니다.\n" +
                        "(시스템에서 한 번만 설치 확인을 눌러주세요)"
                )
            },
            confirmButton = {
                TextButton(onClick = { vm.install() }) { Text("설치 후 재시작") }
            },
            dismissButton = {
                TextButton(onClick = vm::dismiss) { Text("취소") }
            }
        )
        is UpdateState.Installing -> AlertDialog(
            onDismissRequest = {},
            title = { Text("설치 중...") },
            text = {
                Text(
                    "시스템 설치 화면에서 [설치]를 눌러주세요.\n" +
                        "설치가 끝나면 새 버전이 자동으로 시작됩니다."
                )
            },
            confirmButton = {}
        )
        is UpdateState.Error -> AlertDialog(
            onDismissRequest = vm::dismiss,
            title = { Text("업데이트 오류") },
            text = { Text(s.message) },
            confirmButton = {
                TextButton(onClick = vm::dismiss) { Text("확인") }
            }
        )
        is UpdateState.UpToDate -> AlertDialog(
            onDismissRequest = vm::dismiss,
            title = { Text("최신 버전입니다") },
            text = { Text("이미 가장 최신 버전을 사용 중이에요.") },
            confirmButton = {
                TextButton(onClick = vm::dismiss) { Text("확인") }
            }
        )
        else -> Unit
    }
}
