package com.example.testapp.data.repository

import com.example.testapp.data.db.dao.HabitDao
import com.example.testapp.domain.model.Habit
import com.example.testapp.domain.model.HabitEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitRepository(private val dao: HabitDao) {
    fun observeActive(): Flow<List<Habit>> = dao.observeActive().map { it.map { e -> e.toDomain() } }

    suspend fun get(id: Long): Habit? = dao.get(id)?.toDomain()

    suspend fun upsert(habit: Habit): Long {
        return if (habit.id == 0L) dao.insert(habit.toEntity())
        else { dao.update(habit.toEntity()); habit.id }
    }

    suspend fun delete(habit: Habit) = dao.delete(habit.toEntity())

    fun observeEntries(habitId: Long): Flow<List<HabitEntry>> =
        dao.observeEntries(habitId).map { it.map { e -> e.toDomain() } }

    fun observeEntriesBetween(start: String, end: String): Flow<List<HabitEntry>> =
        dao.observeEntriesBetween(start, end).map { it.map { e -> e.toDomain() } }

    suspend fun toggleEntry(habitId: Long, date: String) {
        val existing = dao.getEntry(habitId, date)
        if (existing == null) dao.insertEntry(HabitEntry(habitId = habitId, date = date).toEntity())
        else dao.deleteEntry(habitId, date)
    }
}
