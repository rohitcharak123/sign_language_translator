package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SignDao {
    @Query("SELECT * FROM signs WHERE signLanguage = :language ORDER BY usageCount DESC, gloss ASC")
    fun getSignsByLanguage(language: String): Flow<List<SignEntity>>

    @Query("SELECT * FROM signs WHERE signLanguage = :language AND category = :category ORDER BY gloss ASC")
    fun getSignsByCategory(language: String, category: String): Flow<List<SignEntity>>

    @Query("SELECT * FROM signs WHERE gloss LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%'")
    fun searchSigns(query: String): Flow<List<SignEntity>>

    @Query("SELECT * FROM signs WHERE isCustom = 1 ORDER BY createdAt DESC")
    fun getCustomSigns(): Flow<List<SignEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSign(sign: SignEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(signs: List<SignEntity>)

    @Update
    suspend fun updateSign(sign: SignEntity)

    @Query("UPDATE signs SET usageCount = usageCount + 1 WHERE gloss = :gloss AND signLanguage = :language")
    suspend fun incrementUsage(gloss: String, language: String)

    @Query("DELETE FROM signs WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomSign(id: Long)

    @Query("SELECT COUNT(*) FROM signs")
    suspend fun getSignCount(): Int

    // Translation History
    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC LIMIT 100")
    fun getTranslationHistory(): Flow<List<TranslationHistoryEntity>>

    @Insert
    suspend fun insertHistory(history: TranslationHistoryEntity): Long

    @Query("DELETE FROM translation_history")
    suspend fun clearHistory()
}
