package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class WeatherConditionCategory {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    THUNDERSTORM
}

object WeatherConditionHelper {
    fun getInfo(code: Int, isDay: Boolean = true): WeatherConditionInfo {
        return when (code) {
            0 -> WeatherConditionInfo(
                description = if (isDay) "Despejado" else "Noche despejada",
                icon = if (isDay) Icons.Rounded.WbSunny else Icons.Rounded.NightlightRound,
                category = WeatherConditionCategory.CLEAR
            )
            1 -> WeatherConditionInfo(
                description = "Mayormente despejado",
                icon = if (isDay) Icons.Rounded.WbSunny else Icons.Rounded.NightlightRound,
                category = WeatherConditionCategory.CLEAR
            )
            2 -> WeatherConditionInfo(
                description = "Parcialmente nublado",
                icon = if (isDay) Icons.Rounded.CloudQueue else Icons.Rounded.Cloud,
                category = WeatherConditionCategory.PARTLY_CLOUDY
            )
            3 -> WeatherConditionInfo(
                description = "Nublado",
                icon = Icons.Rounded.Cloud,
                category = WeatherConditionCategory.CLOUDY
            )
            45, 48 -> WeatherConditionInfo(
                description = "Niebla densa",
                icon = Icons.Rounded.BlurOn,
                category = WeatherConditionCategory.FOG
            )
            51, 53, 55 -> WeatherConditionInfo(
                description = "Llovizna leve",
                icon = Icons.Rounded.Grain,
                category = WeatherConditionCategory.DRIZZLE
            )
            56, 57 -> WeatherConditionInfo(
                description = "Llovizna helada",
                icon = Icons.Rounded.AcUnit,
                category = WeatherConditionCategory.DRIZZLE
            )
            61, 63 -> WeatherConditionInfo(
                description = "Lluvia moderada",
                icon = Icons.Rounded.WaterDrop,
                category = WeatherConditionCategory.RAIN
            )
            65 -> WeatherConditionInfo(
                description = "Lluvia fuerte",
                icon = Icons.Rounded.Thunderstorm,
                category = WeatherConditionCategory.RAIN
            )
            66, 67 -> WeatherConditionInfo(
                description = "Lluvia engelante",
                icon = Icons.Rounded.AcUnit,
                category = WeatherConditionCategory.RAIN
            )
            71, 73, 75, 77 -> WeatherConditionInfo(
                description = "Nieve",
                icon = Icons.Rounded.AcUnit,
                category = WeatherConditionCategory.SNOW
            )
            80, 81, 82 -> WeatherConditionInfo(
                description = "Chubascos",
                icon = Icons.Rounded.WaterDrop,
                category = WeatherConditionCategory.RAIN
            )
            85, 86 -> WeatherConditionInfo(
                description = "Chubascos de nieve",
                icon = Icons.Rounded.AcUnit,
                category = WeatherConditionCategory.SNOW
            )
            95 -> WeatherConditionInfo(
                description = "Tormenta eléctrica",
                icon = Icons.Rounded.Thunderstorm,
                category = WeatherConditionCategory.THUNDERSTORM
            )
            96, 99 -> WeatherConditionInfo(
                description = "Tormenta con granizo",
                icon = Icons.Rounded.Thunderstorm,
                category = WeatherConditionCategory.THUNDERSTORM
            )
            else -> WeatherConditionInfo(
                description = "Tiempo variable",
                icon = Icons.Rounded.WbCloudy,
                category = WeatherConditionCategory.PARTLY_CLOUDY
            )
        }
    }
}

data class WeatherConditionInfo(
    val description: String,
    val icon: ImageVector,
    val category: WeatherConditionCategory
)

data class HourlyForecastItem(
    val timeStr: String,
    val temp: Double,
    val feelsLike: Double,
    val weatherCode: Int,
    val condition: String,
    val rainChance: Int,
    val isDay: Boolean
)

data class DailyForecastItem(
    val dayName: String,
    val dateStr: String,
    val weatherCode: Int,
    val condition: String,
    val minTemp: Double,
    val maxTemp: Double,
    val rainChance: Int,
    val uvIndex: Double,
    val sunrise: String,
    val sunset: String
)

data class AirQualityData(
    val aqi: Int,
    val status: String,
    val pm25: Double,
    val pm10: Double,
    val ozone: Double,
    val no2: Double
)

data class CurrentWeather(
    val cityName: String,
    val regionName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val temp: Double,
    val feelsLike: Double,
    val minTemp: Double,
    val maxTemp: Double,
    val weatherCode: Int,
    val condition: String,
    val isDay: Boolean,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: Double,
    val surfacePressure: Double,
    val uvIndex: Double,
    val precipitationChance: Int,
    val sunrise: String,
    val sunset: String,
    val hourly: List<HourlyForecastItem>,
    val daily: List<DailyForecastItem>,
    val airQuality: AirQualityData? = null,
    val lastUpdatedFormatted: String,
    val isGpsLocation: Boolean = false
)

enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F");

    fun convert(tempC: Double): Double {
        return when (this) {
            CELSIUS -> tempC
            FAHRENHEIT -> (tempC * 9.0 / 5.0) + 32.0
        }
    }

    fun format(tempC: Double): String {
        val converted = convert(tempC)
        return "%.0f%s".format(converted, symbol)
    }
}

enum class WindUnit(val label: String) {
    KMH("km/h"),
    MPH("mph"),
    MS("m/s");

    fun convert(kmh: Double): Double {
        return when (this) {
            KMH -> kmh
            MPH -> kmh * 0.621371
            MS -> kmh / 3.6
        }
    }

    fun format(kmh: Double): String {
        return "%.1f %s".format(convert(kmh), label)
    }
}

enum class PressureUnit(val label: String) {
    HPA("hPa"),
    INHG("inHg"),
    MMHG("mmHg"),
    MBAR("mbar");

    fun convert(hpa: Double): Double {
        return when (this) {
            HPA -> hpa
            INHG -> hpa * 0.02953
            MMHG -> hpa * 0.750062
            MBAR -> hpa
        }
    }

    fun format(hpa: Double): String {
        return when (this) {
            INHG -> "%.2f %s".format(convert(hpa), label)
            else -> "%.0f %s".format(convert(hpa), label)
        }
    }
}

enum class TimeFormat(val label: String) {
    H24("24 Horas"),
    H12("12 Horas (AM/PM)")
}

enum class ThemeMode(val label: String) {
    SYSTEM("Sistema"),
    DARK("Oscuro"),
    LIGHT("Claro")
}

enum class UpdateInterval(val label: String) {
    MIN_15("15 min"),
    MIN_30("30 min"),
    HOUR_1("1 hora"),
    MANUAL("Manual")
}

data class UserWeatherPreferences(
    val tempUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windUnit: WindUnit = WindUnit.KMH,
    val pressureUnit: PressureUnit = PressureUnit.HPA,
    val timeFormat: TimeFormat = TimeFormat.H24,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showAdsBanner: Boolean = true,
    val animatedSkyEffects: Boolean = true,
    val morningDigestNotification: Boolean = true,
    val severeWeatherAlerts: Boolean = true,
    val vibrationAlerts: Boolean = true,
    val wifiOnlyUpdate: Boolean = false,
    val updateInterval: UpdateInterval = UpdateInterval.MIN_30,
    val preciseGpsEnabled: Boolean = true
)

