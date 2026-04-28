package com.example.testapp.widget

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.testapp.data.repository.TaskRepository
import com.example.testapp.util.DateUtils
import kotlinx.coroutines.flow.first
import org.koin.core.context.GlobalContext

class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = GlobalContext.get().get<TaskRepository>()
        val tasks: List<TaskSnapshot> = repo
            .observeDueBetween(DateUtils.startOfDay(), DateUtils.endOfDay())
            .first()
            .filter { !it.completed }
            .map { TaskSnapshot(it.id, it.title, it.completed) }

        val openAppAction = actionStartActivity(
            ComponentName(context.packageName, "com.example.testapp.MainActivity")
        )

        provideContent {
            GlanceTheme {
                Body(tasks, openAppAction)
            }
        }
    }
}

internal data class TaskSnapshot(val id: Long, val title: String, val completed: Boolean)

@Composable
private fun Body(
    tasks: List<TaskSnapshot>,
    openAppAction: androidx.glance.action.Action
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickable(openAppAction)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "오늘의 할 일",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GlanceTheme.colors.onBackground
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = "+",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GlanceTheme.colors.onBackground
                ),
                modifier = GlanceModifier.padding(horizontal = 8.dp)
            )
        }
        Spacer(modifier = GlanceModifier.height(8.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "할 일이 없어요",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = GlanceTheme.colors.onBackground
                    )
                )
            }
        } else {
            val visible = tasks.take(6)
            val remaining = tasks.size - visible.size
            visible.forEach { task ->
                TaskRow(task)
                Spacer(modifier = GlanceModifier.height(4.dp))
            }
            if (remaining > 0) {
                Text(
                    text = "+$remaining 더보기",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onBackground
                    )
                )
            }
        }
    }
}

@Composable
private fun TaskRow(task: TaskSnapshot) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .size(14.dp)
                .background(ColorProvider(Color(0xFFB0BEC5)))
        ) {}
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = task.title,
            style = TextStyle(
                fontSize = 14.sp,
                color = GlanceTheme.colors.onBackground
            ),
            maxLines = 1
        )
    }
}
