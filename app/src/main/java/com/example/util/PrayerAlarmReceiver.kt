package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.R
import com.example.data.local.PreferencesManager
import com.example.data.model.PrayerType

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PreferencesManager(context)
        val locContext = LocaleHelper.wrapContext(context, prefs.languageCode)

        val prayerTypeName = intent.getStringExtra("PRAYER_TYPE_NAME")
        val resolvedType = prayerTypeName?.let { runCatching { PrayerType.valueOf(it) }.getOrNull() }
        val prayerName = resolvedType?.let { locContext.getString(LocaleHelper.prayerNameRes(it)) }
            ?: intent.getStringExtra("PRAYER_NAME")
            ?: locContext.getString(R.string.prayer_default_name)

        val prayerTime = intent.getStringExtra("PRAYER_TIME") ?: ""
        val offsetMinutes = intent.getIntExtra("OFFSET_MINUTES", 0)
        val soundMode = intent.getStringExtra("SOUND_MODE") ?: "ezan"
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 1001)

        val title = if (offsetMinutes > 0) {
            locContext.getString(R.string.alarm_title_before_fmt, prayerName, offsetMinutes)
        } else {
            locContext.getString(R.string.alarm_title_now_fmt, prayerName)
        }

        val message = if (offsetMinutes > 0) {
            locContext.getString(R.string.alarm_msg_before_fmt, prayerTime, prayerName)
        } else {
            locContext.getString(R.string.alarm_msg_now_fmt, prayerName, prayerTime)
        }

        PrayerNotificationHelper.showNotification(
            context = locContext,
            notificationId = notificationId,
            title = title,
            content = message,
            soundMode = soundMode
        )
    }
}
