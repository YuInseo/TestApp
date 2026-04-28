package com.example.testapp.update

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.testapp.R
import com.example.testapp.notification.NotificationChannels

class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999)
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                else
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                confirm?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (confirm != null) context.startActivity(confirm)
            }
            PackageInstaller.STATUS_SUCCESS -> {
                relaunchApp(context)
            }
            else -> {
                val msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                notify(context, "업데이트 실패: ${msg ?: "알 수 없는 오류"}")
            }
        }
    }

    private fun relaunchApp(context: Context) {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return
        launch.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        )

        // Notification with PendingIntent so user can tap to launch even if
        // we lose foreground privilege after install.
        val pi = PendingIntent.getActivity(
            context,
            0,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, NotificationChannels.UPDATE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("업데이트 완료")
            .setContentText("새 버전으로 시작합니다")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        context.getSystemService(NotificationManager::class.java)?.notify(9911, n)

        // Try to launch the new app immediately. On Android 10+ background
        // activity launches are restricted, but right after a self-install
        // we are typically allowed to start. Falls back to the notification.
        Handler(Looper.getMainLooper()).postDelayed({
            runCatching { context.startActivity(launch) }
        }, 400)
    }

    private fun notify(context: Context, text: String) {
        val n = NotificationCompat.Builder(context, NotificationChannels.UPDATE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("앱 업데이트")
            .setContentText(text)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)?.notify(9911, n)
    }

    companion object {
        const val ACTION = "com.example.testapp.update.INSTALL_RESULT"
    }
}
