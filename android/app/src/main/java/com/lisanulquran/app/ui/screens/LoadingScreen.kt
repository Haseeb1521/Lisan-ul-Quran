package com.lisanulquran.app.ui.screens

import com.lisanulquran.app.data.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lisanulquran.app.ui.theme.BgBrush
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.Emerald
import com.lisanulquran.app.ui.theme.ErrorRed
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.Muted

@Composable
fun LoadingScreen(progress: Float, error: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBrush)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("لسان القرآن", fontSize = 40.sp, color = Gold)
        Spacer(Modifier.height(8.dp))
        Text("Lisan-ul-Quran", fontSize = 18.sp, color = Emerald)
        Spacer(Modifier.height(32.dp))
        if (error != null) {
            Text(
                tr("Data load nahi ho paya:\n$error", "Could not load data:\n$error"),
                color = ErrorRed,
                textAlign = TextAlign.Center
            )
        } else {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = Emerald
            )
            Spacer(Modifier.height(12.dp))
            Text(
                tr("Pehli baar Quran ka data tayyar ho raha hai (${(progress * 100).toInt()}%). Sirf ek baar hoga.", "Preparing the Quran data for the first time (${(progress * 100).toInt()}%). This happens only once."),
                color = Muted,
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )
        }
    }
}
