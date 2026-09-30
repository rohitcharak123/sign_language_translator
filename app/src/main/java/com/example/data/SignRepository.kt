package com.example.data

import kotlinx.coroutines.flow.Flow

class SignRepository(private val signDao: SignDao) {

    fun getSignsForLanguage(language: String): Flow<List<SignEntity>> =
        signDao.getSignsByLanguage(language)

    fun getSignsByCategory(language: String, category: String): Flow<List<SignEntity>> =
        signDao.getSignsByCategory(language, category)

    fun searchSigns(query: String): Flow<List<SignEntity>> =
        signDao.searchSigns(query)

    fun getCustomSigns(): Flow<List<SignEntity>> =
        signDao.getCustomSigns()

    fun getTranslationHistory(): Flow<List<TranslationHistoryEntity>> =
        signDao.getTranslationHistory()

    suspend fun insertSign(sign: SignEntity): Long =
        signDao.insertSign(sign)

    suspend fun recordTranslation(
        sourceMode: String,
        rawGlosses: String,
        translatedSentence: String,
        signLanguage: String,
        emotion: String,
        confidence: Float
    ): Long {
        return signDao.insertHistory(
            TranslationHistoryEntity(
                sourceMode = sourceMode,
                rawGlosses = rawGlosses,
                translatedSentence = translatedSentence,
                signLanguage = signLanguage,
                emotion = emotion,
                confidence = confidence
            )
        )
    }

    suspend fun incrementSignUsage(gloss: String, language: String) =
        signDao.incrementUsage(gloss, language)

    suspend fun deleteCustomSign(id: Long) =
        signDao.deleteCustomSign(id)

    suspend fun clearHistory() =
        signDao.clearHistory()
}
