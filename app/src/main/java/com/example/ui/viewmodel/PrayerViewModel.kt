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
import com.example.data.remote.NetworkClient
import com.example.data.repository.PrayerRepository
import com.example.util.CompassSensorHelper
import com.example.util.CompassState
import com.example.util.PrayerNotificationHelper
import com.example.util.PrayerTimeHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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
    val isDetectingLocation: Boolean = false
)

class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = PrayerRepository(db.prayerDao(), NetworkClient.diyanetApi)
    val preferences = PreferencesManager(application)
    val compassHelper = CompassSensorHelper(application)
    val locationProvider = com.example.util.LocationProvider(application)

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
            tasbihDhikrIndex = preferences.tasbihDhikrIndex
        )
    )
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    val compassState: StateFlow<CompassState> = compassHelper.compassState

    private var currentRawEntity: PrayerTimeEntity? = null
    private var tickerJob: Job? = null

    init {
        PrayerNotificationHelper.createNotificationChannel(application)
        loadPrayerTimes()
        startClockTicker()
        loadDistrictsForCurrentCity()
        compassHelper.updateWithCity(preferences.cityId)
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

    fun selectCity(cityId: String, cityName: String) {
        preferences.cityId = cityId
        preferences.cityName = cityName
        _uiState.update {
            it.copy(
                cityId = cityId,
                cityName = cityName,
                isFetchingDistricts = true
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
        preferences.districtId = districtId
        preferences.districtName = districtName
        _uiState.update {
            it.copy(
                districtId = districtId,
                districtName = districtName
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
     * the user's city and fetch corresponding prayer times from the Retrofit service.
     */
    fun detectLocationAndFetchPrayerTimes(onCompleted: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDetectingLocation = true, errorMessage = null) }
            val result = locationProvider.detectCityAndFetchPrayerTimes()
            result.onSuccess { locationResult ->
                val city = locationResult.cityInfo
                selectCity(city.cityId, city.cityName)
                _uiState.update { it.copy(isDetectingLocation = false) }
                onCompleted?.invoke(true, "${city.cityName} konumu belirlendi ve vakitler güncellendi.")
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isDetectingLocation = false,
                        errorMessage = err.localizedMessage ?: "Konum tespit edilemedi."
                    )
                }
                onCompleted?.invoke(false, err.localizedMessage ?: "Konum tespit edilemedi.")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        compassHelper.stopListening()
    }
}
