package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [SavedCity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "climaya_weather.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate with default popular cities
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).cityDao()
                                dao.insertCity(
                                    SavedCity(
                                        name = "Madrid",
                                        country = "España",
                                        admin1 = "Comunidad de Madrid",
                                        latitude = 40.4168,
                                        longitude = -3.7038,
                                        isDefault = true
                                    )
                                )
                                dao.insertCity(
                                    SavedCity(
                                        name = "Ciudad de México",
                                        country = "México",
                                        admin1 = "CDMX",
                                        latitude = 19.4326,
                                        longitude = -99.1332
                                    )
                                )
                                dao.insertCity(
                                    SavedCity(
                                        name = "Buenos Aires",
                                        country = "Argentina",
                                        admin1 = "CABA",
                                        latitude = -34.6037,
                                        longitude = -58.3816
                                    )
                                )
                                dao.insertCity(
                                    SavedCity(
                                        name = "Bogotá",
                                        country = "Colombia",
                                        admin1 = "Cundinamarca",
                                        latitude = 4.7110,
                                        longitude = -74.0721
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
