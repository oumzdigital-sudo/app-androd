package com.example.data.repository

import com.example.data.local.ContactSubmission
import com.example.data.local.ContactSubmissionDao
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val dao: ContactSubmissionDao) {

    val allSubmissions: Flow<List<ContactSubmission>> = dao.getAllSubmissions()
    val submissionCount: Flow<Int> = dao.getSubmissionCount()

    suspend fun insert(submission: ContactSubmission): Long {
        return dao.insertSubmission(submission)
    }

    suspend fun delete(submission: ContactSubmission) {
        dao.deleteSubmission(submission)
    }

    suspend fun clearAll() {
        dao.clearAllSubmissions()
    }
}
