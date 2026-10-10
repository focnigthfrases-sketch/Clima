package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class DeviceLocationResult(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val countryName: String
)

class LocationHelper(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): DeviceLocationResult? = withContext(Dispatchers.IO) {
        try {
            val cancellationTokenSource = CancellationTokenSource()
            val location: Location? = suspendCancellableCoroutine { continuation ->
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { loc ->
                    continuation.resume(loc)
                }.addOnFailureListener {
                    // Try last location as fallback
                    fusedLocationClient.lastLocation.addOnSuccessListener { fallbackLoc ->
                        continuation.resume(fallbackLoc)
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }
                }
            }

            if (location != null) {
                val (cityName, countryName) = reverseGeocode(location.latitude, location.longitude)
                DeviceLocationResult(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    cityName = cityName,
                    countryName = countryName
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun reverseGeocode(lat: Double, lon: Double): Pair<String, String> {
        return try {
            val geocoder = Geocoder(context, Locale("es"))
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Mi ubicación"
                val country = addr.countryName ?: ""
                Pair(city, country)
            } else {
                Pair("Mi ubicación", "")
            }
        } catch (e: Exception) {
            Pair("Mi ubicación", "")
        }
    }
}
