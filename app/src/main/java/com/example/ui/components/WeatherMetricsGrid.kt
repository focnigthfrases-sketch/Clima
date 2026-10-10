package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrentWeather
import com.example.model.PressureUnit
import com.example.model.WindUnit

@Composable
fun WeatherMetricsGrid(
    weather: CurrentWeather,
    windUnit: WindUnit,
    pressureUnit: PressureUnit = PressureUnit.HPA,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Viento & Humedad
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val windDirectionCardinal = getWindCardinal(weather.windDirection)
            MetricCard(
                title = "VIENTO",
                icon = Icons.Rounded.Air,
                value = windUnit.format(weather.windSpeed),
                subtitle = "Dirección: $windDirectionCardinal (${weather.windDirection.toInt()}°)",
                modifier = Modifier.weight(1f)
            )

            val humidityDesc = when {
                weather.humidity < 30 -> "Ambiente seco"
                weather.humidity < 60 -> "Nivel óptimo"
                weather.humidity < 80 -> "Humedad moderada"
                else -> "Muy húmedo"
            }
            MetricCard(
                title = "HUMEDAD",
                icon = Icons.Rounded.WaterDrop,
                value = "${weather.humidity}%",
                subtitle = humidityDesc,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Índice UV & Presión
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val uvDesc = when {
                weather.uvIndex < 3 -> "Bajo"
                weather.uvIndex < 6 -> "Moderado"
                weather.uvIndex < 8 -> "Alto"
                weather.uvIndex < 11 -> "Muy alto"
                else -> "Extremo"
            }
            MetricCard(
                title = "ÍNDICE UV",
                icon = Icons.Rounded.WbSunny,
                value = "%.0f - %s".format(weather.uvIndex, uvDesc),
                subtitle = if (weather.uvIndex >= 6) "Usa protección solar" else "Riesgo reducido",
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "PRESIÓN",
                icon = Icons.Rounded.Speed,
                value = pressureUnit.format(weather.surfacePressure),
                subtitle = if (weather.surfacePressure > 1013) "Alta presión" else "Baja presión",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Amanecer y Atardecer
        SunCycleCard(
            sunrise = weather.sunrise,
            sunset = weather.sunset,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    icon: ImageVector,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.22f),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.height(130.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.8f)
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun SunCycleCard(
    sunrise: String,
    sunset: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.22f),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.WbTwilight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SOL Y HORARIO SOLAR",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visual Sun Arc
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                val w = size.width
                val h = size.height

                // Draw horizon line
                drawLine(
                    color = Color.White.copy(alpha = 0.2f),
                    start = Offset(0f, h - 6f),
                    end = Offset(w, h - 6f),
                    strokeWidth = 2f
                )

                // Draw parabolic arc for sun path
                val path = Path().apply {
                    moveTo(20f, h - 6f)
                    quadraticTo(w / 2f, -10f, w - 20f, h - 6f)
                }

                drawPath(
                    path = path,
                    color = Color(0xFFFBBF24).copy(alpha = 0.7f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Amanecer",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    )
                    Text(
                        text = sunrise,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Atardecer",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    )
                    Text(
                        text = sunset,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

private fun getWindCardinal(degrees: Double): String {
    val directions = arrayOf("N", "NE", "E", "SE", "S", "SO", "O", "NO")
    val index = (((degrees + 22.5) % 360) / 45).toInt()
    return directions.getOrElse(index) { "N" }
}
