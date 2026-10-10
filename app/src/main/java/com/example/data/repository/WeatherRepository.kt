package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.api.GeocodingResultDto
import com.example.data.db.CityDao
import com.example.data.db.SavedCity
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class WeatherRepository(
    private val cityDao: CityDao
) {
    val savedCities: Flow<List<SavedCity>> = cityDao.getAllCities()

    suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        cityName: String,
        regionName: String,
        countryName: String,
        isGps: Boolean = false
    ): Result<CurrentWeather> = withContext(Dispatchers.IO) {
        try {
            val forecastResponse = ApiClient.weatherApi.getForecast(
                latitude = latitude,
                longitude = longitude
            )

            val airQualityResponse = try {
                ApiClient.airQualityApi.getAirQuality(
                    latitude = latitude,
                    longitude = longitude
                )
            } catch (e: Exception) {
                null
            }

            val currentDto = forecastResponse.current
                ?: return@withContext Result.failure(Exception("Datos actuales no disponibles"))

            val hourlyDto = forecastResponse.hourly
            val dailyDto = forecastResponse.daily

            // Parse Hourly
            val hourlyItems = mutableListOf<HourlyForecastItem>()
            if (hourlyDto != null && hourlyDto.time.isNotEmpty()) {
                val currentTimeIso = currentDto.time
                // Find index closest to now
                val startIndex = hourlyDto.time.indexOfFirst { it >= currentTimeIso }.let {
                    if (it == -1) 0 else it
                }
                val endIndex = (startIndex + 24).coerceAtMost(hourlyDto.time.size)

                val inputIsoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
                val hourOutputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                for (i in startIndex until endIndex) {
                    val rawTime = hourlyDto.time.getOrNull(i) ?: continue
                    val temp = hourlyDto.temperature2m.getOrNull(i) ?: 0.0
                    val feelsLike = hourlyDto.apparentTemperature?.getOrNull(i) ?: temp
                    val code = hourlyDto.weatherCode.getOrNull(i) ?: 0
                    val rainProb = hourlyDto.precipitationProbability?.getOrNull(i) ?: 0

                    val parsedDate = try {
                        inputIsoFormat.parse(rawTime)
                    } catch (e: Exception) {
                        null
                    }

                    val hourCalendar = Calendar.getInstance().apply {
                        if (parsedDate != null) time = parsedDate
                    }
                    val hourOfDay = hourCalendar.get(Calendar.HOUR_OF_DAY)
                    val isHourDay = hourOfDay in 6..20

                    val timeStr = if (i == startIndex) "Ahora" else {
                        if (parsedDate != null) hourOutputFormat.format(parsedDate) else rawTime.takeLast(5)
                    }

                    val conditionInfo = WeatherConditionHelper.getInfo(code, isHourDay)

                    hourlyItems.add(
                        HourlyForecastItem(
                            timeStr = timeStr,
                            temp = temp,
                            feelsLike = feelsLike,
                            weatherCode = code,
                            condition = conditionInfo.description,
                            rainChance = rainProb,
                            isDay = isHourDay
                        )
                    )
                }
            }

            // Parse Daily
            val dailyItems = mutableListOf<DailyForecastItem>()
            if (dailyDto != null && dailyDto.time.isNotEmpty()) {
                val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dayOfWeekFormat = SimpleDateFormat("EEE", Locale("es"))
                val dateDisplayFormat = SimpleDateFormat("d MMM", Locale("es"))

                val count = dailyDto.time.size.coerceAtMost(7)
                for (i in 0 until count) {
                    val rawDateStr = dailyDto.time[i]
                    val code = dailyDto.weatherCode.getOrNull(i) ?: 0
                    val min = dailyDto.temperature2mMin.getOrNull(i) ?: 0.0
                    val max = dailyDto.temperature2mMax.getOrNull(i) ?: 0.0
                    val rainProb = dailyDto.precipitationProbabilityMax?.getOrNull(i) ?: 0
                    val uv = dailyDto.uvIndexMax?.getOrNull(i) ?: 0.0
                    val rawSunrise = dailyDto.sunrise?.getOrNull(i)?.takeLast(5) ?: "06:00"
                    val rawSunset = dailyDto.sunset?.getOrNull(i)?.takeLast(5) ?: "20:00"

                    val parsedDate = try {
                        inputDateFormat.parse(rawDateStr)
                    } catch (e: Exception) {
                        null
                    }

                    val dayName = if (i == 0) "Hoy" else if (i == 1) "Mañana" else {
                        if (parsedDate != null) dayOfWeekFormat.format(parsedDate).replaceFirstChar { it.uppercase() }
                        else "Día ${i + 1}"
                    }

                    val dateFormatted = if (parsedDate != null) dateDisplayFormat.format(parsedDate) else rawDateStr
                    val conditionInfo = WeatherConditionHelper.getInfo(code, true)

                    dailyItems.add(
                        DailyForecastItem(
                            dayName = dayName,
                            dateStr = dateFormatted,
                            weatherCode = code,
                            condition = conditionInfo.description,
                            minTemp = min,
                            maxTemp = max,
                            rainChance = rainProb,
                            uvIndex = uv,
                            sunrise = rawSunrise,
                            sunset = rawSunset
                        )
                    )
                }
            }

            // Parse Air Quality
            val airQualityData = airQualityResponse?.current?.let { aqDto ->
                val aqi = aqDto.europeanAqi ?: 0
                val status = when (aqi) {
                    in 0..20 -> "Excelente"
                    in 21..40 -> "Buena"
                    in 41..60 -> "Moderada"
                    in 61..80 -> "Pobre"
                    in 81..100 -> "Muy pobre"
                    else -> "Sin datos"
                }
                AirQualityData(
                    aqi = aqi,
                    status = status,
                    pm25 = aqDto.pm25 ?: 0.0,
                    pm10 = aqDto.pm10 ?: 0.0,
                    ozone = aqDto.ozone ?: 0.0,
                    no2 = aqDto.nitrogenDioxide ?: 0.0
                )
            }

            val isDay = currentDto.isDay == 1
            val conditionInfo = WeatherConditionHelper.getInfo(currentDto.weatherCode, isDay)

            val minTempToday = dailyItems.firstOrNull()?.minTemp ?: (currentDto.temperature2m - 4.0)
            val maxTempToday = dailyItems.firstOrNull()?.maxTemp ?: (currentDto.temperature2m + 5.0)
            val sunriseToday = dailyItems.firstOrNull()?.sunrise ?: "06:45"
            val sunsetToday = dailyItems.firstOrNull()?.sunset ?: "20:15"
            val uvToday = dailyItems.firstOrNull()?.uvIndex ?: 4.0
            val rainChanceToday = dailyItems.firstOrNull()?.rainChance ?: 10

            val nowFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            val resultWeather = CurrentWeather(
                cityName = cityName,
                regionName = regionName,
                country = countryName,
                latitude = latitude,
                longitude = longitude,
                temp = currentDto.temperature2m,
                feelsLike = currentDto.apparentTemperature ?: currentDto.temperature2m,
                minTemp = minTempToday,
                maxTemp = maxTempToday,
                weatherCode = currentDto.weatherCode,
                condition = conditionInfo.description,
                isDay = isDay,
                humidity = currentDto.relativeHumidity2m ?: 50,
                windSpeed = currentDto.windSpeed10m ?: 12.0,
                windDirection = currentDto.windDirection10m ?: 180.0,
                surfacePressure = currentDto.surfacePressure ?: 1013.2,
                uvIndex = uvToday,
                precipitationChance = rainChanceToday,
                sunrise = sunriseToday,
                sunset = sunsetToday,
                hourly = hourlyItems,
                daily = dailyItems,
                airQuality = airQualityData,
                lastUpdatedFormatted = nowFormatted,
                isGpsLocation = isGps
            )

            // Update cached temp in Room if exists
            val existing = cityDao.getCityByCoordinates(latitude, longitude)
            if (existing != null) {
                cityDao.updateCity(
                    existing.copy(
                        cachedTemp = currentDto.temperature2m,
                        cachedWeatherCode = currentDto.weatherCode,
                        cachedCondition = conditionInfo.description,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }

            Result.success(resultWeather)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun searchCities(query: String): List<GeocodingResultDto> = withContext(Dispatchers.IO) {
        if (query.isBlank() || query.length < 2) return@withContext emptyList()
        try {
            val response = ApiClient.geocodingApi.searchLocations(query)
            response.results ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addFavoriteCity(
        name: String,
        country: String,
        admin1: String?,
        latitude: Double,
        longitude: Double,
        isGps: Boolean = false
    ): SavedCity = withContext(Dispatchers.IO) {
        val existing = cityDao.getCityByCoordinates(latitude, longitude)
        if (existing != null) {
            return@withContext existing
        }
        val newCity = SavedCity(
            name = name,
            country = country,
            admin1 = admin1,
            latitude = latitude,
            longitude = longitude,
            isCurrentLocation = isGps
        )
        val id = cityDao.insertCity(newCity)
        newCity.copy(id = id)
    }

    suspend fun removeFavoriteCity(city: SavedCity) = withContext(Dispatchers.IO) {
        cityDao.deleteCity(city)
    }

    suspend fun countCities(): Int = withContext(Dispatchers.IO) {
        cityDao.countCities()
    }
}
