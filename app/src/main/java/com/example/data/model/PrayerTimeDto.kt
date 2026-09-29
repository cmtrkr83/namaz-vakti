package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PrayerTimeDto(
    @Json(name = "HicriTarihKisa") val hicriTarihKisa: String? = null,
    @Json(name = "HicriTarihUzun") val hicriTarihUzun: String? = null,
    @Json(name = "MiladiTarihKisa") val miladiTarihKisa: String? = null,
    @Json(name = "MiladiTarihUzun") val miladiTarihUzun: String? = null,
    @Json(name = "AyinSekliURL") val ayinSekliUrl: String? = null,
    @Json(name = "Imsak") val imsak: String,
    @Json(name = "Gunes") val gunes: String,
    @Json(name = "Ogle") val ogle: String,
    @Json(name = "Ikindi") val ikindi: String,
    @Json(name = "Aksam") val aksam: String,
    @Json(name = "Yatsi") val yatsi: String,
    @Json(name = "KibleSaati") val kibleSaati: String? = null
)

@JsonClass(generateAdapter = true)
data class CityDto(
    @Json(name = "SehirID") val sehirId: String,
    @Json(name = "SehirAdi") val sehirAdi: String,
    @Json(name = "SehirAdiEn") val sehirAdiEn: String? = null
)

@JsonClass(generateAdapter = true)
data class DistrictDto(
    @Json(name = "IlceID") val ilceId: String,
    @Json(name = "IlceAdi") val ilceAdi: String,
    @Json(name = "IlceAdiEn") val ilceAdiEn: String? = null
)
