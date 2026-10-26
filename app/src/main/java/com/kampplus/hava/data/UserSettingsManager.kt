package com.kampplus.hava.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

enum class TemperatureUnit(val label: String, val symbol: String) {
    CELSIUS("Santigrat (°C)", "°C"),
    FAHRENHEIT("Fahrenheit (°F)", "°F")
}

enum class AppThemeSetting(val label: String) {
    LIGHT("Açık Tema"),
    DARK("Koyu Tema"),
    SYSTEM("Sistem Ayarını Kullan")
}

/**
 * Kullanıcı ayarlarını (Sıcaklık birimi, Tema tercihi) tutan ve SharedPreferences ile kalıcı saklayan merkezi sınıf.
 */
object UserSettingsManager {

    private const val PREFS_NAME = "hava_user_settings"
    private const val KEY_THEME = "app_theme"
    private const val KEY_TEMP_UNIT = "temp_unit"

    private var sharedPreferences: SharedPreferences? = null

    private val _temperatureUnit = MutableStateFlow(TemperatureUnit.CELSIUS)
    val temperatureUnit: StateFlow<TemperatureUnit> = _temperatureUnit.asStateFlow()

    private val _themeSetting = MutableStateFlow(AppThemeSetting.SYSTEM)
    val themeSetting: StateFlow<AppThemeSetting> = _themeSetting.asStateFlow()

    fun init(context: Context) {
        if (sharedPreferences != null) return
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences = prefs

        val savedThemeName = prefs.getString(KEY_THEME, AppThemeSetting.SYSTEM.name)
        val savedTheme = AppThemeSetting.entries.find { it.name == savedThemeName } ?: AppThemeSetting.SYSTEM
        _themeSetting.value = savedTheme

        val savedTempName = prefs.getString(KEY_TEMP_UNIT, TemperatureUnit.CELSIUS.name)
        val savedTemp = TemperatureUnit.entries.find { it.name == savedTempName } ?: TemperatureUnit.CELSIUS
        _temperatureUnit.value = savedTemp
    }

    fun setThemeSetting(theme: AppThemeSetting) {
        _themeSetting.value = theme
        sharedPreferences?.edit()?.putString(KEY_THEME, theme.name)?.apply()
    }

    fun toggleTemperatureUnit() {
        val newUnit = if (_temperatureUnit.value == TemperatureUnit.CELSIUS) {
            TemperatureUnit.FAHRENHEIT
        } else {
            TemperatureUnit.CELSIUS
        }
        _temperatureUnit.value = newUnit
        sharedPreferences?.edit()?.putString(KEY_TEMP_UNIT, newUnit.name)?.apply()
    }

    fun toggleThemeSetting() {
        val nextTheme = when (_themeSetting.value) {
            AppThemeSetting.LIGHT -> AppThemeSetting.DARK
            AppThemeSetting.DARK -> AppThemeSetting.SYSTEM
            AppThemeSetting.SYSTEM -> AppThemeSetting.LIGHT
        }
        setThemeSetting(nextTheme)
    }

    fun formatTemp(celsius: Double): String {
        return if (_temperatureUnit.value == TemperatureUnit.FAHRENHEIT) {
            val fahrenheit = (celsius * 9 / 5) + 32
            "${fahrenheit.roundToInt()}°F"
        } else {
            "${celsius.roundToInt()}°C"
        }
    }
}
