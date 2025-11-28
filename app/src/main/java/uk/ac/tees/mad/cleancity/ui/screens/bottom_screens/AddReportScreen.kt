package uk.ac.tees.mad.cleancity.ui.screens.bottom_screens


import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import uk.ac.tees.mad.cleancity.ui.states.AddReportUiState
import uk.ac.tees.mad.cleancity.ui.states.ReportCategory
import uk.ac.tees.mad.cleancity.viewmodel.AddReportViewModel
import java.io.File

// Colors - Eco-friendly Green Theme
private val GreenPrimary = Color(0xFF4CAF50)
private val GreenDark = Color(0xFF2E7D32)
private val White = Color(0xFFFFFFFF)
private val Gray100 = Color(0xFFF5F5F5)
private val Gray700 = Color(0xFF616161)
private val ErrorRed = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReportScreen(
    viewModel: AddReportViewModel,
    onReportSubmitted: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Collect states from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    // Local UI state
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }

    // Handle navigation on success
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddReportUiState.Success -> {
                onReportSubmitted(state.reportId)
                Toast.makeText(context, "Report Added Successfully", Toast.LENGTH_SHORT).show()
                viewModel.resetUiState()
            }

            is AddReportUiState.Error -> {
                showError = state.message
            }

            else -> {}
        }
    }

    // Camera setup
    val photoFile = remember {
        File.createTempFile(
            "IMG_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        ).apply {
            createNewFile()
            deleteOnExit()
        }
    }

    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            showError = "Camera permission is required to take photos"
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.onImageCaptured(photoUri)
            showError = null
        }
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.fetchLocation()
        } else {
            showError = "Location permission is required"
        }
    }

    // Functions
    fun takePhoto() {
        val permission = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED -> {
                cameraLauncher.launch(photoUri)
            }

            else -> {
                cameraPermissionLauncher.launch(permission)
            }
        }
    }

    // get the current location
    fun getLocation() {
        val fineLocation = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION

        when {
            ContextCompat.checkSelfPermission(context, fineLocation) ==
                    PackageManager.PERMISSION_GRANTED -> {
                viewModel.fetchLocation()
            }

            else -> {
                locationPermissionLauncher.launch(arrayOf(fineLocation, coarseLocation))
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image Preview/Capture Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clickable { takePhoto() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Gray100
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (formState.imageUri != null) {
                    AsyncImage(
                        model = formState.imageUri,
                        contentDescription = "Captured image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Retake overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Retake photo",
                            tint = White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to Retake",
                            color = White,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Take photo",
                            tint = Gray700,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to Capture Photo",
                            color = Gray700,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Show image error
        if (formState.imageError != null) {
            Text(
                text = formState.imageError!!,
                color = ErrorRed,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Category Selection
        Text(
            text = "Category",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = GreenDark,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = showCategoryMenu,
            onExpandedChange = { showCategoryMenu = it }
        ) {
            OutlinedTextField(
                value = formState.category.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Category") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = showCategoryMenu
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Gray700.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = showCategoryMenu,
                onDismissRequest = { showCategoryMenu = false }
            ) {
                ReportCategory.values().forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.displayName) },
                        onClick = {
                            viewModel.onCategoryChange(category)
                            showCategoryMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Description Field
        Text(
            text = "Description",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = GreenDark,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = formState.description,
            onValueChange = { viewModel.onDescriptionChange(it) },
            placeholder = { Text("Describe the waste issue in detail...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5,
            isError = formState.descriptionError != null,
            supportingText = formState.descriptionError?.let {
                { Text(it, color = ErrorRed) }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = Gray700.copy(alpha = 0.5f),
                cursorColor = GreenPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Location Section
        Text(
            text = "Location",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = GreenDark,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (formState.locationError != null)
                    ErrorRed.copy(alpha = 0.1f) else Gray100
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Location",
                    tint = if (formState.latitude != null) GreenPrimary else Gray700,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formState.address ?: "Location not set",
                        fontSize = 14.sp,
                        color = if (formState.latitude != null)
                            GreenDark else Gray700
                    )
                    if (formState.locationError != null) {
                        Text(
                            text = formState.locationError!!,
                            color = ErrorRed,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { getLocation() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = White,
                contentColor = GreenPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary)
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Use Current Location")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // General Error Message
        if (showError != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ErrorRed.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = showError!!,
                        color = ErrorRed,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Submit Button
        Button(
            onClick = {
                showError = null
                viewModel.submitReport()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                disabledContainerColor = GreenPrimary.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = uiState !is AddReportUiState.Uploading
        ) {
            if (uiState is AddReportUiState.Uploading) {
                CircularProgressIndicator(
                    color = White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Uploading...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.CloudUpload,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Submit Report",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}


//@Preview(showBackground = true)
//@Composable
//private fun AddReportScreenPreview() {
//    AddReportScreen { }
//}