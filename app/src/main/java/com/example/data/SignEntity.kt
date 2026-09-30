package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signs")
data class SignEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gloss: String,
    val translation: String,
    val category: String, // "Greetings", "Emergency", "Health", "Basics", "Questions", "Numbers"
    val signLanguage: String, // ASL, ISL, BSL, JSL
    val datasetSource: String, // WLASL, INCLUDE, MS-ASL, PHOENIX14T, Custom
    val description: String,
    val handShapeHint: String,
    val isCustom: Boolean = false,
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "translation_history")
data class TranslationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceMode: String, // "SIGN_TO_SPEECH" or "SPEECH_TO_SIGN"
    val rawGlosses: String,
    val translatedSentence: String,
    val signLanguage: String,
    val emotion: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)
