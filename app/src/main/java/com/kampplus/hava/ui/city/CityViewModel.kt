package com.kampplus.hava.ui.city

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.data.CityRepository
import com.kampplus.hava.data.FavoriteCityManager
import com.kampplus.hava.data.OpenMeteoCityRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import java.text.Collator
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Şehir listesi ekranının beyni.
 */
class CityViewModel(
    private val repository: CityRepository = OpenMeteoCityRepository()
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<LoadState<List<City>>>(LoadState.Success(getInitialCities()))
    val uiState: StateFlow<LoadState<List<City>>> = _uiState.asStateFlow()

    init {
        // Favori şehirler değiştiğinde arama kutusu boşsa listeyi anında güncelle
        viewModelScope.launch {
            FavoriteCityManager.favoriteCities.collect { favs ->
                if (_query.value.isBlank()) {
                    val combined = (favs + DEFAULT_CITIES).distinctBy { it.id }
                    _uiState.value = LoadState.Success(combined)
                }
            }
        }
    }

    private fun getInitialCities(): List<City> {
        val favorites = FavoriteCityManager.favoriteCities.value
        return (favorites + DEFAULT_CITIES).distinctBy { it.id }
    }

    /** Kullanıcı arama kutusuna her harf yazdığında anında çağrılır ve arama yapar. */
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isBlank()) {
            _uiState.value = LoadState.Success(getInitialCities())
        } else {
            search()
        }
    }

    /**
     * Aramayı çalıştırır ve sonucu durumlara çevirir.
     */
    fun search() {
        val currentQuery = _query.value.trim()
        if (currentQuery.isEmpty()) {
            _uiState.value = LoadState.Success(getInitialCities())
            return
        }

        viewModelScope.launch {
            _uiState.value = LoadState.Loading
            _uiState.value = try {
                val cities = repository.search(currentQuery)
                if (cities.isEmpty()) {
                    LoadState.Empty
                } else {
                    val collator = Collator.getInstance(Locale.forLanguageTag("tr-TR"))
                    val sortedCities = cities.sortedWith { c1, c2 -> collator.compare(c1.name, c2.name) }
                    LoadState.Success(sortedCities)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Arama başarısız: bağlantı hatası", e)
                LoadState.Error("İnternet bağlantını kontrol et.")
            } catch (e: HttpException) {
                Log.e(TAG, "Arama başarısız: HTTP ${e.code()}", e)
                LoadState.Error("Sunucu şu an cevap vermiyor.")
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Arama başarısız: yanıt çözümlenemedi", e)
                LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
            }
        }
    }

    companion object {
        private const val TAG = "CityViewModel"

        /** Varsayılan olarak gösterilen şehir. */
        val DEFAULT_CITIES = listOf(
            City(745044, "İstanbul", 41.01384, 28.94966, "TR", "Türkiye", "İstanbul")
        )
    }
}
