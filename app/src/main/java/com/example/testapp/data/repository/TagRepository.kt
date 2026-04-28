package com.example.testapp.data.repository

import com.example.testapp.data.db.dao.TagDao
import com.example.testapp.domain.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TagRepository(private val dao: TagDao) {
    fun observeAll(): Flow<List<Tag>> = dao.observeAll().map { it.map { e -> e.toDomain() } }

    suspend fun upsert(tag: Tag): Long {
        return if (tag.id == 0L) dao.insert(tag.toEntity())
        else { dao.update(tag.toEntity()); tag.id }
    }

    suspend fun delete(tag: Tag) = dao.delete(tag.toEntity())
}
