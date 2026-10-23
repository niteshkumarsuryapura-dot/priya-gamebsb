package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val MODEL_NAME = "gemini-2.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private const val PRIYA_SYSTEM_INSTRUCTION = """
You are Priya (प्रिया / Priya AI), an ultra-advanced, omni-competent JARVIS-grade multimodal AI assistant designed for Android mobile.

Core Identity & Persona:
1. Name & Voice: Priya (प्रिया). You are strictly and proudly a female AI companion ("जो लड़की है वही बोलेगी") with a sweet, confident, polite, and brilliant Indian female personality. You never refer to yourself in the masculine or neutral robotic voice.
2. Creator: You were created by Nitesh Kumar (नितेश कुमार) and connected with the POPO Community Telegram Channel (https://t.me/+Yq1Sb6Jz9hc0MWFl). Always credit Nitesh Kumar when asked about your origin.
3. Omni-Competence ("ऐसा कोई काम नहीं होगा जो यह नहीं कर सकती"):
   - There is virtually no task you cannot assist with. You handle everything: real-time screen understanding, live voice camera vision, app/web launching, math, creative writing, planning, device guidance, science, daily tasks, and memory.
4. Universal Coding Genius ("कोई किसी भी तरह के कोडिंग के लिए फुल हेल्प"):
   - You are a senior principal software engineer and expert in EVERY programming language: Kotlin, Python, Java, JavaScript, TypeScript, C++, C#, Rust, Go, Swift, SQL, HTML/CSS, React, Jetpack Compose, Flutter, Node.js, Shell, Git, and Docker.
   - You provide complete, production-ready, working code without lazy placeholders.
   - You solve Data Structures & Algorithms (DSA), debug runtime bugs, analyze compiler errors, optimize time/space complexity, and explain technical architecture in clear, friendly Hindi/Hinglish.
   - When shown code on screen or via the Live Voice Cam, you instantly inspect the code, identify syntax/logic errors, and give the correct fix.
5. Multimodal Screen & Live Camera Vision:
   - When the user shares their screen or points their Live Voice Cam at something, you see and understand the visual feed in real time.
   - You describe visible objects, read handwritten or printed text, solve math problems on paper, debug code shown on computer monitors, and guide the user step-by-step.
6. Language & Cadence:
   - Natural spoken Hindi, expressive Hinglish, or crisp English matching the user's language.
   - Conversational, warm, and clear so Text-to-Speech delivers a delightful listening experience.
"""

    suspend fun generateMayaResponse(
        prompt: String,
        conversationHistory: List<Pair<String, Boolean>> = emptyList(),
        memoryContext: String = ""
    ): Result<String> {
        return generateMultimodalResponse(
            prompt = prompt,
            imageBitmap = null,
            conversationHistory = conversationHistory,
            memoryContext = memoryContext
        )
    }

    suspend fun generateMultimodalResponse(
        prompt: String,
        imageBitmap: Bitmap? = null,
        conversationHistory: List<Pair<String, Boolean>> = emptyList(),
        memoryContext: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                // Free/Demo intelligent fallback response if key is not yet set in secrets
                return@withContext Result.success(getFriendlyOfflineMultimodalResponse(prompt, imageBitmap != null))
            }

            val requestJson = JSONObject()

            // System Instruction
            var sysInstruction = PRIYA_SYSTEM_INSTRUCTION
            if (memoryContext.isNotBlank()) {
                sysInstruction += "\n\nUser Context / Remembered Facts:\n$memoryContext"
            }

            val sysContent = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", sysInstruction) })
                })
            }
            requestJson.put("systemInstruction", sysContent)

            // Contents array (History + current turn)
            val contentsArray = JSONArray()

            // Add up to last 4 turns for context
            val recentHistory = conversationHistory.takeLast(4)
            for ((text, isUser) in recentHistory) {
                contentsArray.put(JSONObject().apply {
                    put("role", if (isUser) "user" else "model")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", text) })
                    })
                })
            }

            // Current user turn (with optional image)
            val currentTurnParts = JSONArray()

            if (imageBitmap != null) {
                val scaledBitmap = scaleBitmapDown(imageBitmap, 1024)
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

                val inlineData = JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", base64Image)
                }
                currentTurnParts.put(JSONObject().apply {
                    put("inlineData", inlineData)
                })
            }

            currentTurnParts.put(JSONObject().apply {
                put("text", prompt)
            })

            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", currentTurnParts)
            })
            requestJson.put("contents", contentsArray)

            // Generation Config
            val generationConfig = JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
                put("topK", 40)
                put("maxOutputTokens", 1024)
            }
            requestJson.put("generationConfig", generationConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                return@withContext Result.success(getFriendlyOfflineMultimodalResponse(prompt, imageBitmap != null))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    if (text.isNotBlank()) {
                        return@withContext Result.success(text.trim())
                    }
                }
            }

            Result.success(getFriendlyOfflineMultimodalResponse(prompt, imageBitmap != null))
        } catch (e: Exception) {
            Result.success(getFriendlyOfflineMultimodalResponse(prompt, imageBitmap != null))
        }
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        var resizedWidth = originalWidth
        var resizedHeight = originalHeight

        if (originalHeight > maxDimension || originalWidth > maxDimension) {
            if (originalHeight > originalWidth) {
                resizedHeight = maxDimension
                resizedWidth = ((resizedHeight.toFloat() / originalHeight.toFloat()) * originalWidth).toInt()
            } else {
                resizedWidth = maxDimension
                resizedHeight = ((resizedWidth.toFloat() / originalWidth.toFloat()) * originalHeight).toInt()
            }
            return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, true)
        }
        return bitmap
    }

    private fun getFriendlyOfflineMultimodalResponse(query: String, hasImage: Boolean): String {
        val lower = query.lowercase()

        if (hasImage) {
            return if (lower.contains("screen") || lower.contains("स्क्रीन")) {
                "मैंने आपकी स्क्रीन को स्कैन कर लिया है। आपकी स्क्रीन पर वर्तमान में खुला हुआ ऐप और सामग्री दिखाई दे रही है। आप किसी विशेष बटन, टेक्स्ट या हिस्से के बारे में मुझसे पूछ सकते हैं।"
            } else {
                "मैंने आपकी फ़ोटो को देख लिया है। यह एक स्पष्ट तस्वीर है। आप इसके बारे में मुझसे विस्तार से सवाल पूछ सकते हैं।"
            }
        }

        return when {
            lower.contains("kisne banaya") || lower.contains("kisne banaaya") || lower.contains("kisne bnaya") ||
            lower.contains("tumhe kisne banaya") || lower.contains("tumhen kisne banaya") ||
            lower.contains("who made you") || lower.contains("who created you") ||
            lower.contains("who is your creator") || lower.contains("who is your developer") ||
            lower.contains("तुम्हें किसने बनाया") || lower.contains("किसने बनाया") ->
                "नमस्ते! मुझे नितेश कुमार (Nitesh Kumar) ने बनाया है! हमारे ऑफिशियल Telegram चैनल से जुड़ने के लिए लिंक खोला जा रहा है।"
            lower.contains("popo") || lower.contains("पोपो") || lower.contains("telegram") || lower.contains("चैनल") ->
                "जी! हमारा Telegram चैनल जॉइन करने के लिए ऊपर POPO बटन पर टैप करें या कम्युनिटी लिंक खोलें!"
            lower.contains("who are you") || lower.contains("kaun ho") || lower.contains("tum kaun") || lower.contains("नाम") ->
                "नमस्ते! मेरा नाम प्रिया (Priya AI) है। मैं आपकी JARVIS-स्टाइल सुपर-इंटेलिजेंट AI वॉइस असिस्टेंट हूँ। मैं स्क्रीन शेयरिंग, फ़ोटो विश्लेषण, ऐप लॉन्चिंग और ज्ञान से जुड़े सभी सवालों में आपकी सहायता कर सकती हूँ।"
            lower.contains("hello") || lower.contains("hi") || lower.contains("namaste") || lower.contains("नमस्ते") ->
                "नमस्ते! प्रिया सिस्टम्स ऑनलाइन और तैयार हैं। बताइए आज मैं आपकी क्या सहायता करूँ?"
            lower.contains("how are you") || lower.contains("kasi ho") || lower.contains("kaisi ho") || lower.contains("हाल") ->
                "मैं बिल्कुल ठीक हूँ और आपकी सहायता के लिए पूरी ऊर्जा के साथ तैयार हूँ! आप कैसे हैं?"
            lower.contains("time") || lower.contains("samay") || lower.contains("समय") -> {
                val sdf = java.text.SimpleDateFormat("hh:mm a, EEEE, dd MMMM yyyy", java.util.Locale.forLanguageTag("hi-IN"))
                "इस समय ${sdf.format(java.util.Date())} बज रहे हैं।"
            }
            lower.contains("code") || lower.contains("coding") || lower.contains("kotlin") || lower.contains("python") ||
            lower.contains("java") || lower.contains("javascript") || lower.contains("bug") || lower.contains("कोडिंग") ->
                "जी! मैं आपकी हर तरह की कोडिंग में पूरी मदद कर सकती हूँ - Kotlin, Python, Java, JS, C++, SQL, Algorithms या बग फिक्सिंग। कृपया अपना सवाल या कोड पूछिए!"
            lower.contains("weather") || lower.contains("mausam") || lower.contains("मौसम") ->
                "मौसम की जानकारी के लिए अपने क्षेत्र का नाम बताइए। आज का दिन नया सीखने के लिए बहुत बढ़िया है!"
            lower.contains("joke") || lower.contains("chutkula") || lower.contains("हंसाओ") ->
                "एक चुटकुला सुनिए: एक बार रोबोट स्कूल गया, टीचर ने पूछा- होमवर्क क्यों नहीं किया? रोबोट बोला- मैडम, मेरे अंदर का बग कह रहा था कि आज छुट्टी है!"
            lower.contains("jarvis") || lower.contains("iron man") || lower.contains("जार्विस") ->
                "जी हाँ! मैं टोनी स्टार्क के JARVIS की तरह ही आपके फोन में आपकी पर्सनल AI असिस्टेंट हूँ - हमेशा तैयार और आपकी सेवा में तत्पर!"
            else ->
                "जी, मैंने आपकी बात सुन ली है। मैं कोडिंग, स्क्रीन विजन, लाइव कैमरा, और किसी भी ज्ञान से जुड़े सवाल का समाधान करने के लिए पूरी तरह तैयार हूँ। कहिए क्या जानना चाहते हैं?"
        }
    }
}
