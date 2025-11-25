package uk.ac.tees.mad.cleancity.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.cleancity.navigation.bottom_navigation.BottomNavScreen
import uk.ac.tees.mad.cleancity.navigation.bottom_navigation.bottomNavScreens
@Composable
fun MainScreen(
    navigateToIssueScreen: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val bottomNavController = rememberNavController()
    val navBackStackEntry = bottomNavController.currentBackStackEntryAsState()
    val currentRoute =
        navBackStackEntry.value?.destination?.route ?: BottomNavScreen.Home.route

    //val addReportViewModel= hiltViewModel<AddReportViewModel>()
    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavScreens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = {
                            Text(screen.title)
                        },
                        icon = {
                            Icon(screen.icon, contentDescription = screen.title)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavScreen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {

//            composable(BottomNavScreen.Home.route) {
//                HomeScreen(
//                    navigateToIssueScreen = { reportId ->
//                        navigateToIssueScreen(reportId)
//                    }
//                )
//            }
//
//            composable(BottomNavScreen.AddReport.route) {
//                AddReportScreen(
//                    viewModel = addReportViewModel
//                )
//            }
//
//            composable(BottomNavScreen.Profile.route) {
//                ProfileScreen()
//            }
        }
    }

}


@Preview(showBackground = true, name = "CleanCity – Main Screen with Bottom Navigation")
@Composable
fun MainScreenPreview() {
    val bottomNavController = rememberNavController()

    // Simulate bottom navigation screens
    val bottomNavScreens = listOf(
        BottomNavScreen.Home,
        BottomNavScreen.AddReport,
        BottomNavScreen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ) {
                bottomNavScreens.forEach { screen ->
                    NavigationBarItem(
                        selected = false, // Preview doesn't need real selection
                        onClick = { },
                        label = { Text(screen.title, fontSize = 12.sp) },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = Color.White
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF1F8E9)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.NaturePeople,
                    contentDescription = "CleanCity",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "CleanCity",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Making our city cleaner, together.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF4CAF50),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Minimal data class for preview only
private data class BottomNavScreen(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    companion object {
        val Home = BottomNavScreen("home", "Home", Icons.Default.Home)
        val AddReport = BottomNavScreen("add", "Report", Icons.Default.AddCircle)
        val Profile = BottomNavScreen("profile", "Profile", Icons.Default.Person)
    }
}