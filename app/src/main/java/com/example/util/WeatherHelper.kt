package com.example.util

import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.OpenMeteoResponse
import com.example.data.model.PrayerType
import com.example.data.model.PrayerWeatherInfo
import com.example.data.model.WeatherCondition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

object WeatherHelper {

    /**
     * Maps Open-Meteo hourly weather data to each prayer time slot in [schedule]
     * by matching the nearest hour to each prayer's exact HH:mm time.
     */
    fun mapHourlyWeatherToPrayers(
        schedule: DailyPrayerSchedule,
        response: OpenMeteoResponse,
        referenceCalendar: Calendar = Calendar.getInstance()
    ): Map<PrayerType, PrayerWeatherInfo> {
        val hourly = response.hourly ?: return emptyMap()
        if (hourly.time.isEmpty()) return emptyMap()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:00", Locale.US)
        val result = mutableMapOf<PrayerType, PrayerWeatherInfo>()

        schedule.prayers.forEach { prayer ->
            val parts = prayer.time.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: return@forEach
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            val targetCal = (referenceCalendar.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (minute >= 30) {
                    add(Calendar.HOUR_OF_DAY, 1)
                }
            }

            val exactKey = dateFormat.format(targetCal.time)
            var index = hourly.time.indexOf(exactKey)
            if (index == -1) {
                val hourSuffix = String.format(Locale.US, "T%02d:00", targetCal.get(Calendar.HOUR_OF_DAY))
                index = hourly.time.indexOfFirst { it.endsWith(hourSuffix) }
            }

            if (index in hourly.weatherCode.indices && index in hourly.temperature2m.indices) {
                val code = hourly.weatherCode[index]
                val temp = hourly.temperature2m[index].roundToInt()
                val isDayFlag = hourly.isDay.getOrNull(index)?.let { it == 1 }
                    ?: (prayer.type != PrayerType.IMSAK && prayer.type != PrayerType.YATSI)

                result[prayer.type] = PrayerWeatherInfo(
                    condition = mapWmoCodeToCondition(code, isDayFlag),
                    temperatureCelsius = temp,
                    isDay = isDayFlag
                )
            }
        }

        return result
    }

    fun mapWmoCodeToCondition(wmoCode: Int, isDay: Boolean): WeatherCondition {
        return when (wmoCode) {
            0 -> if (isDay) WeatherCondition.CLEAR_DAY else WeatherCondition.CLEAR_NIGHT
            1, 2 -> WeatherCondition.PARTLY_CLOUDY
            3 -> WeatherCondition.CLOUDY
            45, 48 -> WeatherCondition.FOGGY
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.RAINY
            71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOWY
            95, 96, 99 -> WeatherCondition.THUNDERSTORM
            else -> WeatherCondition.PARTLY_CLOUDY
        }
    }
}
