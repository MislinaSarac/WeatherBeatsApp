package com.kampplus.hava.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import com.kampplus.hava.model.City
import java.util.Locale

/**
 * Cihazın canlı GPS / Ağ konumunu tespit eden yardımcı sınıf.
 */
object LocationHelper {

    @SuppressLint("MissingPermission")
    fun getCurrentLocationCity(context: Context): City? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val location: Location? = try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        } catch (_: SecurityException) {
            null
        }

        if (location == null) return null

        val cityName = getCityNameFromLocation(context, location.latitude, location.longitude)
        val cityId = (location.latitude * 10000 + location.longitude * 10000).toLong()

        return City(
            id = cityId,
            name = cityName,
            latitude = location.latitude,
            longitude = location.longitude,
            countryCode = "TR",
            country = "Türkiye",
            admin1 = cityName
        )
    }

    private fun getCityNameFromLocation(context: Context, lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.forLanguageTag("tr-TR"))

            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val address = addresses?.firstOrNull()
            val name = address?.locality ?: address?.adminArea ?: address?.subAdminArea ?: "Mevcut Konum"
            if (name.isBlank()) "Mevcut Konum" else name
        } catch (_: Exception) {
            "Mevcut Konum"
        }
    }
}
