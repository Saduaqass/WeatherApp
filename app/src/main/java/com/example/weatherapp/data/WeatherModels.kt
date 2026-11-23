package com.example.weatherapp.data

import com.google.gson.annotations.SerializedName

data class GeoResponse(val results: List<GeoLocation>?)
data class GeoLocation(val name: String, val latitude: Double, val longitude: Double, val country: String? = null)
data class WeatherResponse(val current: CurrentWeather, val daily: DailyWeather)
data class CurrentWeather(
    @SerializedName("time") val time: String,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("apparent_temperature") val feelsLike: Double,
    @SerializedName("weather_code") val code: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("wind_direction_10m") val windDirection: Int
)
data class DailyWeather(
    val time: List<String>,
    @SerializedName("temperature_2m_max") val max: List<Double>,
    @SerializedName("temperature_2m_min") val min: List<Double>,
    @SerializedName("weather_code") val codes: List<Int>,
    @SerializedName("precipitation_probability_max") val precipitation: List<Int?>?
)
data class City(val name: String, val latitude: Double, val longitude: Double)
data class ForecastDay(val date: String, val max: Double, val min: Double, val code: Int, val precipitation: Int?)
data class WeatherData(val city: City, val current: CurrentWeather, val forecast: List<ForecastDay>)

fun weatherDescription(code: Int): String = when (code) {
    0 -> "Ашық"
    1 -> "Көбіне ашық"
    2 -> "Ала бұлтты"
    3 -> "Бұлтты"
    45, 48 -> "Тұман"
    51, 53, 55 -> "Сіркіреме жаңбыр"
    56, 57 -> "Қыраулы сіркіреме"
    61, 63, 65 -> "Жаңбыр"
    66, 67 -> "Мұзды жаңбыр"
    71, 73, 75, 77 -> "Қар"
    80, 81, 82 -> "Нөсер жаңбыр"
    85, 86 -> "Қарлы нөсер"
    95 -> "Найзағай"
    96, 99 -> "Бұршақ аралас найзағай"
    else -> "Ауа райы белгісіз"
}

fun weatherEmoji(code: Int): String = when (code) {
    0 -> "☀️"
    1, 2 -> "🌤️"
    3 -> "☁️"
    45, 48 -> "🌫️"
    in 51..67 -> "🌧️"
    in 71..86 -> "❄️"
    in 95..99 -> "⛈️"
    else -> "🌡️"
}

fun windDirection(degrees: Int): String = listOf("С", "СШС", "ШСШ", "Ш", "ОСШ", "ОШС", "О", "СОС")[((degrees + 22) % 360) / 45]
