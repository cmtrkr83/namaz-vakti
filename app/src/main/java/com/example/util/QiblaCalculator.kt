package com.example.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object QiblaCalculator {
    const val MECCA_LATITUDE = 21.422487
    const val MECCA_LONGITUDE = 39.826206

    /**
     * Calculates Qibla bearing in degrees (0..360) from true North.
     */
    fun calculateQiblaAngle(latitude: Double, longitude: Double): Float {
        val userLatRad = Math.toRadians(latitude)
        val userLonRad = Math.toRadians(longitude)
        val meccaLatRad = Math.toRadians(MECCA_LATITUDE)
        val meccaLonRad = Math.toRadians(MECCA_LONGITUDE)

        val deltaLon = meccaLonRad - userLonRad

        val y = sin(deltaLon) * cos(meccaLatRad)
        val x = cos(userLatRad) * sin(meccaLatRad) - sin(userLatRad) * cos(meccaLatRad) * cos(deltaLon)

        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad)
        val normalized = ((bearingDeg + 360) % 360).toFloat()
        return normalized
    }

    /**
     * Calculates distance to Mecca in kilometers.
     */
    fun calculateDistanceToMeccaKm(latitude: Double, longitude: Double): Int {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(MECCA_LATITUDE - latitude)
        val dLon = Math.toRadians(MECCA_LONGITUDE - longitude)
        val lat1 = Math.toRadians(latitude)
        val lat2 = Math.toRadians(MECCA_LATITUDE)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (earthRadiusKm * c).roundToInt()
    }
}
