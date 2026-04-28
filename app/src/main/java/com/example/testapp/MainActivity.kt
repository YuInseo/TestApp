package com.example.testapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testapp.data.preferences.ThemeMode
import com.example.testapp.ui.AppShell
import com.example.testapp.ui.feature.settings.SettingsViewModel
import com.example.testapp.ui.theme.AppTheme
import com.example.testapp.ui.update.UpdateDialog
import com.example.testapp.ui.update.UpdateViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val notifPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* user choice noted */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        setContent { AppRoot() }
    }
}

@Composable
private fun AppRoot() {
    val settingsVm: SettingsViewModel = koinViewModel()
    val updateVm: UpdateViewModel = koinViewModel()
    val s by settingsVm.settings.collectAsStateWithLifecycle()

    val dark = when (s.theme) {
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    LaunchedEffect(Unit) { updateVm.checkOnLaunch() }

    AppTheme(darkTheme = dark, dynamicColor = false) {
        AppShell(onCheckForUpdate = updateVm::checkManually)
        UpdateDialog(updateVm)
    }
}
