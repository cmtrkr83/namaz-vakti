package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import com.example.data.local.TurkishCitiesData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

data class CompassState(
    val azimuth: Float = 0f, // Device heading (0 = North)
    val qiblaAngle: Float = 147f, // Mecca bearing from current location
    val distanceKm: Int = 2400,
    val isFacingQibla: Boolean = false,
    val latitude: Double = 41.0082,
    val longitude: Double = 28.9784,
    val locationName: String = "İstanbul",
    val isGpsActive: Boolean = false
)

class CompassSensorHelper(private val context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val _compassState = MutableStateFlow(CompassState())
    val compassState: StateFlow<CompassState> = _compassState.asStateFlow()

    private var rotationMatrix = FloatArray(9)
    private var orientationAngles = FloatArray(3)
    private var lastAzimuth = 0f

    fun startListening() {
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            val orientationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
            orientationSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    @SuppressLint("MissingPermission")
    fun requestLocation(selectedCityId: String) {
        val defaultCity = TurkishCitiesData.findCityById(selectedCityId) ?: TurkishCitiesData.cities[0]
        
        try {
            val tokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        updateCoordinates(location.latitude, location.longitude, "Mevcut Konum", isGps = true)
                    } else {
                        // Fallback to selected city coordinates
                        updateCoordinates(defaultCity.latitude, defaultCity.longitude, defaultCity.name, isGps = false)
                    }
                }
                .addOnFailureListener {
                    updateCoordinates(defaultCity.latitude, defaultCity.longitude, defaultCity.name, isGps = false)
                }
        } catch (_: SecurityException) {
            updateCoordinates(defaultCity.latitude, defaultCity.longitude, defaultCity.name, isGps = false)
        }
    }

    fun updateWithCity(cityId: String) {
        val city = TurkishCitiesData.findCityById(cityId) ?: TurkishCitiesData.cities[0]
        updateCoordinates(city.latitude, city.longitude, city.name, isGps = false)
    }

    private fun updateCoordinates(lat: Double, lon: Double, name: String, isGps: Boolean) {
        val qibla = QiblaCalculator.calculateQiblaAngle(lat, lon)
        val dist = QiblaCalculator.calculateDistanceToMeccaKm(lat, lon)
        _compassState.value = _compassState.value.copy(
            latitude = lat,
            longitude = lon,
            locationName = name,
            qiblaAngle = qibla,
            distanceKm = dist,
            isGpsActive = isGps
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            if (azimuth < 0) azimuth += 360f

            // Low-pass filter for smooth needle movement
            val smoothAzimuth = lastAzimuth + 0.2f * (azimuth - lastAzimuth)
            lastAzimuth = smoothAzimuth

            val qibla = _compassState.value.qiblaAngle
            val diff = abs(smoothAzimuth - qibla)
            val isFacing = diff < 3.5f || diff > 356.5f

            _compassState.value = _compassState.value.copy(
                azimuth = smoothAzimuth,
                isFacingQibla = isFacing
            )
        } else if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
            val azimuth = event.values[0]
            val qibla = _compassState.value.qiblaAngle
            val diff = abs(azimuth - qibla)
            val isFacing = diff < 4.0f || diff > 356.0f

            _compassState.value = _compassState.value.copy(
                azimuth = azimuth,
                isFacingQibla = isFacing
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
