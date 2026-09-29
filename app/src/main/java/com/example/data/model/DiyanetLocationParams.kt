package com.example.data.model

/**
 * Location-based parameters for fetching Diyanet prayer times.
 *
 * Supports:
 * - [districtId]: Diyanet district ID (e.g. "9541" for Istanbul Merkez)
 * - [cityId]: Diyanet city ID (e.g. "539" for Istanbul)
 * - [countryId]: Diyanet country ID (default "2" for Türkiye)
 * - [latitude] and [longitude]: Geographic GPS coordinates
 * - [locationQuery]: Text search query for city or district name
 */
data class DiyanetLocationParams(
    val districtId: String? = null,
    val cityId: String? = null,
    val countryId: String = "2",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationQuery: String? = null
) {
    companion object {
        fun byDistrictId(districtId: String): DiyanetLocationParams =
            DiyanetLocationParams(districtId = districtId)

        fun byCityId(cityId: String): DiyanetLocationParams =
            DiyanetLocationParams(cityId = cityId)

        fun byCoordinates(latitude: Double, longitude: Double): DiyanetLocationParams =
            DiyanetLocationParams(latitude = latitude, longitude = longitude)

        fun byQuery(query: String): DiyanetLocationParams =
            DiyanetLocationParams(locationQuery = query)
    }
}
