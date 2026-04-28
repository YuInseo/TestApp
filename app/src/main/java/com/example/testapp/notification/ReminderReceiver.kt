package com.example.testapp.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.testapp.MainActivity
import com.example.testapp.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_FIRE -> showReminder(context, intent)
            ACTION_COMPLETE -> {
                // Optional: mark as complete via direct DB call requires async; out of scope here
                cancelNotification(context, intent.getLongExtra(EXTRA_TASK_ID, 0L))
            }
        }
    }

    private fun showReminder(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("task_id", taskId)
        }
        val pi = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDERS)
            .setContentTitle("할 일 알림")
            .setContentText(title)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        context.getSystemService(NotificationManager::class.java)
            ?.notify(taskId.toInt(), notification)
    }

    private fun cancelNotification(context: Context, taskId: Long) {
        context.getSystemService(NotificationManager::class.java)?.cancel(taskId.toInt())
    }

    companion object {
        const val ACTION_FIRE = "com.example.testapp.action.REMINDER_FIRE"
        const val ACTION_COMPLETE = "com.example.testapp.action.REMINDER_COMPLETE"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TITLE = "title"
    }
}
