package com.example.ui.screens

import android.Manifest
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WeatherConditionCategory
import com.example.model.WeatherConditionHelper
import com.example.ui.components.*
import com.example.ui.viewmodel.ActiveSheet
import com.example.ui.viewmodel.WeatherUiState
import com.example.ui.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherMainScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // Intercept back navigation
    BackHandler {
        if (uiState.activeSheet != null) {
            viewModel.closeSheet()
        } else {
            showExitConfirmDialog = true
        }
    }

    // Exit confirmation dialog with "Sí" and "No"
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "¿Deseas salir?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas salir de ClimaYa?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            dismissButton = {
                FilledTonalButton(
                    onClick = { showExitConfirmDialog = false },
                    modifier = Modifier.testTag("exit_dialog_no")
                ) {
                    Text("No")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        (context as? Activity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("exit_dialog_yes")
                ) {
                    Text("Sí")
                }
            }
        )
    }

    // Location Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.requestGpsLocation()
        }
    }

    val weather = uiState.weather
    val conditionCategory = weather?.let {
        WeatherConditionHelper.getInfo(it.weatherCode, it.isDay).category
    } ?: WeatherConditionCategory.CLEAR

    val isDay = weather?.isDay ?: true

    WeatherBackground(
        category = conditionCategory,
        isDay = isDay,
        showEffects = uiState.preferences.animatedSkyEffects,
        modifier = modifier
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                WeatherTopAppBar(
                    onOpenCities = { viewModel.openSheet(ActiveSheet.SAVED_CITIES) },
                    onOpenSearch = { viewModel.openSheet(ActiveSheet.SEARCH) },
                    onOpenSettings = { viewModel.openSheet(ActiveSheet.SETTINGS) },
                    onGpsClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    onRefresh = { viewModel.refreshWeather() },
                    isRefreshing = uiState.isRefreshing
                )
            },
            bottomBar = {
                if (uiState.preferences.showAdsBanner) {
                    AdMobBanner()
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.isLoading && uiState.weather == null) {
                    // Initial loading screen
                    LoadingWeatherView(modifier = Modifier.align(Alignment.Center))
                } else {
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Error banner if any
                        AnimatedVisibility(visible = uiState.errorMessage != null) {
                            uiState.errorMessage?.let { error ->
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ErrorOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = error,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = { viewModel.refreshWeather() }) {
                                            Text("Reintentar", color = MaterialTheme.colorScheme.onErrorContainer)
                                        }
                                    }
                                }
                            }
                        }

                        if (weather != null) {
                            // 1. Hero Header
                            WeatherHeader(
                                weather = weather,
                                tempUnit = uiState.preferences.tempUnit,
                                onToggleUnit = { viewModel.toggleTempUnit() }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 2. Hourly Forecast (24 Hours)
                            if (weather.hourly.isNotEmpty()) {
                                HourlyForecastCard(
                                    hourlyItems = weather.hourly,
                                    tempUnit = uiState.preferences.tempUnit
                                )
                            }

                            // 3. 7-Day Forecast
                            if (weather.daily.isNotEmpty()) {
                                DailyForecastCard(
                                    dailyItems = weather.daily,
                                    tempUnit = uiState.preferences.tempUnit
                                )
                            }

                            // 4. Air Quality (if available)
                            weather.airQuality?.let { aqi ->
                                AirQualityCard(airQuality = aqi)
                            }

                            // 5. Weather Metrics (Wind, Humidity, UV, Pressure, Sun)
                            WeatherMetricsGrid(
                                weather = weather,
                                windUnit = uiState.preferences.windUnit,
                                pressureUnit = uiState.preferences.pressureUnit
                            )

                            // 6. Footer Info
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Actualizado a las ${weather.lastUpdatedFormatted} • Datos de Open-Meteo",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets
    when (uiState.activeSheet) {
        ActiveSheet.SEARCH -> {
            CitySearchSheet(
                query = uiState.searchQuery,
                isSearching = uiState.isSearching,
                results = uiState.searchResults,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onSelectCity = { viewModel.selectSearchResult(it) },
                onDismiss = { viewModel.closeSheet() }
            )
        }

        ActiveSheet.SAVED_CITIES -> {
            SavedCitiesSheet(
                cities = uiState.savedCities,
                selectedCityId = uiState.selectedCity?.id,
                tempUnit = uiState.preferences.tempUnit,
                onSelectCity = { city ->
                    viewModel.loadWeatherForCity(city)
                    viewModel.closeSheet()
                },
                onDeleteCity = { city -> viewModel.removeCity(city) },
                onAddCityClick = { viewModel.openSheet(ActiveSheet.SEARCH) },
                onGpsClick = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                isGpsLoading = uiState.isLocationLoading,
                onDismiss = { viewModel.closeSheet() }
            )
        }

        ActiveSheet.SETTINGS -> {
            SettingsSheet(
                viewModel = viewModel,
                onDismiss = { viewModel.closeSheet() }
            )
        }

        null -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherTopAppBar(
    onOpenCities: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onGpsClick: () -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    TopAppBar(
        title = {
            Text(
                text = "ClimaYa",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onOpenCities,
                modifier = Modifier.testTag("cities_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = "Ciudades guardadas",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(
                onClick = onGpsClick,
                modifier = Modifier.testTag("location_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.MyLocation,
                    contentDescription = "Ubicación actual",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = onOpenSearch,
                modifier = Modifier.testTag("search_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Buscar ciudad",
                    tint = Color.White
                )
            }

            val infiniteTransition = rememberInfiniteTransition(label = "refresh_rotate")
            val angle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "refresh_rotation"
            )

            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing,
                modifier = Modifier.testTag("refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Actualizar clima",
                    tint = Color.White,
                    modifier = Modifier.rotate(if (isRefreshing) angle else 0f)
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Configuración",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun LoadingWeatherView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Consultando pronóstico del tiempo...",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.9f)
            )
        )
    }
}
