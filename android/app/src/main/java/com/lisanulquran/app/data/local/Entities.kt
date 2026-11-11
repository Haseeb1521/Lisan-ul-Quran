package com.lisanulquran.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val arabic: String,
    val meaning: String
)

@Entity(tableName = "grammar")
data class GrammarEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val explanation: String
)

@Entity(tableName = "ayahs")
data class AyahEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surah: Int,
    val ayah: Int,
    val arabic: String,
    val translation: String
)

@Dao
interface QuranDao {
    @Query("SELECT * FROM words")
    fun getWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM grammar")
    fun getGrammar(): Flow<List<GrammarEntity>>

    @Query("SELECT * FROM ayahs")
    fun getAyahs(): Flow<List<AyahEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrammar(items: List<GrammarEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)
}
