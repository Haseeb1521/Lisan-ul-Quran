package com.lisanulquran.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lisanulquran.app.data.tr
import com.lisanulquran.app.ui.components.AppCard
import com.lisanulquran.app.ui.components.ScreenTitle
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.Emerald
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.Muted

private class Example(val ar: String, val hi: String, val en: String)

private class Lesson(
    val titleHi: String,
    val titleEn: String,
    val bodyHi: String,
    val bodyEn: String,
    val examples: List<Example>
)

private val lessons = listOf(
    Lesson(
        "Ism (اسم) - Noun", "Ism (اسم) - Noun",
        "Ism woh lafz hai jo kisi insan, jagah, cheez ya khayal ka naam batata hai aur usme waqt shamil nahi hota.",
        "An ism is a word that names a person, place, thing or idea, and it does not carry a tense.",
        listOf(Example("كِتَاب", "kitab", "book"), Example("رَجُل", "aadmi", "man"), Example("عِلْم", "ilm", "knowledge"))
    ),
    Lesson(
        "Fi'l (فعل) - Verb", "Fi'l (فعل) - Verb",
        "Fi'l woh lafz hai jo kaam ko waqt ke saath batata hai. Teen qismein hain: Madi (guzra hua), Mudari' (maujooda/aane wala), Amr (hukm).",
        "A fi'l is a word that tells an action together with its time. There are three kinds: Madi (past), Mudari' (present/future) and Amr (command).",
        listOf(
            Example("كَتَبَ", "usne likha (Madi)", "he wrote (past)"),
            Example("يَكْتُبُ", "wo likhta hai (Mudari')", "he writes (present)"),
            Example("اكْتُبْ", "likho (Amr)", "write! (command)")
        )
    ),
    Lesson(
        "Harf (حرف) - Particle", "Harf (حرف) - Particle",
        "Harf woh lafz hai jiska matlab akele samajh nahi aata, doosre lafz ke saath milkar poora hota hai.",
        "A harf is a word whose meaning is not complete on its own; it only makes sense together with another word.",
        listOf(
            Example("فِي", "mein", "in"), Example("مِنْ", "se", "from"),
            Example("إِلَى", "tak, ki taraf", "to, towards"), Example("عَلَى", "par", "on")
        )
    ),
    Lesson(
        "Mudhakkar aur Mu'annath", "Masculine and Feminine",
        "Arabic mein har ism mard (mudhakkar) ya aurat (mu'annath) hota hai. Aksar mu'annath ke aakhir mein ة (ta marbuta) hoti hai.",
        "Every Arabic noun is masculine or feminine. A feminine noun usually ends with ة (ta marbuta).",
        listOf(Example("مُعَلِّم", "ustaad (mard)", "teacher (male)"), Example("مُعَلِّمَة", "ustaani (aurat)", "teacher (female)"))
    ),
    Lesson(
        "Mufrad, Tathniya, Jam'", "Singular, Dual, Plural",
        "Ek cheez ke liye mufrad, do ke liye tathniya (aakhir mein انِ ya يْنِ), aur teen ya zyada ke liye jam'.",
        "Mufrad is for one thing, tathniya for two (ending in انِ or يْنِ), and jam' for three or more.",
        listOf(Example("كِتَاب", "ek kitab", "one book"), Example("كِتَابَانِ", "do kitabein", "two books"), Example("كُتُب", "kai kitabein", "many books"))
    ),
    Lesson(
        "Ma'rifa aur Nakira", "Definite and Indefinite",
        "Jab ism ke shuru mein ال lag jaye to wo khaas (ma'rifa) ho jata hai. Bina ال ke aam (nakira) hota hai.",
        "When ال is added at the start of a noun it becomes definite (ma'rifa). Without ال it is indefinite (nakira).",
        listOf(Example("كِتَاب", "ek kitab", "a book"), Example("الْكِتَاب", "wo (khaas) kitab", "the book"))
    ),
    Lesson(
        "Dameer (ضمير) - Pronouns", "Dameer (ضمير) - Pronouns",
        "Dameer woh lafz hai jo ism ki jagah aata hai.",
        "A dameer is a word that takes the place of a noun.",
        listOf(
            Example("هُوَ", "wo (mard)", "he"), Example("هِيَ", "wo (aurat)", "she"),
            Example("أَنَا", "main", "I"), Example("نَحْنُ", "hum", "we"), Example("أَنْتَ", "tum (mard)", "you (m.)")
        )
    ),
    Lesson(
        "Jumla Ismiyya", "Nominal Sentence (Jumla Ismiyya)",
        "Aisa jumla jo ism se shuru ho. Isme mubtada (jis ke baare mein baat ho) aur khabar (jo baat batayi jaye) hoti hai.",
        "A sentence that starts with a noun. It has a mubtada (the subject being talked about) and a khabar (what is said about it).",
        listOf(
            Example("اللَّهُ غَفُورٌ", "Allah bakhshne wala hai", "Allah is Forgiving"),
            Example("الْكِتَابُ جَدِيدٌ", "kitab nayi hai", "the book is new")
        )
    ),
    Lesson(
        "Harf-e-Jarr", "Prepositions (Huruf al-Jarr)",
        "Yeh harf apne baad wale ism ko jarr (zer) dete hain.",
        "These particles put the noun after them in the jarr (kasra) case.",
        listOf(
            Example("بِ", "ke saath, se", "with, by"), Example("لِ", "ke liye", "for"),
            Example("فِي", "mein", "in"), Example("مِنْ", "se", "from"), Example("عَنْ", "se (door/baare mein)", "about, away from")
        )
    )
)

@Composable
fun GrammarScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Gold)
            }
            ScreenTitle("Arabic Grammar", tr("Quran samajhne ki buniyaad", "The foundation for understanding the Quran"))
        }
        lessons.forEach { lesson ->
            AppCard(modifier = Modifier.padding(vertical = 5.dp)) {
                Text(tr(lesson.titleHi, lesson.titleEn), style = MaterialTheme.typography.titleMedium, color = Gold)
                Spacer(Modifier.height(6.dp))
                Text(tr(lesson.bodyHi, lesson.bodyEn), color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                lesson.examples.forEach { ex ->
                    Text("• ${ex.ar} = ${tr(ex.hi, ex.en)}", color = Emerald, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
