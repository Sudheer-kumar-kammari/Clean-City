package uk.ac.tees.mad.cleancity.ui.states

import android.net.Uri

sealed class AddReportUiState {
    object Idle : AddReportUiState()
    object Uploading : AddReportUiState()
    data class Success(val reportId: String) : AddReportUiState()
    data class Error(val message: String) : AddReportUiState()
}


data class AddReportFormState(
    val imageUri: Uri? = null,
    val description: String = "",
    val category:ReportCategory= ReportCategory.ILLEGAL_DUMP,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val city: String? = null,        // ← Add this
    // errors
    // Errors
    val imageError: String? = null,
    val descriptionError: String? = null,
    val locationError: String? = null
)


enum class ReportCategory(val displayName: String) {
    OVERFLOWING_BIN("Overflowing Dustbin"),
    ILLEGAL_DUMP("Illegal Garbage Dumping"),
    BLOCKED_DRAIN("Blocked / Open Drain"),
    DIRTY_STREET("Unclean Road or Street"),
    GARBAGE_NOT_COLLECTED("Garbage Not Collected")
}
