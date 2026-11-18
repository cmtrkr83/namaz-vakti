package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.local.TurkishCitiesData
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

private data class GeocodedPlace(
    val cityName: String?,
    val districtHint: String?
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
     * Checks if fine or coarse location permission has been granted.
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
     * Checks if device location providers (GPS or Network) are enabled.
     */
    fun isLocationServiceEnabled(): Boolean {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            locationManager?.let {
                it.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } ?: true
        } catch (_: Exception) {
            true
        }
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
        val geocoded = reverseGeocodePlace(latitude, longitude)

        // Try normalized Turkish city match first
        val candidateCity = geocoded?.cityName
        if (!candidateCity.isNullOrBlank()) {
            val normalizedCandidate = normalizeTurkish(candidateCity)
            val matchedCity = TurkishCitiesData.cities.find { city ->
                val normalizedTarget = normalizeTurkish(city.name)
                normalizedTarget == normalizedCandidate ||
                    normalizedCandidate.contains(normalizedTarget) ||
                    normalizedTarget.contains(normalizedCandidate)
            }
            if (matchedCity != null) {
                return@withContext DetectedCityInfo(
                    cityId = matchedCity.id,
                    cityName = matchedCity.name,
                    districtId = matchedCity.defaultDistrictId,
                    districtName = geocoded.districtHint?.takeIf { it.isNotBlank() } ?: matchedCity.name,
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
                SecurityException("Konum izni verilmedi. Lütfen konum izni veriniz.")
            )
        }

        val location = getCurrentLocation()
        if (location == null) {
            val message = if (!isLocationServiceEnabled()) {
                "Konum servisi (GPS) kapalı. Lütfen cihaz ayarlarından konumu açın."
            } else {
                "Mevcut konum alınamadı. Lütfen açık alanda tekrar deneyin."
            }
            return@withContext Result.failure(IllegalStateException(message))
        }

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
     * Reverse geocodes latitude and longitude into city & district hints,
     * using modern API 33+ async Geocoder callback with legacy fallback.
     */
    private suspend fun reverseGeocodePlace(latitude: Double, longitude: Double): GeocodedPlace? {
        return try {
            val geocoder = Geocoder(context, Locale("tr", "TR"))
            val addresses: List<Address>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1) { result ->
                        if (continuation.isActive) continuation.resume(result)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)
            }
            val address = addresses?.firstOrNull() ?: return null
            GeocodedPlace(
                cityName = address.adminArea ?: address.locality ?: address.subAdminArea,
                districtHint = address.subAdminArea ?: address.locality
            )
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        fun normalizeTurkish(input: String): String {
            return input.trim()
                .replace('İ', 'I')
                .replace('ı', 'i')
                .replace('Ş', 'S')
                .replace('ş', 's')
                .replace('Ğ', 'G')
                .replace('ğ', 'g')
                .replace('Ü', 'U')
                .replace('ü', 'u')
                .replace('Ö', 'O')
                .replace('ö', 'o')
                .replace('Ç', 'C')
                .replace('ç', 'c')
                .uppercase(Locale.US)
        }
    }
}
