package com.kampplus.hava.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.kampplus.hava.ui.home.HomeScreen
import com.kampplus.hava.ui.navigation.HavaBottomBar
import com.kampplus.hava.ui.profile.ProfileScreen
import com.kampplus.hava.ui.welcome.WelcomeScreen

/**
 * Uygulamanın kaydırmaya duyarlı yüzen alt gezinti çubuğu ile birlikte tüm ekranları ve geçişleri.
 */
@Composable
fun HavaNavHost(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: SPLASH_ROUTE

    val showBottomBar = currentRoute in listOf(HOME_ROUTE, CITY_ROUTE, PROFILE_ROUTE)

    var isBottomBarVisible by remember { mutableStateOf(true) }

    // Rota değiştiğinde alt çubuğu her zaman tekrar görünür yap
    LaunchedEffect(currentRoute) {
        isBottomBarVisible = true
    }

    // Kaydırma yönünü dinleyen bağlantı (Aşağı kaydırınca gizler, yukarı kaydırınca gösterir)
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -12f) {
                    isBottomBarVisible = false
                } else if (available.y > 12f) {
                    isBottomBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = SPLASH_ROUTE,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(SPLASH_ROUTE) {
                    WelcomeScreen(
                        onNavigateToHome = {
                            navController.navigate(HOME_ROUTE) {
                                popUpTo(SPLASH_ROUTE) { inclusive = true }
                            }
                        }
                    )
                }
                composable(HOME_ROUTE) {
                    HomeScreen(
                        onCityClick = { city -> navController.navigate(forecastRoute(city)) }
                    )
                }
                composable(CITY_ROUTE) {
                    CityRoute(
                        onCityClick = { city -> navController.navigate(forecastRoute(city)) }
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

        AnimatedVisibility(
            visible = showBottomBar && isBottomBarVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
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
}

/** Açılış splash ekranının adresi. */
private const val SPLASH_ROUTE = "splash"

/** Ana sayfa ekranının adresi. */
private const val HOME_ROUTE = "home"

/** Liste ekranının adresi. */
private const val CITY_ROUTE = "city"

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
