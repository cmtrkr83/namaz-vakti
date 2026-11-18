package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.TurkishCitiesData
import com.example.util.PrayerTimeHelper
import com.example.util.QiblaCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Namaz Vakti", appName)
  }

  @Test
  fun `calculate qibla angle for Istanbul is approximately 152 degrees`() {
    // Istanbul coordinates: 41.0082° N, 28.9784° E (Great-circle bearing to Mecca is ~151.6°)
    val angle = QiblaCalculator.calculateQiblaAngle(41.0082, 28.9784)
    assertTrue("Qibla angle for Istanbul should be close to 151.6 degrees", abs(angle - 151.6f) < 1.0f)
  }

  @Test
  fun `format seconds to countdown string correctly`() {
    assertEquals("01:30:15", PrayerTimeHelper.formatSecondsToCountdown(5415))
    assertEquals("00:00:00", PrayerTimeHelper.formatSecondsToCountdown(0))
  }

  @Test
  fun `turkish cities list contains all 81 provinces`() {
    assertEquals(81, TurkishCitiesData.cities.size)
  }

  @Test
  fun `diyanet retrofit client resolves location parameters correctly`() {
    val client = com.example.data.remote.NetworkClient.diyanetLocationClient

    // By district ID
    val paramsByDistrict = com.example.data.model.DiyanetLocationParams.byDistrictId("9541")
    assertEquals("9541", client.resolveDistrictId(paramsByDistrict))

    // By GPS Coordinates for Ankara (~39.93, ~32.85)
    val paramsByGps = com.example.data.model.DiyanetLocationParams.byCoordinates(39.9334, 32.8597)
    assertEquals("9206", client.resolveDistrictId(paramsByGps))

    // By City Name search query
    val paramsByQuery = com.example.data.model.DiyanetLocationParams.byQuery("İZMİR")
    assertEquals("9621", client.resolveDistrictId(paramsByQuery))
  }

  @Test
  fun `location provider detects city from coordinates correctly`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val locationProvider = com.example.util.LocationProvider(context)

    // Test detection for Istanbul coordinates (41.0082, 28.9784)
    val istanbul = locationProvider.detectCity(41.0082, 28.9784)
    assertEquals("İSTANBUL", istanbul.cityName)
    assertEquals("539", istanbul.cityId)
    assertEquals("9541", istanbul.districtId)

    // Test detection for Ankara coordinates (39.9334, 32.8597)
    val ankara = locationProvider.detectCity(39.9334, 32.8597)
    assertEquals("ANKARA", ankara.cityName)
    assertEquals("506", ankara.cityId)
    assertEquals("9206", ankara.districtId)

    // Test detection for Antalya coordinates (36.8969, 30.7133)
    val antalya = locationProvider.detectCity(36.8969, 30.7133)
    assertEquals("ANTALYA", antalya.cityName)
    assertEquals("507", antalya.cityId)
  }

  @Test
  fun `location provider normalizes Turkish characters and checks permission state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val locationProvider = com.example.util.LocationProvider(context)
    // Without granted runtime permission, hasLocationPermission() returns false safely
    assertEquals(false, locationProvider.hasLocationPermission())
    assertEquals("ISTANBUL", com.example.util.LocationProvider.normalizeTurkish("İstanbul"))
    assertEquals("IZMIR", com.example.util.LocationProvider.normalizeTurkish("İzmir"))
    assertEquals("SANLIURFA", com.example.util.LocationProvider.normalizeTurkish("Şanlıurfa"))
  }

  @Test
  fun `weather helper maps WMO codes to day and night conditions accurately`() {
    assertEquals(
      com.example.data.model.WeatherCondition.CLEAR_DAY,
      com.example.util.WeatherHelper.mapWmoCodeToCondition(0, isDay = true)
    )
    assertEquals(
      com.example.data.model.WeatherCondition.CLEAR_NIGHT,
      com.example.util.WeatherHelper.mapWmoCodeToCondition(0, isDay = false)
    )
    assertEquals(
      com.example.data.model.WeatherCondition.PARTLY_CLOUDY,
      com.example.util.WeatherHelper.mapWmoCodeToCondition(2, isDay = true)
    )
    assertEquals(
      com.example.data.model.WeatherCondition.CLOUDY,
      com.example.util.WeatherHelper.mapWmoCodeToCondition(3, isDay = true)
    )
    assertEquals(
      com.example.data.model.WeatherCondition.FOGGY,
      com.example.util.WeatherHelper.mapWmoCodeToCondition(45, isDay = true)
    )
    assertEquals(
      com.example.data.model.WeatherCondition.RAINY,
      com.example.util.WeatherHelper.mapWmoCodeToCondition(63, isDay = true)
    )
    assertEquals(
      com.example.data.model.WeatherCondition.SNOWY,
      com.example.util.WeatherHelper.mapWmoCodeToCondition(73, isDay = false)
    )
    assertEquals(
      com.example.data.model.WeatherCondition.THUNDERSTORM,
      com.example.util.WeatherHelper.mapWmoCodeToCondition(95, isDay = true)
    )
  }

  @Test
  fun `weather helper maps nearest hourly forecast to each prayer time`() {
    val refCal = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.NOVEMBER, 18, 12, 0, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val prayers = listOf(
      com.example.data.model.PrayerItem(com.example.data.model.PrayerType.IMSAK, "05:26", false, false),
      com.example.data.model.PrayerItem(com.example.data.model.PrayerType.GUNES, "06:52", false, false),
      com.example.data.model.PrayerItem(com.example.data.model.PrayerType.OGLE, "12:15", true, false),
      com.example.data.model.PrayerItem(com.example.data.model.PrayerType.IKINDI, "15:10", false, true),
      com.example.data.model.PrayerItem(com.example.data.model.PrayerType.AKSAM, "17:45", false, false),
      com.example.data.model.PrayerItem(com.example.data.model.PrayerType.YATSI, "19:10", false, false)
    )
    val schedule = com.example.data.model.DailyPrayerSchedule(
      dateKisa = "18.11.2026",
      dateUzun = "18 Kasım 2026",
      hicriDate = "8 Cemaziyelahir 1448",
      cityName = "İSTANBUL",
      districtName = "İSTANBUL",
      prayers = prayers,
      currentPrayer = prayers[2],
      nextPrayer = prayers[3],
      remainingSecondsToNext = 3600L,
      progressRatio = 0.5f,
      isKerahat = false,
      kerahatReason = null,
      kibleSaati = "11:45"
    )
    val response = com.example.data.model.OpenMeteoResponse(
      hourly = com.example.data.model.OpenMeteoHourly(
        time = listOf(
          "2026-11-18T05:00",
          "2026-11-18T07:00",
          "2026-11-18T12:00",
          "2026-11-18T15:00",
          "2026-11-18T18:00",
          "2026-11-18T19:00"
        ),
        temperature2m = listOf(8.2, 9.6, 15.4, 16.1, 12.8, 10.4),
        weatherCode = listOf(0, 45, 0, 61, 3, 0),
        isDay = listOf(0, 1, 1, 1, 0, 0)
      )
    )

    val mapped = com.example.util.WeatherHelper.mapHourlyWeatherToPrayers(schedule, response, refCal)
    assertEquals(6, mapped.size)
    // 05:26 -> 05:00 (WMO 0, night)
    assertEquals(com.example.data.model.WeatherCondition.CLEAR_NIGHT, mapped[com.example.data.model.PrayerType.IMSAK]?.condition)
    assertEquals(8, mapped[com.example.data.model.PrayerType.IMSAK]?.temperatureCelsius)
    // 06:52 -> rounded to 07:00 (WMO 45, foggy)
    assertEquals(com.example.data.model.WeatherCondition.FOGGY, mapped[com.example.data.model.PrayerType.GUNES]?.condition)
    assertEquals(10, mapped[com.example.data.model.PrayerType.GUNES]?.temperatureCelsius)
    // 12:15 -> 12:00 (WMO 0, day)
    assertEquals(com.example.data.model.WeatherCondition.CLEAR_DAY, mapped[com.example.data.model.PrayerType.OGLE]?.condition)
    // 15:10 -> 15:00 (WMO 61, rainy)
    assertEquals(com.example.data.model.WeatherCondition.RAINY, mapped[com.example.data.model.PrayerType.IKINDI]?.condition)
    // 17:45 -> rounded to 18:00 (WMO 3, cloudy)
    assertEquals(com.example.data.model.WeatherCondition.CLOUDY, mapped[com.example.data.model.PrayerType.AKSAM]?.condition)
    // 19:10 -> 19:00 (WMO 0, night)
    assertEquals(com.example.data.model.WeatherCondition.CLEAR_NIGHT, mapped[com.example.data.model.PrayerType.YATSI]?.condition)
  }
}
