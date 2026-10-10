package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Query("SELECT * FROM saved_cities ORDER BY isCurrentLocation DESC, id ASC")
    fun getAllCities(): Flow<List<SavedCity>>

    @Query("SELECT * FROM saved_cities WHERE id = :id LIMIT 1")
    suspend fun getCityById(id: Long): SavedCity?

    @Query("SELECT * FROM saved_cities WHERE latitude = :lat AND longitude = :lon LIMIT 1")
    suspend fun getCityByCoordinates(lat: Double, lon: Double): SavedCity?

    @Query("SELECT * FROM saved_cities WHERE isCurrentLocation = 1 LIMIT 1")
    suspend fun getCurrentLocationCity(): SavedCity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: SavedCity): Long

    @Update
    suspend fun updateCity(city: SavedCity)

    @Delete
    suspend fun deleteCity(city: SavedCity)

    @Query("DELETE FROM saved_cities WHERE id = :id")
    suspend fun deleteCityById(id: Long)

    @Query("SELECT COUNT(*) FROM saved_cities")
    suspend fun countCities(): Int
}
