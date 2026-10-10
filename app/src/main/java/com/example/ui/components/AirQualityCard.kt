package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AirQualityData

@Composable
fun AirQualityCard(
    airQuality: AirQualityData,
    modifier: Modifier = Modifier
) {
    val statusColor = when (airQuality.aqi) {
        in 0..20 -> Color(0xFF4ADE80) // Green
        in 21..40 -> Color(0xFFA3E635) // Lime
        in 41..60 -> Color(0xFFFBBF24) // Amber
        in 61..80 -> Color(0xFFFB923C) // Orange
        else -> Color(0xFFF87171)     // Red
    }

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
                    imageVector = Icons.Rounded.Air,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CALIDAD DEL AIRE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Índice: ${airQuality.aqi} AQI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Condición: ${airQuality.status}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = airQuality.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pollutants breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PollutantItem(name = "PM2.5", value = "%.1f µg/m³".format(airQuality.pm25))
                PollutantItem(name = "PM10", value = "%.1f µg/m³".format(airQuality.pm10))
                PollutantItem(name = "Ozono (O₃)", value = "%.1f µg/m³".format(airQuality.ozone))
                PollutantItem(name = "NO₂", value = "%.1f µg/m³".format(airQuality.no2))
            }
        }
    }
}

@Composable
private fun PollutantItem(name: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.65f),
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
