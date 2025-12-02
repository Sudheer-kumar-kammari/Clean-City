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
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.*
//import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
//import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
//import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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

    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CleanCity",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenDark
                        )
                        Text(
                            text = "Community Reports",
                            fontSize = 12.sp,
                            color = Gray700
                        )
                    }
                },
                actions = {
                    // Filter Button
                    IconButton(onClick = { showFilterMenu = true }) {
                        Badge(
                            containerColor = if (selectedFilter != ReportFilter.ALL)
                                GreenPrimary else Color.Transparent
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = "Filter",
                                tint = GreenDark
                            )
                        }
                    }

                    // Sort Button
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.Sort,
                            contentDescription = "Sort",
                            tint = GreenDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenLight
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                            onReportClick = navigateToIssueScreen,
//                            onRefresh = { viewModel.refreshReports() }
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
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null
                )
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh")
            }
        }
    }
}


// implementing the loading view
@Composable
private fun LoadingView() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) { // show 5 shimmer cards
            ShimmerReportCardPlaceholder()
        }
    }
}

@Composable
private fun ShimmerReportCardPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 250.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image shimmer area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .placeholder(
                        visible = true,
                        color = Color.LightGray.copy(alpha = 0.3f),
                        highlight = PlaceholderHighlight.shimmer(
                            highlightColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
            )

            Column(modifier = Modifier.padding(12.dp)) {
                // Status badge shimmer
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .placeholder(
                            visible = true,
                            color = Color.LightGray.copy(alpha = 0.3f),
                            highlight = PlaceholderHighlight.shimmer(
                                highlightColor = Color.White.copy(alpha = 0.6f)
                            )
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description shimmer
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
                                    highlightColor = Color.White.copy(alpha = 0.6f)
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer shimmer (user info + stats)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .placeholder(
                                    visible = true,
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    highlight = PlaceholderHighlight.shimmer(
                                        highlightColor = Color.White.copy(alpha = 0.6f)
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
                                        highlightColor = Color.White.copy(alpha = 0.6f)
                                    )
                                )
                        )
                    }

                    // Simulate right side stats shimmer
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .placeholder(
                                visible = true,
                                color = Color.LightGray.copy(alpha = 0.3f),
                                highlight = PlaceholderHighlight.shimmer(
                                    highlightColor = Color.White.copy(alpha = 0.6f)
                                )
                            )
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportsListView(
    reports: List<Report>,
    onReportClick: (String) -> Unit,
//    onRefresh: () -> Unit,
//    onRefresh: () -> Unit
) {
    val pullRefreshState = rememberPullToRefreshState()

//    if (pullRefreshState.isRefreshing) {
//        LaunchedEffect(true) {
//            onRefresh()
//            pullRefreshState.endRefresh()
//        }
//    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = reports,
                key = { it.id }
            ) { report ->
                ReportCard(
                    report = report,
                    onClick = { onReportClick(report.id) }
                )
            }
        }

//        PullToRefreshContainer(
//            state = pullRefreshState,
//            modifier = Modifier.align(Alignment.TopCenter)
//        )
    }
}

@Composable
private fun ReportCard(
    report: Report,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Image Section
            AsyncImage(
                model = report.imageUrl,
                contentDescription = "Report image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            // Content Section
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Status Badge and Category
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
                            .background(
                                color = Gray100,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = report.description,
                    fontSize = 14.sp,
                    color = Gray900,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                // Footer: User info, upvotes, comments, time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User info
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User avatar
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
                        Text(
                            text = report.userName,
                            fontSize = 12.sp,
                            color = Gray700
                        )
                    }

                    // Stats
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Upvotes
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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

                        // Comments
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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

                // Timestamp
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

