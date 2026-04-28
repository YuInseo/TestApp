package com.example.testapp.data.repository

import com.example.testapp.data.db.dao.PomodoroDao
import com.example.testapp.domain.model.PomodoroSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PomodoroRepository(private val dao: PomodoroDao) {
    fun observeAll(): Flow<List<PomodoroSession>> = dao.observeAll().map { it.map { e -> e.toDomain() } }
    fun observeBetween(start: Long, end: Long): Flow<List<PomodoroSession>> =
        dao.observeBetween(start, end).map { it.map { e -> e.toDomain() } }

    suspend fun insert(session: PomodoroSession) = dao.insert(session.toEntity())
    suspend fun focusMillisBetween(start: Long, end: Long) = dao.focusMillisBetween(start, end)
}
