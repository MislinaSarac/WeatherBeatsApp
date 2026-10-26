package com.kampplus.hava.data

import com.kampplus.hava.model.WeatherCondition

/**
 * Kullanıcının anlık ruh hali.
 */
enum class UserMood(val emoji: String, val label: String) {
    HAPPY("😁", "Mutlu"),
    CALM("🌧️", "Sakin"),
    ENERGETIC("🔥", "Enerjik"),
    ROMANTIC("🎧", "Romantik"),
    THOUGHTFUL("🤔", "Düşünceli")
}

data class WeatherMusicRecommendation(
    val playlistName: String,
    val description: String,
    val playlistUrl: String,
    val iconEmoji: String
)

object MusicPlaylistHelper {

    fun getRecommendation(
        condition: WeatherCondition,
        selectedMood: UserMood? = null
    ): WeatherMusicRecommendation {
        if (selectedMood != null) {
            return when (selectedMood) {
                UserMood.HAPPY -> WeatherMusicRecommendation(
                    playlistName = "Mutlu & Neşeli Pop Parçalar 😁",
                    description = "Günün neşesini katlayacak en sevilen Türkçe pop şarkıları ve hareketli ritimler",
                    playlistUrl = "https://fizy.com/listeler/pop-hitler",
                    iconEmoji = "😁"
                )
                UserMood.CALM -> WeatherMusicRecommendation(
                    playlistName = "Sakin & Huzurlu Akustik 🕯️",
                    description = "Dinlenirken, kahve içerken veya kitap okurken dinlenecek dinlendirici Türkçe parçalar",
                    playlistUrl = "https://fizy.com/listeler/akustik-keyif",
                    iconEmoji = "🕯️"
                )
                UserMood.ENERGETIC -> WeatherMusicRecommendation(
                    playlistName = "Yüksek Enerji & Dans Ritimleri 🔥",
                    description = "Güne tempolu başlaman için en hareketli Türkçe ve yabancı coşkulu müzikler",
                    playlistUrl = "https://fizy.com/listeler/yuksek-enerji",
                    iconEmoji = "🔥"
                )
                UserMood.ROMANTIC -> WeatherMusicRecommendation(
                    playlistName = "Romantik & Duygusal Aşk Şarkıları 🎧",
                    description = "Duygusal anlara eşlik edecek en güzel Türkçe aşk şarkıları ve unutulmaz klasikler",
                    playlistUrl = "https://fizy.com/listeler/romantik-sarkilar",
                    iconEmoji = "🎧"
                )
                UserMood.THOUGHTFUL -> WeatherMusicRecommendation(
                    playlistName = "Düşünceli & Derin Melodiler 🤔",
                    description = "Düşüncelere daldığın anlarda sana eşlik edecek en güzel enstrümantal ve akustik eserler",
                    playlistUrl = "https://fizy.com/listeler/enstrumantal",
                    iconEmoji = "🤔"
                )
            }
        }

        return when (condition) {
            WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR -> WeatherMusicRecommendation(
                playlistName = "Güneşli Gün Pop Hitleri ☀️",
                description = "Işıl ışıl bir gün için en popüler Türkçe pop ve enerjik hareketli şarkılar",
                playlistUrl = "https://fizy.com/listeler/pop-hitler",
                iconEmoji = "☀️"
            )
            WeatherCondition.PARTLY_CLOUDY, WeatherCondition.OVERCAST -> WeatherMusicRecommendation(
                playlistName = "Bulutlu Havalara Özel Dinlendirici Parçalar ⛅",
                description = "Sakin ve bulutlu günlerde ruhunu dinlendirecek en özel Türkçe akustik parçalar",
                playlistUrl = "https://fizy.com/listeler/akustik-keyif",
                iconEmoji = "⛅"
            )
            WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS -> WeatherMusicRecommendation(
                playlistName = "Yağmurlu Gün Şarkıları 🌧️",
                description = "Cama vuran yağmur damlaları eşliğinde dinlenecek hisli Türkçe şarkılar ve akustik tınılar",
                playlistUrl = "https://fizy.com/listeler/yagmur-ve-kahve",
                iconEmoji = "🌧️"
            )
            WeatherCondition.THUNDERSTORM -> WeatherMusicRecommendation(
                playlistName = "Fırtına & Derin Şarkılar ⛈️",
                description = "Rüzgarlı ve fırtınalı günlerde dinlenecek güçlü vokaller ve etkileyici ritimler",
                playlistUrl = "https://fizy.com/listeler/yuksek-enerji",
                iconEmoji = "⛈️"
            )
            WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS, WeatherCondition.FOG -> WeatherMusicRecommendation(
                playlistName = "Kış Keyfi & Sıcak Melodiler 🌨️",
                description = "Soğuk kış günlerinde içini ısıtacak en sevilen Türkçe balladlar ve sıcak ezgiler",
                playlistUrl = "https://fizy.com/listeler/kis-sarkilari",
                iconEmoji = "🌨️"
            )
            else -> WeatherMusicRecommendation(
                playlistName = "Günün En Sevilen Türkçe Hitleri 🎧",
                description = "Günün her anında fizy kalitesiyle keyifle dinleyebileceğin en popüler parçalar",
                playlistUrl = "https://fizy.com/listeler/pop-hitler",
                iconEmoji = "🎧"
            )
        }
    }
}
