package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_times")
data class PrayerTimeEntity(
    @PrimaryKey
    val id: String, // Format: "${districtId}_${miladiTarihKisa}"
    val districtId: String,
    val cityName: String,
    val districtName: String,
    val miladiTarihKisa: String,
    val miladiTarihUzun: String,
    val hicriTarihUzun: String,
    val imsak: String,
    val gunes: String,
    val ogle: String,
    val ikindi: String,
    val aksam: String,
    val yatsi: String,
    val kibleSaati: String,
    val ayinSekliUrl: String,
    val cachedTimestamp: Long = System.currentTimeMillis()
)
