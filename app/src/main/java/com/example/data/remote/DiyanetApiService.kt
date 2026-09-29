package com.example.data.remote

import com.example.data.model.CityDto
import com.example.data.model.DistrictDto
import com.example.data.model.PrayerTimeDto
import retrofit2.http.GET
import retrofit2.http.Path

interface DiyanetApiService : DiyanetLocationApiService {
    @GET("vakitler/{districtId}")
    suspend fun getPrayerTimes(@Path("districtId") districtId: String): List<PrayerTimeDto>

    @GET("sehirler/2")
    suspend fun getCities(): List<CityDto>

    @GET("ilceler/{cityId}")
    suspend fun getDistricts(@Path("cityId") cityId: String): List<DistrictDto>
}
