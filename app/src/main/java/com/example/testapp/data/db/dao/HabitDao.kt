package com.example.testapp.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.testapp.data.db.entity.HabitEntity
import com.example.testapp.data.db.entity.HabitEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY createdAt")
    fun observeActive(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun get(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId ORDER BY date DESC")
    fun observeEntries(habitId: Long): Flow<List<HabitEntryEntity>>

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getEntry(habitId: Long, date: String): HabitEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HabitEntryEntity): Long

    @Query("DELETE FROM habit_entries WHERE habitId = :habitId AND date = :date")
    suspend fun deleteEntry(habitId: Long, date: String)

    @Query("SELECT * FROM habit_entries WHERE date BETWEEN :start AND :end")
    fun observeEntriesBetween(start: String, end: String): Flow<List<HabitEntryEntity>>
}
