package com.example.util

import com.example.data.local.PrayerTimeEntity
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object PrayerTimeHelper {

    fun formatSecondsToCountdown(totalSeconds: Long): String {
        val s = (totalSeconds % 60).coerceAtLeast(0)
        val m = ((totalSeconds / 60) % 60).coerceAtLeast(0)
        val h = (totalSeconds / 3600).coerceAtLeast(0)
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
    }

    fun parseTimeToMinutes(timeStr: String): Int {
        val parts = timeStr.trim().split(":")
        if (parts.size >= 2) {
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            return h * 60 + m
        }
        return 0
    }

    fun buildSchedule(
        entity: PrayerTimeEntity,
        calendar: Calendar = Calendar.getInstance()
    ): DailyPrayerSchedule {
        val nowHours = calendar.get(Calendar.HOUR_OF_DAY)
        val nowMinutes = calendar.get(Calendar.MINUTE)
        val nowSeconds = calendar.get(Calendar.SECOND)
        val currentMinutesOfDay = nowHours * 60 + nowMinutes
        val currentSecondsOfDay = currentMinutesOfDay * 60L + nowSeconds

        val imsakMin = parseTimeToMinutes(entity.imsak)
        val gunesMin = parseTimeToMinutes(entity.gunes)
        val ogleMin = parseTimeToMinutes(entity.ogle)
        val ikindiMin = parseTimeToMinutes(entity.ikindi)
        val aksamMin = parseTimeToMinutes(entity.aksam)
        val yatsiMin = parseTimeToMinutes(entity.yatsi)

        // Kerahat checks (45 mins after sun, 45 mins before noon, 45 mins before sunset)
        val isMorningKerahat = currentMinutesOfDay in gunesMin..(gunesMin + 45)
        val isNoonKerahat = currentMinutesOfDay in (ogleMin - 45)..ogleMin
        val isEveningKerahat = currentMinutesOfDay in (aksamMin - 45)..aksamMin

        val isKerahat = isMorningKerahat || isNoonKerahat || isEveningKerahat
        val kerahatReason = when {
            isMorningKerahat -> "Güneş doğuşundan sonraki 45 dakika (Kerahat vakti)"
            isNoonKerahat -> "Öğle ezanından önceki 45 dakika (Zeval kerahati)"
            isEveningKerahat -> "Akşam ezanından önceki 45 dakika (İsfirar kerahati)"
            else -> null
        }

        // Determine current and next prayer
        val currentType: PrayerType
        val nextType: PrayerType
        val currentStartSec: Long
        val nextEndSec: Long

        when {
            currentMinutesOfDay < imsakMin -> {
                // Past midnight before Imsak: Current is Yatsi (from yesterday), Next is Imsak today
                currentType = PrayerType.YATSI
                nextType = PrayerType.IMSAK
                val yesterdayYatsiSec = (yatsiMin - 24 * 60) * 60L
                currentStartSec = yesterdayYatsiSec
                nextEndSec = imsakMin * 60L
            }
            currentMinutesOfDay < gunesMin -> {
                // Between Imsak and Gunes
                currentType = PrayerType.IMSAK
                nextType = PrayerType.GUNES
                currentStartSec = imsakMin * 60L
                nextEndSec = gunesMin * 60L
            }
            currentMinutesOfDay < ogleMin -> {
                // Between Gunes and Ogle
                currentType = PrayerType.GUNES
                nextType = PrayerType.OGLE
                currentStartSec = gunesMin * 60L
                nextEndSec = ogleMin * 60L
            }
            currentMinutesOfDay < ikindiMin -> {
                // Between Ogle and Ikindi
                currentType = PrayerType.OGLE
                nextType = PrayerType.IKINDI
                currentStartSec = ogleMin * 60L
                nextEndSec = ikindiMin * 60L
            }
            currentMinutesOfDay < aksamMin -> {
                // Between Ikindi and Aksam
                currentType = PrayerType.IKINDI
                nextType = PrayerType.AKSAM
                currentStartSec = ikindiMin * 60L
                nextEndSec = aksamMin * 60L
            }
            currentMinutesOfDay < yatsiMin -> {
                // Between Aksam and Yatsi
                currentType = PrayerType.AKSAM
                nextType = PrayerType.YATSI
                currentStartSec = aksamMin * 60L
                nextEndSec = yatsiMin * 60L
            }
            else -> {
                // After Yatsi tonight: Current is Yatsi, Next is tomorrow's Imsak (~+24h)
                currentType = PrayerType.YATSI
                nextType = PrayerType.IMSAK
                currentStartSec = yatsiMin * 60L
                // tomorrow's Imsak assumed same as today
                nextEndSec = (imsakMin + 24 * 60) * 60L
            }
        }

        val remainingSeconds = (nextEndSec - currentSecondsOfDay).coerceAtLeast(0)
        val totalSlotSec = (nextEndSec - currentStartSec).coerceAtLeast(1)
        val elapsedSec = (currentSecondsOfDay - currentStartSec).coerceAtLeast(0)
        val progress = (elapsedSec.toFloat() / totalSlotSec.toFloat()).coerceIn(0f, 1f)

        val prayerItems = listOf(
            PrayerItem(PrayerType.IMSAK, entity.imsak, currentType == PrayerType.IMSAK, nextType == PrayerType.IMSAK),
            PrayerItem(PrayerType.GUNES, entity.gunes, currentType == PrayerType.GUNES, nextType == PrayerType.GUNES),
            PrayerItem(PrayerType.OGLE, entity.ogle, currentType == PrayerType.OGLE, nextType == PrayerType.OGLE),
            PrayerItem(PrayerType.IKINDI, entity.ikindi, currentType == PrayerType.IKINDI, nextType == PrayerType.IKINDI),
            PrayerItem(PrayerType.AKSAM, entity.aksam, currentType == PrayerType.AKSAM, nextType == PrayerType.AKSAM),
            PrayerItem(PrayerType.YATSI, entity.yatsi, currentType == PrayerType.YATSI, nextType == PrayerType.YATSI)
        )

        val currentItem = prayerItems.firstOrNull { it.type == currentType } ?: prayerItems[0]
        val nextItem = prayerItems.firstOrNull { it.type == nextType } ?: prayerItems[1]

        return DailyPrayerSchedule(
            dateKisa = entity.miladiTarihKisa,
            dateUzun = entity.miladiTarihUzun,
            hicriDate = entity.hicriTarihUzun,
            cityName = entity.cityName,
            districtName = entity.districtName,
            prayers = prayerItems,
            currentPrayer = currentItem,
            nextPrayer = nextItem,
            remainingSecondsToNext = remainingSeconds,
            progressRatio = progress,
            isKerahat = isKerahat,
            kerahatReason = kerahatReason,
            kibleSaati = entity.kibleSaati.ifBlank { "11:45" }
        )
    }

    fun getTodayDateKisa(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }

    fun getTodayDateUzun(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy EEEE", Locale("tr", "TR"))
        return sdf.format(Calendar.getInstance().time)
    }
}
