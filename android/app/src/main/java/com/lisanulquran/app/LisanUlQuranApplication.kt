package com.lisanulquran.app

import android.app.Application
import com.lisanulquran.app.data.LearnedStore
import com.lisanulquran.app.notification.DailyAyahNotificationHelper

class LisanUlQuranApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LearnedStore.init(this)
        DailyAyahNotificationHelper.createNotificationChannel(this)
    }
}
