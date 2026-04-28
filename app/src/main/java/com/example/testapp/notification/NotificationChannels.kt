package com.example.testapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val REMINDERS = "reminders"
    const val POMODORO = "pomodoro"
    const val UPDATE = "update"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(REMINDERS, "할 일 알림", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "할 일 마감/리마인더 알림"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(POMODORO, "포모도로", NotificationManager.IMPORTANCE_LOW).apply {
                description = "포모도로 타이머 진행 표시"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(UPDATE, "앱 업데이트", NotificationManager.IMPORTANCE_LOW).apply {
                description = "새 버전 알림"
            }
        )
    }
}
