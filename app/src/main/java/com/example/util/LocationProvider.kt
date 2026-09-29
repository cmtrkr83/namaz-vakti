package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.local.TurkishCitiesData
import com.example.data.local.TurkishCity
import com.example.data.model.DiyanetLocationParams
import com.example.data.model.PrayerTimeDto
import com.example.data.remote.DiyanetRetrofitClient
import com.example.data.remote.NetworkClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Data representing a detected city with Diyanet IDs and coordinates.
 */
data class DetectedCityInfo(
    val cityId: String,
    val cityName: String,
    val districtId: String,
    val districtName: String,
    val latitude: Double,
    val longitude: Double,
    val isFromGps: Boolean = true
)

/**
 * Result bundle containing detected city information and its prayer times from Retrofit.
 */
data class LocationPrayerTimesResult(
    val cityInfo: DetectedCityInfo,
    val prayerTimes: List<PrayerTimeDto>
)

/**
 * Provider class using [FusedLocationProviderClient] to detect the user's geographic location,
 * resolve the nearest city/district, and fetch prayer times from the Diyanet Retrofit service.
 */
class LocationProvider(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context),
    private val diyanetRetrofitClient: DiyanetRetrofitClient = NetworkClient.diyanetLocationClient
) {

    /**
     * Checks if location permission has been granted.
     */
    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    /**
     * Retrieves the current GPS/Network location using [FusedLocationProviderClient].
     * Falls back to [FusedLocationProviderClient.getLastLocation] if current location times out or is null.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(
        priority: Int = Priority.PRIORITY_BALANCED_POWER_ACCURACY
    ): Location? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) return@withContext null

        val cancellationTokenSource = CancellationTokenSource()

        val currentLocation: Location? = suspendCancellableCoroutine { continuation ->
            fusedLocationClient.getCurrentLocation(priority, cancellationTokenSource.token)
                .addOnSuccessListener { loc ->
                    if (continuation.isActive) continuation.resume(loc)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
                .addOnCanceledListener {
                    if (continuation.isActive) continuation.resume(null)
                }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }

        if (currentLocation != null) {
            return@withContext currentLocation
        }

        // Fallback to last known location
        suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { lastLoc ->
                    if (continuation.isActive) continuation.resume(lastLoc)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }
    }

    /**
     * Detects the user's city from geographic coordinates using reverse geocoding
     * and [TurkishCitiesData] proximity matching.
     */
    suspend fun detectCity(latitude: Double, longitude: Double): DetectedCityInfo = withContext(Dispatchers.IO) {
        val geocodedCityName = reverseGeocodeCityName(latitude, longitude)

        // Try exact/partial match with Turkish city names first if geocoder returned something
        if (!geocodedCityName.isNullOrBlank()) {
            val matchedCity = TurkishCitiesData.cities.find {
                it.name.contains(geocodedCityName.trim(), ignoreCase = true) ||
                        geocodedCityName.contains(it.name, ignoreCase = true)
            }
            if (matchedCity != null) {
                return@withContext DetectedCityInfo(
                    cityId = matchedCity.id,
                    cityName = matchedCity.name,
                    districtId = matchedCity.defaultDistrictId,
                    districtName = matchedCity.name,
                    latitude = latitude,
                    longitude = longitude,
                    isFromGps = true
                )
            }
        }

        // Proximity calculation: find nearest city center
        val nearest = diyanetRetrofitClient.findNearestCity(latitude, longitude)
            ?: TurkishCitiesData.cities[0]

        DetectedCityInfo(
            cityId = nearest.id,
            cityName = nearest.name,
            districtId = nearest.defaultDistrictId,
            districtName = nearest.name,
            latitude = latitude,
            longitude = longitude,
            isFromGps = true
        )
    }

    /**
     * Detects the user's current city via GPS and fetches the corresponding prayer times
     * from the Diyanet Retrofit service.
     */
    suspend fun detectCityAndFetchPrayerTimes(): Result<LocationPrayerTimesResult> = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext Result.failure(
                SecurityException("Konum izni verilmedi. Lütfen ayarlardan veya ekrandan konum izni veriniz.")
            )
        }

        val location = getCurrentLocation()
            ?: return@withContext Result.failure(
                IllegalStateException("Mevcut konum tespit edilemedi. Lütfen GPS servisinin açık olduğunu kontrol edin.")
            )

        try {
            val cityInfo = detectCity(location.latitude, location.longitude)
            val params = DiyanetLocationParams.byCoordinates(location.latitude, location.longitude)
            val prayerTimes = diyanetRetrofitClient.getPrayerTimes(params)

            if (prayerTimes.isNotEmpty()) {
                Result.success(LocationPrayerTimesResult(cityInfo, prayerTimes))
            } else {
                Result.failure(Exception("${cityInfo.cityName} için vakit verisi bulunamadı."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches prayer times from the Retrofit service for an explicitly provided [Location].
     */
    suspend fun fetchPrayerTimesForLocation(location: Location): Result<LocationPrayerTimesResult> = withContext(Dispatchers.IO) {
        try {
            val cityInfo = detectCity(location.latitude, location.longitude)
            val params = DiyanetLocationParams.byCoordinates(location.latitude, location.longitude)
            val prayerTimes = diyanetRetrofitClient.getPrayerTimes(params)
            Result.success(LocationPrayerTimesResult(cityInfo, prayerTimes))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Reverse geocodes latitude and longitude into a city name.
     */
    private fun reverseGeocodeCityName(latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale("tr", "TR"))
            @Suppress("DEPRECATION")
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            val address = addresses?.firstOrNull()
            address?.adminArea ?: address?.subAdminArea ?: address?.locality
        } catch (_: Exception) {
            null
        }
    }
}
