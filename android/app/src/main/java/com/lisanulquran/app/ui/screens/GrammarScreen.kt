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
import com.lisanulquran.app.ui.components.AppCard
import com.lisanulquran.app.ui.components.ScreenTitle
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.Emerald
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.Muted

private class Lesson(val title: String, val body: String, val examples: List<String>)

private val lessons = listOf(
    Lesson(
        "Ism (اسم) - Noun",
        "Ism woh lafz hai jo kisi insan, jagah, cheez ya khayal ka naam batata hai aur usme waqt shamil nahi hota.",
        listOf("كِتَاب = kitab (kitaab)", "رَجُل = aadmi", "عِلْم = ilm")
    ),
    Lesson(
        "Fi'l (فعل) - Verb",
        "Fi'l woh lafz hai jo kaam ko waqt ke saath batata hai. Teen qismein hain: Madi (guzra hua), Mudari' (maujooda/aane wala), Amr (hukm).",
        listOf("كَتَبَ = usne likha (Madi)", "يَكْتُبُ = wo likhta hai (Mudari')", "اكْتُبْ = likho (Amr)")
    ),
    Lesson(
        "Harf (حرف) - Particle",
        "Harf woh lafz hai jiska matlab akele samajh nahi aata, doosre lafz ke saath milkar poora hota hai.",
        listOf("فِي = mein", "مِنْ = se", "إِلَى = tak, ki taraf", "عَلَى = par")
    ),
    Lesson(
        "Mudhakkar aur Mu'annath",
        "Arabic mein har ism mard (mudhakkar) ya aurat (mu'annath) hota hai. Aksar mu'annath ke aakhir mein ة (ta marbuta) hoti hai.",
        listOf("مُعَلِّم = ustaad (mard)", "مُعَلِّمَة = ustaani (aurat)")
    ),
    Lesson(
        "Mufrad, Tathniya, Jam'",
        "Ek cheez ke liye mufrad, do ke liye tathniya (aakhir mein انِ ya يْنِ), aur teen ya zyada ke liye jam'.",
        listOf("كِتَاب = ek kitab", "كِتَابَانِ = do kitabein", "كُتُب = kai kitabein")
    ),
    Lesson(
        "Ma'rifa aur Nakira",
        "Jab ism ke shuru mein ال lag jaye to wo khaas (ma'rifa) ho jata hai. Bina ال ke aam (nakira) hota hai.",
        listOf("كِتَاب = ek kitab", "الْكِتَاب = wo (khaas) kitab")
    ),
    Lesson(
        "Dameer (ضمير) - Pronouns",
        "Dameer woh lafz hai jo ism ki jagah aata hai.",
        listOf("هُوَ = wo (mard)", "هِيَ = wo (aurat)", "أَنَا = main", "نَحْنُ = hum", "أَنْتَ = tum (mard)")
    ),
    Lesson(
        "Jumla Ismiyya",
        "Aisa jumla jo ism se shuru ho. Isme mubtada (jis ke baare mein baat ho) aur khabar (jo baat batayi jaye) hoti hai.",
        listOf("اللَّهُ غَفُورٌ = Allah bakhshne wala hai", "الْكِتَابُ جَدِيدٌ = kitab nayi hai")
    ),
    Lesson(
        "Harf-e-Jarr",
        "Yeh harf apne baad wale ism ko jarr (zer) dete hain.",
        listOf("بِ = ke saath, se", "لِ = ke liye", "فِي = mein", "مِنْ = se", "عَنْ = se (door/baare mein)")
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
            ScreenTitle("Arabic Grammar", "Quran samajhne ki buniyaad")
        }
        lessons.forEach { lesson ->
            AppCard(modifier = Modifier.padding(vertical = 5.dp)) {
                Text(lesson.title, style = MaterialTheme.typography.titleMedium, color = Gold)
                Spacer(Modifier.height(6.dp))
                Text(lesson.body, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                lesson.examples.forEach { ex ->
                    Text("• $ex", color = Emerald, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
