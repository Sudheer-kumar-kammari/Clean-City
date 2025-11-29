package uk.ac.tees.mad.cleancity.ui.states

import com.google.firebase.Timestamp

// Data class representing a Report from Firebase
data class Report(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileUrl: String = "",

    val imageUrl: String = "",
    val description: String = "",
    val category: String = "",

    val location: ReportLocation = ReportLocation(),

    val status: String = "pending", // pending, in_progress, resolved
    val upvotes: Int = 0,
    val upvotedBy: List<String> = emptyList(),
    val commentCount: Int = 0,

    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val resolvedAt: Timestamp? = null,
    val resolvedBy: String? = null
)

data class ReportLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val city: String = "",
    val geohash: String = ""
)

// ui state of the home screen
sealed class HomeUiState{
    object Loading: HomeUiState()
    data class Success(val reports:List<Report>): HomeUiState()
    data class Error(val message:String): HomeUiState()
}

// Filter Options
enum class ReportFilter(val displayName:String){
    ALL("All Reports"),
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved")
}

enum class ReportSort(displayName:String){
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    MOST_UPVOTED("Most Upvoted"),
    NEAREST("Nearest to Me")
}