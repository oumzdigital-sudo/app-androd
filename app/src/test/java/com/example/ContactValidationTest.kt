package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.LogLevel
import com.example.data.repository.ContactRepository
import com.example.ui.viewmodel.ContactFormViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ContactValidationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: ContactRepository
    private lateinit var viewModel: ContactFormViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .setTransactionExecutor(testDispatcher.asExecutor())
            .setQueryExecutor(testDispatcher.asExecutor())
            .build()
        repository = ContactRepository(database.contactSubmissionDao())
        viewModel = ContactFormViewModel(context, repository, ioDispatcher = testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        database.close()
    }

    @Test
    fun `empty inputs trigger validation errors and error logs`() = runTest(testDispatcher) {
        viewModel.submitForm()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.nombreError)
        assertNotNull(state.correoError)
        assertFalse(state.isSubmitted)
        assertEquals("", state.mensajeFeedback)

        val logs = viewModel.logs.value
        assertTrue(logs.any { it.level == LogLevel.ERROR })
    }

    @Test
    fun `invalid email triggers format error`() = runTest(testDispatcher) {
        viewModel.onNombreChange("Juan Perez")
        viewModel.onCorreoChange("correo_invalido_sin_arroba")
        viewModel.submitForm()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.nombreError)
        assertNotNull(state.correoError)
        assertFalse(state.isSubmitted)
    }

    @Test
    fun `valid inputs succeed, update feedback message, and persist in Room`() = runTest(testDispatcher) {
        viewModel.onNombreChange("Ana Torres")
        viewModel.onCorreoChange("ana.torres@ejemplo.com")
        viewModel.submitForm()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.nombreError)
        assertNull(state.correoError)
        assertTrue(state.isSubmitted)
        assertTrue(state.mensajeFeedback.contains("Ana Torres"))
        assertTrue(state.mensajeFeedback.contains("ana.torres@ejemplo.com"))

        // Check Room persistence directly from DAO query
        val saved = database.contactSubmissionDao().getSubmissionById(1)
        assertNotNull(saved)
        assertEquals("Ana Torres", saved?.nombre)
        assertEquals("ana.torres@ejemplo.com", saved?.correo)
    }
}
