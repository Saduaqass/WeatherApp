package com.example.weatherapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

data class WeatherUiState(
    val city: City = City("Алматы", 43.238949, 76.889709),
    val query: String = "Алматы",
    val weather: WeatherData? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val searching: Boolean = false,
    val darkTheme: Boolean = false,
    val fahrenheit: Boolean = false,
    val favorites: List<City> = emptyList()
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WeatherRepository(application)
    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.favorites, repository.darkTheme, repository.fahrenheit) { favorites, dark, fahrenheit -> Triple(favorites, dark, fahrenheit) }.collect { (favorites, dark, fahrenheit) ->
                val old = _state.value
                _state.value = old.copy(favorites = favorites, darkTheme = dark, fahrenheit = fahrenheit)
                if (old.fahrenheit != fahrenheit && old.weather != null) refresh()
            }
        }
        refresh()
    }
    fun updateQuery(value: String) { _state.update { it.copy(query = value) } }
    fun searchCity() {
        val name = _state.value.query.trim()
        if (name.isBlank() || _state.value.searching) return
        viewModelScope.launch {
            _state.update { it.copy(searching = true, error = null) }
            try {
                val city = repository.findCity(name)
                if (city == null) _state.update { it.copy(error = "Қала табылмады") }
                else { _state.update { it.copy(city = city, query = city.name) }; load(city) }
            } catch (_: IOException) { _state.update { it.copy(error = "Интернет байланысы жоқ") } }
            catch (_: Exception) { _state.update { it.copy(error = "Деректерді жүктеу мүмкін болмады") } }
            finally { _state.update { it.copy(searching = false) } }
        }
    }
    fun selectCity(city: City) { _state.update { it.copy(city = city, query = city.name, error = null) }; refresh() }
    fun refresh() = load(_state.value.city)
    private fun load(city: City) {
        if (_state.value.loading) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try { val weather = repository.getWeather(city, _state.value.fahrenheit); _state.update { it.copy(weather = weather, city = city) } }
            catch (_: IOException) { _state.update { it.copy(error = "Интернет байланысы жоқ") } }
            catch (_: Exception) { _state.update { it.copy(error = "Деректерді жүктеу мүмкін болмады") } }
            finally { _state.update { it.copy(loading = false) } }
        }
    }
    fun toggleFavorite(city: City = _state.value.city) {
        viewModelScope.launch {
            val list = _state.value.favorites.toMutableList()
            val existing = list.indexOfFirst { it.latitude == city.latitude && it.longitude == city.longitude }
            if (existing >= 0) list.removeAt(existing) else list.add(city)
            repository.saveFavorites(list)
        }
    }
    fun setDarkTheme(value: Boolean) { viewModelScope.launch { repository.saveDark(value) } }
    fun setFahrenheit(value: Boolean) { viewModelScope.launch { repository.saveFahrenheit(value) } }
}
