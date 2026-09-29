package com.example.data.remote

import com.example.data.model.CityDto
import com.example.data.model.DistrictDto
import com.example.data.model.PrayerTimeDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service to fetch prayer times and location metadata from Diyanet API
 * using provided location-based parameters (district ID, city ID, country ID, or query parameters).
 */
interface DiyanetLocationApiService {

    /**
     * Fetches monthly prayer times for a specific Diyanet district ID.
     * @param districtId The Diyanet Ilce ID (e.g. "9541" for Istanbul Merkez, "9206" for Ankara Cankaya).
     */
    @GET("vakitler/{districtId}")
    suspend fun getPrayerTimesByDistrictId(
        @Path("districtId") districtId: String
    ): List<PrayerTimeDto>

    /**
     * Fetches prayer times using query-based location parameters.
     */
    @GET("vakitler")
    suspend fun getPrayerTimesByLocationQuery(
        @Query("ilceId") districtId: String,
        @Query("sehirId") cityId: String? = null
    ): List<PrayerTimeDto>

    /**
     * Fetches the list of all cities for a country.
     * Country ID 2 corresponds to Turkey (81 provinces).
     */
    @GET("sehirler/{countryId}")
    suspend fun getCitiesByCountry(
        @Path("countryId") countryId: String = "2"
    ): List<CityDto>

    /**
     * Fetches all districts (ilceler) within a specific city.
     * @param cityId The Diyanet Sehir ID (e.g. "539" for Istanbul).
     */
    @GET("ilceler/{cityId}")
    suspend fun getDistrictsByCity(
        @Path("cityId") cityId: String
    ): List<DistrictDto>
}
