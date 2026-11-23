package com.example.weatherapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.weatherapp.WeatherViewModel
import com.example.weatherapp.WeatherUiState
import com.example.weatherapp.data.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable private fun Page(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(16.dp), content = {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        content()
    })
}
@Composable private fun WeatherCard(content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}
@Composable private fun Status(state: WeatherUiState, onRetry: () -> Unit) {
    if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
    state.error?.let { msg -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { Text(msg, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f)); TextButton(onClick = onRetry) { Text("Қайта көру") } } }
}
@Composable fun HomeScreen(state: WeatherUiState, vm: WeatherViewModel, goCurrent: () -> Unit) = Page("Ауа райы") {
    Text("WeatherApp", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    OutlinedTextField(value = state.query, onValueChange = vm::updateQuery, label = { Text("Қала атауы") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    Button(onClick = vm::searchCity, enabled = !state.searching && !state.loading, modifier = Modifier.fillMaxWidth()) { Text(if (state.searching) "Ізделуде…" else "Ауа райын көрсету") }
    Status(state, vm::refresh)
    state.weather?.let { data ->
        WeatherCard {
            Text(data.city.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("${weatherEmoji(data.current.code)}  ${data.current.temperature.pretty()}${if (state.fahrenheit) "°F" else "°C"}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text(weatherDescription(data.current.code))
            Button(onClick = goCurrent, modifier = Modifier.fillMaxWidth()) { Text("Ағымдағы ауа райы") }
        }
    }
}
@Composable fun CurrentScreen(state: WeatherUiState, vm: WeatherViewModel) = Page("Ағымдағы ауа райы") {
    Status(state, vm::refresh)
    state.weather?.let { data ->
        WeatherCard {
            Text(data.city.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(weatherEmoji(data.current.code), style = MaterialTheme.typography.displayMedium)
            Text("${data.current.temperature.pretty()}${if (state.fahrenheit) "°F" else "°C"}", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
            Text(weatherDescription(data.current.code), style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            Text("Сезіледі: ${data.current.feelsLike.pretty()}°")
            Text("Ылғалдылық: ${data.current.humidity}%")
            Text("Жел: ${data.current.windSpeed.pretty()} км/сағ, ${windDirection(data.current.windDirection)}")
            Text("Жел бағыты: ${data.current.windDirection}°")
            Text("Жаңартылған: ${data.current.time.replace('T', ' ')}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = vm::refresh, enabled = !state.loading) { Text("Жаңарту") }
                OutlinedButton(onClick = { vm.toggleFavorite() }) { Text(if (state.favorites.any { it.latitude == data.city.latitude && it.longitude == data.city.longitude }) "★" else "☆") }
            }
        }
    }
}
@Composable fun ForecastScreen(state: WeatherUiState, vm: WeatherViewModel) = Page("Ауа райы болжамы") {
    Status(state, vm::refresh)
    if (state.weather == null && !state.loading) Text("Болжамды көру үшін басты беттен қаланы таңдаңыз.")
    state.weather?.forecast?.forEach { day ->
        WeatherCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(formatDate(day.date), fontWeight = FontWeight.SemiBold)
                    Text(weatherDescription(day.code))
                    day.precipitation?.let { Text("Жауын ықтималдығы: $it%", style = MaterialTheme.typography.bodySmall) }
                }
                Text(weatherEmoji(day.code), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(12.dp))
                Text("${day.min.pretty()}° / ${day.max.pretty()}°", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable fun CitiesScreen(state: WeatherUiState, vm: WeatherViewModel) = Page("Қалалар") {
    Text("${state.city.name} қаласын таңдаулыға қосуға болады.")
    val selected = state.favorites.any { it.latitude == state.city.latitude && it.longitude == state.city.longitude }
    OutlinedButton(onClick = { vm.toggleFavorite() }, modifier = Modifier.fillMaxWidth()) { Text(if (selected) "★ Таңдаулыдан өшіру" else "☆ Таңдаулыға қосу") }
    if (state.favorites.isEmpty()) Text("Таңдаулы қалалар әзірге жоқ.")
    state.favorites.forEach { city ->
        WeatherCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(city.name, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { vm.selectCity(city) }) { Text("Таңдау") }
                TextButton(onClick = { vm.toggleFavorite(city) }) { Text("Өшіру") }
            }
        }
    }
    Status(state, vm::refresh)
}
@Composable fun SettingsScreen(state: WeatherUiState, vm: WeatherViewModel) = Page("Баптаулар") {
    WeatherCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Қараңғы тақырып", Modifier.weight(1f)); Switch(checked = state.darkTheme, onCheckedChange = vm::setDarkTheme) }
        HorizontalDivider()
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Температура бірлігі", Modifier.weight(1f)); TextButton(onClick = { vm.setFahrenheit(!state.fahrenheit) }) { Text(if (state.fahrenheit) "°F" else "°C") } }
        HorizontalDivider()
        Text("WeatherApp")
        Text("Қосымша нұсқасы: 1.0")
        Text("Дереккөз: Open-Meteo")
    }
}
private fun Double.pretty() = if (this % 1.0 == 0.0) toInt().toString() else String.format(Locale.getDefault(), "%.1f", this)
private fun formatDate(value: String): String = runCatching { LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale("kk"))) }.getOrDefault(value)
