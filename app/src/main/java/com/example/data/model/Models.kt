package com.example.data.model

enum class MayaState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

data class ChatMessage(
    val id: Long = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "hi",
    val isVoice: Boolean = true
)

data class MayaMemory(
    val id: Long = 0,
    val key: String,
    val value: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)
