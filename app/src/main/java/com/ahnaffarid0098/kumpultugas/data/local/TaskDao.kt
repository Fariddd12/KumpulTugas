package com.ahnaffarid0098.kumpultugas.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("SELECT * FROM task ORDER BY id DESC")
    fun getTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE userEmail = :email ORDER BY id DESC")
    suspend fun getTasksByEmail(email: String): List<TaskEntity>

    @Query("SELECT * FROM task WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("DELETE FROM task WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Delete
    suspend fun delete(task: TaskEntity)
}