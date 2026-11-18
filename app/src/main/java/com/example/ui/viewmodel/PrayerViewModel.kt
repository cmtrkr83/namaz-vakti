package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DailyWisdom
import com.example.data.local.DailyWisdomData
import com.example.data.local.PreferencesManager
import com.example.data.local.PrayerTimeEntity
import com.example.data.local.TurkishCitiesData
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.DistrictDto
import com.example.data.model.OpenMeteoResponse
import com.example.data.model.PrayerType
import com.example.data.model.PrayerWeatherInfo
import com.example.data.remote.NetworkClient
import com.example.data.repository.PrayerRepository
import com.example.util.CompassSensorHelper
import com.example.util.CompassState
import com.example.util.LocationProvider
import com.example.util.PrayerNotificationHelper
import com.example.util.PrayerTimeHelper
import com.example.util.WeatherHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PrayerUiState(
    val isLoading: Boolean = false,
    val schedule: DailyPrayerSchedule? = null,
    val monthlyTimes: List<PrayerTimeEntity> = emptyList(),
    val cityId: String = "539",
    val cityName: String = "İSTANBUL",
    val districtId: String = "9541",
    val districtName: String = "İSTANBUL",
    val districts: List<DistrictDto> = emptyList(),
    val isFetchingDistricts: Boolean = false,
    val errorMessage: String? = null,
    val notificationOffsetMinutes: Int = 0,
    val notificationSoundMode: String = "ezan",
    val notificationRepeatDaily: Boolean = true,
    val prayerNotificationToggles: Map<String, Boolean> = mapOf(
        "imsak" to true,
        "gunes" to false,
        "ogle" to true,
        "ikindi" to true,
        "aksam" to true,
        "yatsi" to true
    ),
    val tasbihCount: Int = 0,
    val tasbihTarget: Int = 33,
    val tasbihDhikrIndex: Int = 0,
    val dailyWisdom: DailyWisdom = DailyWisdomData.getTodayWisdom(),
    val testNotificationMessage: String? = null,
    val isDetectingLocation: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val locationBannerDismissed: Boolean = false,
    val isUsingAutoLocation: Boolean = true,
    val prayerWeather: Map<PrayerType, PrayerWeatherInfo> = emptyMap()
)

class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = PrayerRepository(db.prayerDao(), NetworkClient.diyanetApi)
    val preferences = PreferencesManager(application)
    val compassHelper = CompassSensorHelper(application)
    val locationProvider = LocationProvider(application)

    private val initialPermissionGranted = locationProvider.hasLocationPermission()

    private val _uiState = MutableStateFlow(
        PrayerUiState(
            cityId = preferences.cityId,
            cityName = preferences.cityName,
            districtId = preferences.districtId,
            districtName = preferences.districtName,
            notificationOffsetMinutes = preferences.notificationOffsetMinutes,
            notificationSoundMode = preferences.notificationSoundMode,
            notificationRepeatDaily = preferences.notificationRepeatDaily,
            prayerNotificationToggles = mapOf(
                "imsak" to preferences.isPrayerNotificationEnabled("imsak"),
                "gunes" to preferences.isPrayerNotificationEnabled("gunes"),
                "ogle" to preferences.isPrayerNotificationEnabled("ogle"),
                "ikindi" to preferences.isPrayerNotificationEnabled("ikindi"),
                "aksam" to preferences.isPrayerNotificationEnabled("aksam"),
                "yatsi" to preferences.isPrayerNotificationEnabled("yatsi")
            ),
            tasbihCount = preferences.tasbihCount,
            tasbihTarget = preferences.tasbihTarget,
            tasbihDhikrIndex = preferences.tasbihDhikrIndex,
            hasLocationPermission = initialPermissionGranted,
            locationBannerDismissed = preferences.locationBannerDismissed,
            isUsingAutoLocation = preferences.autoLocationEnabled
        )
    )
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    val compassState: StateFlow<CompassState> = compassHelper.compassState

    private var currentRawEntity: PrayerTimeEntity? = null
    private var tickerJob: Job? = null
    private var weatherJob: Job? = null
    private var hasSyncedStartupLocation = false
    private var currentCoordinates: Pair<Double, Double>? = null
    private var cachedWeatherCoords: Pair<Double, Double>? = null
    private var cachedWeatherResponse: OpenMeteoResponse? = null

    init {
        PrayerNotificationHelper.createNotificationChannel(application)
        loadPrayerTimes()
        startClockTicker()
        loadDistrictsForCurrentCity()
        compassHelper.updateWithCity(preferences.cityId)

        if (initialPermissionGranted && preferences.autoLocationEnabled) {
            hasSyncedStartupLocation = true
            detectLocationAndFetchPrayerTimes()
        }
    }

    private fun startClockTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                currentRawEntity?.let { entity ->
                    val updatedSchedule = PrayerTimeHelper.buildSchedule(entity)
                    _uiState.update { it.copy(schedule = updatedSchedule) }
                }
                delay(1000)
            }
        }
    }

    /**
     * Re-checks OS location permission state (e.g. on ON_RESUME after returning from Settings
     * or after runtime permission dialog) and auto-detects city if permission was newly granted.
     */
    fun refreshLocationPermissionState(autoFetchIfNewlyGranted: Boolean = true) {
        val grantedNow = locationProvider.hasLocationPermission()
        val previouslyGranted = _uiState.value.hasLocationPermission

        _uiState.update {
            it.copy(
                hasLocationPermission = grantedNow,
                locationBannerDismissed = if (grantedNow) false else it.locationBannerDismissed
            )
        }

        if (grantedNow && autoFetchIfNewlyGranted && (!previouslyGranted || (!hasSyncedStartupLocation && preferences.autoLocationEnabled))) {
            hasSyncedStartupLocation = true
            preferences.autoLocationEnabled = true
            _uiState.update { it.copy(isUsingAutoLocation = true) }
            detectLocationAndFetchPrayerTimes()
        }
    }

    fun dismissLocationBanner() {
        preferences.locationBannerDismissed = true
        _uiState.update { it.copy(locationBannerDismissed = true) }
    }

    fun loadPrayerTimes(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val cityId = _uiState.value.cityId
            val cityName = _uiState.value.cityName
            val districtId = _uiState.value.districtId
            val districtName = _uiState.value.districtName

            // 1. Try local cache first for today's date
            val todayKisa = PrayerTimeHelper.getTodayDateKisa()
            val localToday = repository.getPrayerTimesForDate(districtId, todayKisa)
            if (localToday != null && !forceRefresh) {
                currentRawEntity = localToday
                val schedule = PrayerTimeHelper.buildSchedule(localToday)
                _uiState.update {
                    it.copy(
                        schedule = schedule,
                        isLoading = false
                    )
                }
                refreshWeatherForSchedule(schedule, forceRefresh = false)
            }

            // 2. Fetch fresh times from Diyanet API
            val result = repository.fetchAndCachePrayerTimes(cityId, cityName, districtId, districtName)
            result.onSuccess { entities ->
                val todayEntity = entities.find { it.miladiTarihKisa == todayKisa }
                    ?: entities.firstOrNull()

                if (todayEntity != null) {
                    currentRawEntity = todayEntity
                    val schedule = PrayerTimeHelper.buildSchedule(todayEntity)
                    _uiState.update {
                        it.copy(
                            schedule = schedule,
                            monthlyTimes = entities,
                            isLoading = false
                        )
                    }

                    refreshWeatherForSchedule(schedule, forceRefresh = forceRefresh)

                    // Schedule alarm notifications
                    PrayerNotificationHelper.schedulePrayerAlarms(
                        getApplication(),
                        schedule,
                        preferences
                    )

                    // Update AppWidgets with latest prayer times
                    com.example.widget.PrayerWidgetProvider.updateAllWidgets(getApplication())
                } else {
                    _uiState.update { it.copy(isLoading = false, monthlyTimes = entities) }
                }
            }.onFailure { err ->
                if (currentRawEntity == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.localizedMessage ?: "Vakitler yüklenirken bir hata oluştu."
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun selectCity(cityId: String, cityName: String, fromManualPick: Boolean = true) {
        if (fromManualPick) {
            preferences.autoLocationEnabled = false
        }
        preferences.cityId = cityId
        preferences.cityName = cityName
        currentCoordinates = TurkishCitiesData.findCityById(cityId)?.let { it.latitude to it.longitude }
        cachedWeatherResponse = null
        cachedWeatherCoords = null
        _uiState.update {
            it.copy(
                cityId = cityId,
                cityName = cityName,
                isFetchingDistricts = true,
                isUsingAutoLocation = !fromManualPick,
                prayerWeather = emptyMap()
            )
        }
        compassHelper.updateWithCity(cityId)

        viewModelScope.launch {
            val districts = repository.fetchDistricts(cityId)
            val firstDistrict = districts.firstOrNull()
            val newDistrictId = firstDistrict?.ilceId ?: TurkishCitiesData.findCityById(cityId)?.defaultDistrictId ?: "9541"
            val newDistrictName = firstDistrict?.ilceAdi ?: cityName

            preferences.districtId = newDistrictId
            preferences.districtName = newDistrictName

            _uiState.update {
                it.copy(
                    districtId = newDistrictId,
                    districtName = newDistrictName,
                    districts = districts,
                    isFetchingDistricts = false
                )
            }
            loadPrayerTimes(forceRefresh = true)
        }
    }

    fun selectDistrict(districtId: String, districtName: String) {
        preferences.autoLocationEnabled = false
        preferences.districtId = districtId
        preferences.districtName = districtName
        _uiState.update {
            it.copy(
                districtId = districtId,
                districtName = districtName,
                isUsingAutoLocation = false
            )
        }
        loadPrayerTimes(forceRefresh = true)
    }

    private fun loadDistrictsForCurrentCity() {
        viewModelScope.launch {
            val districts = repository.fetchDistricts(_uiState.value.cityId)
            _uiState.update { it.copy(districts = districts) }
        }
    }

    // Notification Settings
    fun togglePrayerNotification(prayerKey: String, enabled: Boolean) {
        preferences.setPrayerNotificationEnabled(prayerKey, enabled)
        val updatedMap = _uiState.value.prayerNotificationToggles.toMutableMap()
        updatedMap[prayerKey] = enabled
        _uiState.update { it.copy(prayerNotificationToggles = updatedMap) }

        _uiState.value.schedule?.let {
            PrayerNotificationHelper.schedulePrayerAlarms(getApplication(), it, preferences)
        }
    }

    fun setNotificationOffsetMinutes(minutes: Int) {
        preferences.notificationOffsetMinutes = minutes
        _uiState.update { it.copy(notificationOffsetMinutes = minutes) }

        _uiState.value.schedule?.let {
            PrayerNotificationHelper.schedulePrayerAlarms(getApplication(), it, preferences)
        }
    }

    fun setNotificationSoundMode(mode: String) {
        preferences.notificationSoundMode = mode
        _uiState.update { it.copy(notificationSoundMode = mode) }

        _uiState.value.schedule?.let {
            PrayerNotificationHelper.schedulePrayerAlarms(getApplication(), it, preferences)
        }
    }

    fun setNotificationRepeatDaily(repeat: Boolean) {
        preferences.notificationRepeatDaily = repeat
        _uiState.update { it.copy(notificationRepeatDaily = repeat) }
    }

    fun triggerTestNotification() {
        val soundMode = _uiState.value.notificationSoundMode
        val title = "Ezan & Namaz Hatırlatıcısı (Test)"
        val message = "Namaz vakitleri bildirim ayarlarınız başarıyla test edildi."

        PrayerNotificationHelper.showNotification(
            context = getApplication(),
            notificationId = 9999,
            title = title,
            content = message,
            soundMode = soundMode
        )

        _uiState.update { it.copy(testNotificationMessage = "Test bildirimi gönderildi!") }
        viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(testNotificationMessage = null) }
        }
    }

    // Tasbih / Zikirmatik
    fun incrementTasbih() {
        val newCount = _uiState.value.tasbihCount + 1
        preferences.tasbihCount = newCount
        _uiState.update { it.copy(tasbihCount = newCount) }
    }

    fun resetTasbih() {
        preferences.tasbihCount = 0
        _uiState.update { it.copy(tasbihCount = 0) }
    }

    fun setTasbihTarget(target: Int) {
        preferences.tasbihTarget = target
        _uiState.update { it.copy(tasbihTarget = target) }
    }

    fun selectTasbihDhikr(index: Int) {
        preferences.tasbihDhikrIndex = index
        _uiState.update { it.copy(tasbihDhikrIndex = index) }
    }

    /**
     * Uses LocationProvider with FusedLocationProviderClient to automatically detect
     * the user's city & district and fetch corresponding prayer times from Diyanet API.
     */
    fun detectLocationAndFetchPrayerTimes(onCompleted: ((Boolean, String) -> Unit)? = null) {
        if (_uiState.value.isDetectingLocation) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDetectingLocation = true,
                    hasLocationPermission = locationProvider.hasLocationPermission(),
                    errorMessage = null
                )
            }
            val result = locationProvider.detectCityAndFetchPrayerTimes()
            result.onSuccess { locationResult ->
                val city = locationResult.cityInfo
                preferences.autoLocationEnabled = true
                preferences.cityId = city.cityId
                preferences.cityName = city.cityName

                // Match geocoded district hint against Diyanet district list if available
                val districts = repository.fetchDistricts(city.cityId)
                val normalizedHint = LocationProvider.normalizeTurkish(city.districtName)
                val matchedDistrict = districts.find {
                    val normDist = LocationProvider.normalizeTurkish(it.ilceAdi)
                    normDist == normalizedHint || normalizedHint.contains(normDist) || normDist.contains(normalizedHint)
                } ?: districts.firstOrNull()

                val resolvedDistrictId = matchedDistrict?.ilceId ?: city.districtId
                val resolvedDistrictName = matchedDistrict?.ilceAdi ?: city.cityName

                preferences.districtId = resolvedDistrictId
                preferences.districtName = resolvedDistrictName

                currentCoordinates = city.latitude to city.longitude
                cachedWeatherResponse = null
                cachedWeatherCoords = null

                compassHelper.updateCoordinates(
                    lat = city.latitude,
                    lon = city.longitude,
                    name = "$resolvedDistrictName, ${city.cityName}",
                    isGps = true
                )

                _uiState.update {
                    it.copy(
                        cityId = city.cityId,
                        cityName = city.cityName,
                        districtId = resolvedDistrictId,
                        districtName = resolvedDistrictName,
                        districts = districts,
                        isDetectingLocation = false,
                        hasLocationPermission = true,
                        isUsingAutoLocation = true,
                        prayerWeather = emptyMap()
                    )
                }

                loadPrayerTimes(forceRefresh = true)
                onCompleted?.invoke(true, "$resolvedDistrictName, ${city.cityName} konumu için vakitler güncellendi.")
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isDetectingLocation = false,
                        hasLocationPermission = locationProvider.hasLocationPermission(),
                        errorMessage = err.localizedMessage ?: "Konum tespit edilemedi."
                    )
                }
                onCompleted?.invoke(false, err.localizedMessage ?: "Konum tespit edilemedi.")
            }
        }
    }

    private fun resolveTargetCoordinates(cityId: String): Pair<Double, Double>? {
        currentCoordinates?.takeIf { _uiState.value.isUsingAutoLocation }?.let { return it }
        return TurkishCitiesData.findCityById(cityId)?.let { it.latitude to it.longitude }
    }

    private fun refreshWeatherForSchedule(schedule: DailyPrayerSchedule, forceRefresh: Boolean) {
        val coords = resolveTargetCoordinates(_uiState.value.cityId) ?: return
        val cached = cachedWeatherResponse
        if (!forceRefresh && cached != null && cachedWeatherCoords == coords) {
            val mapped = WeatherHelper.mapHourlyWeatherToPrayers(schedule, cached)
            if (mapped.isNotEmpty()) {
                _uiState.update { it.copy(prayerWeather = mapped) }
                return
            }
        }

        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.weatherApi.getHourlyWeather(
                        latitude = coords.first,
                        longitude = coords.second
                    )
                }
                cachedWeatherCoords = coords
                cachedWeatherResponse = response
                val mapped = WeatherHelper.mapHourlyWeatherToPrayers(schedule, response)
                _uiState.update { it.copy(prayerWeather = mapped) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Keep default prayer icons gracefully if weather fetch fails
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        weatherJob?.cancel()
        compassHelper.stopListening()
    }
}
