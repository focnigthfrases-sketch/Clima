package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrentWeather
import com.example.model.TemperatureUnit
import com.example.model.WeatherConditionHelper

@Composable
fun WeatherHeader(
    weather: CurrentWeather,
    tempUnit: TemperatureUnit,
    onToggleUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conditionInfo = WeatherConditionHelper.getInfo(weather.weatherCode, weather.isDay)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Location Title & GPS Tag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (weather.isGpsLocation) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = "Ubicación GPS actual",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = weather.cityName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        if (weather.country.isNotBlank() || weather.regionName.isNotBlank()) {
            val subtitle = listOfNotNull(
                weather.regionName.takeIf { it.isNotBlank() },
                weather.country.takeIf { it.isNotBlank() }
            ).joinToString(", ")
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.8f)
                ),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Main Temperature Display with Unit Toggle
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.clickable { onToggleUnit() }
        ) {
            Text(
                text = "%.0f".format(tempUnit.convert(weather.temp)),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 86.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                ),
                modifier = Modifier.testTag("current_temperature_text")
            )
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier
                    .padding(top = 10.dp, start = 4.dp)
                    .clip(CircleShape)
                    .testTag("temp_unit_toggle")
            ) {
                Text(
                    text = tempUnit.symbol,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Weather Condition Pill
        Surface(
            color = Color.White.copy(alpha = 0.18f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = conditionInfo.icon,
                    contentDescription = conditionInfo.description,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = weather.condition,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Feels like & High/Low
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sensación: ${tempUnit.format(weather.feelsLike)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.9f)
                )
            )
            Text(
                text = "•",
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = "Mín: ${tempUnit.format(weather.minTemp)}  Máx: ${tempUnit.format(weather.maxTemp)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
