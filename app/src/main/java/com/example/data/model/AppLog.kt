package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    INFO,
    WARNING,
    ERROR,
    SUCCESS
}

data class AppLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val level: LogLevel,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String? = null
) {
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
