package uk.ac.tees.mad.cleancity.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.cleancity.ui.states.AddReportFormState
import uk.ac.tees.mad.cleancity.ui.states.AddReportUiState
import uk.ac.tees.mad.cleancity.ui.states.ReportCategory
import uk.ac.tees.mad.cleancity.util.CloudinaryUploadHelper
import uk.ac.tees.mad.cleancity.utils.LocationHelper
import javax.inject.Inject


@HiltViewModel
class AddReportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddReportUiState>(AddReportUiState.Idle)
    val uiState: StateFlow<AddReportUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AddReportFormState())
    val formState: StateFlow<AddReportFormState> = _formState.asStateFlow()

    private val locationHelper = LocationHelper(context)
    private val cloudinaryUploadHelper = CloudinaryUploadHelper(context)


    //     Update image URI in the form
    fun onImageCaptured(uri: Uri) {
        _formState.value = _formState.value.copy(
            imageUri = uri,
            imageError = null
        )
    }


    // Update description
    fun onDescriptionChange(description: String) {
        _formState.value = _formState.value.copy(
            description = description,
            descriptionError = null
        )
    }

    // Update category
    fun onCategoryChange(category: ReportCategory) {
        _formState.value = _formState.value.copy(
            category = category
        )
    }


    // Fetch current location with address

    fun fetchLocation() {
        viewModelScope.launch {
            try {
                if (!locationHelper.hasLocationPermission()) {
                    _formState.value = _formState.value.copy(
                        locationError = "Location permission required"
                    )
                    return@launch
                }

                val locationData = locationHelper.getCurrentLocationWithAddress()
                if (locationData != null) {
                    // Build a readable address string
                    val addressText = buildString {
                        locationData.address?.let { append(it) }
                            ?: run {
                                // Fallback to city, state if full address not available
                                locationData.city?.let { append(it) }
                                locationData.state?.let {
                                    if (isNotEmpty()) append(", ")
                                    append(it)
                                }
                                locationData.country?.let {
                                    if (isNotEmpty()) append(", ")
                                    append(it)
                                }
                            }

                        // If still empty, show coordinates
                        if (isEmpty()) {
                            append("Lat: ${String.format("%.4f", locationData.latitude)}, ")
                            append("Long: ${String.format("%.4f", locationData.longitude)}")
                        }
                    }

                    _formState.value = _formState.value.copy(
                        latitude = locationData.latitude,
                        longitude = locationData.longitude,
                        address = addressText,
                        city = locationData.city,
                        locationError = null
                    )
                } else {
                    _formState.value = _formState.value.copy(
                        locationError = "Unable to get location"
                    )
                }
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    locationError = "Location error: ${e.message}"
                )
            }
        }
    }

    // Submit report to Firebase
    fun submitReport() {
        val currentState = _formState.value
        val currentUser = firebaseAuth.currentUser

        // Validation Guard clause
        if (!validateForm(currentState)) {
            return
        }
        // safety cleck will never happen
        if (currentUser == null) {
            _uiState.value = AddReportUiState.Error("Please login to submit reports")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddReportUiState.Uploading

            try {
                // Upload image to Cloudinary
                val imageUrl = cloudinaryUploadHelper.uploadImage(currentState.imageUri!!)
                if (imageUrl == null) {
                    _uiState.value = AddReportUiState.Error("Failed to upload image")
                    return@launch
                }


                //  Create report data
                val reportData = hashMapOf(
                    "userId" to currentUser.uid,
                    "userName" to (currentUser.displayName ?: "Anonymous"),
                    "userProfileUrl" to (currentUser.photoUrl?.toString() ?: ""),

                    "imageUrl" to imageUrl,
                    "description" to currentState.description,
                    "category" to currentState.category.name.lowercase(),

                    "location" to hashMapOf(
                        "latitude" to currentState.latitude!!,
                        "longitude" to currentState.longitude!!,
                        "address" to (currentState.address ?: ""),
                        "city" to (currentState.city ?: "Unknown"),
                        "geohash" to generateGeohash(
                            currentState.latitude!!,
                            currentState.longitude!!
                        )
                    ),

                    "status" to "pending",
                    "upvotes" to 0,
                    "upvotedBy" to emptyList<String>(),
                    "commentCount" to 0,

                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "resolvedAt" to null,
                    "resolvedBy" to null
                )

                // Add report to Firestore
                val reportRef = firestore.collection("reports").add(reportData).await()
                val reportId = reportRef.id

                // Update user's report count
                firestore.collection("users").document(currentUser.uid)
                    .set(
                        hashMapOf(
                            "reportsSubmitted" to FieldValue.increment(1),
                            "updatedAt" to FieldValue.serverTimestamp()
                        ),
                        SetOptions.merge()  // Creates if doesn't exist
                    )
                    .await()

                // Step 5: Success!
                _uiState.value = AddReportUiState.Success(reportId)

                // Reset form
                _formState.value = AddReportFormState()

            } catch (e: Exception) {
                _uiState.value = AddReportUiState.Error(
                    "Failed to submit report: ${e.message}"
                )
            }
        }
    }

    // validation of the form
    private fun validateForm(state: AddReportFormState): Boolean {
        var isValid = true

        if (state.imageUri == null) {
            _formState.value = state.copy(imageError = "Please capture a photo")
            isValid = false
        }

        if (state.description.isBlank()) {
            _formState.value = state.copy(
                descriptionError = "Please add a description"
            )
            isValid = false
        }

        if (state.latitude == null || state.longitude == null) {
            _formState.value = state.copy(
                locationError = "Please set location"
            )
            isValid = false
        }

        return isValid
    }


    //     Generate simple geohash for location
    // useful to group near by location convert to string
    private fun generateGeohash(lat: Double, lon: Double): String {

        return GeoFireUtils.getGeoHashForLocation(GeoLocation(lat, lon))
    }

    // reset for the ui state
    fun resetUiState() {
        _uiState.value = AddReportUiState.Idle
    }

}