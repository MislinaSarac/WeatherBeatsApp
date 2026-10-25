package com.kampplus.hava.ui.city

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.data.CityRepository
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
 *
 * Görev dağılımı:
 * - Ekran (Composable) yalnızca çizer.
 * - ViewModel state'i tutar ve kararları verir.
 * - Repository veriyi getirir.
 *
 * ViewModel ekran döndüğünde (Activity yeniden kurulurken) yaşamaya devam eder;
 * bu yüzden arama metni ve sonuçlar kaybolmaz.
 *
 * @param repository Varsayılanı gerçek servis. Testlerde sahte bir repository verilir.
 *   Tüm parametrelerin varsayılanı olduğu için Kotlin parametresiz bir kurucu da üretir;
 *   ekranlarda `viewModel()` bunu kullanabilir.
 */
class CityViewModel(
    private val repository: CityRepository = OpenMeteoCityRepository()
) : ViewModel() {

    // State ViewModel'de MutableStateFlow ile tutulur.
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // Varsayılan olarak 3 büyük şehir anında gösterilir (Gecikmesiz açılış).
    private val _uiState = MutableStateFlow<LoadState<List<City>>>(LoadState.Success(DEFAULT_CITIES))
    val uiState: StateFlow<LoadState<List<City>>> = _uiState.asStateFlow()

    /** Kullanıcı arama kutusuna her harf yazdığında çağrılır. */
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isBlank()) {
            _uiState.value = LoadState.Success(DEFAULT_CITIES)
        }
    }

    /**
     * Aramayı çalıştırır ve sonucu durumlara çevirir.
     */
    fun search() {
        val currentQuery = _query.value.trim()
        if (currentQuery.isEmpty()) {
            _uiState.value = LoadState.Success(DEFAULT_CITIES)
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

        /** Varsayılan olarak gösterilen 3 büyük şehir. */
        val DEFAULT_CITIES = listOf(
            City(745044, "İstanbul", 41.01384, 28.94966, "TR", "Türkiye", "İstanbul"),
            City(323786, "Ankara", 39.91987, 32.85427, "TR", "Türkiye", "Ankara"),
            City(311044, "İzmir", 38.41273, 27.13838, "TR", "Türkiye", "İzmir")
        )
    }
}
