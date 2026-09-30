package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.model.EmotionType
import com.example.model.SignLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiSignTranslator {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Translates a sequence of sign glosses into a grammatically natural, fluent spoken sentence.
     * Incorporates non-manual signals and emotional markers.
     */
    suspend fun refineSignGlossesToSpeech(
        glosses: List<String>,
        signLanguage: SignLanguage,
        emotion: EmotionType
    ): String = withContext(Dispatchers.IO) {
        if (glosses.isEmpty()) return@withContext ""

        val rawGlossText = glosses.joinToString(" ")
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // If no API key or placeholder, use intelligent offline rule-based translation
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext offlineHeuristicGlossToSentence(glosses, signLanguage, emotion)
        }

        val prompt = """
            You are SignBridge AI, a certified professional Sign Language interpreter for ${signLanguage.displayName} (${signLanguage.code}).
            Grammar structure of ${signLanguage.code}: ${signLanguage.grammarStructure}.
            
            Input sign glosses: "$rawGlossText"
            Signer's detected facial emotion / non-manual marker: ${emotion.displayName} (${emotion.toneDescription})
            
            Translate these sign language glosses into a single natural, fluent spoken English sentence that a hearing person will understand.
            Incorporate the emotional tone naturally (e.g., if Inquiring, frame as a polite question; if Urgent, convey urgency).
            Return ONLY the final translated sentence, without any explanations or quotation marks.
        """.trimIndent()

        try {
            val responseText = executeGeminiPrompt(apiKey, prompt)
            if (responseText.isNotBlank() && !responseText.startsWith("Error:")) {
                responseText.trim().trim('"', '\'')
            } else {
                offlineHeuristicGlossToSentence(glosses, signLanguage, emotion)
            }
        } catch (e: Exception) {
            Log.w("GeminiSignTranslator", "Falling back to offline translation", e)
            offlineHeuristicGlossToSentence(glosses, signLanguage, emotion)
        }
    }

    /**
     * Converts spoken/written text into target sign language gloss sequence for the 3D Avatar.
     */
    suspend fun convertSpeechToSignGlosses(
        spokenText: String,
        targetLanguage: SignLanguage
    ): List<String> = withContext(Dispatchers.IO) {
        if (spokenText.isBlank()) return@withContext emptyList()

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext offlineSentenceToGlosses(spokenText, targetLanguage)
        }

        val prompt = """
            You are SignBridge AI. Convert this spoken English sentence into an optimal uppercase sign language gloss sequence for ${targetLanguage.displayName} (${targetLanguage.code}).
            Target sign syntax: ${targetLanguage.grammarStructure}.
            
            Spoken sentence: "$spokenText"
            
            Return ONLY a comma-separated list of uppercase sign glosses suitable for animation (e.g. HELLO, HOW, YOU).
        """.trimIndent()

        try {
            val responseText = executeGeminiPrompt(apiKey, prompt)
            val glosses = responseText.split(",", "\n")
                .map { it.trim().uppercase().replace(Regex("[^A-Z0-9 ?]"), "") }
                .filter { it.isNotBlank() }

            if (glosses.isNotEmpty()) glosses else offlineSentenceToGlosses(spokenText, targetLanguage)
        } catch (e: Exception) {
            offlineSentenceToGlosses(spokenText, targetLanguage)
        }
    }

    /**
     * Cross-translates between sign languages (e.g. ASL -> ISL, BSL -> ASL)
     */
    suspend fun crossTranslateSignLanguage(
        sourceGloss: String,
        sourceLang: SignLanguage,
        targetLang: SignLanguage
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Pair(
                sourceGloss,
                "Direct cross-translation from ${sourceLang.code} to ${targetLang.code}"
            )
        }

        val prompt = """
            Sign language cross-translation:
            Source language: ${sourceLang.displayName} (${sourceLang.code})
            Target language: ${targetLang.displayName} (${targetLang.code})
            Source sign gloss: "$sourceGloss"
            
            Provide:
            1. Equivalent gloss in ${targetLang.code}
            2. Brief cultural or grammatical note (e.g., two-handed vs one-handed, spatial marker)
            Format as JSON: {"targetGloss": "...", "note": "..."}
        """.trimIndent()

        try {
            val responseText = executeGeminiPrompt(apiKey, prompt)
            val jsonStart = responseText.indexOf('{')
            val jsonEnd = responseText.lastIndexOf('}')
            if (jsonStart != -1 && jsonEnd != -1) {
                val json = JSONObject(responseText.substring(jsonStart, jsonEnd + 1))
                val targetGloss = json.optString("targetGloss", sourceGloss)
                val note = json.optString("note", "Cross-lingual sign equivalent")
                Pair(targetGloss, note)
            } else {
                Pair(sourceGloss, "Mapped to ${targetLang.code}")
            }
        } catch (e: Exception) {
            Pair(sourceGloss, "Direct mapping (${sourceLang.code} -> ${targetLang.code})")
        }
    }

    private fun executeGeminiPrompt(apiKey: String, prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("topP", 0.9)
            })
        }

        val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            return "Error: HTTP ${response.code}"
        }

        val jsonResponse = JSONObject(responseBody)
        val candidates = jsonResponse.optJSONArray("candidates") ?: return ""
        val firstCandidate = candidates.optJSONObject(0) ?: return ""
        val content = firstCandidate.optJSONObject("content") ?: return ""
        val parts = content.optJSONArray("parts") ?: return ""
        val firstPart = parts.optJSONObject(0) ?: return ""
        return firstPart.optString("text", "")
    }

    /**
     * Offline rule-based NLP to reconstruct sign glosses into fluent English sentences.
     */
    private fun offlineHeuristicGlossToSentence(
        glosses: List<String>,
        signLanguage: SignLanguage,
        emotion: EmotionType
    ): String {
        val upperGlosses = glosses.map { it.uppercase().trim() }

        // Specific high-frequency idiomatic combinations
        if (upperGlosses.contains("HELP")) {
            return if (emotion == EmotionType.URGENCY) {
                "Emergency! I need help immediately!"
            } else {
                "Can you please help me?"
            }
        }
        if (upperGlosses.contains("I AM DEAF") || (upperGlosses.contains("ME") && upperGlosses.contains("DEAF"))) {
            return "I am deaf and use sign language to communicate."
        }
        if (upperGlosses.contains("HELLO") && upperGlosses.contains("THANK YOU")) {
            return "Hello! Thank you very much."
        }
        if (upperGlosses.contains("WHERE") && upperGlosses.contains("DOCTOR")) {
            return "Where is the nearest doctor or clinic?"
        }
        if (upperGlosses.contains("WHERE")) {
            val otherWords = upperGlosses.filter { it != "WHERE" }.joinToString(" ").lowercase()
            return if (otherWords.isNotBlank()) "Where can I find the $otherWords?" else "Where is it located?"
        }
        if (upperGlosses.contains("WATER") && upperGlosses.contains("PLEASE")) {
            return "May I please have some water?"
        }

        // Generic grammar reconstruction
        val words = glosses.map { gloss ->
            when (gloss.uppercase()) {
                "ME" -> "I"
                "YOU" -> "you"
                "GO" -> "am going to"
                "WANT" -> "would like"
                "EAT" -> "eat food"
                "STORE" -> "the store"
                "HOME" -> "home"
                "HELLO" -> "Hello"
                "THANK YOU" -> "Thank you"
                "PLEASE" -> "Please"
                "YES" -> "Yes"
                "NO" -> "No"
                else -> gloss.lowercase()
            }
        }

        var sentence = words.joinToString(" ").replaceFirstChar { it.uppercase() }
        val punctuation = when (emotion) {
            EmotionType.INQUIRING -> "?"
            EmotionType.URGENCY, EmotionType.EMPHATIC -> "!"
            else -> "."
        }

        if (!sentence.endsWith(".") && !sentence.endsWith("?") && !sentence.endsWith("!")) {
            sentence += punctuation
        }
        return sentence
    }

    /**
     * Offline tokenizer from spoken text to sign language glosses.
     */
    private fun offlineSentenceToGlosses(spokenText: String, targetLanguage: SignLanguage): List<String> {
        val clean = spokenText.uppercase()
            .replace(Regex("[.,?!;:'\"]"), "")
            .split(" ")
            .filter { it.isNotBlank() }

        val stopWords = setOf("IS", "AM", "ARE", "THE", "A", "AN", "TO", "OF", "IN", "ON", "AT")
        val filtered = clean.filter { it !in stopWords }

        return if (filtered.isNotEmpty()) filtered else clean
    }
}
