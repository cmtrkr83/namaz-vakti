package com.example.data.model

enum class PrayerType(val displayName: String, val subtitle: String) {
    IMSAK("İmsak", "Sahur Bitişi"),
    GUNES("Güneş", "Güneş Doğuşu"),
    OGLE("Öğle", "Öğle Vakti"),
    IKINDI("İkindi", "İkindi Vakti"),
    AKSAM("Akşam", "İftar Vakti"),
    YATSI("Yatsı", "Yatsı Vakti")
}

data class PrayerItem(
    val type: PrayerType,
    val time: String, // HH:mm
    val isCurrent: Boolean = false,
    val isNext: Boolean = false
)

data class DailyPrayerSchedule(
    val dateKisa: String,
    val dateUzun: String,
    val hicriDate: String,
    val cityName: String,
    val districtName: String,
    val prayers: List<PrayerItem>,
    val currentPrayer: PrayerItem,
    val nextPrayer: PrayerItem,
    val remainingSecondsToNext: Long,
    val progressRatio: Float,
    val isKerahat: Boolean,
    val kerahatReason: String?,
    val kibleSaati: String
)
