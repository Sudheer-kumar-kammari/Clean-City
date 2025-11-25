//package uk.ac.tees.mad.cleancity.utils
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import android.location.Address
//import android.location.Geocoder
//import android.location.Location
//import android.os.Build
//import androidx.core.content.ContextCompat
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.Priority
//import com.google.android.gms.tasks.CancellationTokenSource
//import kotlinx.coroutines.suspendCancellableCoroutine
//import java.io.IOException
//import kotlin.coroutines.resume
//import kotlin.coroutines.resumeWithException
//
//class LocationHelper(private val context: Context) {
//    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
//
//    private val geocoder: Geocoder = Geocoder(context)
//
//    // check for the permission
//    // permission granted or not
//    fun hasLocationPermission(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    // get current Location
//    suspend fun getCurrentLocation(): Location? {
//        if (!hasLocationPermission()) {
//            throw SecurityException("Location permission not granted")
//        }
//
//        // to get location asynchronously
//        return suspendCancellableCoroutine { continuation ->
//            val cancellationTokenSource = CancellationTokenSource()
//
//            fusedLocationClient.getCurrentLocation(
//                Priority.PRIORITY_HIGH_ACCURACY,
//                cancellationTokenSource.token
//            ).addOnSuccessListener { location: Location? ->
//                continuation.resume(location)
//            }.addOnFailureListener { exception ->
//                continuation.resumeWithException(exception)
//            }
//
//            continuation.invokeOnCancellation {
//                cancellationTokenSource.cancel()
//            }
//        }
//    }
//
//
//
////     Get location with full address information
////     Returns LocationData with coordinates and human-readable address
//
//
////    input (l,l)-> address
//    suspend fun getCurrentLocationWithAddress(): LocationData? {
//        val location = getCurrentLocation() ?: return null
//
//        val address = getAddressFromLocation(
//            location.latitude,
//            location.longitude
//        )
//
//        return LocationData(
//            latitude = location.latitude,
//            longitude = location.longitude,
//            address = address?.getAddressLine(0), // Full address string
//            city = address?.locality,              // City name
//            state = address?.adminArea,            // State/Region
//            country = address?.countryName,        // Country name
//            postalCode = address?.postalCode       // ZIP/Postal code
//        )
//    }
//
//
////     Reverse geocoding: Convert coordinates to address
//     // reverse geocoding
//    private suspend fun getAddressFromLocation(
//        latitude: Double,
//        longitude: Double
//    ): Address? {
//        return try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                // Android 13+ (API 33+) - Use new async API
//                suspendCancellableCoroutine { continuation ->
//                    geocoder.getFromLocation(
//                        latitude,
//                        longitude,
//                        1  // Max results
//                    ) { addresses ->
//                        continuation.resume(addresses.firstOrNull())
//                    }
//                }
//            } else {
//                // Below Android 13 ---
//                @Suppress("DEPRECATION")
//                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
//                addresses?.firstOrNull()
//            }
//        } catch (e: IOException) {
//            // Network error or no geocoding service
//            null
//        } catch (e: Exception) {
//            // Other errors
//            null
//        }
//    }
//
//    suspend fun getLocationString(): String {
//        val location = getCurrentLocation()
//        return if (location != null) {
//            "Lat: ${String.format("%.4f", location.latitude)}, " +
//                    "Long: ${String.format("%.4f", location.longitude)}"
//        } else {
//            "Location unavailable"
//        }
//    }
//}
//
//
//// Data class to hold location information
//
//data class  LocationData(
//    val latitude: Double,
//    val longitude: Double,
//    val address: String? = null,
//    val city: String? = null,          // City: "Middlesbrough"
//    val state: String? = null,         // State/Region: "England"
//    val country: String? = null,       // Country: "United Kingdom"
//    val postalCode: String? = null     // Postal code: "TS1 2PD"
//)