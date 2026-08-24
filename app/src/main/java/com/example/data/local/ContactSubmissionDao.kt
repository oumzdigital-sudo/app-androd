package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactSubmissionDao {
    @Query("SELECT * FROM contact_submissions ORDER BY timestamp DESC")
    fun getAllSubmissions(): Flow<List<ContactSubmission>>

    @Query("SELECT * FROM contact_submissions WHERE id = :id LIMIT 1")
    suspend fun getSubmissionById(id: Long): ContactSubmission?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: ContactSubmission): Long

    @Delete
    suspend fun deleteSubmission(submission: ContactSubmission)

    @Query("DELETE FROM contact_submissions")
    suspend fun clearAllSubmissions()

    @Query("SELECT COUNT(*) FROM contact_submissions")
    fun getSubmissionCount(): Flow<Int>
}
