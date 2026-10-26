package com.kampplus.hava.ui.forecast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.R
import com.kampplus.hava.data.UserSettingsManager
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.DailyForecast
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.HourlyForecast
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.theme.HavaTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ForecastRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: ForecastViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ForecastScreen(
        cityName = viewModel.cityName,
        subtitle = viewModel.subtitle,
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        modifier = modifier
    )
}

/**
 * Şehir detay ekranı.
 *
 * Ekran kaydırıldığında şehir adı ve derece üst çubuğa sabitleşir.
 * Şehir ismi ve sıcaklık dereceleri daha büyük ve belirgindir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    cityName: String,
    subtitle: String,
    uiState: LoadState<Forecast>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tempUnit by UserSettingsManager.temperatureUnit.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val isScrolled by remember {
        derivedStateOf { scrollState.value > 140 }
    }

    val currentForecast = (uiState as? LoadState.Success)?.data?.current
    val currentCode = currentForecast?.weatherCode
    val currentTemp = currentForecast?.temperatureC

    val screenGradient = if (currentCode != null) {
        getWeatherScreenGradient(WeatherCondition.fromWeatherCode(currentCode))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF0A0F2D), Color(0xFF030712)))
    }

    val displayCityName = cityName.ifEmpty { subtitle }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = isScrolled,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = displayCityName,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            )
                            if (currentTemp != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = degrees(currentTemp),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRetry) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenGradient)
                .padding(innerPadding)
        ) {
            when (uiState) {
                LoadState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
                LoadState.Empty -> ErrorView(message = "Bu şehir için tahmin bulunamadı.")
                is LoadState.Error -> ErrorView(message = uiState.message, onRetry = onRetry)
                is LoadState.Success -> ForecastContent(
                    cityName = cityName,
                    subtitle = subtitle,
                    forecast = uiState.data,
                    scrollState = scrollState
                )
            }
        }
    }
}

@Composable
private fun ForecastContent(
    cityName: String,
    subtitle: String,
    forecast: Forecast,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Üst Ana Hava Durumu Bilgileri & Büyük Ortalanmış Şehir İsmi
        CurrentWeatherHeader(
            cityName = cityName,
            subtitle = subtitle,
            current = forecast.current,
            hourly = forecast.hourly
        )

        // Saatlik Tahminler
        SectionTitle(stringResource(R.string.detail_hourly))
        HourlyRow(hours = forecast.hourly)

        // 7 Günlük Tahminler
        SectionTitle(stringResource(R.string.detail_daily))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                forecast.daily.forEachIndexed { index, day ->
                    if (index > 0) HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    DailyRow(day = day, isToday = index == 0)
                }
            }
        }
    }
}

/** Üst Büyük Şehir İsmi + Dev Derece + Hava Koşulu + 3 Alt Metrik Kutusu */
@Composable
private fun CurrentWeatherHeader(
    cityName: String,
    subtitle: String,
    current: CurrentWeather,
    hourly: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    val precip = hourly.firstOrNull()?.precipitationProbability ?: 28
    val wind = current.windSpeedKmh?.roundToInt() ?: 8
    val humidity = current.humidityPercent ?: 64

    val displayTitle = when {
        cityName.isNotEmpty() && subtitle.isNotEmpty() && !subtitle.contains(cityName) -> "$cityName, $subtitle"
        cityName.isNotEmpty() -> cityName
        else -> subtitle.ifEmpty { "Şehir Detayı" }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Ortadaki Ekstra Büyük ve Kalın Şehir İsmi
        Text(
            text = displayTitle,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 34.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )

        // Büyük Hava İkonu
        Text(text = condition.emoji, fontSize = 88.sp)

        // Dev Sıcaklık Derecesi
        Text(
            text = degrees(current.temperatureC),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            color = Color.White
        )

        Text(
            text = condition.label,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = Color.White
        )

        if (current.apparentTemperatureC != null) {
            Text(
                text = "Hissedilen ${degrees(current.apparentTemperatureC)}",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                color = Color.White.copy(alpha = 0.88f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3 Metrik Kutusu (Yağış, Nem, Rüzgar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricBox(label = "Yağış", value = "%$precip", emoji = "🌧️")
            MetricBox(label = "Nem", value = "%$humidity", emoji = "💧")
            MetricBox(label = "Rüzgar", value = "$wind km/s", emoji = "💨")
        }
    }
}

@Composable
private fun MetricBox(label: String, value: String, emoji: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

/**
 * Saatlik Tahminler.
 */
@Composable
private fun HourlyRow(hours: List<HourlyForecast>, modifier: Modifier = Modifier) {
    val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
    val upcoming = hours.filter { !it.time.isBefore(now) }.take(HOURLY_COUNT)
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items = upcoming, key = { it.time.toString() }) { hour ->
            Card(
                modifier = Modifier.width(80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = hour.time.format(HOUR_FORMAT),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                    Text(
                        text = WeatherCondition.fromWeatherCode(hour.weatherCode).emoji,
                        fontSize = 24.sp
                    )
                    Text(
                        text = degrees(hour.temperatureC),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = percent(hour.precipitationProbability) ?: " ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

/** Günlük tahmin satırı */
@Composable
private fun DailyRow(day: DailyForecast, isToday: Boolean, modifier: Modifier = Modifier) {
    val dayCondition = WeatherCondition.fromWeatherCode(day.weatherCode)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isToday) stringResource(R.string.today) else day.date.format(DAY_FORMAT).replaceFirstChar(Char::titlecase),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${dayCondition.emoji} ${dayCondition.label}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(
            text = "${degrees(day.minTemperatureC)} / ${degrees(day.maxTemperatureC)}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = Color.White
    )
}

/**
 * Hava durumuna uygun 3 geçişli yumuşak dinamik gradyan rengi üretir.
 */
private fun getWeatherScreenGradient(condition: WeatherCondition): Brush {
    return when (condition) {
        // Güneşli: Altın Sarı -> Turuncu -> Derin Turuncu
        WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR ->
            Brush.verticalGradient(listOf(Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFF57C00)))

        // Parçalı Bulutlu: Açık Mavi -> Mavi -> Yumuşak Mavi
        WeatherCondition.PARTLY_CLOUDY ->
            Brush.verticalGradient(listOf(Color(0xFF64B5F6), Color(0xFF1E88E5), Color(0xFF1565C0)))

        // Kapalı / Sisli: Duman Grisi -> Mavi-Gri -> Koyu Gri
        WeatherCondition.OVERCAST, WeatherCondition.FOG ->
            Brush.verticalGradient(listOf(Color(0xFF90A4AE), Color(0xFF607D8B), Color(0xFF37474F)))

        // Yağmurlu: Koyu Gri-Mavi -> Gece Mavisi -> Derin Lacivert
        WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS ->
            Brush.verticalGradient(listOf(Color(0xFF455A64), Color(0xFF1A237E), Color(0xFF0A0F2D)))

        // Fırtınalı: Derin Mor -> Koyu Gece Mavi -> Kömür Grisi
        WeatherCondition.THUNDERSTORM ->
            Brush.verticalGradient(listOf(Color(0xFF4A148C), Color(0xFF1A237E), Color(0xFF121212)))

        // Karlı: Açık Buz Mavisi -> Soğuk Turkuaz -> Beyazımsı Mavi
        WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS ->
            Brush.verticalGradient(listOf(Color(0xFFE0F7FA), Color(0xFF4DD0E1), Color(0xFF0097A7)))

        else ->
            Brush.verticalGradient(listOf(Color(0xFF0A0F2D), Color(0xFF060919), Color(0xFF030712)))
    }
}

private fun degrees(celsius: Double) = UserSettingsManager.formatTemp(celsius)
private fun percent(value: Int?) = value?.takeIf { it > 0 }?.let { "%$it" }

private const val HOURLY_COUNT = 24
private val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("tr"))

@Preview(showBackground = true)
@Composable
private fun ForecastScreenPreview() {
    HavaTheme {
        ForecastScreen(
            cityName = "Ankara",
            subtitle = "Ankara, Türkiye",
            uiState = LoadState.Success(
                Forecast(
                    current = CurrentWeather(
                        temperatureC = 23.0,
                        weatherCode = 0,
                        observedAt = LocalDateTime.of(2026, 1, 15, 14, 0),
                        apparentTemperatureC = 22.0,
                        humidityPercent = 45,
                        windSpeedKmh = 12.0
                    ),
                    hourly = List(8) {
                        HourlyForecast(
                            time = LocalDateTime.now().plusHours(it.toLong()),
                            temperatureC = 18.0 + it,
                            weatherCode = 2,
                            precipitationProbability = it * 5
                        )
                    },
                    daily = List(7) {
                        DailyForecast(
                            date = LocalDate.of(2026, 1, 15).plusDays(it.toLong()),
                            minTemperatureC = 17.0 + it,
                            maxTemperatureC = 23.0 + it,
                            weatherCode = 61,
                            precipitationProbability = 40
                        )
                    }
                )
            ),
            onBack = {},
            onRetry = {}
        )
    }
}
