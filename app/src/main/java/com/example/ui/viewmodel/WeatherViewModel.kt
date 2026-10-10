package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeocodingResultDto
import com.example.data.db.AppDatabase
import com.example.data.db.SavedCity
import com.example.data.location.LocationHelper
import com.example.data.repository.WeatherRepository
import com.example.model.CurrentWeather
import com.example.model.TemperatureUnit
import com.example.model.UserWeatherPreferences
import com.example.model.WindUnit
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ActiveSheet {
    SEARCH,
    SAVED_CITIES,
    SETTINGS
}

data class WeatherUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val weather: CurrentWeather? = null,
    val selectedCity: SavedCity? = null,
    val savedCities: List<SavedCity> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<GeocodingResultDto> = emptyList(),
    val activeSheet: ActiveSheet? = null,
    val errorMessage: String? = null,
    val preferences: UserWeatherPreferences = UserWeatherPreferences(),
    val isLocationLoading: Boolean = false
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = WeatherRepository(database.cityDao())
    private val locationHelper = LocationHelper(application)

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Observe saved cities
        viewModelScope.launch {
            repository.savedCities.collectLatest { cities ->
                _uiState.update { it.copy(savedCities = cities) }
                // If no city selected yet, pick the first one (or Madrid default)
                if (_uiState.value.selectedCity == null && cities.isNotEmpty()) {
                    val defaultCity = cities.firstOrNull { it.isCurrentLocation }
                        ?: cities.firstOrNull { it.isDefault }
                        ?: cities.first()
                    loadWeatherForCity(defaultCity)
                }
            }
        }
    }

    fun loadWeatherForCity(city: SavedCity, isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedCity = city,
                    isLoading = if (isRefresh) it.isLoading else true,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            val result = repository.getWeatherData(
                latitude = city.latitude,
                longitude = city.longitude,
                cityName = city.name,
                regionName = city.admin1 ?: "",
                countryName = city.country,
                isGps = city.isCurrentLocation
            )

            result.onSuccess { weather ->
                _uiState.update {
                    it.copy(
                        weather = weather,
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = err.message ?: "No se pudo cargar la información del tiempo"
                    )
                }
            }
        }
    }

    fun refreshWeather() {
        val currentCity = _uiState.value.selectedCity
        if (currentCity != null) {
            loadWeatherForCity(currentCity, isRefresh = true)
        } else {
            val currentWeather = _uiState.value.weather
            if (currentWeather != null) {
                loadWeatherByCoords(
                    lat = currentWeather.latitude,
                    lon = currentWeather.longitude,
                    name = currentWeather.cityName,
                    region = currentWeather.regionName,
                    country = currentWeather.country,
                    isGps = currentWeather.isGpsLocation,
                    isRefresh = true
                )
            }
        }
    }

    fun requestGpsLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocationLoading = true, errorMessage = null) }
            val location = locationHelper.getCurrentLocation()
            if (location != null) {
                val gpsCity = repository.addFavoriteCity(
                    name = location.cityName,
                    country = location.countryName,
                    admin1 = null,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    isGps = true
                )
                loadWeatherForCity(gpsCity)
                _uiState.update { it.copy(isLocationLoading = false) }
            } else {
                _uiState.update {
                    it.copy(
                        isLocationLoading = false,
                        errorMessage = "No se pudo obtener la ubicación GPS. Verifica los permisos de localización."
                    )
                }
            }
        }
    }

    fun loadWeatherByCoords(
        lat: Double,
        lon: Double,
        name: String,
        region: String,
        country: String,
        isGps: Boolean = false,
        isRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = if (isRefresh) it.isLoading else true,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            val result = repository.getWeatherData(
                latitude = lat,
                longitude = lon,
                cityName = name,
                regionName = region,
                countryName = country,
                isGps = isGps
            )

            result.onSuccess { weather ->
                _uiState.update {
                    it.copy(
                        weather = weather,
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = err.message ?: "Error al actualizar"
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(350) // Debounce
            _uiState.update { it.copy(isSearching = true) }
            val results = repository.searchCities(query.trim())
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    fun selectSearchResult(result: GeocodingResultDto) {
        viewModelScope.launch {
            val savedCity = repository.addFavoriteCity(
                name = result.name,
                country = result.country ?: result.countryCode ?: "",
                admin1 = result.admin1,
                latitude = result.latitude,
                longitude = result.longitude,
                isGps = false
            )
            _uiState.update {
                it.copy(
                    activeSheet = null,
                    searchQuery = "",
                    searchResults = emptyList()
                )
            }
            loadWeatherForCity(savedCity)
        }
    }

    fun addCityToFavorites(cityName: String, country: String, admin1: String?, lat: Double, lon: Double) {
        viewModelScope.launch {
            val saved = repository.addFavoriteCity(
                name = cityName,
                country = country,
                admin1 = admin1,
                latitude = lat,
                longitude = lon,
                isGps = false
            )
            _uiState.update { it.copy(selectedCity = saved) }
        }
    }

    fun removeCity(city: SavedCity) {
        viewModelScope.launch {
            repository.removeFavoriteCity(city)
            if (_uiState.value.selectedCity?.id == city.id) {
                val remaining = _uiState.value.savedCities.filter { it.id != city.id }
                if (remaining.isNotEmpty()) {
                    loadWeatherForCity(remaining.first())
                }
            }
        }
    }

    fun toggleTempUnit() {
        _uiState.update { current ->
            val next = if (current.preferences.tempUnit == TemperatureUnit.CELSIUS) {
                TemperatureUnit.FAHRENHEIT
            } else {
                TemperatureUnit.CELSIUS
            }
            current.copy(preferences = current.preferences.copy(tempUnit = next))
        }
    }

    fun setWindUnit(unit: WindUnit) {
        _uiState.update { current ->
            current.copy(preferences = current.preferences.copy(windUnit = unit))
        }
    }

    fun setTempUnit(unit: TemperatureUnit) {
        _uiState.update { current ->
            current.copy(preferences = current.preferences.copy(tempUnit = unit))
        }
    }

    fun setPressureUnit(unit: com.example.model.PressureUnit) {
        _uiState.update { current ->
            current.copy(preferences = current.preferences.copy(pressureUnit = unit))
        }
    }

    fun setTimeFormat(format: com.example.model.TimeFormat) {
        _uiState.update { current ->
            current.copy(preferences = current.preferences.copy(timeFormat = format))
        }
    }

    fun setThemeMode(mode: com.example.model.ThemeMode) {
        _uiState.update { current ->
            current.copy(preferences = current.preferences.copy(themeMode = mode))
        }
    }

    fun setUpdateInterval(interval: com.example.model.UpdateInterval) {
        _uiState.update { current ->
            current.copy(preferences = current.preferences.copy(updateInterval = interval))
        }
    }

    fun toggleAdsBanner() {
        _uiState.update { current ->
            val next = !current.preferences.showAdsBanner
            current.copy(preferences = current.preferences.copy(showAdsBanner = next))
        }
    }

    fun toggleSkyEffects() {
        _uiState.update { current ->
            val next = !current.preferences.animatedSkyEffects
            current.copy(preferences = current.preferences.copy(animatedSkyEffects = next))
        }
    }

    fun toggleMorningDigest() {
        _uiState.update { current ->
            val next = !current.preferences.morningDigestNotification
            current.copy(preferences = current.preferences.copy(morningDigestNotification = next))
        }
    }

    fun toggleSevereWeatherAlerts() {
        _uiState.update { current ->
            val next = !current.preferences.severeWeatherAlerts
            current.copy(preferences = current.preferences.copy(severeWeatherAlerts = next))
        }
    }

    fun toggleVibrationAlerts() {
        _uiState.update { current ->
            val next = !current.preferences.vibrationAlerts
            current.copy(preferences = current.preferences.copy(vibrationAlerts = next))
        }
    }

    fun toggleWifiOnly() {
        _uiState.update { current ->
            val next = !current.preferences.wifiOnlyUpdate
            current.copy(preferences = current.preferences.copy(wifiOnlyUpdate = next))
        }
    }

    fun togglePreciseGps() {
        _uiState.update { current ->
            val next = !current.preferences.preciseGpsEnabled
            current.copy(preferences = current.preferences.copy(preciseGpsEnabled = next))
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            refreshWeather()
        }
    }

    fun openSheet(sheet: ActiveSheet) {
        _uiState.update { it.copy(activeSheet = sheet) }
    }

    fun closeSheet() {
        _uiState.update {
            it.copy(
                activeSheet = null,
                searchQuery = "",
                searchResults = emptyList()
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
