package com.example.data.remote

import com.example.data.local.TurkishCitiesData
import com.example.data.local.TurkishCity
import com.example.data.model.CityDto
import com.example.data.model.DiyanetLocationParams
import com.example.data.model.DistrictDto
import com.example.data.model.PrayerTimeDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Client manager for Diyanet Retrofit service.
 * Configures Retrofit with Moshi and OkHttpClient and provides methods
 * to query prayer times using location-based parameters.
 */
class DiyanetRetrofitClient(
    baseUrl: String = DEFAULT_BASE_URL,
    customOkHttpClient: OkHttpClient? = null
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://ezanvakti.emushaf.net/"
    }

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = customOkHttpClient ?: OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: DiyanetLocationApiService = retrofit.create(DiyanetLocationApiService::class.java)

    /**
     * Fetches prayer times from Diyanet API using the provided [DiyanetLocationParams].
     *
     * Resolves the target location from either:
     * 1. Direct districtId
     * 2. Geographic coordinates (GPS latitude/longitude)
     * 3. City ID
     * 4. Text search query
     */
    suspend fun getPrayerTimes(params: DiyanetLocationParams): List<PrayerTimeDto> {
        val resolvedDistrictId = resolveDistrictId(params)
        return apiService.getPrayerTimesByDistrictId(resolvedDistrictId)
    }

    /**
     * Fetches all 81 cities in Turkey.
     */
    suspend fun getCities(countryId: String = "2"): List<CityDto> {
        return apiService.getCitiesByCountry(countryId)
    }

    /**
     * Fetches all districts for a given city ID.
     */
    suspend fun getDistricts(cityId: String): List<DistrictDto> {
        return apiService.getDistrictsByCity(cityId)
    }

    /**
     * Resolves the Diyanet district ID from location parameters.
     */
    fun resolveDistrictId(params: DiyanetLocationParams): String {
        // 1. Direct district ID takes top priority
        if (!params.districtId.isNullOrBlank()) {
            return params.districtId
        }

        // 2. Resolve GPS coordinates to nearest Turkish city/district
        if (params.latitude != null && params.longitude != null) {
            val nearest = findNearestCity(params.latitude, params.longitude)
            if (nearest != null) {
                return nearest.defaultDistrictId
            }
        }

        // 3. Resolve by city ID
        if (!params.cityId.isNullOrBlank()) {
            val city = TurkishCitiesData.findCityById(params.cityId)
            if (city != null) {
                return city.defaultDistrictId
            }
        }

        // 4. Resolve by text query
        if (!params.locationQuery.isNullOrBlank()) {
            val matched = TurkishCitiesData.cities.find {
                it.name.contains(params.locationQuery.trim(), ignoreCase = true)
            }
            if (matched != null) {
                return matched.defaultDistrictId
            }
        }

        // Default: Istanbul (9541)
        return "9541"
    }

    /**
     * Finds nearest Turkish city based on geographic distance (Haversine formula).
     */
    fun findNearestCity(latitude: Double, longitude: Double): TurkishCity? {
        return TurkishCitiesData.cities.minByOrNull { city ->
            val dLat = Math.toRadians(city.latitude - latitude)
            val dLon = Math.toRadians(city.longitude - longitude)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(latitude)) * cos(Math.toRadians(city.latitude)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            6371.0 * c
        }
    }
}
