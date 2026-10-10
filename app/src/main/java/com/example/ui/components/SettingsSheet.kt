package com.example.ui.components

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    viewModel: WeatherViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences = uiState.preferences
    val context = LocalContext.current
    var isInterstitialLoading by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
                .padding(bottom = 36.dp)
        ) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Ajustes de ClimaYa",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cerrar ajustes")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. PUBLICIDAD (GOOGLE ADMOB)
            SettingsSectionHeader(title = "MONETIZACIÓN Y ANUNCIOS (ADMOB)", icon = Icons.Rounded.MonetizationOn)
            
            SettingsSwitchItem(
                title = "Mostrar banner de prueba",
                subtitle = "Banner de AdMob en la pantalla principal",
                checked = preferences.showAdsBanner,
                onCheckedChange = { viewModel.toggleAdsBanner() },
                testTag = "toggle_admob_banner"
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilledTonalButton(
                onClick = {
                    (context as? Activity)?.let { activity ->
                        isInterstitialLoading = true
                        AdMobInterstitialHelper.loadAndShow(
                            activity = activity,
                            onAdLoaded = { isInterstitialLoading = false },
                            onAdDismissed = {
                                isInterstitialLoading = false
                                Toast.makeText(context, "Anuncio intersticial cerrado", Toast.LENGTH_SHORT).show()
                            },
                            onAdFailed = { error ->
                                isInterstitialLoading = false
                                Toast.makeText(context, "Error cargando anuncio: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                enabled = !isInterstitialLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("show_interstitial_ad_button")
            ) {
                if (isInterstitialLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cargando anuncio...")
                } else {
                    Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Probar anuncio intersticial (AdMob)")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. UNIDADES Y FORMATO
            SettingsSectionHeader(title = "UNIDADES Y MEDICIÓN", icon = Icons.Rounded.Straighten)

            Text(
                text = "Temperatura",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                UnitOptionChip(
                    title = "Celsius (°C)",
                    selected = preferences.tempUnit == TemperatureUnit.CELSIUS,
                    onClick = { viewModel.setTempUnit(TemperatureUnit.CELSIUS) },
                    modifier = Modifier.weight(1f).testTag("select_celsius")
                )
                UnitOptionChip(
                    title = "Fahrenheit (°F)",
                    selected = preferences.tempUnit == TemperatureUnit.FAHRENHEIT,
                    onClick = { viewModel.setTempUnit(TemperatureUnit.FAHRENHEIT) },
                    modifier = Modifier.weight(1f).testTag("select_fahrenheit")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Velocidad del viento",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WindUnit.values().forEach { unit ->
                    UnitOptionChip(
                        title = unit.label,
                        selected = preferences.windUnit == unit,
                        onClick = { viewModel.setWindUnit(unit) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Presión atmosférica",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PressureUnit.values().forEach { unit ->
                    UnitOptionChip(
                        title = unit.label,
                        selected = preferences.pressureUnit == unit,
                        onClick = { viewModel.setPressureUnit(unit) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Formato de hora",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TimeFormat.values().forEach { format ->
                    UnitOptionChip(
                        title = format.label,
                        selected = preferences.timeFormat == format,
                        onClick = { viewModel.setTimeFormat(format) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. APARIENCIA Y TEMA
            SettingsSectionHeader(title = "APARIENCIA Y TEMA", icon = Icons.Rounded.Palette)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.values().forEach { mode ->
                    UnitOptionChip(
                        title = mode.label,
                        selected = preferences.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SettingsSwitchItem(
                title = "Fondo dinámico interactivo",
                subtitle = "Gradientes según el clima, cielo estrellado nocturno",
                checked = preferences.animatedSkyEffects,
                onCheckedChange = { viewModel.toggleSkyEffects() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. NOTIFICACIONES Y ALERTAS
            SettingsSectionHeader(title = "NOTIFICACIONES Y ALERTAS", icon = Icons.Rounded.NotificationsActive)

            SettingsSwitchItem(
                title = "Resumen meteorológico matutino",
                subtitle = "Previsión diaria al despertar",
                checked = preferences.morningDigestNotification,
                onCheckedChange = { viewModel.toggleMorningDigest() }
            )

            SettingsSwitchItem(
                title = "Alertas de clima severo",
                subtitle = "Avisos urgentes de lluvia fuerte o tormentas",
                checked = preferences.severeWeatherAlerts,
                onCheckedChange = { viewModel.toggleSevereWeatherAlerts() }
            )

            SettingsSwitchItem(
                title = "Vibración para avisos",
                subtitle = "Retroalimentación táctil en alertas meteorológicas",
                checked = preferences.vibrationAlerts,
                onCheckedChange = { viewModel.toggleVibrationAlerts() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. DATOS Y SINCRONIZACIÓN
            SettingsSectionHeader(title = "DATOS Y SINCRONIZACIÓN", icon = Icons.Rounded.Sync)

            Text(
                text = "Intervalo de actualización",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UpdateInterval.values().forEach { interval ->
                    UnitOptionChip(
                        title = interval.label,
                        selected = preferences.updateInterval == interval,
                        onClick = { viewModel.setUpdateInterval(interval) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SettingsSwitchItem(
                title = "Solo con Wi-Fi",
                subtitle = "Ahorra datos móviles al actualizar",
                checked = preferences.wifiOnlyUpdate,
                onCheckedChange = { viewModel.toggleWifiOnly() }
            )

            SettingsSwitchItem(
                title = "Ubicación GPS precisa",
                subtitle = "Usa Google Fused Location para coordenadas exactas",
                checked = preferences.preciseGpsEnabled,
                onCheckedChange = { viewModel.togglePreciseGps() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    viewModel.clearCache()
                    Toast.makeText(context, "Caché de datos meteorológicos restablecida", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Rounded.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Borrar caché y actualizar datos")
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 6. ACERCA DE LA APLICACIÓN
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ClimaYa v1.0.0",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Proveedor de datos: Open-Meteo Weather API (Global sin clave)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Red publicitaria: Google AdMob (Modo de pruebas activo)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Almacenamiento: Room Database local con persistencia offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier
            )
        }
    }
}

@Composable
private fun UnitOptionChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (selected) ButtonDefaults.outlinedButtonBorder() else null,
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
        }
    }
}
