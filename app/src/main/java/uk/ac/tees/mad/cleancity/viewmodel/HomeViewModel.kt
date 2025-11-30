package uk.ac.tees.mad.cleancity.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.cleancity.ui.states.*
import uk.ac.tees.mad.cleancity.utils.LocationHelper
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


@HiltViewModel
class HomeViewModel
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow(ReportFilter.ALL)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _selectedSort = MutableStateFlow(ReportSort.NEWEST)
    val selectedSort: StateFlow<ReportSort> = _selectedSort.asStateFlow()

    private val locationHelper = LocationHelper(context)
    private var allReports: List<Report> = emptyList()
    private var userLocation: Pair<Double, Double>? = null

    init {
        fetchReports()
        fetchUserLocation()
    }

    // fetch all reports

    fun fetchReports() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                val snapshot = firestore.collection("reports")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val reports = snapshot.documents.mapNotNull { doc ->
                    try {
                        val locationMap = doc.get("location") as? Map<*, *>
                        Report(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            userName = doc.getString("userName") ?: "Anonymous",
                            userProfileUrl = doc.getString("userProfileUrl") ?: "",

                            imageUrl = doc.getString("imageUrl") ?: "",
                            description = doc.getString("description") ?: "",
                            category = doc.getString("category") ?: "",

                            location = ReportLocation(
                                latitude = locationMap?.get("latitude") as? Double ?: 0.0,
                                longitude = locationMap?.get("longitude") as? Double ?: 0.0,
                                address = locationMap?.get("address") as? String ?: "",
                                city = locationMap?.get("city") as? String ?: "",
                                geohash = locationMap?.get("geohash") as? String ?: ""
                            ),

                            status = doc.getString("status") ?: "pending",
                            upvotes = (doc.getLong("upvotes") ?: 0).toInt(),
                            upvotedBy = (doc.get("upvotedBy") as? List<*>)
                                ?.mapNotNull { it as? String } ?: emptyList(),
                            commentCount = (doc.getLong("commentCount") ?: 0).toInt(),

                            createdAt = doc.getTimestamp("createdAt"),
                            updatedAt = doc.getTimestamp("updatedAt"),
                            resolvedAt = doc.getTimestamp("resolvedAt"),
                            resolvedBy = doc.getString("resolvedBy")
                        )
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Error parsing report: ${e.message}")
                        null
                    }
                }
                allReports = reports
                applyFilterAndSort()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching reports: ${e.message}")
                _uiState.value = HomeUiState.Error("Failed to load reports: ${e.message}")
            }
        }
    }

    // user's current locaton ->distance sorting

    private fun fetchUserLocation() {
        viewModelScope.launch {
            try {
                if (locationHelper.hasLocationPermission()) {
                    val location = locationHelper.getCurrentLocationWithAddress()
                    if (location != null) {
                        userLocation = Pair(location.latitude, location.longitude)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching location: ${e.message}")
            }
        }
    }

    // update filter
    fun onFilterChange(filter: ReportFilter) {
        _selectedFilter.value = filter
        applyFilterAndSort()
    }

    // update sort
    fun onSortChange(sort: ReportSort) {
        _selectedSort.value = sort
        applyFilterAndSort()
    }

    private fun applyFilterAndSort() {
        viewModelScope.launch {
            try {
                var filteredReports = allReports

                // apply filter
                // Apply filter
                filteredReports = when (_selectedFilter.value) {
                    ReportFilter.ALL -> filteredReports
                    ReportFilter.PENDING -> filteredReports.filter { it.status == "pending" }
                    ReportFilter.IN_PROGRESS -> filteredReports.filter { it.status == "in_progress" }
                    ReportFilter.RESOLVED -> filteredReports.filter { it.status == "resolved" }
                }

                // Apply sort
                filteredReports = when (_selectedSort.value) {
                    ReportSort.NEWEST -> filteredReports.sortedByDescending {
                        it.createdAt?.seconds ?: 0
                    }

                    ReportSort.OLDEST -> filteredReports.sortedBy {
                        it.createdAt?.seconds ?: 0
                    }

                    ReportSort.MOST_UPVOTED -> filteredReports.sortedByDescending {
                        it.upvotes
                    }

                    ReportSort.NEAREST -> {
                        userLocation?.let { (userLat, userLon) ->
                            filteredReports.sortedBy { report ->
                                calculateDistance(
                                    userLat, userLon,
                                    report.location.latitude,
                                    report.location.longitude
                                )
                            }
                        } ?: filteredReports
                    }
                }
                _uiState.value = HomeUiState.Success(filteredReports)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error filtering reports: ${e.message}")
            }
        }
    }

    // Calculate distance between two points (Haversine formula)
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }


    // Refresh reports
    fun refreshReports() {
        fetchReports()
    }
}