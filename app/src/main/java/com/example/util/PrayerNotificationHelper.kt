package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.PreferencesManager
import com.example.data.model.DailyPrayerSchedule
import java.util.Calendar

object PrayerNotificationHelper {
    const val CHANNEL_ID = "namaz_vakti_channel"
    private const val CHANNEL_NAME = "Namaz Vakti Bildirimleri"
    private const val NOTIFICATION_ID_BASE = 1000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Namaz vakti ezan ve hatırlatıcı bildirimleri"
                enableLights(true)
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        content: String,
        soundMode: String = "ezan"
    ) {
        createNotificationChannel(context)

        // Haptic feedback
        triggerVibration(context, soundMode)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (soundMode != "vibrate") {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(soundUri)
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }

    private fun triggerVibration(context: Context, soundMode: String) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 300, 200, 300)
                    val amplitudes = intArrayOf(0, 255, 0, 255)
                    it.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(500)
                }
            }
        } catch (_: Exception) {}
    }

    fun schedulePrayerAlarms(
        context: Context,
        schedule: DailyPrayerSchedule,
        prefs: PreferencesManager
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val offsetMinutes = prefs.notificationOffsetMinutes
        val soundMode = prefs.notificationSoundMode

        schedule.prayers.forEachIndexed { index, prayer ->
            val prayerKey = prayer.type.name.lowercase()
            if (!prefs.isPrayerNotificationEnabled(prayerKey)) return@forEachIndexed

            val parts = prayer.time.split(":")
            if (parts.size < 2) return@forEachIndexed
            val prayerHour = parts[0].toIntOrNull() ?: return@forEachIndexed
            val prayerMinute = parts[1].toIntOrNull() ?: return@forEachIndexed

            val targetCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, prayerHour)
                set(Calendar.MINUTE, prayerMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, -offsetMinutes)
            }

            // Only schedule if it's in the future today
            if (targetCal.timeInMillis > System.currentTimeMillis()) {
                val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    putExtra("PRAYER_NAME", prayer.type.displayName)
                    putExtra("PRAYER_TIME", prayer.time)
                    putExtra("OFFSET_MINUTES", offsetMinutes)
                    putExtra("SOUND_MODE", soundMode)
                    putExtra("NOTIFICATION_ID", NOTIFICATION_ID_BASE + index)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    NOTIFICATION_ID_BASE + index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        targetCal.timeInMillis,
                        pendingIntent
                    )
                } catch (_: SecurityException) {
                    // Fallback for permission restrictions
                }
            }
        }
    }
}
