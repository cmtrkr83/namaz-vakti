package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Namaz"
        val prayerTime = intent.getStringExtra("PRAYER_TIME") ?: ""
        val offsetMinutes = intent.getIntExtra("OFFSET_MINUTES", 0)
        val soundMode = intent.getStringExtra("SOUND_MODE") ?: "ezan"
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 1001)

        val title = if (offsetMinutes > 0) {
            "$prayerName Vaktine $offsetMinutes Dakika Kaldı"
        } else {
            "$prayerName Vakti Girdi"
        }

        val message = if (offsetMinutes > 0) {
            "Saat $prayerTime itibarıyla $prayerName vakti başlayacak. Hazırlığınızı yapabilirsiniz."
        } else {
            "$prayerName vakti girdi ($prayerTime). Haydin namaza, haydin kurtuluşa."
        }

        PrayerNotificationHelper.showNotification(
            context = context,
            notificationId = notificationId,
            title = title,
            content = message,
            soundMode = soundMode
        )
    }
}
