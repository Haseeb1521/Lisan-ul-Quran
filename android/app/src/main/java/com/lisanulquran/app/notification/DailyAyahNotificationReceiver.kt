package com.lisanulquran.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyAyahNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        DailyAyahNotificationHelper.createNotificationChannel(context)
        DailyAyahNotificationHelper.showDailyNotification(
            context,
            "Daily Ayah",
            "Aaj ki ayah padhne ke liye app kholein."
        )
    }
}
