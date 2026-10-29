package com.kampplus.hava.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kampplus.hava.data.FavoriteCityManager
import com.kampplus.hava.data.LocationHelper
import com.kampplus.hava.data.MusicPlaylistHelper
import com.kampplus.hava.data.OpenMeteoWeatherRepository
import com.kampplus.hava.data.UserMood
import com.kampplus.hava.data.UserSettingsManager
import com.kampplus.hava.model.City
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.city.CityCard
import com.kampplus.hava.ui.theme.HavaTheme
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Uygulamanın Ana Sayfası.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onCityClick: (City) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val favoriteCities by FavoriteCityManager.favoriteCities.collectAsStateWithLifecycle()

    var selectedMood by remember { mutableStateOf<UserMood?>(null) }
    var forecasts by remember { mutableStateOf<Map<Long, Forecast>>(emptyMap()) }
    val weatherRepository = remember { OpenMeteoWeatherRepository() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            coroutineScope.launch {
                val currentCity = LocationHelper.getCurrentLocationCity(context)
                if (currentCity != null) {
                    FavoriteCityManager.setAsPrimaryFavorite(currentCity)
                }
            }
        }
    }

    fun handleLocationClick() {
        val hasFine =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            coroutineScope.launch {
                val currentCity = LocationHelper.getCurrentLocationCity(context)
                if (currentCity != null) {
                    FavoriteCityManager.setAsPrimaryFavorite(currentCity)
                }
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(favoriteCities) {
        val newForecasts = forecasts.toMutableMap()
        favoriteCities.forEach { city ->
            if (!newForecasts.containsKey(city.id)) {
                try {
                    val result = weatherRepository.getForecast(city.latitude, city.longitude)
                    newForecasts[city.id] = result
                } catch (_: Exception) {
                }
            }
        }
        forecasts = newForecasts
    }

    // En son eklenen favori şehir (yoksa ilk favori)
    val latestCity = favoriteCities.lastOrNull() ?: favoriteCities.firstOrNull()
    val latestForecast = latestCity?.let { forecasts[it.id] }

    val currentCode = latestForecast?.current?.weatherCode ?: 0
    val currentCondition = WeatherCondition.fromWeatherCode(currentCode)
    val musicRecommendation = MusicPlaylistHelper.getRecommendation(currentCondition, selectedMood)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    WeatherGreetingHeader(
                        condition = currentCondition,
                        onLocationClick = { handleLocationClick() }
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. ÜST KISIM: En Son Eklenen Favori Şehrin Dinamik Renkli Detaylı Hava Kartı
            if (latestCity != null) {
                item {
                    HeroWeatherCard(
                        city = latestCity,
                        forecast = latestForecast,
                        onClick = { onCityClick(latestCity) }
                    )
                }
            }

            // 2. ORTA KISIM 1: Ferah ve Kaydırılabilir Ruh Hali Kartı ("Bugün Nasıl Hissediyorsun?")
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Bugün nasıl hissediyorsun? 😊",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color(0xFF0A0F2D)
                        )
                        Text(
                            text = "Ruh halini seç, hava durumuyla uyumlu müzik önerini özelleştir.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = Color(0xFF0A0F2D).copy(alpha = 0.7f)
                        )

                        // Ferah, sıkışmayan yatay kaydırılabilir mod butonları
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserMood.entries.forEach { mood ->
                                val isSelected = selectedMood == mood
                                Surface(
                                    modifier = Modifier
                                        .clickable {
                                            selectedMood = if (isSelected) null else mood
                                        }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF0A0F2D) else Color(0xFFE0E0E0),
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) Color(0xFF0A0F2D) else Color(0xFFF5F7FA)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = mood.emoji, fontSize = 20.sp)
                                        Text(
                                            text = mood.label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            ),
                                            color = if (isSelected) Color.White else Color(0xFF0A0F2D)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. ORTA KISIM 2: Lacivert Müzik Yönlendirme Kartı
            item {
                val musicTagText = if (selectedMood != null) {
                    "SEÇİLEN MOD: ${selectedMood?.label?.uppercase(Locale.forLanguageTag("tr-TR"))} ${selectedMood?.emoji}"
                } else {
                    "GÜNÜN HAVASI & MÜZİK MODU"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F2D)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF6FF6FD)
                            )
                            Text(
                                text = musicTagText,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.1.sp
                                ),
                                color = Color(0xFF6FF6FD)
                            )
                        }

                        Text(
                            text = musicRecommendation.playlistName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = Color.White
                        )

                        Text(
                            text = musicRecommendation.description,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                lineHeight = 22.sp
                            ),
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(musicRecommendation.playlistUrl))
                                    context.startActivity(intent)
                                }
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF00696E), Color(0xFF004F53))
                                    ),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "FİZY'DE DİNLE 🎵",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 4. ALT KISIM: Diğer Favori Şehirler
            if (favoriteCities.size > 1) {
                item {
                    Text(
                        text = "Diğer Favori Şehirleriniz",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(favoriteCities.filter { it.id != latestCity?.id }, key = { it.id }) { city ->
                    val forecast = forecasts[city.id]
                    SimpleCityWeatherCard(
                        city = city,
                        forecast = forecast,
                        onClick = { onCityClick(city) }
                    )
                }
            }
        }
    }
}

/**
 * Üst Sabit Merhaba Başlığı ve Vurgulu Tavsiye Mesajı (Sağda Lacivert Konum İkonu ve Altında Metin).
 */
@Composable
private fun WeatherGreetingHeader(condition: WeatherCondition, onLocationClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Merhaba! 👋",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))

            val annotatedString = buildAnnotatedString {
                when (condition) {
                    WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR -> {
                        append("Bugün hava güneşli, ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFFF57C00))) {
                            append("harika bir gün!")
                        }
                        append(" ☀️")
                    }
                    WeatherCondition.PARTLY_CLOUDY -> {
                        append("Bugün hava parçalı bulutlu, ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) {
                            append("hafif bir ceket")
                        }
                        append(" alın. ⛅")
                    }
                    WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS -> {
                        append("Yağmur bekleniyor, ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF00696E))) {
                            append("şemsiyeni")
                        }
                        append(" unutma ☔")
                    }
                    WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS -> {
                        append("Hava karlı, ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF00838F))) {
                            append("sıkı giyinmeyi")
                        }
                        append(" unutma ❄️")
                    }
                    WeatherCondition.THUNDERSTORM -> {
                        append("Fırtına uyarısı var, ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF311B92))) {
                            append("dikkatli ol")
                        }
                        append(" ⛈️")
                    }
                    else -> {
                        append("Hava kapalı, ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) {
                            append("sakin bir gün")
                        }
                        append(" ☁️")
                    }
                }
            }

            Text(
                text = annotatedString,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f)
            )
        }

        // Bir tık küçültülen Lacivert Arka Planlı Konum İkonu
        Surface(
            onClick = onLocationClick,
            shape = CircleShape,
            color = Color(0xFF0A0F2D),
            modifier = Modifier
                .padding(start = 12.dp)
                .size(44.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Mevcut Konum",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Hava durumuna uygun dinamik gradyan rengi üretir.
 */
private fun getWeatherCardGradient(condition: WeatherCondition): Brush {
    return when (condition) {
        // Güneşli: Sarı -> Turuncu
        WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR ->
            Brush.verticalGradient(listOf(Color(0xFFFFB300), Color(0xFFF57C00)))

        // Parçalı Bulutlu: Açık mavi -> Mavi
        WeatherCondition.PARTLY_CLOUDY ->
            Brush.verticalGradient(listOf(Color(0xFF64B5F6), Color(0xFF1976D2)))

        // Kapalı / Sisli: Gri-mavi tonları
        WeatherCondition.OVERCAST, WeatherCondition.FOG ->
            Brush.verticalGradient(listOf(Color(0xFF78909C), Color(0xFF455A64)))

        // Yağmurlu: Koyu gri -> Lacivert
        WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS ->
            Brush.verticalGradient(listOf(Color(0xFF37474F), Color(0xFF102A43)))

        // Fırtınalı: Koyu mor-gri
        WeatherCondition.THUNDERSTORM ->
            Brush.verticalGradient(listOf(Color(0xFF311B92), Color(0xFF1A237E)))

        // Karlı: Buz mavisi -> Soğuk turkuaz
        WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS ->
            Brush.verticalGradient(listOf(Color(0xFF4DD0E1), Color(0xFF00838F)))

        else ->
            Brush.verticalGradient(listOf(Color(0xFF0A0F2D), Color(0xFF030712)))
    }
}

/**
 * Havaya göre rengi değişen detaylı Hava Durumu Kartı.
 */
@Composable
private fun HeroWeatherCard(city: City, forecast: Forecast?, onClick: () -> Unit) {
    val condition = forecast?.current?.let { WeatherCondition.fromWeatherCode(it.weatherCode) } ?: WeatherCondition.CLEAR
    val cardGradient = getWeatherCardGradient(condition)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Şehir Başlığı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${city.name}, ${city.country ?: "Türkiye"}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "Favori Şehir",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                if (forecast != null) {
                    val apparent = forecast.current.apparentTemperatureC
                    val precip = forecast.hourly.firstOrNull()?.precipitationProbability ?: 28
                    val wind = forecast.current.windSpeedKmh?.roundToInt() ?: 8
                    val humidity = forecast.current.humidityPercent ?: 64

                    // Derece ve Hava Durumu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = UserSettingsManager.formatTemp(forecast.current.temperatureC),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 50.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White
                            )
                            if (apparent != null) {
                                Text(
                                    text = "Hissedilen ${UserSettingsManager.formatTemp(apparent)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = condition.label,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Text(
                            text = condition.emoji,
                            fontSize = 68.sp
                        )
                    }

                    // 3 Metrik Kutusu (Yağış %, Rüzgar km/s, Nem %)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeatherMetricPill(label = "Yağış", value = "%$precip", emoji = "🌧️")
                        WeatherMetricPill(label = "Rüzgar", value = "$wind km/s", emoji = "💨")
                        WeatherMetricPill(label = "Nem", value = "%$humidity", emoji = "💧")
                    }

                    // Günlük Tahmin Listesi (Gelecek Günler)
                    if (forecast.daily.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            forecast.daily.take(3).forEach { day ->
                                val dayCondition = WeatherCondition.fromWeatherCode(day.weatherCode)
                                val dayName = day.date.dayOfWeek.getDisplayName(
                                    TextStyle.FULL,
                                    Locale.forLanguageTag("tr-TR")
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${UserSettingsManager.formatTemp(
                                            day.minTemperatureC
                                        )} / ${UserSettingsManager.formatTemp(day.maxTemperatureC)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                    Text(
                                        text = "${dayCondition.emoji} ${dayCondition.label}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Hava durumu verileri yükleniyor...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherMetricPill(label: String, value: String, emoji: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SimpleCityWeatherCard(city: City, forecast: Forecast?, onClick: () -> Unit) {
    CityCard(
        city = city,
        onClick = onClick,
        temperature = forecast?.current?.temperatureC?.roundToInt(),
        isFavorite = true,
        onFavoriteClick = { FavoriteCityManager.toggleFavorite(city) }
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HavaTheme {
        HomeScreen(onCityClick = {})
    }
}
