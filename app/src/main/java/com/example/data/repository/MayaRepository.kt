package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.db.MayaDatabase
import com.example.data.db.MemoryEntity
import com.example.data.db.MessageEntity
import com.example.data.model.ChatMessage
import com.example.data.model.MayaMemory
import com.example.data.remote.GeminiApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class MayaRepository(private val database: MayaDatabase) {

    val allMessages: Flow<List<ChatMessage>> = database.messageDao().getAllMessages().map { entities ->
        entities.map { it.toChatMessage() }
    }

    val allMemories: Flow<List<MayaMemory>> = database.memoryDao().getAllMemories().map { entities ->
        entities.map { it.toMayaMemory() }
    }

    suspend fun saveMessage(text: String, isUser: Boolean, isVoice: Boolean = true, language: String = "hi"): Long {
        return database.messageDao().insertMessage(
            MessageEntity(
                text = text,
                isUser = isUser,
                timestamp = System.currentTimeMillis(),
                language = language,
                isVoice = isVoice
            )
        )
    }

    suspend fun askMaya(userPrompt: String, isVoice: Boolean = true): Result<String> {
        // Save user prompt
        saveMessage(userPrompt, isUser = true, isVoice = isVoice)

        // Get past messages for conversation history
        val pastEntities = database.messageDao().getAllMessages().firstOrNull() ?: emptyList()
        val history = pastEntities.takeLast(10).map { Pair(it.text, it.isUser) }

        // Get memories
        val pastMemories = database.memoryDao().getAllMemories().firstOrNull() ?: emptyList()
        val memoryContext = pastMemories.joinToString("\n") { "- ${it.key}: ${it.value}" }

        val result = GeminiApiClient.generateMayaResponse(
            prompt = userPrompt,
            conversationHistory = history,
            memoryContext = memoryContext
        )

        result.onSuccess { reply ->
            saveMessage(reply, isUser = false, isVoice = isVoice)
            // Auto check if user asked to remember something
            checkAndExtractMemory(userPrompt, reply)
        }

        return result
    }

    suspend fun explainScreen(
        bitmap: Bitmap,
        userPrompt: String = "Explain what is on the screen right now",
        isVoice: Boolean = true
    ): Result<String> {
        val displayPrompt = if (userPrompt.isNotBlank()) userPrompt else "स्क्रीन पर क्या है समझाइए"
        saveMessage("📱 [Screen Share] $displayPrompt", isUser = true, isVoice = isVoice)

        val pastEntities = database.messageDao().getAllMessages().firstOrNull() ?: emptyList()
        val history = pastEntities.takeLast(6).map { Pair(it.text, it.isUser) }

        val pastMemories = database.memoryDao().getAllMemories().firstOrNull() ?: emptyList()
        val memoryContext = pastMemories.joinToString("\n") { "- ${it.key}: ${it.value}" }

        val enrichedPrompt = """
The user is sharing their device screen.
User query: "$displayPrompt"
Please analyze what is visible on this screen right now.
Provide a clear, natural, conversational spoken explanation in Hindi/English.
Explain what app, webpage, video, text, or interface is visible, and answer any specific question the user asked about it.
Keep it concise and voice-friendly.
""".trimIndent()

        val result = GeminiApiClient.generateMultimodalResponse(
            prompt = enrichedPrompt,
            imageBitmap = bitmap,
            conversationHistory = history,
            memoryContext = memoryContext
        )

        result.onSuccess { reply ->
            saveMessage(reply, isUser = false, isVoice = isVoice)
        }

        return result
    }

    suspend fun explainPhoto(
        bitmap: Bitmap,
        userPrompt: String = "Explain what is in this photo",
        isVoice: Boolean = true
    ): Result<String> {
        val displayPrompt = if (userPrompt.isNotBlank()) userPrompt else "फ़ोटो समझाइए"
        saveMessage("📷 [Photo Capture] $displayPrompt", isUser = true, isVoice = isVoice)

        val pastEntities = database.messageDao().getAllMessages().firstOrNull() ?: emptyList()
        val history = pastEntities.takeLast(6).map { Pair(it.text, it.isUser) }

        val pastMemories = database.memoryDao().getAllMemories().firstOrNull() ?: emptyList()
        val memoryContext = pastMemories.joinToString("\n") { "- ${it.key}: ${it.value}" }

        val enrichedPrompt = """
The user captured a photo using their device camera.
User query: "$displayPrompt"
Please analyze what is in this photo.
Explain the main subject, objects, scene, or text visible in the picture clearly, concisely, and naturally in spoken Hindi and English.
""".trimIndent()

        val result = GeminiApiClient.generateMultimodalResponse(
            prompt = enrichedPrompt,
            imageBitmap = bitmap,
            conversationHistory = history,
            memoryContext = memoryContext
        )

        result.onSuccess { reply ->
            saveMessage(reply, isUser = false, isVoice = isVoice)
        }

        return result
    }

    suspend fun explainLiveCamFrame(
        bitmap: Bitmap,
        userPrompt: String = "What is visible through the camera right now?",
        isVoice: Boolean = true
    ): Result<String> {
        val displayPrompt = if (userPrompt.isNotBlank()) userPrompt else "कैमरे के सामने क्या है?"
        saveMessage("👁️ [Live Voice Cam] $displayPrompt", isUser = true, isVoice = isVoice)

        val pastEntities = database.messageDao().getAllMessages().firstOrNull() ?: emptyList()
        val history = pastEntities.takeLast(6).map { Pair(it.text, it.isUser) }

        val pastMemories = database.memoryDao().getAllMemories().firstOrNull() ?: emptyList()
        val memoryContext = pastMemories.joinToString("\n") { "- ${it.key}: ${it.value}" }

        val enrichedPrompt = """
The user is pointing their Live Voice Camera at an object, environment, document, book, screen, or code.
User question / command: "$displayPrompt"
You are Priya, the user's omni-capable multimodal AI companion.
1. Directly inspect what the camera is seeing.
2. If it is programming code or error message: analyze the code, identify bugs, explain what is wrong, and provide the exact fix.
3. If it is math or problem-solving: solve the problem step-by-step.
4. If it is an object, scene, document, sign, or item: describe it clearly and answer the user's question directly.
5. Provide a helpful, concise, spoken-friendly answer in natural Hindi/Hinglish (or English if prompted).
""".trimIndent()

        val result = GeminiApiClient.generateMultimodalResponse(
            prompt = enrichedPrompt,
            imageBitmap = bitmap,
            conversationHistory = history,
            memoryContext = memoryContext
        )

        result.onSuccess { reply ->
            saveMessage(reply, isUser = false, isVoice = isVoice)
        }

        return result
    }

    private suspend fun checkAndExtractMemory(userPrompt: String, reply: String) {
        val lower = userPrompt.lowercase()
        if (lower.contains("remember that") || lower.contains("याद रखना") || lower.contains("my name is") || lower.contains("मेरा नाम") || lower.contains("मेरा पसंदीदा")) {
            saveMemory(
                key = "User Preference",
                value = userPrompt,
                category = "Personal"
            )
        }
    }

    suspend fun saveMemory(key: String, value: String, category: String = "General"): Long {
        return database.memoryDao().insertMemory(
            MemoryEntity(
                key = key,
                value = value,
                category = category,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteMemory(id: Long) {
        database.memoryDao().deleteMemoryById(id)
    }

    suspend fun clearHistory() {
        database.messageDao().clearAllMessages()
    }

    suspend fun clearAllMemories() {
        database.memoryDao().clearAllMemories()
    }
}

private fun MessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = id,
        text = text,
        isUser = isUser,
        timestamp = timestamp,
        language = language,
        isVoice = isVoice
    )
}

private fun MemoryEntity.toMayaMemory(): MayaMemory {
    return MayaMemory(
        id = id,
        key = key,
        value = value,
        category = category,
        timestamp = timestamp
    )
}
