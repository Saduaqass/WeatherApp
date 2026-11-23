package com.example.weatherapp.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private val Context.settingsStore by preferencesDataStore(name = "weather_settings")

class WeatherRepository(context: Context) {
    private val store = context.settingsStore
    private val gson = Gson()
    private val geocoding = Retrofit.Builder().baseUrl("https://geocoding-api.open-meteo.com/").addConverterFactory(GsonConverterFactory.create()).build().create(GeocodingApi::class.java)
    private val forecast = Retrofit.Builder().baseUrl("https://api.open-meteo.com/").addConverterFactory(GsonConverterFactory.create()).build().create(ForecastApi::class.java)
    private val favoritesKey = stringPreferencesKey("favorites")
    private val darkKey = booleanPreferencesKey("dark_theme")
    private val fahrenheitKey = booleanPreferencesKey("fahrenheit")
    val favorites: Flow<List<City>> = store.data.map { prefs ->
        runCatching { gson.fromJson<List<City>>(prefs[favoritesKey] ?: "[]", object : TypeToken<List<City>>() {}.type) ?: emptyList() }.getOrDefault(emptyList())
    }
    val darkTheme: Flow<Boolean> = store.data.map { it[darkKey] ?: false }
    val fahrenheit: Flow<Boolean> = store.data.map { it[fahrenheitKey] ?: false }
    suspend fun saveFavorites(cities: List<City>) { store.edit { it[favoritesKey] = gson.toJson(cities) } }
    suspend fun saveDark(value: Boolean) { store.edit { it[darkKey] = value } }
    suspend fun saveFahrenheit(value: Boolean) { store.edit { it[fahrenheitKey] = value } }
    suspend fun findCity(name: String): City? = geocoding.search(name.trim()).results?.firstOrNull()?.let { City(if (it.country == null) it.name else "${it.name}, ${it.country}", it.latitude, it.longitude) }
    suspend fun getWeather(city: City, fahrenheit: Boolean): WeatherData {
        val data = forecast.forecast(city.latitude, city.longitude, temperatureUnit = if (fahrenheit) "fahrenheit" else "celsius")
        val days = data.daily.time.indices.map { i -> ForecastDay(data.daily.time[i], data.daily.max[i], data.daily.min[i], data.daily.codes[i], data.daily.precipitation?.getOrNull(i)) }
        return WeatherData(city, data.current, days)
    }
}
