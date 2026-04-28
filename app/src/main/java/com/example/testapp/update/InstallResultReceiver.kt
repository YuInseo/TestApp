package com.example.testapp.update

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import androidx.core.app.NotificationCompat
import com.example.testapp.R
import com.example.testapp.notification.NotificationChannels

class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999)
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                confirm?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (confirm != null) context.startActivity(confirm)
            }
            PackageInstaller.STATUS_SUCCESS -> notify(context, "업데이트 완료")
            else -> {
                val msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                notify(context, "업데이트 실패: $msg")
            }
        }
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
