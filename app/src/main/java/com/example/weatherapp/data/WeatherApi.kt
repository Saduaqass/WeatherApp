package com.example.weatherapp.data

import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {
    @GET("v1/search") suspend fun search(@Query("name") name: String, @Query("count") count: Int = 1, @Query("language") language: String = "kk", @Query("format") format: String = "json"): GeoResponse
}

interface ForecastApi {
    @GET("v1/forecast") suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,wind_direction_10m",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weather_code,precipitation_probability_max",
        @Query("forecast_days") days: Int = 5,
        @Query("timezone") timezone: String = "auto",
        @Query("temperature_unit") temperatureUnit: String = "celsius"
    ): WeatherResponse
}
