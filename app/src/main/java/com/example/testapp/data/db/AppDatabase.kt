package com.example.testapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.testapp.data.db.dao.HabitDao
import com.example.testapp.data.db.dao.PomodoroDao
import com.example.testapp.data.db.dao.TagDao
import com.example.testapp.data.db.dao.TaskDao
import com.example.testapp.data.db.dao.TaskListDao
import com.example.testapp.data.db.entity.HabitEntity
import com.example.testapp.data.db.entity.HabitEntryEntity
import com.example.testapp.data.db.entity.PomodoroSessionEntity
import com.example.testapp.data.db.entity.SubtaskEntity
import com.example.testapp.data.db.entity.TagEntity
import com.example.testapp.data.db.entity.TaskEntity
import com.example.testapp.data.db.entity.TaskListEntity
import com.example.testapp.data.db.entity.TaskTagCrossRef

@Database(
    entities = [
        TaskEntity::class,
        SubtaskEntity::class,
        TaskListEntity::class,
        TagEntity::class,
        TaskTagCrossRef::class,
        PomodoroSessionEntity::class,
        HabitEntity::class,
        HabitEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskListDao(): TaskListDao
    abstract fun tagDao(): TagDao
    abstract fun pomodoroDao(): PomodoroDao
    abstract fun habitDao(): HabitDao

    companion object {
        fun build(context: Context): AppDatabase = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "ticktick_clone.db"
        )
            .addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL(
                        "INSERT INTO task_lists (name, colorArgb, sortOrder, isInbox) VALUES " +
                            "('받은편지함', ${0xFF42A5F5L}, 0, 1)," +
                            "('업무', ${0xFFEC407AL}, 1, 0)," +
                            "('개인', ${0xFF66BB6AL}, 2, 0)"
                    )
                }
            })
            .build()
    }
}
