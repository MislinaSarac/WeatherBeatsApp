package com.kampplus.hava.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kampplus.hava.R
import com.kampplus.hava.ui.theme.HavaTheme
import kotlinx.coroutines.delay

/**
 * Uygulamanın sade karşılama / giriş splash ekranı.
 * 0.10 saniye (100 ms) sonra anında Ana Sayfa'ya geçer.
 */
@Composable
fun WelcomeScreen(onNavigateToHome: () -> Unit, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) {
        delay(100L)
        onNavigateToHome()
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF030718),
            Color(0xFF0A0F2D),
            Color(0xFF1B0C3D),
            Color(0xFF430C78)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Orta Kısım: Hava Durumu Görseli
            Image(
                painter = painterResource(id = R.drawable.weather_icon),
                contentDescription = "Hava Durumu Görseli",
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(32.dp))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Başlık
            Text(
                text = "SENİN GÖKYÜZÜN SENİN ŞARKIN",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = 1.5.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Alt Başlık
            Text(
                text = "HAVANA GÖRE MÜZİK",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = 1.5.sp
                ),
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    HavaTheme {
        WelcomeScreen(onNavigateToHome = {})
    }
}
