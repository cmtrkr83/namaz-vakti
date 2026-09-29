package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.local.PrayerTimeEntity
import com.example.data.model.PrayerType
import com.example.util.PrayerTimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * AppWidgetProvider that displays:
 * 1. Currently performable prayer time (Şu an kılınabilir namaz vakti)
 * 2. Countdown time remaining until the next prayer (Sıradaki namaz vaktine kalan süre)
 * 3. Daily times overview and location info.
 */
class PrayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            // First immediately update with synchronous fast views to prevent "Widget eklenemedi" timeout
            val initialViews = buildFastRemoteViews(context)
            appWidgetManager.updateAppWidget(appWidgetId, initialViews)

            // Then asynchronously load the precise database values and update
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val views = buildRemoteViews(context)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (_: Exception) {
                    // Fail silently, initial fast view is already displayed
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        updateAllWidgets(context)
    }

    companion object {

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, PrayerWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                // Immediate fast update
                val fastViews = buildFastRemoteViews(context)
                for (widgetId in appWidgetIds) {
                    appWidgetManager.updateAppWidget(widgetId, fastViews)
                }

                // Async database update
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val views = buildRemoteViews(context)
                        for (widgetId in appWidgetIds) {
                            appWidgetManager.updateAppWidget(widgetId, views)
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }

        fun buildFastRemoteViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_prayer_times)
            val prefs = PreferencesManager(context)
            val entity = getFallbackEntity(prefs)
            applyEntityToViews(context, views, entity)
            return views
        }

        suspend fun buildRemoteViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_prayer_times)
            val prefs = PreferencesManager(context)
            val todayKisa = PrayerTimeHelper.getTodayDateKisa()
            val entity = try {
                val db = AppDatabase.getDatabase(context)
                db.prayerDao().getPrayerTimeForDate(prefs.districtId, todayKisa)
                    ?: getFallbackEntity(prefs)
            } catch (_: Exception) {
                getFallbackEntity(prefs)
            }

            applyEntityToViews(context, views, entity)
            return views
        }

        private fun applyEntityToViews(context: Context, views: RemoteViews, entity: PrayerTimeEntity) {
            val schedule = PrayerTimeHelper.buildSchedule(entity, Calendar.getInstance())

            // Location
            val locationText = "📍 ${entity.cityName} - ${entity.districtName}"
            views.setTextViewText(R.id.widget_location, locationText)

            // Current Performable Prayer (Şu an kılınabilir namaz)
            val (performableName, performableRange) = getPerformablePrayerDetails(schedule.currentPrayer.type, entity)
            views.setTextViewText(R.id.widget_current_name, performableName)
            views.setTextViewText(R.id.widget_current_range, performableRange)

            // Next Prayer & Countdown (Sıradaki vakte kalan süre)
            val countdownStr = PrayerTimeHelper.formatSecondsToCountdown(schedule.remainingSecondsToNext)
            views.setTextViewText(R.id.widget_next_countdown, countdownStr)
            views.setTextViewText(
                R.id.widget_next_info,
                "${schedule.nextPrayer.type.displayName} (${schedule.nextPrayer.time})"
            )

            // Strip times
            views.setTextViewText(R.id.widget_time_imsak, "İmsak\n${entity.imsak}")
            views.setTextViewText(R.id.widget_time_gunes, "Güneş\n${entity.gunes}")
            views.setTextViewText(R.id.widget_time_ogle, "Öğle\n${entity.ogle}")
            views.setTextViewText(R.id.widget_time_ikindi, "İkindi\n${entity.ikindi}")
            views.setTextViewText(R.id.widget_time_aksam, "Akşam\n${entity.aksam}")
            views.setTextViewText(R.id.widget_time_yatsi, "Yatsı\n${entity.yatsi}")

            // Tap root view to open the app
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        }

        private fun getPerformablePrayerDetails(
            currentType: PrayerType,
            entity: PrayerTimeEntity
        ): Pair<String, String> {
            return when (currentType) {
                PrayerType.IMSAK -> "Sabah Namazı" to "Vakit: ${entity.imsak} - ${entity.gunes}"
                PrayerType.GUNES -> "Kuşluk / İşrak Vakti" to "Kerâhat sonrası: ${entity.gunes} - ${entity.ogle}"
                PrayerType.OGLE -> "Öğle Namazı" to "Vakit: ${entity.ogle} - ${entity.ikindi}"
                PrayerType.IKINDI -> "İkindi Namazı" to "Vakit: ${entity.ikindi} - ${entity.aksam}"
                PrayerType.AKSAM -> "Akşam Namazı" to "Vakit: ${entity.aksam} - ${entity.yatsi}"
                PrayerType.YATSI -> "Yatsı Namazı" to "Vakit: ${entity.yatsi} - ${entity.imsak}"
            }
        }

        private fun getFallbackEntity(prefs: PreferencesManager): PrayerTimeEntity {
            val todayKisa = PrayerTimeHelper.getTodayDateKisa()
            val todayUzun = PrayerTimeHelper.getTodayDateUzun()
            return PrayerTimeEntity(
                id = "${prefs.districtId}_$todayKisa",
                districtId = prefs.districtId,
                cityName = prefs.cityName,
                districtName = prefs.districtName,
                miladiTarihKisa = todayKisa,
                miladiTarihUzun = todayUzun,
                hicriTarihUzun = "1448 Hicri",
                imsak = "05:26",
                gunes = "06:50",
                ogle = "13:00",
                ikindi = "16:18",
                aksam = "18:56",
                yatsi = "20:15",
                kibleSaati = "11:45",
                ayinSekliUrl = ""
            )
        }
    }
}
