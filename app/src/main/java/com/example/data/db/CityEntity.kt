package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_cities")
data class SavedCity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val country: String,
    val admin1: String? = null,
    val latitude: Double,
    val longitude: Double,
    val isCurrentLocation: Boolean = false,
    val isDefault: Boolean = false,
    val cachedTemp: Double? = null,
    val cachedWeatherCode: Int? = null,
    val cachedCondition: String? = null,
    val lastUpdated: Long = 0L
)
