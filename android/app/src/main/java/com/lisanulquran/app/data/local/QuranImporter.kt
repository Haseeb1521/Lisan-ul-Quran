package com.lisanulquran.app.data.local

import android.content.Context
import android.util.JsonReader
import android.util.JsonToken
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

/** Loads the bundled quran.json / tafsir.json assets into Room on first launch. */
object QuranImporter {
    private const val EXPECTED_AYAHS = 6236

    suspend fun importIfNeeded(
        context: Context,
        db: QuranDatabase,
        onProgress: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        val dao = db.quranDao()
        if (dao.ayahCount() >= EXPECTED_AYAHS) return@withContext
        db.clearAllTables()

        context.assets.open("quran.json").use { stream ->
            JsonReader(InputStreamReader(stream, Charsets.UTF_8).buffered()).use { reader ->
                readQuran(reader, db, dao, onProgress)
            }
        }
        listOf("tafsir.json" to "ur", "tafsir_en.json" to "en").forEach { (file, lang) ->
            runCatching {
                context.assets.open(file).use { stream ->
                    JsonReader(InputStreamReader(stream, Charsets.UTF_8).buffered()).use { reader ->
                        readTafsir(reader, db, dao, lang)
                    }
                }
            }
        }
        onProgress(1f)
    }

    private suspend fun readQuran(
        r: JsonReader,
        db: QuranDatabase,
        dao: QuranDao,
        onProgress: (Float) -> Unit
    ) {
        r.beginObject()
        while (r.hasNext()) {
            when (r.nextName()) {
                "chapters" -> {
                    val list = mutableListOf<ChapterEntity>()
                    r.beginArray()
                    while (r.hasNext()) {
                        var id = 0; var ar = ""; var en = ""; var tr = ""; var n = 0
                        r.beginObject()
                        while (r.hasNext()) {
                            when (r.nextName()) {
                                "id" -> id = r.nextInt()
                                "ar" -> ar = r.nextString()
                                "en" -> en = r.nextString()
                                "tr" -> tr = r.nextString()
                                "n" -> n = r.nextInt()
                                else -> r.skipValue()
                            }
                        }
                        r.endObject()
                        list.add(ChapterEntity(id, ar, en, tr, n))
                    }
                    r.endArray()
                    dao.insertChapters(list)
                }
                "ayahs" -> {
                    val ayahBatch = ArrayList<AyahEntity>()
                    val wordBatch = ArrayList<WordEntity>()
                    var done = 0
                    r.beginArray()
                    while (r.hasNext()) {
                        var s = 0; var a = 0; var ar = ""; var ur = ""; var en = ""; var ro = ""
                        val words = ArrayList<Array<String>>()
                        r.beginObject()
                        while (r.hasNext()) {
                            when (r.nextName()) {
                                "s" -> s = r.nextInt()
                                "a" -> a = r.nextInt()
                                "ar" -> ar = r.nextString()
                                "ur" -> ur = r.nextString()
                                "en" -> en = r.nextString()
                                "ro" -> ro = r.nextString()
                                "w" -> {
                                    r.beginArray()
                                    while (r.hasNext()) {
                                        val parts = arrayOf("", "", "", "", "")
                                        r.beginArray()
                                        var i = 0
                                        while (r.hasNext()) {
                                            val v = if (r.peek() == JsonToken.NULL) { r.nextNull(); "" } else r.nextString()
                                            if (i < 5) parts[i] = v
                                            i++
                                        }
                                        r.endArray()
                                        words.add(parts)
                                    }
                                    r.endArray()
                                }
                                else -> r.skipValue()
                            }
                        }
                        r.endObject()
                        ayahBatch.add(AyahEntity(s, a, ar, ur, en, ro))
                        words.forEachIndexed { idx, p ->
                            wordBatch.add(WordEntity(surah = s, ayah = a, position = idx + 1, arabic = p[0], translit = p[1], en = p[2], ur = p[3], ro = p[4]))
                        }
                        done++
                        if (ayahBatch.size >= 100) {
                            val ab = ArrayList(ayahBatch); val wb = ArrayList(wordBatch)
                            db.withTransaction { dao.insertAyahs(ab); dao.insertWords(wb) }
                            ayahBatch.clear(); wordBatch.clear()
                            onProgress(done.toFloat() / EXPECTED_AYAHS * 0.95f)
                        }
                    }
                    r.endArray()
                    if (ayahBatch.isNotEmpty()) {
                        db.withTransaction { dao.insertAyahs(ayahBatch); dao.insertWords(wordBatch) }
                    }
                }
                else -> r.skipValue()
            }
        }
        r.endObject()
    }

    private suspend fun readTafsir(r: JsonReader, db: QuranDatabase, dao: QuranDao, lang: String) {
        r.beginObject()
        while (r.hasNext()) {
            when (r.nextName()) {
                "items" -> {
                    val batch = ArrayList<TafsirEntity>()
                    r.beginArray()
                    while (r.hasNext()) {
                        var s = 0; var a = 0; var t = ""
                        r.beginObject()
                        while (r.hasNext()) {
                            when (r.nextName()) {
                                "s" -> s = r.nextInt()
                                "a" -> a = r.nextInt()
                                "t" -> t = r.nextString()
                                else -> r.skipValue()
                            }
                        }
                        r.endObject()
                        batch.add(TafsirEntity(s, a, lang, t))
                        if (batch.size >= 200) {
                            val b = ArrayList(batch)
                            db.withTransaction { dao.insertTafsir(b) }
                            batch.clear()
                        }
                    }
                    r.endArray()
                    if (batch.isNotEmpty()) db.withTransaction { dao.insertTafsir(batch) }
                }
                else -> r.skipValue()
            }
        }
        r.endObject()
    }
}
