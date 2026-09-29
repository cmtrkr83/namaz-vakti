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
  }
}
