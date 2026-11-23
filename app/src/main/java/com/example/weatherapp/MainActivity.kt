package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.weatherapp.data.weatherDescription
import com.example.weatherapp.ui.screens.*
import com.example.weatherapp.ui.theme.WeatherTheme

class MainActivity : ComponentActivity() {
    private val viewModel: WeatherViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.state.collectAsState()
            WeatherTheme(state.darkTheme) {
                val nav = rememberNavController()
                val tabs = listOf("home" to "Басты бет", "current" to "Қазір", "forecast" to "Болжам", "cities" to "Қалалар", "settings" to "Баптаулар")
                Scaffold(bottomBar = {
                    NavigationBar {
                        val route = currentRoute(nav)
                        tabs.forEach { (key, label) -> NavigationBarItem(selected = route == key, onClick = { nav.navigate(key) { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true } }, icon = { Text(listOf("⌂", "☀", "▤", "★", "⚙")[tabs.indexOf(key to label)]) }, label = { Text(label, maxLines = 1) }) }
                    }
                }) { padding ->
                    NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(padding)) {
                        composable("home") { HomeScreen(state, viewModel, goCurrent = { nav.navigate("current") }) }
                        composable("current") { CurrentScreen(state, viewModel) }
                        composable("forecast") { ForecastScreen(state, viewModel) }
                        composable("cities") { CitiesScreen(state, viewModel) }
                        composable("settings") { SettingsScreen(state, viewModel) }
                    }
                }
            }
        }
    }
}

@Composable private fun currentRoute(nav: androidx.navigation.NavHostController): String? = nav.currentBackStackEntryAsState().value?.destination?.route
