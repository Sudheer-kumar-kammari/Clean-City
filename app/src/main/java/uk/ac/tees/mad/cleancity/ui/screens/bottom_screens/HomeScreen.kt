package uk.ac.tees.mad.cleancity.ui.screens.bottom_screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import uk.ac.tees.mad.cleancity.ui.states.HomeUiState
import uk.ac.tees.mad.cleancity.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel= hiltViewModel(),
    navigateToIssueScreen:(String)->Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()

    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//    ) {
//        when (val state = uiState) {
//            is HomeUiState.Loading -> {
//                LoadingView()
//            }
//
//            is HomeUiState.Success -> {
//                if (state.reports.isEmpty()) {
//                    EmptyStateView(
//                        filter = selectedFilter,
//                        onRefresh = { viewModel.refreshReports() }
//                    )
//                } else {
//                    ReportsListView(
//                        reports = state.reports,
//                        onReportClick = navigateToIssueScreen,
//                        onRefresh = { viewModel.refreshReports() }
//                    )
//                }
//            }
//
//            is HomeUiState.Error -> {
//                ErrorView(
//                    message = state.message,
//                    onRetry = { viewModel.refreshReports() }
//                )
//            }
//        }
//    }

    Box(
        modifier=Modifier.fillMaxSize()
    ){
        LoadingView()
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
