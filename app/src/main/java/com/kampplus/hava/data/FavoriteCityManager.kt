package com.kampplus.hava.data

import com.kampplus.hava.model.City
import com.kampplus.hava.ui.city.CityViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Kullanıcının favori şehirlerini yöneten merkezi sınıf.
 */
object FavoriteCityManager {

    private val defaultCity = CityViewModel.DEFAULT_CITIES.first()

    private val _favoriteCities = MutableStateFlow<List<City>>(listOf(defaultCity))
    val favoriteCities: StateFlow<List<City>> = _favoriteCities.asStateFlow()

    private val _favoriteCityIds = MutableStateFlow<Set<Long>>(setOf(defaultCity.id))
    val favoriteCityIds: StateFlow<Set<Long>> = _favoriteCityIds.asStateFlow()

    private val _favoriteCity = MutableStateFlow<City?>(defaultCity)
    val favoriteCity: StateFlow<City?> = _favoriteCity.asStateFlow()

    fun toggleFavorite(city: City) {
        val currentList = _favoriteCities.value.toMutableList()
        val currentIds = _favoriteCityIds.value.toMutableSet()

        if (currentIds.contains(city.id)) {
            currentIds.remove(city.id)
            currentList.removeAll { it.id == city.id }
        } else {
            currentIds.add(city.id)
            currentList.add(city)
        }

        _favoriteCityIds.value = currentIds
        val updatedList = if (currentList.isEmpty()) listOf(defaultCity) else currentList
        _favoriteCities.value = updatedList
        _favoriteCity.value = updatedList.firstOrNull()
    }

    fun setAsPrimaryFavorite(city: City) {
        val currentList = _favoriteCities.value.toMutableList()
        val currentIds = _favoriteCityIds.value.toMutableSet()

        currentList.removeAll { it.id == city.id }
        currentIds.add(city.id)
        currentList.add(city) // Sona ekleyerek Ana Sayfada öne çıkarır

        _favoriteCityIds.value = currentIds
        _favoriteCities.value = currentList
        _favoriteCity.value = city
    }

    fun isFavorite(cityId: Long): Boolean {
        return _favoriteCityIds.value.contains(cityId)
    }
}
