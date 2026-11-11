package com.lisanulquran.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lisanulquran.app.data.AppSettings
import com.lisanulquran.app.data.tr

class DailyAyahNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppSettings.init(context)
        DailyAyahNotificationHelper.createNotificationChannel(context)
        DailyAyahNotificationHelper.showDailyNotification(
            context,
            "Daily Ayah",
            tr("Aaj ki ayah padhne ke liye app kholein.", "Open the app to read today's ayah.")
        )
    }
}
