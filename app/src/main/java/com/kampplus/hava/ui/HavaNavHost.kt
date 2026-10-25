package com.kampplus.hava.ui

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.city.CityRoute
import com.kampplus.hava.ui.forecast.ForecastRoute
import com.kampplus.hava.ui.forecast.ForecastViewModel
import com.kampplus.hava.ui.history.HistoryScreen
import com.kampplus.hava.ui.navigation.HavaBottomBar
import com.kampplus.hava.ui.profile.ProfileScreen
import com.kampplus.hava.ui.welcome.WelcomeScreen

/**
 * Uygulamanın alt gezinti çubuğu ile birlikte tüm ekranları ve aralarındaki geçişler.
 */
@Composable
fun HavaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: WELCOME_ROUTE

    // Tahmin detay ekranında alt çubuğu gizleriz, ana ekranlarda gösteririz
    val isMainDestination = currentRoute in listOf(WELCOME_ROUTE, CITY_ROUTE, HISTORY_ROUTE, PROFILE_ROUTE)

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (isMainDestination) {
                HavaBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { targetRoute ->
                        if (currentRoute != targetRoute) {
                            navController.navigate(targetRoute) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = WELCOME_ROUTE,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(WELCOME_ROUTE) {
                WelcomeScreen(
                    onNavigateToCities = { navController.navigate(CITY_ROUTE) }
                )
            }
            composable(CITY_ROUTE) {
                CityRoute(
                    onCityClick = { city -> navController.navigate(forecastRoute(city)) }
                )
            }
            composable(HISTORY_ROUTE) {
                HistoryScreen(
                    onCityClick = {
                        navController.navigate(CITY_ROUTE)
                    }
                )
            }
            composable(PROFILE_ROUTE) {
                ProfileScreen()
            }
            composable(
                route = FORECAST_ROUTE,
                arguments = listOf(
                    navArgument(ForecastViewModel.ARG_NAME) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(ForecastViewModel.ARG_SUBTITLE) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(ForecastViewModel.ARG_LATITUDE) {
                        type = NavType.StringType
                        defaultValue = "0"
                    },
                    navArgument(ForecastViewModel.ARG_LONGITUDE) {
                        type = NavType.StringType
                        defaultValue = "0"
                    }
                )
            ) {
                ForecastRoute(onBack = { navController.popBackStack() })
            }
        }
    }
}

/** Hoş geldiniz ekranının adresi. */
private const val WELCOME_ROUTE = "welcome"

/** Liste ekranının adresi. */
private const val CITY_ROUTE = "city"

/** Geçmiş ekranının adresi. */
private const val HISTORY_ROUTE = "history"

/** Profil ekranının adresi. */
private const val PROFILE_ROUTE = "profile"

/** Tahmin ekranının adres şablonu. */
private const val FORECAST_ROUTE =
    "forecast?name={name}&subtitle={subtitle}&latitude={latitude}&longitude={longitude}"

private fun forecastRoute(city: City): String = buildString {
    append("forecast")
    append("?name=").append(Uri.encode(city.name))
    append("&subtitle=").append(Uri.encode(city.subtitle))
    append("&latitude=").append(city.latitude)
    append("&longitude=").append(city.longitude)
}
