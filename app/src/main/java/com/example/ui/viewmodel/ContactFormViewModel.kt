package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ContactSubmission
import com.example.data.model.AppLog
import com.example.data.model.LogLevel
import com.example.data.repository.ContactRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormUiState(
    val nombre: String = "",
    val correo: String = "",
    val nombreError: String? = null,
    val correoError: String? = null,
    val mensajeFeedback: String = "",
    val isSubmitted: Boolean = false,
    val isSubmitting: Boolean = false,
    val activeStep: Int = 1 // 1: Datos, 2: Validación, 3: Guardado en Room
)

class ContactFormViewModel(
    application: Application,
    private val repository: ContactRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FormUiState())
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    private val _logs = MutableStateFlow<List<AppLog>>(emptyList())
    val logs: StateFlow<List<AppLog>> = _logs.asStateFlow()

    val savedSubmissions: StateFlow<List<ContactSubmission>> = repository.allSubmissions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val submissionsCount: StateFlow<Int> = repository.submissionCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        addLog("Sistema inicializado. Base de datos Room lista.", LogLevel.INFO)
    }

    fun onNombreChange(newValue: String) {
        _uiState.update {
            it.copy(
                nombre = newValue,
                nombreError = if (it.nombreError != null) null else null
            )
        }
    }

    fun onCorreoChange(newValue: String) {
        _uiState.update {
            it.copy(
                correo = newValue,
                correoError = if (it.correoError != null) null else null
            )
        }
    }

    fun submitForm() {
        val currentState = _uiState.value
        val nombreTrimmed = currentState.nombre.trim()
        val correoTrimmed = currentState.correo.trim()

        var hasError = false
        var nombreErr: String? = null
        var correoErr: String? = null

        // Step 2: Validating
        _uiState.update { it.copy(activeStep = 2) }

        if (nombreTrimmed.isEmpty()) {
            nombreErr = "Por favor ingresa tu nombre"
            hasError = true
            addLog("Validación fallida: El campo 'Nombre' está vacío", LogLevel.ERROR, "Campo: editNombre")
        } else if (nombreTrimmed.length < 2) {
            nombreErr = "El nombre debe tener al menos 2 caracteres"
            hasError = true
            addLog("Validación fallida: Nombre demasiado corto ('$nombreTrimmed')", LogLevel.WARNING)
        }

        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (correoTrimmed.isEmpty()) {
            correoErr = "Por favor ingresa tu correo electrónico"
            hasError = true
            addLog("Validación fallida: El campo 'Correo' está vacío", LogLevel.ERROR, "Campo: editCorreo")
        } else if (!emailRegex.matches(correoTrimmed)) {
            correoErr = "Formato de correo no válido (ejemplo: usuario@dominio.com)"
            hasError = true
            addLog("Validación fallida: Formato de correo incorrecto ('$correoTrimmed')", LogLevel.ERROR)
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    nombreError = nombreErr,
                    correoError = correoErr,
                    mensajeFeedback = "",
                    isSubmitted = false,
                    activeStep = 1
                )
            }
            return
        }

        // Success -> Persist in Room (Step 3)
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isSubmitting = true, activeStep = 3) }
            try {
                val newSubmission = ContactSubmission(
                    nombre = nombreTrimmed,
                    correo = correoTrimmed
                )
                val id = repository.insert(newSubmission)

                val successMsg = "¡Hola $nombreTrimmed! Tu correo ($correoTrimmed) ha sido registrado correctamente."
                _uiState.update {
                    it.copy(
                        nombreError = null,
                        correoError = null,
                        mensajeFeedback = successMsg,
                        isSubmitted = true,
                        isSubmitting = false,
                        activeStep = 3
                    )
                }
                addLog(
                    "Envío exitoso y guardado en Room (ID #$id): $nombreTrimmed <$correoTrimmed>",
                    LogLevel.SUCCESS,
                    "Tabla: contact_submissions | Timestamp: ${System.currentTimeMillis()}"
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, activeStep = 1) }
                addLog("Error crítico al persistir datos: ${e.message}", LogLevel.ERROR, e.stackTraceToString())
            }
        }
    }

    fun resetForm() {
        _uiState.update {
            FormUiState()
        }
        addLog("Formulario restablecido a valores iniciales", LogLevel.INFO)
    }

    fun deleteSubmission(submission: ContactSubmission) {
        viewModelScope.launch(ioDispatcher) {
            repository.delete(submission)
            addLog("Registro eliminado: ${submission.nombre} (${submission.correo})", LogLevel.WARNING)
        }
    }

    fun clearAllSubmissions() {
        viewModelScope.launch(ioDispatcher) {
            repository.clearAll()
            addLog("Todos los registros locales fueron eliminados", LogLevel.WARNING)
        }
    }

    fun addLog(message: String, level: LogLevel, details: String? = null) {
        val newLog = AppLog(
            message = message,
            level = level,
            details = details
        )
        _logs.update { listOf(newLog) + it.take(49) }
    }

    fun clearLogs() {
        _logs.update { emptyList() }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val database = AppDatabase.getDatabase(application)
            val repository = ContactRepository(database.contactSubmissionDao())
            return ContactFormViewModel(application, repository) as T
        }
    }
}
