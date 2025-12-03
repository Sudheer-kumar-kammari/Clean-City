package uk.ac.tees.mad.cleancity.ui.screens.bottom_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import uk.ac.tees.mad.cleancity.ui.states.HomeUiState
import uk.ac.tees.mad.cleancity.ui.states.Report
import uk.ac.tees.mad.cleancity.ui.states.ReportFilter
import uk.ac.tees.mad.cleancity.ui.states.ReportSort
import uk.ac.tees.mad.cleancity.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val GreenPrimary = Color(0xFF4CAF50)
private val GreenDark = Color(0xFF2E7D32)
private val GreenLight = Color(0xFFE8F5E9)
private val White = Color(0xFFFFFFFF)
private val Gray100 = Color(0xFFF5F5F5)
private val Gray300 = Color(0xFFE0E0E0)
private val Gray700 = Color(0xFF616161)
private val Gray900 = Color(0xFF212121)
private val Orange = Color(0xFFFF9800)
private val Blue = Color(0xFF2196F3)

// Bottom Sheet Type
private enum class BottomSheetType {
    FILTER, SORT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    navigateToIssueScreen: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()

    // Single bottom sheet state
    var bottomSheetType by remember { mutableStateOf<BottomSheetType?>(null) }
    val showBottomSheet = bottomSheetType != null

    // Main content column with TopAppBar and content
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GreenLight,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title
                Text(
                    text = "Clean City",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenDark
                )

                // Filter and Sort Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filter Button
                    FilterChip(
                        selected = selectedFilter != ReportFilter.ALL,
                        onClick = { bottomSheetType = BottomSheetType.FILTER },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FilterList,
                                    contentDescription = "Filter",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Filter", fontSize = 13.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = White,
                            containerColor = White,
                            labelColor = GreenDark
                        )
                    )

                    // Sort Button
                    FilterChip(
                        selected = false,
                        onClick = { bottomSheetType = BottomSheetType.SORT },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Sort,
                                    contentDescription = "Sort",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Sort", fontSize = 13.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = White,
                            labelColor = GreenDark
                        )
                    )


                    IconButton(
                        onClick = viewModel::refreshReports
                    ){
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = GreenDark,
                        )
                    }

                }
            }
        }

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    LoadingView()
                }

                is HomeUiState.Success -> {
                    if (state.reports.isEmpty()) {
                        EmptyStateView(
                            filter = selectedFilter,
                            onRefresh = { viewModel.refreshReports() }
                        )
                    } else {
                        ReportsListView(
                            reports = state.reports,
                            onReportClick = navigateToIssueScreen
                        )
                    }
                }

                is HomeUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.refreshReports() }
                    )
                }
            }
        }
    }

    // Single Bottom Sheet for both Filter and Sort
    if (showBottomSheet) {
        SelectionBottomSheet(
            type = bottomSheetType!!,
            selectedFilter = selectedFilter,
            selectedSort = selectedSort,
            onFilterSelected = { filter ->
                viewModel.onFilterChange(filter)
                bottomSheetType = null
            },
            onSortSelected = { sort ->
                viewModel.onSortChange(sort)
                bottomSheetType = null
            },
            onDismiss = { bottomSheetType = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionBottomSheet(
    type: BottomSheetType,
    selectedFilter: ReportFilter,
    selectedSort: ReportSort,
    onFilterSelected: (ReportFilter) -> Unit,
    onSortSelected: (ReportSort) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Header
            Text(
                text = when (type) {
                    BottomSheetType.FILTER -> "Filter Reports"
                    BottomSheetType.SORT -> "Sort Reports"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GreenDark,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Gray300
            )

            // Options
            when (type) {
                BottomSheetType.FILTER -> {
                    ReportFilter.values().forEach { filter ->
                        FilterOption(
                            filter = filter,
                            isSelected = selectedFilter == filter,
                            onClick = { onFilterSelected(filter) }
                        )
                    }
                }

                BottomSheetType.SORT -> {
                    ReportSort.values().forEach { sort ->
                        SortOption(
                            sort = sort,
                            isSelected = selectedSort == sort,
                            onClick = { onSortSelected(sort) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FilterOption(
    filter: ReportFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (filter) {
                ReportFilter.ALL -> Icons.Filled.List
                ReportFilter.PENDING -> Icons.Filled.Schedule
                ReportFilter.IN_PROGRESS -> Icons.Filled.Loop
                ReportFilter.RESOLVED -> Icons.Filled.CheckCircle
            },
            contentDescription = null,
            tint = if (isSelected) GreenPrimary else Gray700,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = filter.displayName,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) GreenPrimary else Gray900,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = GreenPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SortOption(
    sort: ReportSort,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (sort) {
                ReportSort.NEWEST -> Icons.Filled.ArrowDownward
                ReportSort.OLDEST -> Icons.Filled.ArrowUpward
                ReportSort.MOST_UPVOTED -> Icons.Filled.ThumbUp
                ReportSort.NEAREST -> Icons.Filled.LocationOn
            },
            contentDescription = null,
            tint = if (isSelected) GreenPrimary else Gray700,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = sort.displayName,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) GreenPrimary else Gray900,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = GreenPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Oops! Something went wrong",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Gray700
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary
                )
            ) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    filter: ReportFilter,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (filter) {
                    ReportFilter.ALL -> "No Reports Yet"
                    ReportFilter.PENDING -> "No Pending Reports"
                    ReportFilter.IN_PROGRESS -> "No In Progress Reports"
                    ReportFilter.RESOLVED -> "No Resolved Reports"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GreenDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Be the first to report a waste issue!",
                fontSize = 14.sp,
                color = Gray700
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh")
            }
        }
    }
}

@Composable
private fun LoadingView() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) { ShimmerReportCardPlaceholder() }
    }
}

@Composable
private fun ShimmerReportCardPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 250.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .placeholder(
                        visible = true,
                        color = Color.LightGray.copy(alpha = 0.3f),
                        highlight = PlaceholderHighlight.shimmer(
                            highlightColor = White.copy(alpha = 0.6f)
                        )
                    )
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .placeholder(
                            visible = true,
                            color = Color.LightGray.copy(alpha = 0.3f),
                            highlight = PlaceholderHighlight.shimmer(
                                highlightColor = White.copy(alpha = 0.6f)
                            )
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .placeholder(
                                visible = true,
                                color = Color.LightGray.copy(alpha = 0.3f),
                                highlight = PlaceholderHighlight.shimmer(
                                    highlightColor = White.copy(alpha = 0.6f)
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .placeholder(
                                    visible = true,
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    highlight = PlaceholderHighlight.shimmer(
                                        highlightColor = White.copy(alpha = 0.6f)
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .placeholder(
                                    visible = true,
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    highlight = PlaceholderHighlight.shimmer(
                                        highlightColor = White.copy(alpha = 0.6f)
                                    )
                                )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .placeholder(
                                visible = true,
                                color = Color.LightGray.copy(alpha = 0.3f),
                                highlight = PlaceholderHighlight.shimmer(
                                    highlightColor = White.copy(alpha = 0.6f)
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportsListView(
    reports: List<Report>,
    onReportClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = reports) { report ->
            ReportCard(report = report, onClick = { onReportClick(report.id) })
        }
    }
}

@Composable
private fun ReportCard(report: Report, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = report.imageUrl,
                contentDescription = "Report image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = report.status)
                    Text(
                        text = getCategoryDisplayName(report.category),
                        fontSize = 12.sp,
                        color = Gray700,
                        modifier = Modifier
                            .background(color = Gray100, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = report.description,
                    fontSize = 14.sp,
                    color = Gray900,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Gray700,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = report.location.address.takeIf { it.isNotBlank() }
                            ?: "${report.location.latitude}, ${report.location.longitude}",
                        fontSize = 12.sp,
                        color = Gray700,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(GreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            if (report.userProfileUrl.isNotBlank()) {
                                AsyncImage(
                                    model = report.userProfileUrl,
                                    contentDescription = "User avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = report.userName.firstOrNull()?.toString() ?: "U",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenDark
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = report.userName, fontSize = 12.sp, color = Gray700)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ThumbUp,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = report.upvotes.toString(),
                                fontSize = 12.sp,
                                color = Gray700
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Message,
                                contentDescription = null,
                                tint = Blue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = report.commentCount.toString(),
                                fontSize = 12.sp,
                                color = Gray700
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatTimestamp(report.createdAt),
                    fontSize = 11.sp,
                    color = Gray700
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (color, text) = when (status) {
        "pending" -> Orange to "Pending"
        "in_progress" -> Blue to "In Progress"
        "resolved" -> GreenPrimary to "Resolved"
        else -> Gray700 to "Unknown"
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

private fun getCategoryDisplayName(category: String): String {
    return when (category.lowercase()) {
        "overflowing_bin" -> "Overflowing Bin"
        "illegal_dump" -> "Illegal Dump"
        "blocked_drain" -> "Blocked Drain"
        "dirty_street" -> "Dirty Street"
        "garbage_not_collected" -> "Not Collected"
        else -> category.replace("_", " ").capitalize()
    }
}

private fun formatTimestamp(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return "Unknown time"
    val date = timestamp.toDate()
    val now = Date()
    val diffInMillis = now.time - date.time
    val diffInMinutes = diffInMillis / (60 * 1000)
    val diffInHours = diffInMinutes / 60
    val diffInDays = diffInHours / 24
    return when {
        diffInMinutes < 1 -> "Just now"
        diffInMinutes < 60 -> "${diffInMinutes}m ago"
        diffInHours < 24 -> "${diffInHours}h ago"
        diffInDays < 7 -> "${diffInDays}d ago"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    }
}