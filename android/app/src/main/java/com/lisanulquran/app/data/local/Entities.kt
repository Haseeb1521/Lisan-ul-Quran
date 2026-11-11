package com.lisanulquran.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: Int,
    val nameArabic: String,
    val nameSimple: String,
    val nameTranslated: String,
    val versesCount: Int
)

@Entity(tableName = "ayahs", primaryKeys = ["surah", "ayah"])
data class AyahEntity(
    val surah: Int,
    val ayah: Int,
    val arabic: String,
    val urdu: String,
    val english: String
)

@Entity(
    tableName = "words",
    indices = [Index("arabic"), Index(value = ["surah", "ayah"])]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surah: Int,
    val ayah: Int,
    val position: Int,
    val arabic: String,
    val translit: String,
    val en: String,
    val ur: String
)

@Entity(tableName = "tafsir", primaryKeys = ["surah", "ayah"])
data class TafsirEntity(
    val surah: Int,
    val ayah: Int,
    val text: String
)

data class VocabRow(
    val arabic: String,
    val translit: String,
    val en: String,
    val ur: String,
    val freq: Int
)

fun meaningOf(en: String, ur: String, lang: String): String =
    if (lang == "ur") ur.ifBlank { en } else en.ifBlank { ur }

@Dao
interface QuranDao {
    @Query("SELECT COUNT(*) FROM ayahs")
    suspend fun ayahCount(): Int

    @Query("SELECT * FROM chapters ORDER BY id")
    fun chapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun chapter(id: Int): ChapterEntity?

    @Query("SELECT * FROM ayahs WHERE surah = :surah ORDER BY ayah")
    fun ayahs(surah: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM words WHERE surah = :surah ORDER BY ayah, position")
    fun words(surah: Int): Flow<List<WordEntity>>

    @Query("SELECT * FROM tafsir WHERE surah = :surah")
    fun tafsir(surah: Int): Flow<List<TafsirEntity>>

    @Query("SELECT * FROM ayahs ORDER BY surah, ayah LIMIT 1 OFFSET :offset")
    suspend fun ayahAt(offset: Int): AyahEntity?

    @Query("SELECT * FROM words WHERE surah = :surah AND ayah = :ayah ORDER BY position")
    suspend fun wordsOf(surah: Int, ayah: Int): List<WordEntity>

    @Query("SELECT * FROM tafsir WHERE surah = :surah AND ayah <= :ayah ORDER BY ayah DESC LIMIT 1")
    suspend fun tafsirFor(surah: Int, ayah: Int): TafsirEntity?

    @Query(
        """SELECT arabic, MAX(translit) AS translit, MAX(en) AS en, MAX(ur) AS ur, COUNT(*) AS freq
           FROM words
           WHERE :q = '' OR en LIKE '%' || :q || '%' OR ur LIKE '%' || :q || '%'
                 OR translit LIKE '%' || :q || '%' OR arabic LIKE '%' || :q || '%'
           GROUP BY arabic
           ORDER BY freq DESC, arabic"""
    )
    fun vocab(q: String): Flow<List<VocabRow>>

    @Query(
        """SELECT arabic, MAX(translit) AS translit, MAX(en) AS en, MAX(ur) AS ur, COUNT(*) AS freq
           FROM words
           GROUP BY arabic
           HAVING COUNT(*) >= 30 AND MAX(en) != ''
           ORDER BY RANDOM()
           LIMIT :n"""
    )
    suspend fun randomVocab(n: Int): List<VocabRow>

    @Query(
        """SELECT arabic, MAX(translit) AS translit, MAX(en) AS en, MAX(ur) AS ur, COUNT(*) AS freq
           FROM words
           WHERE arabic IN (:list)
           GROUP BY arabic
           ORDER BY freq DESC"""
    )
    suspend fun vocabByArabic(list: List<String>): List<VocabRow>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(list: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(list: List<AyahEntity>)

    @Insert
    suspend fun insertWords(list: List<WordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTafsir(list: List<TafsirEntity>)
}
