package com.kampplus.hava.ui.city

import com.kampplus.hava.testing.FakeCityRepository
import com.kampplus.hava.testing.MainDispatcherRule
import com.kampplus.hava.testing.sampleCity
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * Şehir listesi ekranının durumlarını ve arama davranışlarını doğrular.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCityRepository()

    private fun createViewModel() = CityViewModel(repository)

    @Test
    fun `baslangicta varsayilan favori sehir aninda gosterilir`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state is LoadState.Success)
        assertEquals(1, (state as LoadState.Success).data.size)
        assertEquals("", viewModel.query.value)
    }

    @Test
    fun `arama yapildiginda veri gelince Success durumuna gecilir`() = runTest {
        repository.result = listOf(sampleCity(), sampleCity(id = 2L, name = "İzmir"))

        val viewModel = createViewModel()
        viewModel.onQueryChange("Bursa")
        viewModel.search()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Success bekleniyordu, gelen: $state", state is LoadState.Success)
        assertEquals(2, (state as LoadState.Success).data.size)
    }

    @Test
    fun `bos liste Empty durumuna cevrilir`() = runTest {
        repository.result = emptyList()

        val viewModel = createViewModel()
        viewModel.onQueryChange("xyz")
        viewModel.search()
        advanceUntilIdle()

        assertEquals(LoadState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `baglanti hatasi anlasilir mesaja cevrilir`() = runTest {
        repository.error = IOException("no network")

        val viewModel = createViewModel()
        viewModel.onQueryChange("Bursa")
        viewModel.search()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Error bekleniyordu, gelen: $state", state is LoadState.Error)
        assertEquals("İnternet bağlantını kontrol et.", (state as LoadState.Error).message)
    }

    @Test
    fun `sunucu hatasi farkli mesaj gosterir`() = runTest {
        repository.error = HttpException(
            Response.error<Unit>(500, "".toResponseBody("application/json".toMediaType()))
        )

        val viewModel = createViewModel()
        viewModel.onQueryChange("Bursa")
        viewModel.search()
        advanceUntilIdle()

        assertEquals(
            "Sunucu şu an cevap vermiyor.",
            (viewModel.uiState.value as LoadState.Error).message
        )
    }

    @Test
    fun `arama kutusu temizlenince varsayilan sehirler geri gelir`() = runTest {
        val viewModel = createViewModel()
        viewModel.onQueryChange("Rize")
        viewModel.search()
        advanceUntilIdle()

        viewModel.onQueryChange("")
        val state = viewModel.uiState.value
        assertTrue(state is LoadState.Success)
        assertEquals(1, (state as LoadState.Success).data.size)
    }

    @Test
    fun `search guncel metinle istek atar`() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("Rize")
        viewModel.search()
        advanceUntilIdle()

        assertEquals("Rize", repository.lastQuery)
    }
}
