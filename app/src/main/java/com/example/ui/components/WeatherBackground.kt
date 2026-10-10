package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.model.WeatherConditionCategory
import kotlin.random.Random

object WeatherGradients {
    // Clear Day
    val clearDay = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0284C7), // Sky Blue 600
            Color(0xFF38BDF8), // Light Sky 400
            Color(0xFF7DD3FC)  // Soft Cyan
        )
    )

    // Clear Night
    val clearNight = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Slate 900
            Color(0xFF1E1B4B), // Indigo 950
            Color(0xFF312E81)  // Indigo 900
        )
    )

    // Cloudy Day
    val cloudyDay = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF475569), // Slate 600
            Color(0xFF64748B), // Slate 500
            Color(0xFF94A3B8)  // Slate 400
        )
    )

    // Cloudy Night
    val cloudyNight = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Dark Slate
            Color(0xFF1E293B), // Slate 800
            Color(0xFF334155)  // Slate 700
        )
    )

    // Rain / Drizzle
    val rain = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E293B), // Slate 800
            Color(0xFF334E68), // Deep Blue Slate
            Color(0xFF486581)  // Blue Slate
        )
    )

    // Thunderstorm
    val thunderstorm = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF090D16), // Near black
            Color(0xFF1E1B4B), // Indigo night
            Color(0xFF3B0764)  // Dark violet purple
        )
    )

    // Snow
    val snow = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF64748B), // Slate
            Color(0xFF94A3B8), // Soft Slate
            Color(0xFFCBD5E1)  // Frost White
        )
    )

    // Fog
    val fog = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF475569),
            Color(0xFF64748B),
            Color(0xFF94A3B8)
        )
    )
}

@Composable
fun WeatherBackground(
    category: WeatherConditionCategory,
    isDay: Boolean,
    showEffects: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val gradient = remember(category, isDay) {
        when {
            category == WeatherConditionCategory.THUNDERSTORM -> WeatherGradients.thunderstorm
            category == WeatherConditionCategory.RAIN || category == WeatherConditionCategory.DRIZZLE -> WeatherGradients.rain
            category == WeatherConditionCategory.SNOW -> WeatherGradients.snow
            category == WeatherConditionCategory.FOG -> WeatherGradients.fog
            category == WeatherConditionCategory.CLOUDY || category == WeatherConditionCategory.PARTLY_CLOUDY -> {
                if (isDay) WeatherGradients.cloudyDay else WeatherGradients.cloudyNight
            }
            else -> {
                if (isDay) WeatherGradients.clearDay else WeatherGradients.clearNight
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        // Decorative stars for night
        if (showEffects && !isDay && (category == WeatherConditionCategory.CLEAR || category == WeatherConditionCategory.PARTLY_CLOUDY)) {
            StarsOverlay()
        }
        content()
    }
}

@Composable
fun StarsOverlay(modifier: Modifier = Modifier) {
    val stars = remember {
        List(40) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 2f + 1f)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        stars.forEach { (rx, ry, radius) ->
            drawCircle(
                color = Color.White.copy(alpha = 0.6f + (radius / 5f)),
                radius = radius,
                center = Offset(rx * w, ry * (h * 0.45f))
            )
        }
    }
}
