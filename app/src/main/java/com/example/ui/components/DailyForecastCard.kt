package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyForecastItem
import com.example.model.TemperatureUnit
import com.example.model.WeatherConditionHelper

@Composable
fun DailyForecastCard(
    dailyItems: List<DailyForecastItem>,
    tempUnit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    if (dailyItems.isEmpty()) return

    // Find overall min and max across all days for normalized temperature bars
    val weekMin = dailyItems.minOfOrNull { it.minTemp } ?: 0.0
    val weekMax = dailyItems.maxOfOrNull { it.maxTemp } ?: 30.0
    val totalRange = (weekMax - weekMin).coerceAtLeast(1.0)

    Surface(
        color = Color.Black.copy(alpha = 0.22f),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PRONÓSTICO 7 DÍAS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            dailyItems.forEachIndexed { index, item ->
                DailyForecastRow(
                    item = item,
                    tempUnit = tempUnit,
                    weekMin = weekMin,
                    weekMax = weekMax,
                    totalRange = totalRange
                )
                if (index < dailyItems.size - 1) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRow(
    item: DailyForecastItem,
    tempUnit: TemperatureUnit,
    weekMin: Double,
    weekMax: Double,
    totalRange: Double,
    modifier: Modifier = Modifier
) {
    val conditionInfo = WeatherConditionHelper.getInfo(item.weatherCode, true)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        // Day Name
        Text(
            text = item.dayName,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = if (item.dayName == "Hoy") FontWeight.Bold else FontWeight.Medium
            ),
            modifier = Modifier.width(68.dp)
        )

        // Weather Icon
        Icon(
            imageVector = conditionInfo.icon,
            contentDescription = conditionInfo.description,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Rain Probability
        Box(
            modifier = Modifier.width(36.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (item.rainChance > 15) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.WaterDrop,
                        contentDescription = null,
                        tint = Color(0xFF67E8F9),
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "${item.rainChance}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF67E8F9),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Min Temp
        Text(
            text = tempUnit.format(item.minTemp),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.width(42.dp)
        )

        // Temperature range gradient bar
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .padding(horizontal = 4.dp)
        ) {
            val barWidth = size.width
            val barHeight = size.height

            // Background track
            drawRoundRect(
                color = Color.White.copy(alpha = 0.15f),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
            )

            // Active range
            val startRatio = ((item.minTemp - weekMin) / totalRange).toFloat().coerceIn(0f, 1f)
            val endRatio = ((item.maxTemp - weekMin) / totalRange).toFloat().coerceIn(0f, 1f)
            val left = startRatio * barWidth
            val width = ((endRatio - startRatio) * barWidth).coerceAtLeast(6f)

            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF38BDF8), // Cool cyan/sky
                        Color(0xFFFBBF24), // Warm amber
                        Color(0xFFF97316)  // Warm orange
                    ),
                    startX = 0f,
                    endX = barWidth
                ),
                topLeft = Offset(left, 0f),
                size = Size(width, barHeight),
                cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Max Temp
        Text(
            text = tempUnit.format(item.maxTemp),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.width(42.dp)
        )
    }
}
